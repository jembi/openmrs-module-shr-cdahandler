package org.openmrs.module.shr.cdahandler.processor;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.Component4;
import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.hl7.datatypes.ANY;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.INT;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
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
import org.openmrs.Provider;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaDocumentModel;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;

public class AphpProcessor extends Processor {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	public static final String code = "34117-2";
	
	public static final String codeSystem = "2.16.840.1.113883.6.1";
	
	@Override
	public Encounter process(CdaDocumentModel cdaDocumentModel) {
				
		ClinicalDocument cd = cdaDocumentModel.getClinicalDocument();
		
		Patient p = null;
		List<Provider> providers = null;
		
		try {
			p = parsePatient(cd);
			providers = parseProvider(cd);
			
		}
		catch (DocumentParseException e1) {
			e1.printStackTrace();
		}
		
		Encounter e = new Encounter();
		e.setEncounterType(getEncounterType(cd));
		
		for (Section section : cd.getAllSections()) {
			
			String code = section.getCode().getCode();
			String codeSystem = section.getCode().getCodeSystemName();
						
			try {
				e.setEncounterDatetime(parseDocumentDateTime(cd));
			}
			catch (DocumentParseException e1) {
				e1.printStackTrace();
			}
			e.setPatient(p);
			
			Set<Obs> res = new HashSet<Obs>();
			
			if (code.equals("10162-6")) {
				Obs obs = new Obs();
				Concept obsGrouper = Context.getConceptService().getConceptByMapping(code, codeSystem);
				
				obs.setConcept(obsGrouper);
		
				for (Entry entry : section.getEntries()) {
					res.addAll(parseEntry(entry, obs));
				}
				
				for (Obs o : res) {	
					
					o.setEncounter(e);
					o.setPerson(p);
					e.addObs(o);
					
				}
			}
			
		}
		
		Encounter encounter = Context.getEncounterService().saveEncounter(e);		
		return encounter;
		
	}
	
	@Override
	public String getCode() {
		return code;
	}
	
	@Override
	public String getCodeSystem() {
		return codeSystem;
	}
	
	private EncounterType getEncounterType(ClinicalDocument cd) {
		EncounterType et = Context.getEncounterService().getEncounterType(cd.getCode().getCode());
		if (et == null) {
			et = new EncounterType(cd.getCode().getCode(), String.format(
			    "CDA document; document code (LOINC, %s), type id (%s, %s)", cd.getCode().getCode(), cd.getTypeId()
			            .getRoot(), cd.getTypeId().getExtension()));
			Context.getEncounterService().saveEncounterType(et);
		}
		return et;
	}
	
	private Date parseDocumentDateTime(ClinicalDocument cd) throws DocumentParseException {
		if (cd.getAuthors().isEmpty())
			throw new DocumentParseException("Unable to locate author -> time");
		
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
		try {
			return format.parse(cd.getAuthors().get(0).getTime().getValue());
		}
		catch (ParseException e) {
			throw new DocumentParseException("Unable to parse time value");
		}
	}
	
	private Set<Obs> parseEntry(Entry entry, Obs obsGroup) {
		Set<Obs> res = new HashSet<Obs>();

		if (entry.getAct() != null) {
			//we only handle events
			if (entry.getAct().getMoodCode().compareTo(x_DocumentActMood.EVN) == 0) {
				for (EntryRelationship er : entry.getAct().getEntryRelationships()) {
					res.addAll(parseEntryRelationship(er));
				}
			}
		} else if (entry.getObservation() != null) {
			//we only handle events
			if (entry.getObservation().getMoodCode().compareTo(x_ActMoodDocumentObservation.EVN) == 0) {
				
				if (entry.getObservation().getValues() != null && !entry.getObservation().getValues().isEmpty()) {
					Obs obs = new Obs();

					obs.setConcept(getConcept(entry.getObservation().getCode().getCodeSystemName(), entry.getObservation()
					        .getCode().getCode()));
					//TODO multiple values
					processValue(obs, entry.getObservation().getValues().get(0));
					processEffectiveTime(obs, entry.getObservation().getEffectiveTime());
					
					obsGroup.addGroupMember(obs);
					res.add(obsGroup);
					res.add(obs);
				}
				
				for (EntryRelationship er : entry.getObservation().getEntryRelationships()) {
					res.addAll(parseEntryRelationship(er));
				}
			}
		} else if (entry.getOrganizer() != null) {
			for (Component4 c : entry.getOrganizer().getComponents()) {
				res.addAll(parseComponent4(c));
			}
		}
		
		return res;
	}
	
