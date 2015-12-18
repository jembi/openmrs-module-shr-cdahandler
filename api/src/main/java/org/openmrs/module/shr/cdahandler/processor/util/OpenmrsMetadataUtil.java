package org.openmrs.module.shr.cdahandler.processor.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.datatypes.generic.CD;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.datatypes.generic.CS;
import org.marc.everest.interfaces.IEnumeratedVocabulary;
import org.openmrs.CareSetting;
import org.openmrs.CareSetting.CareSettingType;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.LocationAttributeType;
import org.openmrs.OrderType;
import org.openmrs.PersonAttributeType;
import org.openmrs.ProviderAttributeType;
import org.openmrs.RelationshipType;
import org.openmrs.VisitAttributeType;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.attribute.BaseAttributeType;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.configuration.CdaHandlerConfiguration;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.order.ObservationOrder;
import org.openmrs.module.shr.cdahandler.order.ProcedureOrder;
import org.openmrs.util.OpenmrsConstants;

/**
 * Utilities for OpenMRS MetaData creation/lookup
 * @author Justin Fyfe
 *
 */
public class OpenmrsMetadataUtil {
	
	/**
	 * Get the singleton instance
	 */
	public static OpenmrsMetadataUtil getInstance()
	{
		if(s_instance == null)
		{
			synchronized (s_lockObject) {
				if(s_instance == null) // Another thread might have created while we were waiting for a lock
					s_instance = new OpenmrsMetadataUtil();
			}
		}
		return s_instance;
	}

	// Log
	protected final Log log = LogFactory.getLog(this.getClass());
	// singleton instance
	private static OpenmrsMetadataUtil s_instance;
	
	private static Object s_lockObject = new Object();
	
	// Auto create encounter roles
	private final CdaHandlerConfiguration m_configuration = CdaHandlerConfiguration.getInstance();
	
