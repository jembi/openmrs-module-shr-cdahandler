package org.openmrs.module.shr.cdahandler.processor.util;

import java.util.Collections;
import java.util.List;

import org.dom4j.DocumentException;
import org.marc.everest.datatypes.AD;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.PN;
import org.marc.everest.datatypes.TEL;
import org.marc.everest.datatypes.TS;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.datatypes.generic.COLL;
import org.marc.everest.formatters.FormatterUtil;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.PatientRole;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
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
	private final LocationOrganizationProcessorUtil m_locationUtil = LocationOrganizationProcessorUtil.getInstance();
	
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
	public Patient createPatient(PatientRole importPatient) throws DocumentImportException {
		
		if(!this.m_configuration.getAutoCreatePatients()) // not supposed to be here
			throw new IllegalStateException("Cannot auto-create patients according to current global properties");
		
		
		Patient res = new Patient();
		res = this.updatePatientInformation(res, importPatient);
		
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
			if(pit == null)
				pit = Context.getPatientService().getPatientIdentifierTypeByUuid(id.getRoot());
			if(pit != null && pit.getName().equals(this.m_configuration.getEcidRoot())) 
				break; // don't look any further we have the ECID
		}
		
		// If none found 
		if (pit==null && this.m_configuration.getAutoCreatePatientIdType()) {
			candidateId = patientIds.get(0);
			//create new id type
			pit = new PatientIdentifierType();
			pit.setName(candidateId.getRoot());
			pit.setUuid(candidateId.getRoot());
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
			res = this.createPatient(patient);
		} else if(!matches.isEmpty()){
			res = matches.get(0);
			
			// Update data for patient
			if(this.m_configuration.getAutoCreatePatients())
			{
				res = this.updatePatientInformation(res, patient);
				res = Context.getPatientService().savePatient(res);
			}
		}
		else 
			throw new DocumentImportException(String.format("Patient %s not found", pid.getIdentifier()));
		
		
		return res;
	}

	/**
	 * Update patient information based on new information from the CDA
	 */
	private Patient updatePatientInformation(Patient res, PatientRole importPatient) throws DocumentImportException {

		// Now add other ids
		for(II id : importPatient.getId())
		{
			PatientIdentifierType pit = Context.getPatientService().getPatientIdentifierTypeByName(id.getRoot());
			if(pit == null)
				pit = Context.getPatientService().getPatientIdentifierTypeByUuid(id.getRoot());
			if(pit == null && this.m_configuration.getAutoCreatePatientIdType())
			{
				pit = new PatientIdentifierType();
				pit.setName(id.getRoot());
				pit.setUuid(id.getRoot());
				pit.setDescription(String.format("OpenHIE SHR generated patient identifier type for '%s' authority", id.getAssigningAuthorityName() != null ? id.getAssigningAuthorityName() : id.getRoot()));
				
				pit = Context.getPatientService().savePatientIdentifierType(pit);
			}
			else if(pit == null && !this.m_configuration.getAutoCreatePatientIdType())
				continue; // next
			
			PatientIdentifier pid = new PatientIdentifier();
			pid.setIdentifierType(pit);
			pid.setIdentifier(id.getExtension());

			// Is there already an identifier with this type/extensions?
			PatientIdentifier existingPid = res.getPatientIdentifier(pit);
			if(existingPid != null && existingPid.getIdentifier().equals(pid.getIdentifier())) // Already have an ID
				continue;
			else if(existingPid != null)
				throw new DocumentImportException("Patient can only have one ID assigned from one authority");
			
			// Provider organization
			if(importPatient.getProviderOrganization() != null && importPatient.getProviderOrganization().getId() != null)
				for(II provId : importPatient.getProviderOrganization().getId())
					if(provId.getRoot().equals(id.getRoot()))
						pid.setLocation(this.m_locationUtil.processOrganization(importPatient.getProviderOrganization()));
			
			// Get default location
			if(pid.getLocation() == null)
				pid.setLocation(Context.getLocationService().getDefaultLocation());
			
			pid.setPreferred(id.getRoot().equals(this.m_configuration.getEcidRoot()));
			res.addIdentifier(pid);
		}
		
		// Don't attempt to parse a null patient
		if(importPatient.getPatient() != null)
		{	
			// Add names if they exist
			if(importPatient.getPatient().getName() != null)
				for(PN pn : importPatient.getPatient().getName())
					if(!pn.isNull())
					{
						PersonName name = this.m_datatypeUtil.parseEN(pn);

						// Does the patient already have this name?
						boolean hasName = false;
						for(PersonName existingName : res.getNames())
							hasName |= existingName.getFamilyName().equals(name.getFamilyName()) &&
								existingName.getGivenName().equals(name.getGivenName());
						
						if(!hasName)
							res.addName(name);
					}
				
			if(res.getNames().size() == 0)
				res.getNames().add(new PersonName("*", null, "*"));
			
			// Set gender if a current gender is unknown
			if(res.getGender() == null)
			{
				if(importPatient.getPatient().getAdministrativeGenderCode().isNull())
					res.setGender("U");
				else
					res.setGender(importPatient.getPatient().getAdministrativeGenderCode().getCode().getCode());
			}
			
			//set patient birthdate if current is unknown
			if(res.getBirthdate() == null)
			{
				if(importPatient.getPatient().getBirthTime() != null && !importPatient.getPatient().getBirthTime().isNull())
				{
					res.setBirthdateEstimated(importPatient.getPatient().getBirthTime().getDateValuePrecision() <= TS.MONTH);
					res.setBirthdate(importPatient.getPatient().getBirthTime().getDateValue().getTime());
				}
				else
					throw new DocumentImportException("Patient missing birthdate");
			}
			
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

				Concept	valueConcept = this.m_conceptUtil.getConcept(importPatient.getPatient().getMaritalStatusCode(), new CE<String>());
				if(valueConcept == null)
					valueConcept = this.m_conceptUtil.createConcept(importPatient.getPatient().getMaritalStatusCode());
				
				// Now we want to set the concept
				if(maritalStatusConcept != null)
					this.m_conceptUtil.addAnswerToConcept(maritalStatusConcept, valueConcept);

				// Now set the values
				maritalStatus.setPerson(res);
				maritalStatus.setValue(valueConcept.getId().toString());

				PersonAttribute existingData = res.getAttribute(maritalStatusType); 
				if(existingData != null)
					res.removeAttribute(existingData);
				res.addAttribute(maritalStatus);
			}
		}
		else
			throw new DocumentImportException("Missing patient demographics information"); 

		// Telecoms
		if(importPatient.getTelecom() != null)
		{
			// Remove telecoms
			PersonAttributeType telecomAttributeType = this.m_metadataUtil.getOrCreatePersonTelecomAttribute();
			PersonAttribute telecomAttribute = res.getAttribute(telecomAttributeType);
			while(telecomAttribute != null)
			{
				res.removeAttribute(telecomAttribute);
				telecomAttribute = res.getAttribute(telecomAttributeType);
			}
			
			// Now add new
			for(TEL tel : importPatient.getTelecom())
			{
				if(tel == null || tel.isNull() || tel.getValue() == null) continue;
				
				
				telecomAttribute = new PersonAttribute();
				telecomAttribute.setAttributeType(telecomAttributeType);
				telecomAttribute.setValue(String.format("%s: %s", FormatterUtil.toWireFormat(tel.getUse()), tel.getValue()));
				telecomAttribute.setPerson(res);
				res.addAttribute(telecomAttribute);
			}
		}
		
		// Address
		if(importPatient.getAddr() != null)
		{
			// Clear existing addresses and use this as a "last known address"
			for(PersonAddress existingAddr : res.getAddresses())
				res.removeAddress(existingAddr);
			
			// Now add
			for(AD ad : importPatient.getAddr())
				if(!ad.isNull())
				{
					PersonAddress address = this.m_datatypeUtil.parseAD(ad);
					res.addAddress(address);
				}
		}
		return res;
    }
}
