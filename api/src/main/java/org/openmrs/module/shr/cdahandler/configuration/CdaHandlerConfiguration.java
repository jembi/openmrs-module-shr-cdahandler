package org.openmrs.module.shr.cdahandler.configuration;

import org.marc.everest.datatypes.PQ;
import org.marc.everest.formatters.FormatterUtil;
import org.marc.everest.util.SimpleSiUnitConverter;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;

/**
 * Consolidated configuration class
 */
public final class CdaHandlerConfiguration {
	
	/**
     * Creates or gets the instance of the configuration
     */
    public static final CdaHandlerConfiguration getInstance()
    {
    	if(s_instance == null)
    		synchronized (s_lockObject) {
    			if(s_instance == null)
    			{
    				s_instance = new CdaHandlerConfiguration();
    				s_instance.initialize();
    			}
            }
    	return s_instance;
    }
	// Automatically create concepts
    public static final String PROP_AUTOCREATE_CONCEPTS = "shr-cdahandler.autocreate.concepts";
	// Automatically create locations
    public static final String PROP_AUTOCREATE_LOCATIONS = "shr-cdahandler.autocreate.locations";
	// Property name controlling auto-creation of encounter roles
    public static final String PROP_AUTOCREATE_METADATA = "shr-cdahandler.autocreate.metaData";
	// Property name controlling the auto-creation of patient id types
    public static final String PROP_AUTOCREATE_PATIENTIDTYPE = "shr-cdahandler.autocreate.patients.idtype";
	// Property name controlling the auto-creation of patients
    public static final String PROP_AUTOCREATE_PATIENTS = "shr-cdahandler.autocreate.patients";
	// Property name controlling the auto-creation of persons
    public static final String PROP_AUTOCREATE_PERSONS = "shr-cdahandler.autocreate.persons";
	// Property name controlling the auto-creation of users
    public static final String PROP_AUTOCREATE_USERS = "shr-cdahandler.autocreate.users";
	// Property name controlling auto-creation of entities
    public static final String PROP_AUTOCREATE_PROVIDERS = "shr-cdahandler.autocreate.providers";
    // Property controlling the format of complex identifiers
    public static final String PROP_ID_FORMAT = "shr-cdahandler.id.format";
    // Strict validation
    public static final String PROP_VALIDATE_STRUCTURE = "shr-cdahandler.validation.cda";
    // Strict validation
    public static final String PROP_VALIDATE_CONCEPT_STRUCTURE = "shr-cdahandler.validation.conceptStructure";
    // The root of EPIDs
    public static final String PROP_EPID_ROOT = "shr-cdahandler.id.epidRoot";
    // The root of ECIDs
	private static final String PROP_ECID_ROOT = "shr-cdahandler.id.ecidRoot";
    // The root of Objects in the SHR
	private static final String PROP_SHR_ROOT = "shr-cdahandler.id.root";
	
    // Update existing
    public static final String PROP_UPDATE_EXISTING = "shr-cdahandler.updateExisting";
    private Boolean m_autoCreateProviders = true;
    private Boolean m_autoCreateLocations = true;
    private Boolean m_autoCreateConcepts = true;
    private Boolean m_autoCreateMetadata = true;
    private Boolean m_autoCreatePatients = true;
    private Boolean m_autoCreatePatientIdType = true;
    private Boolean m_autoCreatePersons = true;
    private Boolean m_validateInstances = true;
    private Boolean m_validateConceptStructure = true;
    private Boolean m_updateExisting = false;
    private String m_epidRoot = "";
    private String m_ecidRoot = "";
    private String m_shrRoot = "1.2.3.4.5";
    private Boolean m_autoCreateUsers = true;
    
    private String m_idFormat = "%2$s^^^&%1$s&ISO";
    // Singleton instance
    private static CdaHandlerConfiguration s_instance = null;
    
    private static Object s_lockObject = new Object();
    
    /**
     * Private ctor
     */
    private CdaHandlerConfiguration()
    {
    	
    }

    /**
	 * Get the shr-cdahandler.autocreate.concepts value
	 */
	public boolean getAutoCreateConcepts()
	{
		return this.m_autoCreateConcepts;
	}
    
    /**
	 * Get the shr-cdahandler.autocreate.locations value
	 */
	public boolean getAutoCreateLocations()
	{
		return this.m_autoCreateLocations;
	}

	/**
	 * Get the shr-cdahandler.autocreate.metadata value
	 * @return
	 */
	public boolean getAutoCreateMetaData() {
		return this.m_autoCreateMetadata;
    }

	/**
	 * Get the shr-cdahandler.autocreate.patientidtype value
	 */
	public boolean getAutoCreatePatientIdType()
	{
		return this.m_autoCreatePatientIdType;
	}
	
	/**
	 * Get the shr-cdahandler.autocreate.patients value
	 */
	public boolean getAutoCreatePatients() {
		return this.m_autoCreatePatients;
    }
	
	/**
	 * Get the shr-cdahandler.autocreate.persons value
	 * @return
	 */
	public boolean getAutoCreatePersons() {
		return this.m_autoCreatePersons;
    }

	/**
	 * Get the shr-cdahandler.autocreate.providers value
	 */
	public boolean getAutoCreateProviders() {
		return this.m_autoCreateProviders;
    }

