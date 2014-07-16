package org.openmrs.module.shr.cdahandler.processor.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.marc.everest.datatypes.AD;
import org.marc.everest.datatypes.EN;
import org.marc.everest.datatypes.PN;
import org.marc.everest.datatypes.TEL;
import org.marc.everest.formatters.FormatterUtil;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.AssociatedEntity;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;

/**
 * Parsing utilities for Person objects
 * @author Justin
 *
 */
public final class PersonProcessorUtil {

	// singleton instance
	private static PersonProcessorUtil s_instance;
	private static Object s_lockObject = new Object();
	
	// Auto create providers
	private Boolean m_autoCreatePersons = true;
	
	/**
	 * Private ctor
	 */
	private PersonProcessorUtil()
	{
	}
	
	/**
	 * Initialize instance
	 */
	private void initializeInstance()
	{
		String propertyValue = Context.getAdministrationService().getGlobalProperty(CdaHandlerConstants.PROP_AUTOCREATE_PERSONS);
		if(propertyValue != null && !propertyValue.isEmpty())
			this.m_autoCreatePersons = Boolean.parseBoolean(propertyValue);
		else
			Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(CdaHandlerConstants.PROP_AUTOCREATE_PERSONS, this.m_autoCreatePersons.toString()));
	}
	
	/**
	 * Get the singleton instance
	 */
	public static PersonProcessorUtil getInstance()
	{
		if(s_instance == null)
		{
			synchronized (s_lockObject) {
				if(s_instance == null)
				{
					s_instance = new PersonProcessorUtil();
					s_instance.initializeInstance();
				}
			}
		}
		return s_instance;
	}
		
	/**
	 * Parse an HL7v3 Person class into the equivalent OpenMRS class
	 * @param person The HL7v3
	 * @return The parsed OpenMRS person
	 */
	public Person createPerson(
			org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Person person) throws DocumentImportException {
		
		if(person == null || person.getNullFlavor() != null)
			throw new DocumentImportException("Cannot parse a null person relationship");
		else if(!this.m_autoCreatePersons)
			throw new IllegalStateException("Cannot auto-create persons according to current global properties");
		
		Person res = new Person();
		
		// Parse names
		if(person.getName() != null)
			for(PN pn : person.getName())
				if(!pn.isNull())
					res.addName(DatatypeProcessorUtil.getInstance().parseEN(pn));
		
		// Gender not provided.. Assign
		res.setGender("U");
		
		res = Context.getPersonService().savePerson(res);
		return res;
	}

	/**
	 * Process an associated entity, which is similar to a patient however is not a patient 
	 * per se
	 * 
	 * This method is kind of hackish. It works by finding all the people associated with "associatedWith" and then 
	 * determines if "entity" has the same name as the "associatedWith". If not it creates the person. 
	 * @throws DocumentImportException 
	 */
	public Relationship processAssociatedEntity(AssociatedEntity entity, Person associatedWith) throws DocumentImportException {
		// Ensure person is not null 
		if (entity == null || entity.getNullFlavor() != null)
			throw new DocumentImportException("Entity role is null");
		else if(entity.getAssociatedPerson() == null || entity.getAssociatedPerson().getNullFlavor() != null)
			throw new DocumentImportException("Entity role is null");
		
		DatatypeProcessorUtil datatypeProcessor = DatatypeProcessorUtil.getInstance();
		OpenmrsMetadataUtil metadataProcessor = OpenmrsMetadataUtil.getInstance();

		// Person names for the name search
		Set<PersonName> entityNames = new HashSet<PersonName>();
		for(EN en : entity.getAssociatedPerson().getName())
			if(!en.isNull())
				entityNames.add(datatypeProcessor.parseEN(en));

		// Get relationship type
		RelationshipType relationshipType = metadataProcessor.getOrCreateRelationshipType(entity.getCode());
		
		// Find all persons with the same name
		List<Relationship> candidatePersons = Context.getPersonService().getRelationshipsByPerson(associatedWith);
		for(Relationship rel : candidatePersons)
			if((rel.getPersonA().getNames().containsAll(entityNames) || rel.getPersonB().getNames().containsAll(entityNames))
					&& rel.getRelationshipType().equals(relationshipType)) // entity is person A
				return rel;

		// Create the person
		Person person = this.createPerson(entity);
		
		// Create a relationship
		Relationship rel = new Relationship();
		rel.setPersonB(associatedWith);
		rel.setPersonA(person);
		rel.setRelationshipType(relationshipType);
		rel = Context.getPersonService().saveRelationship(rel);
		
		return rel;
    }

	/**
	 * Create a person from associated entity
	 * @param entity
	 * @throws DocumentImportException 
	 */
	protected Person createPerson(AssociatedEntity entity) throws DocumentImportException {
		
		if(entity.getAssociatedPerson() == null || entity.getAssociatedPerson().getNullFlavor() != null)
			throw new DocumentImportException("AssociatedEntity is missing Person relationship");
		else if(!this.m_autoCreatePersons)
			throw new IllegalStateException("Cannot create persons");
		
		DatatypeProcessorUtil datatypeProcessor = DatatypeProcessorUtil.getInstance();
		OpenmrsMetadataUtil metadataUtil = OpenmrsMetadataUtil.getInstance();
		
		Person res = this.createPerson(entity.getAssociatedPerson());
	    
	    // Set addresses
	    if(entity.getAddr() != null)
	    	for(AD ad : entity.getAddr())
	    		if(ad != null && !ad.isNull())
	    			res.addAddress(datatypeProcessor.parseAD(ad));
	    
	    // Set Telecoms
	    if(entity.getTelecom() != null)
	    	for(TEL tel : entity.getTelecom())
	    	{
    			if(tel == null || tel.isNull()) continue;
				
				PersonAttribute telecomAttribute = new PersonAttribute();
				telecomAttribute.setAttributeType(metadataUtil.getOrCreatePersonTelecomAttribute());
				telecomAttribute.setValue(String.format("%s: %s", FormatterUtil.toWireFormat(tel.getUse()), tel.getValue()));
				telecomAttribute.setPerson(res);
				res.addAttribute(telecomAttribute);
	    	}
	    
	    return res;
	    
    }
	
}
