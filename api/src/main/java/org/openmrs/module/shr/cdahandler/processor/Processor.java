package org.openmrs.module.shr.cdahandler.processor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openhealthtools.mdht.uml.cda.AssignedAuthor;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaDocumentModel;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;



public abstract class Processor {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	public abstract String getDocumentType();
	public abstract String getNamespace();
	public abstract CdaDocumentModel process(CdaDocumentModel cdaDocumentModel);
	
	public Patient parsePatient(ClinicalDocument cd) throws DocumentParseException {
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
	
	public PatientIdentifierType getIdentifierType(String idType) {
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
	
	public Patient createPatient(org.openhealthtools.mdht.uml.cda.Patient importPatient, PatientIdentifierType idType, String id) {
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
		
		//set patient birthdate
		String dateString = importPatient.getBirthTime().getValue();
		Date date = null;
		
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		try {
	        date =  df.parse(dateString);
        }
        catch (ParseException e) {
	        e.printStackTrace();
        }  
		
		res.setBirthdate(date);
		
		Context.getPatientService().savePatient(res);
		return res;
	}
	
	public Provider parseProvider(ClinicalDocument cd) throws DocumentParseException {
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
	
	public Provider createProvider(AssignedAuthor aa, String idRoot, String idExtension) {
		Provider res = new Provider();
		
		res.setIdentifier(idRoot + "-" + (idExtension!=null && !idExtension.isEmpty() ? idExtension : UUID.randomUUID()));
		res.setName(
			aa.getAssignedPerson().getNames().get(0).getGivens().get(0).getText() + " " +
			aa.getAssignedPerson().getNames().get(0).getFamilies().get(0).getText()
			);
		
		Context.getProviderService().saveProvider(res);
		return res;
	}
	
}
