package org.openmrs.module.shr.cdahandler.processor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openhealthtools.mdht.uml.cda.AssignedAuthor;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.Component4;
import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.ANY;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActMoodDocumentObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.x_DocumentActMood;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaDocumentModel;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;


public class ApsProcessor extends Processor {

	protected final Log log = LogFactory.getLog(this.getClass());

	public static final String TYPE = "iheAps";
	
	@Override
    public CdaDocumentModel process(CdaDocumentModel cdaDocumentModel) {

		System.out.println("-------------------");
		ClinicalDocument cd = cdaDocumentModel.getClinicalDocument();
		
		System.out.println(cd.getTitle());
		System.out.println(cd.getId().getExtension());
		
		Encounter e = new Encounter();
		Patient p = null;
		
        try {
	        p = parsePatient(cd);
			e.setEncounterType(getEncounterType(cd));
			e.setEncounterDatetime(parseDocumentDateTime(cd));
			e.setPatient(p);
			e.setProvider(getDefaultEncounterRole(), parseProvider(cd));
	             
        }
        catch (DocumentParseException e1) {
	        e1.printStackTrace();
        }
		
		Set<Obs> obs = parseObs(cd);
		for (Obs o : obs) {
			o.setEncounter(e);
			o.setPerson(p);
		}
		e.setObs(obs);
		
		Context.getEncounterService().saveEncounter(e);
		return null;
	
    }

	@Override
    public String getDocumentType() {
	    return TYPE;
    }
	
	private EncounterType getEncounterType(ClinicalDocument cd) {
		EncounterType et = Context.getEncounterService().getEncounterType(cd.getCode().getCode());
		if (et == null) {
			et = new EncounterType(
				cd.getCode().getCode(),
				String.format(
					"CDA document; document code (LOINC, %s), type id (%s, %s)",
					cd.getCode().getCode(), cd.getTypeId().getRoot(), cd.getTypeId().getExtension()
				)
			);
			Context.getEncounterService().saveEncounterType(et);
		}
		return et;
	}
	
	private Date parseDocumentDateTime(ClinicalDocument cd) throws DocumentParseException {
		if (cd.getAuthors().isEmpty())
			throw new DocumentParseException("Unable to locate author -> time");
		
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmssZ");
		try {
			return format.parse(cd.getAuthors().get(0).getTime().getValue());
		} catch (ParseException e) {
			throw new DocumentParseException("Unable to parse time value");
		}
	}
	
	private Patient parsePatient(ClinicalDocument cd) throws DocumentParseException {
		Patient res = null;
		if (cd.getPatients().isEmpty() || cd.getPatientRoles().isEmpty())
			throw new DocumentParseException("No patient identifiers found in document");
		
		String idRoot = cd.getPatientRoles().get(0).getIds().get(0).getRoot();
		String idExtension = cd.getPatientRoles().get(0).getIds().get(0).getExtension();
		
		PatientIdentifierType pit = getIdentifierType(idRoot);
		List<Patient> matches = Context.getPatientService().getPatients(null, idExtension, Collections.singletonList(pit), true);
		
		if (matches.isEmpty()) {
			res = createPatient(cd.getPatients().get(0), pit, idExtension);
		} else {
			res = matches.get(0);
		}
		
		return res;
	}
	
	private PatientIdentifierType getIdentifierType(String idType) {
		PatientIdentifierType pit = Context.getPatientService().getPatientIdentifierTypeByName(idType);
		if (pit==null) {
			//create new id type
			pit = new PatientIdentifierType();
			pit.setName(idType);
			pit.setDescription("OpenHIE SHR generated patient identifier type for '" + idType + "' authority");
			Context.getPatientService().savePatientIdentifierType(pit);
		}
		return pit;
	}
	
	private Patient createPatient(org.openhealthtools.mdht.uml.cda.Patient importPatient, PatientIdentifierType idType, String id) {
		Patient res = new Patient();
		
		PatientIdentifier pi = new PatientIdentifier();
		pi.setIdentifierType(idType);
		pi.setIdentifier(id);
		//TODO should use location from CDA document
		pi.setLocation(Context.getLocationService().getDefaultLocation());
		pi.setPreferred(true);
		res.addIdentifier(pi);
		
		PersonName pn = new PersonName();
		pn.setFamilyName(importPatient.getNames().get(0).getFamilies().get(0).getText());
		pn.setGivenName(importPatient.getNames().get(0).getGivens().get(0).getText());
		res.addName(pn);
		
		res.setGender(importPatient.getAdministrativeGenderCode().getCode());
		
		//TODO birthdate
		
		Context.getPatientService().savePatient(res);
		return res;
	}
	