	/**
	 * Private ctor
	 */
	protected OpenmrsMetadataUtil()
	{
		
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
		if(!this.m_configuration.getAutoCreateMetaData())
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
	 * Creates a person attribute 
	 */
	private PersonAttributeType createPersonAttributeType(String attributeName, String dataType, String description) {
		if(!this.m_configuration.getAutoCreateMetaData())
			throw new IllegalStateException("Cannot create attribute type");
		PersonAttributeType res = new PersonAttributeType();
		res.setName(attributeName);
		res.setFormat(dataType);
		res.setDescription(description);
		res.setForeignKey(0);
		res = Context.getPersonService().savePersonAttributeType(res);
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
	 * Get an EncounterRole from the RoleCode
	 * @param cs The ActParticipation value on the role
	 * @return The OpenMRS EncounterRole
	 * @throws DocumentImportException 
	 */
	@SuppressWarnings("deprecation")
	public EncounterRole getOrCreateEncounterRole(CS<? extends IEnumeratedVocabulary> cs) throws DocumentImportException {
		
		//Delicious encounter rolls just like Suranga used to make
		//https://github.com/jembi/rhea-shr-adapter/blob/3e25fa0cd276327ca83127283213b6658af9e9ef/api/src/main/java/org/openmrs/module/rheashradapter/util/RHEA_ORU_R01Handler.java#L422
		
		// Get the UUID of this vocabulary entry
		// TODO: There has to be a better way to do this currently this will create a cda.encounterrol.ActParticipation.AUT|LA|etc.
		String codeKey = DatatypeProcessorUtil.getInstance().formatSimpleCode(cs);
		EncounterRole encounterRole = null;
		for(EncounterRole role : Context.getEncounterService().getAllEncounterRoles(false))
			if(role.getDescription().equals(codeKey))
				encounterRole = role;

		if(encounterRole == null && this.m_configuration.getAutoCreateMetaData()) {
			encounterRole = new EncounterRole();
			encounterRole.setName(cs.getCode().getCode());
			encounterRole.setDescription(codeKey);
			encounterRole = Context.getEncounterService().saveEncounterRole(encounterRole);
		} 
		else if(encounterRole == null && !this.m_configuration.getAutoCreateMetaData())
			throw new DocumentImportException(String.format("Encounter role %s is unknown", cs.getCode()));
		
		return encounterRole;
	}
	
	/**
	 * Get the encounter type
	 * @param code The code representing the type (class) of section
	 * @return The encounter type
	 * @throws DocumentImportException 
	 */
	public EncounterType getOrCreateEncounterType(CE<String> code) throws DocumentImportException {

		// Get the codekey and code display
		String codeKey = DatatypeProcessorUtil.getInstance().formatCodeValue(code);
		String display = code.getDisplayName();
		if(display == null || display.isEmpty())
			display = code.getCode();
		
		EncounterType encounterType = null;
		for(EncounterType type : Context.getEncounterService().getAllEncounterTypes())
			if(type.getDescription().equals(codeKey))
				encounterType = type;
				
		if(encounterType == null && this.m_configuration.getAutoCreateMetaData()) {
			encounterType = new EncounterType();
			encounterType.setName(display);
			encounterType.setDescription(codeKey);
			encounterType = Context.getEncounterService().saveEncounterType(encounterType);
		} 
		else if(encounterType == null && !this.m_configuration.getAutoCreateMetaData())
			throw new DocumentImportException(String.format("Encounter type %s is unknown", code.getCode()));
		
		return encounterType;
	}
	
	/**
	 * Get the location's external id attribute type
	 * @return The attribute type representing location external id
	 * @throws DocumentImportException 
	 */
	public LocationAttributeType getOrCreateLocationExternalIdAttributeType() throws DocumentImportException
	{
		LocationAttributeType res = this.getAttributeType(CdaHandlerConstants.ATTRIBUTE_NAME_EXTERNAL_ID, LocationAttributeType.class);
		if(res == null)
			res = this.createAttributeType(
				CdaHandlerConstants.ATTRIBUTE_NAME_EXTERNAL_ID, 
				"org.openmrs.customdatatype.datatype.FreeTextDatatype",
				"External Identifiers",
				0, 1,
				LocationAttributeType.class);
		return res;
	}
	
	/**
	 * Get the telecommunications address location attribute type
	 * @return The attribute type representing location's telecommunications address
	 * @throws DocumentImportException 
	 */
	public LocationAttributeType getOrCreateLocationTelecomAttribute() throws DocumentImportException
	{
		LocationAttributeType res = this.getAttributeType(CdaHandlerConstants.ATTRIBUTE_NAME_TELECOM, LocationAttributeType.class);
		if(res == null)
			res = this.createAttributeType(
				CdaHandlerConstants.ATTRIBUTE_NAME_TELECOM, 
				"org.openmrs.customdatatype.datatype.FreeTextDatatype",
				"Telcom Address",
				0, 5,
				LocationAttributeType.class);
		return res;
	}

	
	/**
	 * Get the person marital status attribute type
	 * @throws DocumentImportException 
	 */
	public PersonAttributeType getOrCreatePersonMaritalStatusAttribute() throws DocumentImportException
	{
		
		PersonAttributeType res = this.getPersonAttributeType(CdaHandlerConstants.ATTRIBUTE_NAME_CIVIL_STATUS);
		if(res == null)
		{
			res = this.createPersonAttributeType(
				CdaHandlerConstants.ATTRIBUTE_NAME_CIVIL_STATUS, 
				"org.openmrs.Concept",
				"Civil Status");
			Concept civilStatusConcept = Context.getConceptService().getConcept(OpenmrsConstants.CIVIL_STATUS_CONCEPT_ID);
			if(civilStatusConcept == null)
				civilStatusConcept = OpenmrsConceptUtil.getInstance().getOrCreateRMIMConcept(CdaHandlerConstants.RMIM_CONCEPT_UUID_MARITAL_STATUS, new CD<String>());
			res.setForeignKey(civilStatusConcept.getId());
			res = Context.getPersonService().savePersonAttributeType(res);
		}
		return res;
		
	}

	/**
	 * Get the telecommunications address provider type
	 * @return The attribute type representing provider's telecommunications address
	 * @throws DocumentImportException 
	 */
	public PersonAttributeType getOrCreatePersonOrganizationAttribute() throws DocumentImportException
	{
		PersonAttributeType res = this.getPersonAttributeType(CdaHandlerConstants.ATTRIBUTE_NAME_ORGANIZATION);
		if(res == null)
			res = this.createPersonAttributeType(
				CdaHandlerConstants.ATTRIBUTE_NAME_ORGANIZATION, 
				"org.openmrs.Location",
				"Related Organization");
		return res;
	}

	/**
	 * Get the telecommunications address provider type
	 * @return The attribute type representing provider's telecommunications address
	 * @throws DocumentImportException 
	 */
	public PersonAttributeType getOrCreatePersonTelecomAttribute() throws DocumentImportException
	{
		PersonAttributeType res = this.getPersonAttributeType(CdaHandlerConstants.ATTRIBUTE_NAME_TELECOM);
		if(res == null)
			res = this.createPersonAttributeType(
				CdaHandlerConstants.ATTRIBUTE_NAME_TELECOM, 
				"java.lang.String",
				"Telecom Address");
		return res;
	}

	/**
	 * Get or create relationship type
	 * @throws DocumentImportException 
	 */
	public RelationshipType getOrCreateRelationshipType(CE<String> relationship) throws DocumentImportException {
		
		// TODO: Find a better way of mapping this code as there are a few code ssytems that have similar codes
		String relationshipTypeName = DatatypeProcessorUtil.getInstance().formatCodeValue(relationship);
		RelationshipType visitType = null;
		for(RelationshipType type : Context.getPersonService().getAllRelationshipTypes())
			if(type.getDescription() != null && type.getDescription().equals(relationshipTypeName))
				visitType = type;
		
		if(visitType == null && this.m_configuration.getAutoCreateMetaData())
		{
			visitType = new RelationshipType();
			visitType.setName(relationshipTypeName);
			visitType.setDescription(relationshipTypeName);
			visitType.setaIsToB(relationship.getCode());
			visitType.setbIsToA(relationship.getCode());
			visitType = Context.getPersonService().saveRelationshipType(visitType);
		}
		else if(visitType == null && !this.m_configuration.getAutoCreateMetaData())
			throw new DocumentImportException(String.format("Cannot find specified relationship type %s", relationship));
		return visitType;
    }
	
	/**
	 * Get the confidentiality code
	 * @return The attribute type representing the confidentiality code
	 * @throws DocumentImportException 
	 */
	public VisitAttributeType getOrCreateVisitConfidentialityCodeAttributeType() throws DocumentImportException
	{
		VisitAttributeType res = this.getAttributeType(CdaHandlerConstants.ATTRIBUTE_NAME_CONFIDENTIALITY, VisitAttributeType.class);
		if(res == null)
			res = this.createAttributeType(
				CdaHandlerConstants.ATTRIBUTE_NAME_CONFIDENTIALITY, 
				"org.openmrs.customdatatype.datatype.FreeTextDatatype",
				"Confidentiality Code",
				0, 1, 
				VisitAttributeType.class);
		return res;
		
	}

	/**
	 * Get the external id (provenance, origin information) of visit information attribute type
	 * @return The attribute type representing visit external id
	 * @throws DocumentImportException 
	 */
	public VisitAttributeType getOrCreateVisitExternalIdAttributeType() throws DocumentImportException
	{
		VisitAttributeType res = this.getAttributeType(CdaHandlerConstants.ATTRIBUTE_NAME_EXTERNAL_ID, VisitAttributeType.class);
		if(res == null)
			res = this.createAttributeType(
				CdaHandlerConstants.ATTRIBUTE_NAME_EXTERNAL_ID, 
				"org.openmrs.customdatatype.datatype.FreeTextDatatype",
				"External Identifier",
				0, 1,
				VisitAttributeType.class);
		return res;
	}
	
	/**
	 * Get the original document
	 * @return The attribute type representing visit original copy
	 * @throws DocumentImportException 
	 */
	public VisitAttributeType getOrCreateVisitOriginalCopyAttributeType() throws DocumentImportException
	{
		VisitAttributeType res = this.getAttributeType(CdaHandlerConstants.ATTRIBUTE_NAME_ORIGINAL_COPY, VisitAttributeType.class);
		if(res == null)
			res = this.createAttributeType(
				CdaHandlerConstants.ATTRIBUTE_NAME_ORIGINAL_COPY, 
				"org.openmrs.customdatatype.datatype.LongFreeTextDatatype",
				"Original Copy",
				0, 1,
				VisitAttributeType.class);
		return res;
	}

	/**
	 * Get the person attribute type
	 */
	private PersonAttributeType getPersonAttributeType(String name) {
	    return Context.getPersonService().getPersonAttributeTypeByName(name);
    }

	/**
	 * Gets the visit type specified, if none exists, creates it
	 * @param visitTypeName The name of the visit type to get or create
	 * @return The visit type
	 * @throws DocumentImportException 
	 */
	public VisitType getVisitType(String visitTypeName) throws DocumentImportException {
		VisitType visitType = null;
		for(VisitType type : Context.getVisitService().getAllVisitTypes())
			if(type.getName().equals(visitTypeName))
				visitType = type;
		
		if(visitType == null && this.m_configuration.getAutoCreateMetaData())
		{
			visitType = new VisitType();
			visitType.setName(visitTypeName);
			visitType.setDescription("Auto Created");
			visitType = Context.getVisitService().saveVisitType(visitType);
		}
		else if(visitType == null && !this.m_configuration.getAutoCreateMetaData())
			throw new DocumentImportException(String.format("Cannot find specified visit type %s", visitTypeName));
		return visitType;
	}

	/**
	 * Inpatient care setting
	 * @return
	 */
	public CareSetting getOrCreateInpatientCareSetting() {
		CareSetting setting = Context.getOrderService().getCareSettingByName("INPATIENT");
		if(setting == null && this.m_configuration.getAutoCreateMetaData())
			setting = new CareSetting("INPATIENT", "Auto Created", CareSettingType.INPATIENT);
		return setting;
    }

	/**
	 * Get or create the order type for procedures
	 * @throws DocumentImportException 
	 */
	public OrderType getOrCreateProcedureOrderType() {
		OrderType res = Context.getOrderService().getOrderTypeByUuid(CdaHandlerConstants.UUID_ORDER_TYPE_PROCEDURE);
		if(res == null && this.m_configuration.getAutoCreateMetaData())
		{
			res = new OrderType("Procedure Order", "Procedure Order", ProcedureOrder.class.getName());
			res.setUuid(CdaHandlerConstants.UUID_ORDER_TYPE_PROCEDURE);
			res.addConceptClass(Context.getConceptService().getConceptClassByUuid(ConceptClass.PROCEDURE_UUID));
			res = Context.getOrderService().saveOrderType(res);
		}
		return res;
    }

	/**
	 * Get or create observation order type
	 */
	public OrderType getOrCreateObservationOrderType() {
		OrderType res = Context.getOrderService().getOrderTypeByUuid(CdaHandlerConstants.UUID_ORDER_TYPE_OBSERVATION);
		if(res == null && this.m_configuration.getAutoCreateMetaData())
		{
			res = new OrderType("Observation Order", "Observation Order", ObservationOrder.class.getName());
			res.setUuid(CdaHandlerConstants.UUID_ORDER_TYPE_OBSERVATION);
			res.addConceptClass(Context.getConceptService().getConceptClassByUuid(ConceptClass.TEST_UUID));
			res = Context.getOrderService().saveOrderType(res);
		}
		return res;
    }

	
}
