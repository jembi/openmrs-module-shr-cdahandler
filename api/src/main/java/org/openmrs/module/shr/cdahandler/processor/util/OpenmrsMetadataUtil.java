package org.openmrs.module.shr.cdahandler.processor.util;

import java.util.ResourceBundle;

import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.datatypes.generic.CS;
import org.marc.everest.interfaces.IEnumeratedVocabulary;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.GlobalProperty;
import org.openmrs.VisitAttribute;
import org.openmrs.VisitAttributeType;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerGlobalPropertyNames;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;

/**
 * Utilities for OpenMRS MetaData creation/lookup
 * @author Justin Fyfe
 *
 */
public class OpenmrsMetadataUtil {

	// singleton instance
	private static OpenmrsMetadataUtil s_instance;
	private static Object s_lockObject = new Object();
	
	// Auto create encounter roles
	private Boolean m_autoCreateMetadata = true;
	
	// Visit provenance type attribute
	private VisitAttributeType m_visitProvenanceType = null;
	// Original copy
	private VisitAttributeType m_visitOriginalCopyType = null;
	// Confidentiality code attribute
	private VisitAttributeType m_visitConfidentiality = null;
	
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
			if(role.getName().equals(codeKey))
				encounterRole = role;
				
		if(encounterRole == null && this.m_autoCreateMetadata) {
			encounterRole = new EncounterRole();
			encounterRole.setName(codeKey);
			encounterRole.setDescription(this.getInternationalizedString("autocreated"));
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

		String codeKey = DatatypeProcessorUtil.getInstance().formatCodeValue(code);
		EncounterType encounterType = null;
		for(EncounterType type : Context.getEncounterService().getAllEncounterTypes(false))
			if(type.getName().equals(codeKey))
				encounterType = type;
				
		if(encounterType == null && this.m_autoCreateMetadata) {
			encounterType = new EncounterType();
			encounterType.setName(codeKey);
			encounterType.setDescription(this.getInternationalizedString("autocreated"));
			encounterType = Context.getEncounterService().saveEncounterType(encounterType);
		} 
		else if(encounterType == null && !this.m_autoCreateMetadata)
			throw new DocumentParseException(String.format("Encounter type %s is unknown", code.getCode()));
		
		return encounterType;
	}

	/**
	 * Get the provenance of visit information attribute type
	 * @return The attribute type representing visit provenance
	 * @throws DocumentParseException 
	 */
	public VisitAttributeType getVisitProvenanceStatementAttributeType() throws DocumentParseException
	{
		// TODO: This stores what document created the data. Would it make sense to be a pointer rather than a clob?
		if(this.m_visitProvenanceType == null)
			this.m_visitProvenanceType = getVisitAttributeType(this.getInternationalizedString("provenance"));
		if(this.m_visitProvenanceType == null && this.m_autoCreateMetadata)
		{
			this.m_visitProvenanceType = new VisitAttributeType();
			this.m_visitProvenanceType.setName(this.getInternationalizedString("provenance"));
			this.m_visitProvenanceType.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
			this.m_visitProvenanceType.setDescription(this.getInternationalizedString("provenance.description"));
			this.m_visitProvenanceType.setMinOccurs(0);
			this.m_visitProvenanceType.setMaxOccurs(1);
			this.m_visitProvenanceType = Context.getVisitService().saveVisitAttributeType(this.m_visitProvenanceType);
		}
		else if(this.m_visitProvenanceType == null && !this.m_autoCreateMetadata)
			throw new DocumentParseException("Cannot create necessary meta-data to store visit provenance");
		return this.m_visitProvenanceType;
	}

	/**
	 * Get the original document
	 * @return The attribute type representing visit original copy
	 * @throws DocumentParseException 
	 */
	public VisitAttributeType getVisitOriginalCopyAttributeType() throws DocumentParseException
	{
		// TODO: This stores what document created the data. Would it make sense to be a pointer rather than a clob?
		if(this.m_visitOriginalCopyType == null)
			this.m_visitOriginalCopyType = getVisitAttributeType(this.getInternationalizedString("original"));
		if(this.m_visitOriginalCopyType == null && this.m_autoCreateMetadata)
		{
			this.m_visitOriginalCopyType = new VisitAttributeType();
			this.m_visitOriginalCopyType.setName(this.getInternationalizedString("original"));
			this.m_visitOriginalCopyType.setDatatypeClassname("org.openmrs.customdatatype.datatype.LongFreeTextDatatype");
			this.m_visitOriginalCopyType.setDescription(this.getInternationalizedString("original.description"));
			this.m_visitOriginalCopyType.setMinOccurs(0);
			this.m_visitOriginalCopyType.setMaxOccurs(1);
			this.m_visitOriginalCopyType.setPreferredHandlerClassname("org.openmrs.web.attribute.handler.LongFreeTextFileUploadHandler");
			this.m_visitOriginalCopyType = Context.getVisitService().saveVisitAttributeType(this.m_visitConfidentiality);
		}
		else if(this.m_visitOriginalCopyType == null && !this.m_autoCreateMetadata)
			throw new DocumentParseException("Cannot create necessary meta-data to store original copy");
		return this.m_visitOriginalCopyType;
	}
	

	/**
	 * Get the confidentiality code
	 * @return The attribute type representing the confidentiality code
	 * @throws DocumentParseException 
	 */
	public VisitAttributeType getVisitConfidentialityCodeAttributeType() throws DocumentParseException
	{
		// TODO: This stores what document created the data. Would it make sense to be a pointer rather than a clob?
		if(this.m_visitConfidentiality == null)
			this.m_visitConfidentiality = getVisitAttributeType(this.getInternationalizedString("confidentiality"));
		if(this.m_visitConfidentiality == null && this.m_autoCreateMetadata)
		{
			this.m_visitConfidentiality = new VisitAttributeType();
			this.m_visitConfidentiality.setName(this.getInternationalizedString("confidentiality"));
			this.m_visitConfidentiality.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
			this.m_visitConfidentiality.setDescription(this.getInternationalizedString("confidentiality.description"));
			this.m_visitConfidentiality.setMinOccurs(0);
			this.m_visitConfidentiality.setMaxOccurs(3);
			this.m_visitConfidentiality = Context.getVisitService().saveVisitAttributeType(this.m_visitConfidentiality);
		}
		else if(this.m_visitConfidentiality == null && !this.m_autoCreateMetadata)
			throw new DocumentParseException("Cannot create necessary meta-data to store visit confidentiality");

		return this.m_visitConfidentiality;
	}


	/**
	 * Gets the visit attribute type specified or creates it if it doesn't exist
	 * @return
	 */
	public VisitAttributeType getVisitAttributeType(String attributeTypeName) {
		
		VisitAttributeType attributeType = null;
		for(VisitAttributeType attrType : Context.getVisitService().getAllVisitAttributeTypes())
			if(attrType.getName().equals(attributeTypeName))
				attributeType = attrType;
		return attributeType;
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