	private Provider parseProvider(ClinicalDocument cd) throws DocumentParseException {
		Provider res = null;
		if (cd.getAuthors().isEmpty())
			throw new DocumentParseException("No authors found");
		
		AssignedAuthor aa = cd.getAuthors().get(0).getAssignedAuthor();
		String idRoot = aa.getIds().get(0).getRoot();
		String idExtension = aa.getIds().get(0).getExtension();
		
		if (idExtension==null || idExtension.isEmpty()) {
			log.warn("No extension specified for author id");
		} else {
			res = Context.getProviderService().getProviderByIdentifier(idExtension);
		}
		
		if (res==null)
			res = createProvider(aa, idRoot, idExtension);
		
		return res;
	}
	
	private Provider createProvider(AssignedAuthor aa, String idRoot, String idExtension) {
		Provider res = new Provider();
		
		res.setIdentifier(idRoot + "-" + (idExtension!=null && !idExtension.isEmpty() ? idExtension : UUID.randomUUID()));
		res.setName(
			aa.getAssignedPerson().getNames().get(0).getGivens().get(0).getText() + " " +
			aa.getAssignedPerson().getNames().get(0).getFamilies().get(0).getText()
			);
		
		Context.getProviderService().saveProvider(res);
		return res;
	}
	
	@SuppressWarnings("deprecation")
	private EncounterRole getDefaultEncounterRole() {
		//Delicious encounter rolls just like Suranga used to make
		//https://github.com/jembi/rhea-shr-adapter/blob/3e25fa0cd276327ca83127283213b6658af9e9ef/api/src/main/java/org/openmrs/module/rheashradapter/util/RHEA_ORU_R01Handler.java#L422
		String uuid = Context.getAdministrationService().getGlobalProperty("cdaAntepartum.encounterrole.uuid");
		EncounterRole encounterRole = Context.getEncounterService().getEncounterRoleByUuid(uuid);

		if(encounterRole == null) {
			encounterRole = new EncounterRole();
			encounterRole.setName("Default CDA Antepartum Encounter Role");
			encounterRole.setDescription("Created by the OpenHIE SHR");
	
			encounterRole = Context.getEncounterService().saveEncounterRole(encounterRole);
			Context.getAdministrationService().setGlobalProperty("cdaAntepartum.encounterrole.uuid", encounterRole.getUuid());
		} 
		
		return encounterRole;
	}
	
	private Set<Obs> parseObs(ClinicalDocument cd) {
		Set<Obs> res = new HashSet<Obs>();
		
		for (Section section : cd.getAllSections()) {
			for (Entry entry : section.getEntries()) {
				res.addAll(parseEntry(entry));
			}
		}
		
		return res;
	}
	
	private Set<Obs> parseEntry(Entry entry) {
		Set<Obs> res = new HashSet<Obs>();
		
		if (entry.getAct()!=null) {
			//we only handle events
			if (entry.getAct().getMoodCode().compareTo(x_DocumentActMood.EVN)==0) {
				for (EntryRelationship er : entry.getAct().getEntryRelationships()) {
					res.addAll( parseEntryRelationship(er) );
				}
			}
		} else if (entry.getObservation()!=null) {
			//we only handle events
			if (entry.getObservation().getMoodCode().compareTo(x_ActMoodDocumentObservation.EVN)==0) {
				
				if (entry.getObservation().getValues()!=null && !entry.getObservation().getValues().isEmpty()) {
					Obs obs = new Obs();
					obs.setConcept(
						getConcept(
							entry.getObservation().getCode().getCodeSystem(),
							entry.getObservation().getCode().getCode()
							)
						);
					//TODO multiple values
					processValue(obs, entry.getObservation().getValues().get(0));
					processEffectiveTime(obs, entry.getObservation().getEffectiveTime());
					
					res.add(obs);
				}
				
				for (EntryRelationship er : entry.getObservation().getEntryRelationships()) {
					res.addAll( parseEntryRelationship(er) );
				}
			}
		} else if (entry.getOrganizer()!=null) {
			for (Component4 c : entry.getOrganizer().getComponents()) {
				res.addAll( parseComponent4(c) );
			}
		}
		
		return res;
	}
	