	//argh. Entry, EntryRelationship, ComponentX all share the same interfaces,
	//BUT a lot of their common methods aren't defined in the interfaces, but rather the classes themselves.
	//We'll just duplicate the code for now, but a better method should be found
	private Set<Obs> parseEntryRelationship(EntryRelationship entry) {
		Set<Obs> res = new HashSet<Obs>();
		
		if (entry.getAct() != null) {
			//we only handle events
			if (entry.getAct().getMoodCode().compareTo(x_DocumentActMood.EVN) == 0) {
				for (EntryRelationship er : entry.getAct().getEntryRelationships()) {
					res.addAll(parseEntryRelationship(er));
				}
			}
		} else if (entry.getObservation() != null) {
			//we only handle events
			if (entry.getObservation().getMoodCode().compareTo(x_ActMoodDocumentObservation.EVN) == 0) {
				
				if (entry.getObservation().getValues() != null && !entry.getObservation().getValues().isEmpty()) {
					Obs obs = new Obs();
					obs.setConcept(getConcept(entry.getObservation().getCode().getCodeSystem(), entry.getObservation()
					        .getCode().getCode()));
					//TODO multiple values
					processValue(obs, entry.getObservation().getValues().get(0));
					processEffectiveTime(obs, entry.getObservation().getEffectiveTime());
					
					res.add(obs);
				}
				
				for (EntryRelationship er : entry.getObservation().getEntryRelationships()) {
					res.addAll(parseEntryRelationship(er));
				}
			}
		} else if (entry.getOrganizer() != null) {
			for (Component4 c : entry.getOrganizer().getComponents()) {
				res.addAll(parseComponent4(c));
			}
		}
		
		return res;
	}
	
	//argh. Entry, EntryRelationship, ComponentX all share the same interfaces,
	//BUT a lot of their common methods aren't defined in the interfaces, but rather the classes themselves.
	//We'll just duplicate the code for now, but a better method should be found
	private Set<Obs> parseComponent4(Component4 entry) {
		Set<Obs> res = new HashSet<Obs>();
		
		if (entry.getAct() != null) {
			//we only handle events
			if (entry.getAct().getMoodCode().compareTo(x_DocumentActMood.EVN) == 0) {
				for (EntryRelationship er : entry.getAct().getEntryRelationships()) {
					res.addAll(parseEntryRelationship(er));
				}
			}
		} else if (entry.getObservation() != null) {
			//we only handle events
			if (entry.getObservation().getMoodCode().compareTo(x_ActMoodDocumentObservation.EVN) == 0) {
				
				if (entry.getObservation().getValues() != null && !entry.getObservation().getValues().isEmpty()) {
					Obs obs = new Obs();
					obs.setConcept(getConcept(entry.getObservation().getCode().getCodeSystem(), entry.getObservation()
					        .getCode().getCode()));
					//TODO multiple values
					processValue(obs, entry.getObservation().getValues().get(0));
					processEffectiveTime(obs, entry.getObservation().getEffectiveTime());
					
					res.add(obs);
				}
				
				for (EntryRelationship er : entry.getObservation().getEntryRelationships()) {
					res.addAll(parseEntryRelationship(er));
				}
			}
		} else if (entry.getOrganizer() != null) {
			for (Component4 c : entry.getOrganizer().getComponents()) {
				res.addAll(parseComponent4(c));
			}
		}
		
		return res;
	}
	
	private void processValue(Obs dst, ANY value) {
		if (value instanceof PQ) {
			dst.setValueNumeric(((PQ) value).getValue().doubleValue());
		} else if (value instanceof CD || value instanceof CE) {
			String code = ((CD) value).getCode();
			String namespace = ((CD) value).getCodeSystem();
			if (code != null && !code.isEmpty()) {
				Concept answer = getConcept(namespace, code);
				dst.setValueCoded(answer);
			}
		}else if (value instanceof INT ) {
			BigInteger i = ((INT) value).getValue();
			dst.setValueNumeric(i.doubleValue());
		}else if (value instanceof ST ) {
			String str = ((ST)value).getText();	
	            dst.setValueText(str);
           
		}else if (value instanceof TS ) {
			String dateString = ((TS) value).getValue();			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
			Date date = null;
			
			try {
		 
				date = formatter.parse(dateString);
				dst.setValueDate(date);
		 
			} catch (ParseException e) {
				e.printStackTrace();
			}
					
			dst.setValueDate(date);
		}
	}
	
	private void processEffectiveTime(Obs dst, IVL_TS time) {
		//TODO
		dst.setObsDatetime(new Date());
	}
	
	private Concept getConcept(String namespace, String code) {
		if (namespace == null || namespace.isEmpty())
			namespace = "Default";
		
		ConceptService serv = Context.getConceptService();
		Concept c = serv.getConceptByMapping(code, namespace);
		if (c == null) {
			System.out.println("c == null");
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
		
		if (cs == null) {
			cs = new ConceptSource();
			cs.setName(namespace);
			cs.setDescription(namespace);
			Context.getConceptService().saveConceptSource(cs);
		}
		
		return cs;
	}
	
}
