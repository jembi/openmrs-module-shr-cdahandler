package org.openmrs.module.shr.cdahandler.processor.util;

import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.datatypes.generic.CS;
import org.marc.everest.interfaces.IEnumeratedVocabulary;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.GlobalProperty;
import org.openmrs.LocationAttributeType;
import org.openmrs.ProviderAttributeType;
import org.openmrs.VisitAttribute;
import org.openmrs.VisitAttributeType;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.attribute.BaseAttributeType;
import org.openmrs.module.shr.cdahandler.CdaHandlerGlobalPropertyNames;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;

/**
 * Utilities for OpenMRS MetaData creation/lookup
 * @author Justin Fyfe
 *
 */
public final class OpenmrsMetadataUtil {
	
	// Log
	protected final Log log = LogFactory.getLog(this.getClass());

	// singleton instance
	private static OpenmrsMetadataUtil s_instance;
	private static Object s_lockObject = new Object();
	
	// Auto create encounter roles
	private Boolean m_autoCreateMetadata = true;
	
	
	/**
	 * Private ctor
	 */
	private OpenmrsMetadataUtil()
	{
		
	}
	
	/**
	 * Initialize instance
	 */
	private void initializeInstance()
	{
		// Auto create encounter roles
		String propertyValue = Context.getAdministrationService().getGlobalProperty(CdaHandlerGlobalPropertyNames.AUTOCREATE_METADATA);
		if(propertyValue != null && !propertyValue.isEmpty())
			this.m_autoCreateMetadata = Boolean.parseBoolean(propertyValue);
		else
			Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(CdaHandlerGlobalPropertyNames.AUTOCREATE_METADATA, this.m_autoCreateMetadata.toString()));
		
	}
	
	/**
	 * Get the singleton instance
	 */
	public static OpenmrsMetadataUtil getInstance()
	{
		if(s_instance == null)
		{
			synchronized (s_lockObject) {
				if(s_instance == null) // Another thread might have created while we were waiting for a lock
				{
					s_instance = new OpenmrsMetadataUtil();
					s_instance.initializeInstance();
				}
			}
		}
		return s_instance;
	}

	/**
	 * Get an internationalized string
	 * @param name The name of the string
	 * @return
	 */
	public String getInternationalizedString(String name)
	{
		
		ResourceBundle bundle = ResourceBundle.getBundle("messages");
		return bundle.getString(String.format("shr-cdahandler.%s", name));
	}
	
	/**
	 * Get an EncounterRole from the RoleCode
	 * @param cs The ActParticipation value on the role
	 * @return The OpenMRS EncounterRole
	 * @throws DocumentParseException 
	 */
	@SuppressWarnings("deprecation")
	public EncounterRole getEncounterRole(CS<? extends IEnumeratedVocabulary> cs) throws DocumentParseException {
		
		//Delicious encounter rolls just like Suranga used to make
		//https://github.com/jembi/rhea-shr-adapter/blob/3e25fa0cd276327ca83127283213b6658af9e9ef/api/src/main/java/org/openmrs/module/rheashradapter/util/RHEA_ORU_R01Handler.java#L422
		
		// Get the UUID of this vocabulary entry
		// TODO: There has to be a better way to do this currently this will create a cda.encounterrol.ActParticipation.AUT|LA|etc.
		String codeKey = DatatypeProcessorUtil.getInstance().formatSimpleCode(cs);
		EncounterRole encounterRole = null;
		for(EncounterRole role : Context.getEncounterService().getAllEncounterRoles(false))
			if(role.getDescription().equals(codeKey))
				encounterRole = role;
				
		if(encounterRole == null && this.m_autoCreateMetadata) {
			encounterRole = new EncounterRole();
			encounterRole.setName(cs.getCode().getCode());
			encounterRole.setDescription(codeKey);
			encounterRole = Context.getEncounterService().saveEncounterRole(encounterRole);
		} 
		else if(encounterRole == null && !this.m_autoCreateMetadata)
			throw new DocumentParseException(String.format("Encounter role %s is unknown", cs.getCode()));
		
		return encounterRole;
	}

	/**
	 * Get the encounter type
	 * @param code The code representing the type (class) of section
	 * @return The encounter type
	 * @throws DocumentParseException 
	 */
	public EncounterType getEncounterType(CE<String> code) throws DocumentParseException {

		// Get the codekey and code display
		String codeKey = DatatypeProcessorUtil.getInstance().formatCodeValue(code);
		String display = code.getDisplayName();
		if(display == null || display.isEmpty())
			display = code.getCode();
		
		EncounterType encounterType = null;
		for(EncounterType type : Context.getEncounterService().getAllEncounterTypes())
			if(type.getDescription().equals(codeKey))
				encounterType = type;
				
		if(encounterType == null && this.m_autoCreateMetadata) {
			encounterType = new EncounterType();
			encounterType.setName(display);
			encounterType.setDescription(codeKey);
			encounterType = Context.getEncounterService().saveEncounterType(encounterType);
		} 
		else if(encounterType == null && !this.m_autoCreateMetadata)
			throw new DocumentParseException(String.format("Encounter type %s is unknown", code.getCode()));
		
		return encounterType;
	}

	/**
	 * Get the location's external id attribute type
	 * @return The attribute type representing location external id
	 * @throws DocumentParseException 
	 */
	public LocationAttributeType getLocationExternalIdAttributeType() throws DocumentParseException
	{
		LocationAttributeType res = this.getAttributeType(this.getInternationalizedString("externalId"), LocationAttributeType.class);
		if(res == null)
			res = this.createAttributeType(
				this.getInternationalizedString("externalId"), 
				"org.openmrs.customdatatype.datatype.FreeTextDatatype",
				this.getInternationalizedString("externalId.description"),
				0, 1,
				LocationAttributeType.class);
		return res;
	}
	
	/**
	 * Get the telecommunications address location attribute type
	 * @return The attribute type representing location's telecommunications address
	 * @throws DocumentParseException 
	 */
	public LocationAttributeType getLocationTelecomAttribute() throws DocumentParseException
	{
		LocationAttributeType res = this.getAttributeType(this.getInternationalizedString("telecom"), LocationAttributeType.class);
		if(res == null)
			res = this.createAttributeType(
				this.getInternationalizedString("telecom"), 
				"org.openmrs.customdatatype.datatype.FreeTextDatatype",
				this.getInternationalizedString("telecom.description"),
				0, 5,
				LocationAttributeType.class);
		return res;
	}
	
	/**
	 * Get the telecommunications address provider type
	 * @return The attribute type representing provider's telecommunications address
	 * @throws DocumentParseException 
	 */
	public ProviderAttributeType getProviderTelecomAttribute() throws DocumentParseException
	{
		ProviderAttributeType res = this.getAttributeType(this.getInternationalizedString("telecom"), ProviderAttributeType.class);
		if(res == null)
			res = this.createAttributeType(
				this.getInternationalizedString("telecom"), 
				"org.openmrs.customdatatype.datatype.FreeTextDatatype",
				this.getInternationalizedString("telecom.description"),
				0, 5,
				ProviderAttributeType.class);
		return res;
	}
	
	/**
	 * Get the telecommunications address provider type
	 * @return The attribute type representing provider's telecommunications address
	 * @throws DocumentParseException 
	 */
	public ProviderAttributeType getProviderOrganizationAttribute() throws DocumentParseException
	{
		ProviderAttributeType res = this.getAttributeType(this.getInternationalizedString("organization"), ProviderAttributeType.class);
		if(res == null)
			res = this.createAttributeType(
				this.getInternationalizedString("organization"), 
				"org.openmrs.customdatatype.datatype.FreeTextDatatype",
				this.getInternationalizedString("organization.description"),
				0, 1,
				ProviderAttributeType.class);
		return res;
	}
	
	/**
	 * Get the external id (provenance, origin information) of visit information attribute type
	 * @return The attribute type representing visit external id
	 * @throws DocumentParseException 
	 */
	public VisitAttributeType getVisitExternalIdAttributeType() throws DocumentParseException
	{
		VisitAttributeType res = this.getAttributeType(this.getInternationalizedString("externalId"), VisitAttributeType.class);
		if(res == null)
			res = this.createAttributeType(
				this.getInternationalizedString("externalId"), 
				"org.openmrs.customdatatype.datatype.FreeTextDatatype",
				this.getInternationalizedString("externalId.description"),
				0, 1,
				VisitAttributeType.class);
		return res;
	}

	/**
	 * Get the original document
	 * @return The attribute type representing visit original copy
	 * @throws DocumentParseException 
	 */
	public VisitAttributeType getVisitOriginalCopyAttributeType() throws DocumentParseException
	{
		VisitAttributeType res = this.getAttributeType(this.getInternationalizedString("original"), VisitAttributeType.class);
		if(res == null)
			res = this.createAttributeType(
				this.getInternationalizedString("original"), 
				"org.openmrs.customdatatype.datatype.LongFreeTextDatatype",
				this.getInternationalizedString("original.description"),
				0, 1,
				VisitAttributeType.class);
		return res;
	}
	
	/**
	 * Get the confidentiality code
	 * @return The attribute type representing the confidentiality code
	 * @throws DocumentParseException 
	 */
	public VisitAttributeType getVisitConfidentialityCodeAttributeType() throws DocumentParseException
	{
		VisitAttributeType res = this.getAttributeType(this.getInternationalizedString("confidentiality"), VisitAttributeType.class);
		if(res == null)
			res = this.createAttributeType(
				this.getInternationalizedString("confidentiality"), 
				"org.openmrs.customdatatype.datatype.FreeTextDatatype",
				this.getInternationalizedString("confidentiality.description"),
				0, 1, 
				VisitAttributeType.class);
		return res;
		
	}

	/**
	 * Get an attribute type 
	 * 
	 * @param name The name of the attribute to search for
	 * @param attributeType The type of attribute to search for
	 * @return The located to created attribute type
	 */
	@SuppressWarnings("unchecked")
    public <T extends BaseAttributeType<?>> T getAttributeType(String name, Class<T> attributeType) 
	{
		T res = null;
		
		// Get a list of appropriate types to scan
		List<? extends BaseAttributeType<?>> allTypes = null;
		if(VisitAttributeType.class.equals(attributeType))
			allTypes = Context.getVisitService().getAllVisitAttributeTypes();
		else if(LocationAttributeType.class.equals(attributeType))
			allTypes = Context.getLocationService().getAllLocationAttributeTypes();
		else
			allTypes = Context.getProviderService().getAllProviderAttributeTypes();
		
		// Scan the attribute types
		for(BaseAttributeType<?> attrType : allTypes)
			if(attrType.getName().equals(name))
				res = (T)attrType;
		
				
		return res;
	}
	
	/**
	 * Creates a base attribute type
	 * 
	 * @param name The name of the visit attribute type
	 * @param datatType The type of data in the visit attribute
	 * @param description The description of the attribute type
	 */
	@SuppressWarnings("unchecked")
    public <T extends BaseAttributeType<?>> T createAttributeType(String name, String dataType, String description, int minOccurs, int maxOccurs, Class<T> attributeType)
	{
		if(!this.m_autoCreateMetadata)
			throw new IllegalStateException("Cannot create meta-data");
		
		try
		{

			T res = attributeType.newInstance();
			res.setName(name);
			res.setDatatypeClassname(dataType);
			res.setDescription(description);
			res.setMinOccurs(minOccurs);
			res.setMaxOccurs(maxOccurs);
	
			if(dataType.equals("org.openmrs.customdatatype.datatype.LongFreeTextDatatype"))
				res.setPreferredHandlerClassname("org.openmrs.web.attribute.handler.LongFreeTextFileUploadHandler");
			
			if(VisitAttributeType.class.equals(attributeType))
				res = (T)Context.getVisitService().saveVisitAttributeType((VisitAttributeType)res);
			else if(LocationAttributeType.class.equals(attributeType))
				res = (T)Context.getLocationService().saveLocationAttributeType((LocationAttributeType)res);
			else
				res = (T)Context.getProviderService().saveProviderAttributeType((ProviderAttributeType)res);
				
			return res;
		}
		catch(Exception e)
		{
			log.error("Could not create attribute type", e);
			return null;
		}
	}

	/**
	 * Gets the visit type specified, if none exists, creates it
	 * @param visitTypeName The name of the visit type to get or create
	 * @return The visit type
	 * @throws DocumentParseException 
	 */
	public VisitType getVisitType(String visitTypeName) throws DocumentParseException {
		VisitType visitType = null;
		for(VisitType type : Context.getVisitService().getAllVisitTypes())
			if(type.getName().equals(visitTypeName))
				visitType = type;
		
		if(visitType == null && this.m_autoCreateMetadata)
		{
			visitType = new VisitType();
			visitType.setName(visitTypeName);
			visitType.setDescription(this.getInternationalizedString("autocreated"));
			visitType = Context.getVisitService().saveVisitType(visitType);
		}
		else if(visitType == null && !this.m_autoCreateMetadata)
			throw new DocumentParseException(String.format("Cannot find specified visit type %s", visitTypeName));
		return visitType;
	}

	
}