	//argh. Entry, EntryRelationship, ComponentX all share the same interfaces,
	//BUT a lot of their common methods aren't defined in the interfaces, but rather the classes themselves.
	//We'll just duplicate the code for now, but a better method should be found
	private Set<Obs> parseEntryRelationship(EntryRelationship entry) {
		Set<Obs> res = new HashSet<Obs>();
		
		if (entry.getAct()!=null) {
			//we only handle events
			if (entry.getAct().getMoodCode().compareTo(x_DocumentActMood.EVN)==0) {
				for (EntryRelationship er : entry.getAct().getEntryRelationships()) {
					res.addAll( parseEntryRelationship(er) );
				}
			}
		} else if (entry.getObservation()!=null) {
			//we only handle events
			if (entry.getObservation().getMoodCode().compareTo(x_ActMoodDocumentObservation.EVN)==0) {
				
				if (entry.getObservation().getValues()!=null && !entry.getObservation().getValues().isEmpty()) {
					Obs obs = new Obs();
					obs.setConcept(
						getConcept(
							entry.getObservation().getCode().getCodeSystem(),
							entry.getObservation().getCode().getCode()
							)
						);
					//TODO multiple values
					processValue(obs, entry.getObservation().getValues().get(0));
					processEffectiveTime(obs, entry.getObservation().getEffectiveTime());
					
					res.add(obs);
				}
				
				for (EntryRelationship er : entry.getObservation().getEntryRelationships()) {
					res.addAll( parseEntryRelationship(er) );
				}
			}
		} else if (entry.getOrganizer()!=null) {
			for (Component4 c : entry.getOrganizer().getComponents()) {
				res.addAll( parseComponent4(c) );
			}
		}
		
		return res;
	}
	
	//argh. Entry, EntryRelationship, ComponentX all share the same interfaces,
	//BUT a lot of their common methods aren't defined in the interfaces, but rather the classes themselves.
	//We'll just duplicate the code for now, but a better method should be found
	private Set<Obs> parseComponent4(Component4 entry) {
		Set<Obs> res = new HashSet<Obs>();
		
		if (entry.getAct()!=null) {
			//we only handle events
			if (entry.getAct().getMoodCode().compareTo(x_DocumentActMood.EVN)==0) {
				for (EntryRelationship er : entry.getAct().getEntryRelationships()) {
					res.addAll( parseEntryRelationship(er) );
				}
			}
		} else if (entry.getObservation()!=null) {
			//we only handle events
			if (entry.getObservation().getMoodCode().compareTo(x_ActMoodDocumentObservation.EVN)==0) {
				
				if (entry.getObservation().getValues()!=null && !entry.getObservation().getValues().isEmpty()) {
					Obs obs = new Obs();
					obs.setConcept(
						getConcept(
							entry.getObservation().getCode().getCodeSystem(),
							entry.getObservation().getCode().getCode()
							)
						);
					//TODO multiple values
					processValue(obs, entry.getObservation().getValues().get(0));
					processEffectiveTime(obs, entry.getObservation().getEffectiveTime());
					
					res.add(obs);
				}
				
				for (EntryRelationship er : entry.getObservation().getEntryRelationships()) {
					res.addAll( parseEntryRelationship(er) );
				}
			}
		} else if (entry.getOrganizer()!=null) {
			for (Component4 c : entry.getOrganizer().getComponents()) {
				res.addAll( parseComponent4(c) );
			}
		}
		
		return res;
	}
	
	private void processValue(Obs dst, ANY value) {
		if (value instanceof PQ) {
			dst.setValueNumeric(((PQ)value).getValue().doubleValue());
		} else if (value instanceof CD || value instanceof CE) {
			String code = ((CD)value).getCode();
			String namespace = ((CD)value).getCodeSystem();
			if (code!=null && !code.isEmpty()) {
				dst.setValueCoded(getConcept(namespace, code));
			}
		}
	}
	
	private void processEffectiveTime(Obs dst, IVL_TS time) {
		//TODO
		dst.setObsDatetime(new Date());
	}
	
	private Concept getConcept(String namespace, String code) {
		if (namespace==null || namespace.isEmpty())
			namespace = "Default";
		
		ConceptService serv = Context.getConceptService();
		Concept c = serv.getConceptByMapping(code, namespace);
		
		if (c==null) {
			c = new Concept();
			
			ConceptReferenceTerm crt = new ConceptReferenceTerm();
			crt.setConceptSource(getConceptSource(namespace));
			crt.setCode(code);
			c.getConceptMappings().add(new ConceptMap(crt, serv.getConceptMapTypeByName("NARROWER-THAN")));
			
			//datatypes are an issue
			c.setDatatype(serv.getConceptDatatypeByName("N/A"));
			c.setConceptClass(serv.getConceptClassByName("Misc"));
			c.addName(new ConceptName(namespace + ":" + code, Context.getLocale()));
			serv.saveConcept(c);
		}
		
		return c;
	}
	
	private ConceptSource getConceptSource(String namespace) {
		ConceptSource cs = Context.getConceptService().getConceptSourceByName(namespace);
		
		if (cs==null) {
			cs = new ConceptSource();
			cs.setName(namespace);
			cs.setDescription(namespace);
			Context.getConceptService().saveConceptSource(cs);
		}
		
		return cs;
	}
	
}