	/**
	 * Get the shr-cdahandler.autocreate.users value
	 * @return
	 */
	public boolean getAutoCreateUsers()	{
		return this.m_autoCreateUsers;
	}
	
	/**
	 * Get the EPID root
	 * @return
	 */
	public String getEpidRoot() {
		return this.m_epidRoot;
	}
	
	/**
	 * Get the shr-cdahandler.idformat value
	 */
	public String getIdFormat() {
		return this.m_idFormat;
    }

	/**
	 * Get the shr-cdahandler.id.ecidRoot value
	 */
	public String getEcidRoot() {
		return this.m_ecidRoot;
    }

	/**
	 * Get the root of SHR records
	 */
	public String getShrRoot() {
		return this.m_shrRoot;
	}
	
	/**
	 * Get the root of Visits
	 */
	public String getVisitRoot() {
		return this.m_shrRoot + ".1";
	}
	
	/**
	 * Get the root of encounters
	 */
	public String getEncounterRoot() {
		return this.m_shrRoot + ".2";
	}	

	/**
	 * Get the root of Obs
	 */
	public String getObsRoot() {
		return this.m_shrRoot + ".3";
	}	

	/**
	 * Get the root of orders
	 */
	public String getOrderRoot() {
		return this.m_shrRoot + ".4";
	}	

	/**
	 * Get the root of problems
	 */
	public String getProblemRoot() {
		return this.m_shrRoot + ".5";
	}	

	/**
	 * Get the root of allergies
	 */
	public String getAllergyRoot() {
		return this.m_shrRoot + ".6";
	}	

	/**
     * Read a global property
     */
    private <T> T getOrCreateGlobalProperty(String propertyName, T defaultValue)
    {
		String propertyValue = Context.getAdministrationService().getGlobalProperty(propertyName);
		if(propertyValue != null && !propertyValue.isEmpty())
			return (T)FormatterUtil.fromWireFormat(propertyValue, defaultValue.getClass());
		else
		{
			Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(propertyName, defaultValue.toString()));
			return defaultValue;
		}
    }

	/**
	 * Get the shr-cdahandler.updatedExisting value
	 * 
	 * This affects the way that the SHR will persist entries / documents with identical IDs. If set to true
	 * then the SHR will simply perform an update. When false, all IDs must be unique or else a duplicate exception
	 * is thrown
	 * @return
	 */
	public boolean getUpdateExisting() {
		return this.m_updateExisting;
    }

	/**
	 * Get the shr-cdahandler.validate.concept value
	 */
	public boolean getValidateConceptStructure() {
		return this.m_validateConceptStructure;
    }

	/**
	 * Get the shr-cdahandler.validate.cda value
	 * @return
	 */
	public boolean getValidationEnabled() {
		return this.m_validateInstances;
    }
	
	/**
     * Initialize
     */
	private void initialize() {
		
		// Initialize PQ converters
		PQ.getUnitConverters().add(new SimpleSiUnitConverter());


		this.m_autoCreateProviders = this.getOrCreateGlobalProperty(PROP_AUTOCREATE_PROVIDERS, this.m_autoCreateProviders);
		this.m_idFormat = this.getOrCreateGlobalProperty(PROP_ID_FORMAT, this.m_idFormat);
		this.m_autoCreateLocations = this.getOrCreateGlobalProperty(PROP_AUTOCREATE_LOCATIONS, this.m_autoCreateLocations);
		this.m_autoCreatePersons = this.getOrCreateGlobalProperty(PROP_AUTOCREATE_PERSONS, this.m_autoCreatePersons);
		this.m_autoCreatePatients = this.getOrCreateGlobalProperty(PROP_AUTOCREATE_PATIENTS, this.m_autoCreatePatients);
		this.m_autoCreatePatientIdType = this.getOrCreateGlobalProperty(PROP_AUTOCREATE_PATIENTIDTYPE, this.m_autoCreatePatientIdType);
		this.m_autoCreateConcepts = this.getOrCreateGlobalProperty(PROP_AUTOCREATE_CONCEPTS, this.m_autoCreateConcepts);
		this.m_autoCreateMetadata = this.getOrCreateGlobalProperty(PROP_AUTOCREATE_METADATA, this.m_autoCreateMetadata);
		this.m_validateInstances = this.getOrCreateGlobalProperty(PROP_VALIDATE_STRUCTURE, this.m_validateInstances);
		this.m_validateConceptStructure = this.getOrCreateGlobalProperty(PROP_VALIDATE_CONCEPT_STRUCTURE, this.m_validateInstances);
		this.m_updateExisting = this.getOrCreateGlobalProperty(PROP_UPDATE_EXISTING, this.m_updateExisting);
		this.m_autoCreateUsers = this.getOrCreateGlobalProperty(PROP_AUTOCREATE_USERS,  this.m_autoCreateUsers);
		this.m_epidRoot = this.getOrCreateGlobalProperty(PROP_EPID_ROOT, this.m_epidRoot);
		this.m_ecidRoot = this.getOrCreateGlobalProperty(PROP_ECID_ROOT, this.m_ecidRoot);
		this.m_shrRoot = this.getOrCreateGlobalProperty(PROP_SHR_ROOT, this.m_shrRoot);
	}
}
