package org.openmrs.module.shr.cdahandler.processor.util;

import java.util.Collections;
import java.util.List;

import org.dom4j.DocumentException;
import org.marc.everest.datatypes.AD;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.PN;
import org.marc.everest.datatypes.TEL;
import org.marc.everest.datatypes.generic.COLL;
import org.marc.everest.formatters.FormatterUtil;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.PatientRole;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.configuration.CdaHandlerConfiguration;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;

/**
 * Represents a series of utility functions interacting with Patientrole
 * @author Justin
 *
 */
public final class PatientRoleProcessorUtil {

	// Auto create patients
	
	/**
	 * Get the singleton instance
	 */
	public static PatientRoleProcessorUtil getInstance()
	{
		if(s_instance == null)
		{
			synchronized (s_lockObject) {
				if(s_instance == null)
					s_instance = new PatientRoleProcessorUtil();
			}
		}
		return s_instance;
	}
	// singleton instance
	private static PatientRoleProcessorUtil s_instance;
	
	private static Object s_lockObject = new Object();
	// Configuration and util instances
	private final CdaHandlerConfiguration m_configuration = CdaHandlerConfiguration.getInstance();
	private final DatatypeProcessorUtil m_datatypeUtil = DatatypeProcessorUtil.getInstance();
	private final OpenmrsMetadataUtil m_metadataUtil = OpenmrsMetadataUtil.getInstance();
	
	
	private final OpenmrsConceptUtil m_conceptUtil = OpenmrsConceptUtil.getInstance();
	
	/**
	 * Private ctor
	 */
	private PatientRoleProcessorUtil()
	{
	}

	/**
	 * Creates a patient from the specified CDA PatientRole object
	 * @param importPatient The PatientRole from which the OpenMRS patient should be created
	 * @param id The identifier to use for the patient
	 * @return
	 */
	public Patient createPatient(PatientRole importPatient, PatientIdentifier id) throws DocumentImportException {
		
		if(!this.m_configuration.getAutoCreatePatients()) // not supposed to be here
			throw new IllegalStateException("Cannot auto-create patients according to current global properties");
		
		
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
						res.addName(this.m_datatypeUtil.parseEN(pn));
			
			// Set gender
			if(importPatient.getPatient().getAdministrativeGenderCode().isNull())
				res.setGender("U");
			else
				res.setGender(importPatient.getPatient().getAdministrativeGenderCode().getCode().getCode());
			
			//set patient birthdate
			if(importPatient.getPatient().getBirthTime() != null && !importPatient.getPatient().getBirthTime().isNull())
				res.setBirthdate(importPatient.getPatient().getBirthTime().getDateValue().getTime());
			else
				throw new DocumentImportException("Patient missing birthdate");
			
			// Marital status code
			if(importPatient.getPatient().getMaritalStatusCode() != null &&
					!importPatient.getPatient().getMaritalStatusCode().isNull())
			{
				PersonAttribute maritalStatus = new PersonAttribute();
				PersonAttributeType maritalStatusType = this.m_metadataUtil.getOrCreatePersonMaritalStatusAttribute();
				maritalStatus.setAttributeType(maritalStatusType);
				// This is a coded value, so we need to find the code
				Concept maritalStatusConcept = null;
				if(maritalStatusType.getForeignKey() != null)
					maritalStatusConcept = Context.getConceptService().getConcept(maritalStatusType.getForeignKey());

				Concept	valueConcept = this.m_conceptUtil.getConcept(importPatient.getPatient().getMaritalStatusCode());
				if(valueConcept == null)
					valueConcept = this.m_conceptUtil.createConcept(importPatient.getPatient().getMaritalStatusCode());
				
				// Now we want to set the concept
				if(maritalStatusConcept != null)
					this.m_conceptUtil.addAnswerToConcept(maritalStatusConcept, valueConcept);
				// Now set the values
				maritalStatus.setPerson(res);
				maritalStatus.setValue(valueConcept.getId().toString());
				res.addAttribute(maritalStatus);
			}
		}
		else
			throw new DocumentImportException("Missing patient demographics information"); 

		// Telecoms
		if(importPatient.getTelecom() != null)
			for(TEL tel : importPatient.getTelecom())
			{
				if(tel == null || tel.isNull()) continue;
				
				PersonAttribute telecomAttribute = new PersonAttribute();
				telecomAttribute.setAttributeType(this.m_metadataUtil.getOrCreatePersonTelecomAttribute());
				telecomAttribute.setValue(String.format("%s: %s", FormatterUtil.toWireFormat(tel.getUse()), tel.getValue()));
				telecomAttribute.setPerson(res);
				res.addAttribute(telecomAttribute);
			}
		
		// Address
		if(importPatient.getAddr() != null)
			for(AD ad : importPatient.getAddr())
				if(!ad.isNull())
					res.addAddress(this.m_datatypeUtil.parseAD(ad));
		

		
		res = Context.getPatientService().savePatient(res);
		
		return res;
	}
	
	/**
	 * Generates or retrieves the patient identifier type from the ids
	 * Based heavily on code from SurangaK
	 * @param patientIds Identifiers to scan for a useful ID type
	 * @return The PatientIdentifierType of the first found match in ids or a new PatientIdentifierType based on the first entry
	 * @throws DocumentImportException 
	 */
	public PatientIdentifier getApplicablePatientIdentifier(COLL<II> patientIds) throws DocumentImportException {
		
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
		if (pit==null && this.m_configuration.getAutoCreatePatientIdType()) {
			candidateId = patientIds.get(0);
			//create new id type
			pit = new PatientIdentifierType();
			pit.setName(candidateId.getRoot());
			pit.setDescription(String.format("OpenHIE SHR generated patient identifier type for '%s' authority", candidateId.getAssigningAuthorityName() != null ? candidateId.getAssigningAuthorityName() : candidateId.getRoot())); 
			Context.getPatientService().savePatientIdentifierType(pit);
		}
		else if(pit == null && !this.m_configuration.getAutoCreatePatientIdType())
			throw new DocumentImportException(String.format("Could not find a known patient identifier type"));
		return new PatientIdentifier(
				candidateId.getExtension(), 
				pit, 
				Context.getLocationService().getDefaultLocation());
	}
	
	/**
	 * Parse OpenMRS patient data from a CDA PatientRole
	 * @param cd
	 * @return
	 * @throws DocumentImportException
	 * @throws DocumentException 
	 */
	public Patient processPatient(PatientRole patient) throws DocumentImportException {

		// Ensure patient is not null and has identifiers
		Patient res = null;
		if (patient == null || patient.getNullFlavor() != null)
			throw new DocumentImportException("Patient role is null");
		else if(patient.getId() == null || patient.getId().isNull() || patient.getId().isEmpty())
			throw new DocumentImportException("No patient identifiers found in document");
		
		// Create identifier type or get identifier type
		PatientIdentifier pid = this.getApplicablePatientIdentifier(patient.getId());
		List<Patient> matches = Context.getPatientService().getPatients(null, pid.getIdentifier(), Collections.singletonList(pid.getIdentifierType()), true);
		
		if (matches.isEmpty() && this.m_configuration.getAutoCreatePatients()) {
			res = this.createPatient(patient, pid);
		} else if(!matches.isEmpty()){
			res = matches.get(0);
		}
		else 
			throw new DocumentImportException(String.format("Patient %s not found", pid.getIdentifier()));
		
		
		return res;
	}
}
