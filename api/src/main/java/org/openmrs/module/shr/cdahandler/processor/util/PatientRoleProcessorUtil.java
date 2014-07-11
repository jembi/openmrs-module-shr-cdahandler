package org.openmrs.module.shr.cdahandler.processor.util;

import java.util.Collections;
import java.util.List;

import org.dom4j.DocumentException;
import org.marc.everest.datatypes.AD;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.PN;
import org.marc.everest.datatypes.generic.COLL;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.PatientRole;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerGlobalPropertyNames;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;

/**
 * Represents a series of utility functions interacting with Patientrole
 * @author Justin
 *
 */
public final class PatientRoleProcessorUtil {

	// Auto create patients
	
	// singleton instance
	private static PatientRoleProcessorUtil s_instance;
	private static Object s_lockObject = new Object();
	
	// Auto create providers
	private Boolean m_autoCreatePatients = true;
	private Boolean m_autoCreatePatientIdType = true;
	
	/**
	 * Private ctor
	 */
	private PatientRoleProcessorUtil()
	{
	}
	
	/**
	 * Initialize instance
	 */
	private void initializeInstance()
	{
		// Auto create patients
		String propertyValue = Context.getAdministrationService().getGlobalProperty(CdaHandlerGlobalPropertyNames.AUTOCREATE_PATIENTS);
		if(propertyValue != null && !propertyValue.isEmpty())
			this.m_autoCreatePatients = Boolean.parseBoolean(propertyValue);
		else
			Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(CdaHandlerGlobalPropertyNames.AUTOCREATE_PATIENTS, this.m_autoCreatePatients.toString()));
		
		// Auto create id types
		propertyValue = Context.getAdministrationService().getGlobalProperty(CdaHandlerGlobalPropertyNames.AUTOCREATE_PATIENTIDTYPE);
		if(propertyValue != null && !propertyValue.isEmpty())
			this.m_autoCreatePatientIdType = Boolean.parseBoolean(propertyValue);
		else
			Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(CdaHandlerGlobalPropertyNames.AUTOCREATE_PATIENTIDTYPE, this.m_autoCreatePatientIdType.toString()));
		
	}
	
	/**
	 * Get the singleton instance
	 */
	public static PatientRoleProcessorUtil getInstance()
	{
		if(s_instance == null)
		{
			synchronized (s_lockObject) {
				if(s_instance == null)
				{
					s_instance = new PatientRoleProcessorUtil();
					s_instance.initializeInstance();
				}
			}
		}
		return s_instance;
	}

	/**
	 * Parse OpenMRS patient data from a CDA PatientRole
	 * @param cd
	 * @return
	 * @throws DocumentParseException
	 * @throws DocumentException 
	 */
	public Patient parsePatient(PatientRole patient) throws DocumentParseException {

		// Ensure patient is not null and has identifiers
		Patient res = null;
		if (patient == null || patient.getNullFlavor() != null)
			throw new DocumentParseException("Patient role is null");
		else if(patient.getId() == null || patient.getId().isNull() || patient.getId().isEmpty())
			throw new DocumentParseException("No patient identifiers found in document");
		
		// Create identifier type or get identifier type
		PatientIdentifier pid = this.getApplicablePatientIdentifier(patient.getId());
		List<Patient> matches = Context.getPatientService().getPatients(null, pid.getIdentifier(), Collections.singletonList(pid.getIdentifierType()), true);
		
		if (matches.isEmpty() && this.m_autoCreatePatients) {
			res = this.createPatient(patient, pid);
		} else if(!matches.isEmpty()){
			res = matches.get(0);
		}
		else 
			throw new DocumentParseException(String.format("Patient %s not found", pid.getIdentifier()));
		
		return res;
	}
	
	/**
	 * Generates or retrieves the patient identifier type from the ids
	 * Based heavily on code from SurangaK
	 * @param patientIds Identifiers to scan for a useful ID type
	 * @return The PatientIdentifierType of the first found match in ids or a new PatientIdentifierType based on the first entry
	 * @throws DocumentParseException 
	 */
	private PatientIdentifier getApplicablePatientIdentifier(COLL<II> patientIds) throws DocumentParseException {
		
		// There may be multiple identifiers here, need to figure out a way to weed out the ones we're not interested in
		PatientIdentifierType pit = null;
		II candidateId = null;
		for(II id : patientIds)
		{
			candidateId = id;
			pit = Context.getPatientService().getPatientIdentifierTypeByName(candidateId.getRoot());
			if(pit != null)
				break; // don't look any further
		}
		
		// If none found 
		if (pit==null && this.m_autoCreatePatientIdType) {
			candidateId = patientIds.get(0);
			//create new id type
			pit = new PatientIdentifierType();
			pit.setName(candidateId.getRoot());
			pit.setDescription(String.format("OpenHIE SHR generated patient identifier type for '%s' authority", candidateId.getAssigningAuthorityName() != null ? candidateId.getAssigningAuthorityName() : candidateId.getRoot())); 
			Context.getPatientService().savePatientIdentifierType(pit);
		}
		else if(pit == null && !this.m_autoCreatePatientIdType)
			throw new DocumentParseException(String.format("Could not find a known patient identifier type"));
		return new PatientIdentifier(
				candidateId.getExtension(), 
				pit, 
				Context.getLocationService().getDefaultLocation());
	}
	
	/**
	 * Creates a patient from the specified CDA PatientRole object
	 * @param importPatient The PatientRole from which the OpenMRS patient should be created
	 * @param id The identifier to use for the patient
	 * @return
	 */
	public Patient createPatient(PatientRole importPatient, PatientIdentifier id) throws DocumentParseException {
		
		if(!this.m_autoCreatePatients) // not supposed to be here
			throw new IllegalStateException("Cannot auto-create patients according to current global properties");
		
		DatatypeProcessorUtil datatypeProcessorUtil = DatatypeProcessorUtil.getInstance();
		
		Patient res = new Patient();
		
		id.setPreferred(true);
		res.addIdentifier(id);

		// Don't attempt to parse a null patient
		if(importPatient.getPatient() != null)
		{	
			// Add names if they exist
			if(importPatient.getPatient().getName() != null)
				for(PN pn : importPatient.getPatient().getName())
					if(!pn.isNull())
						res.addName(datatypeProcessorUtil.parseEN(pn));
			
			// Set gender
			res.setGender(importPatient.getPatient().getAdministrativeGenderCode().getCode().getCode());
			
			//set patient birthdate
			res.setBirthdate(importPatient.getPatient().getBirthTime().getDateValue().getTime());
	
		}
		else
			throw new DocumentParseException("Missing patient demographics information"); 
		
		// Address
		if(importPatient.getAddr() != null)
			for(AD ad : importPatient.getAddr())
				if(!ad.isNull())
					res.addAddress(datatypeProcessorUtil.parseAD(ad));
		
		res = Context.getPatientService().savePatient(res);
		
		return res;
	}
}
