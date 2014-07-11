package org.openmrs.module.shr.cdahandler.processor.util;

import org.apache.commons.lang.NotImplementedException;
import org.marc.everest.datatypes.AD;
import org.marc.everest.datatypes.TEL;
import org.marc.everest.formatters.FormatterUtil;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.AssignedAuthor;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.AssignedEntity;
import org.openmrs.GlobalProperty;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerGlobalPropertyNames;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;

/**
 * Parser utilities for Assigned* classes (like AssignedEntity, AssignedAuthor, etc.)
 * @author Justin
 *
 */
public final class AssignedEntityProcessorUtil {

	// singleton instance
	private static AssignedEntityProcessorUtil s_instance;
	private static Object s_lockObject = new Object();
	
	// Auto create providers
	private Boolean m_autoCreateProviders = true;
	
	/**
	 * Private ctor
	 */
	private AssignedEntityProcessorUtil()
	{
		
	}
	
	/**
	 * Initialize instance
	 */
	private void initializeInstance()
	{
		String propertyValue = Context.getAdministrationService().getGlobalProperty(CdaHandlerGlobalPropertyNames.AUTOCREATE_PROVIDERS);
		if(propertyValue != null && !propertyValue.isEmpty())
			this.m_autoCreateProviders = Boolean.parseBoolean(propertyValue);
		else
			Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(CdaHandlerGlobalPropertyNames.AUTOCREATE_PROVIDERS, this.m_autoCreateProviders.toString()));
	}
	
	/**
	 * Get the singleton instance
	 */
	public static AssignedEntityProcessorUtil getInstance()
	{
		if(s_instance == null)
		{
			synchronized (s_lockObject) {
				if(s_instance == null) // Check to make sure another thread hasn't constructed
				{
					s_instance = new AssignedEntityProcessorUtil();
					s_instance.initializeInstance();
				}
			}
		}
		return s_instance;
	}
	
	/**
	 * Parse a provider from the AssignedAuthor node
	 * @param aut The AssignedAuthor to parse
	 * @return The parsed assigned author
	 */
	public Provider parseProvider(AssignedAuthor aut) throws DocumentParseException {

		DatatypeProcessorUtil datatypeProcessorUtil = DatatypeProcessorUtil.getInstance();
		
		if (aut == null || aut.getNullFlavor() != null)
			throw new DocumentParseException("AssignedAuthor role is null");
		else if(aut.getId() == null || aut.getId().isNull() || aut.getId().isEmpty())
			throw new DocumentParseException("No identifiers found for author");
		
		// TODO: How to add this like the ECID/EPID identifiers in the current SHR
		// ie. root becomes an attribute and the extension becomes the value
		// Anyways, for now represent in the standard ITI guidance for II data type
		String id = datatypeProcessorUtil.formatIdentifier(aut.getId().get(0));
		
		Provider res = null;
		
		if (id.equals(datatypeProcessorUtil.emptyIdString())) 
			throw new DocumentParseException("No data specified for author id");
		else 				
			res = Context.getProviderService().getProviderByIdentifier(id);
			
		if (res==null && this.m_autoCreateProviders)
			res = this.createProvider(aut, id);
		else if(res == null && !this.m_autoCreateProviders)
			throw new DocumentParseException(String.format("Unknown provider %s", id));
		return res;
	}
	
	/**
	 * Create a provider from the AssignedAuthor data
	 * @param aa The assigned author
	 * @param idRoot The processed root identifier
	 * @param idExtension The processed extension
	 * @return
	 */
	public Provider createProvider(AssignedAuthor aa, String id) throws DocumentParseException {

		if(!this.m_autoCreateProviders)
			throw new IllegalStateException("Cannot auto-create providers according to current global properties");
		
		// Get the processor util
		PersonProcessorUtil personProcessorUtil = PersonProcessorUtil.getInstance();
		OpenmrsMetadataUtil metadataUtil = OpenmrsMetadataUtil.getInstance();
		
		Provider res = new Provider();
		
		res.setIdentifier(id);
		if(aa.getAssignedAuthorChoiceIfAssignedAuthoringDevice() != null)
			throw new NotImplementedException("OpenSHR doesn't support storing of AuthoringDevices .. yet");
		else
			res.setPerson(personProcessorUtil.createPerson(aa.getAssignedAuthorChoiceIfAssignedPerson()));
		
		
		// Address
		if(aa.getAddr() != null && !aa.getAddr().isNull())
		{
			if(res.getPerson() == null)
				res.setPerson(new Person());
			for(AD ad : aa.getAddr())
				if(!ad.isNull())
					res.getPerson().addAddress(DatatypeProcessorUtil.getInstance().parseAD(ad));
		}
				
		// Telecom?
		if(aa.getTelecom() != null)
			for(TEL tel : aa.getTelecom())
			{
				if(tel == null || tel.isNull()) continue;
				
				ProviderAttribute telecomAttribute = new ProviderAttribute();
				telecomAttribute.setAttributeType(metadataUtil.getProviderTelecomAttribute());
				telecomAttribute.setValueReferenceInternal(String.format("%s: %s", FormatterUtil.toWireFormat(tel.getUse()), tel.getValue()));
				res.addAttribute(telecomAttribute);
			}
		
		// Organization
		// TODO: This could be assigned to person attribute type of location
		if(aa.getRepresentedOrganization() != null && aa.getRepresentedOrganization().getNullFlavor() == null)
		{
			ProviderAttribute organizationAttribute = new ProviderAttribute();
			organizationAttribute.setAttributeType(metadataUtil.getProviderOrganizationAttribute());
			organizationAttribute.setValueReferenceInternal(aa.getRepresentedOrganization().getName().toString());
			res.addAttribute(organizationAttribute);
		}
		
		res = Context.getProviderService().saveProvider(res);
		return res;
	}

	/**
	 * Create a provider from an assigned entity if applicable
	 * @param assignedEntity The assigned entity class to parse
	 * @return The OpenMRS provider
	 * @throws DocumentParseException 
	 */
	public Provider parseProvider(AssignedEntity assignedEntity) throws DocumentParseException {

		if (assignedEntity == null || assignedEntity.getNullFlavor() != null)
			throw new DocumentParseException("AssignedEntity role is null");
		else if(assignedEntity.getId() == null || assignedEntity.getId().isNull() || assignedEntity.getId().isEmpty())
			throw new DocumentParseException("No identifiers found for author");

		DatatypeProcessorUtil datatypeProcessorUtil = DatatypeProcessorUtil.getInstance();
		
		// TODO: How to add this like the ECID/EPID identifiers in the current SHR
		// ie. root becomes an attribute and the extension becomes the value
		// Anyways, for now represent in the standard ITI guidance for II data type
		String id = datatypeProcessorUtil.formatIdentifier(assignedEntity.getId().get(0));
		
		Provider res = null;
		
		if (id.equals(datatypeProcessorUtil.emptyIdString())) 
			throw new DocumentParseException("No data specified for author id");
		else 				
			res = Context.getProviderService().getProviderByIdentifier(id);
			
		if (res==null)
			res = this.createProvider(assignedEntity, id);
		
		return res;
	
	}

	/**
	 * Create a provider from the assigned entity
	 * @param assignedEntity The assigned entity to be created
	 * @param id The id to assign to the entity
	 * @return The OpenMRS provider created
	 * @throws DocumentParseException 
	 */
	private Provider createProvider(AssignedEntity assignedEntity,
			String id) throws DocumentParseException {
		
		if(!this.m_autoCreateProviders) // not supposed to be here
			throw new IllegalStateException("Cannot auto-create providers according to current global properties");
		
		// Get person processor
		PersonProcessorUtil personProcessorUtil = PersonProcessorUtil.getInstance();
		OpenmrsMetadataUtil metadataUtil = OpenmrsMetadataUtil.getInstance();
		
		Provider res = new Provider();
		
		res.setIdentifier(id);
		if(assignedEntity.getAssignedPerson() != null )
			res.setPerson(personProcessorUtil.createPerson(assignedEntity.getAssignedPerson()));

		// Address
		if(assignedEntity.getAddr() != null && !assignedEntity.getAddr().isNull())
		{
			if(res.getPerson() == null)
				res.setPerson(new Person());
			for(AD ad : assignedEntity.getAddr())
				if(!ad.isNull())
					res.getPerson().addAddress(DatatypeProcessorUtil.getInstance().parseAD(ad));
		}
				
		// Telecom?
		if(assignedEntity.getTelecom() != null)
			for(TEL tel : assignedEntity.getTelecom())
			{
				if(tel == null || tel.isNull()) continue;
				
				ProviderAttribute telecomAttribute = new ProviderAttribute();
				telecomAttribute.setAttributeType(metadataUtil.getProviderTelecomAttribute());
				telecomAttribute.setValueReferenceInternal(String.format("%s: %s", FormatterUtil.toWireFormat(tel.getUse()), tel.getValue()));
				res.addAttribute(telecomAttribute);
			}
		
		// Organization
		// TODO: This could be assigned to person attribute type of location
		if(assignedEntity.getRepresentedOrganization() != null && assignedEntity.getRepresentedOrganization().getNullFlavor() == null)
		{
			ProviderAttribute organizationAttribute = new ProviderAttribute();
			organizationAttribute.setAttributeType(metadataUtil.getProviderOrganizationAttribute());
			organizationAttribute.setValueReferenceInternal(assignedEntity.getRepresentedOrganization().getName().get(0).toString());
			res.addAttribute(organizationAttribute);
		}
		
		res = Context.getProviderService().saveProvider(res);
		return res;
	}
	
}
