package org.openmrs.module.shr.cdahandler.configuration;


/**
 * Consolidated configuration class
 */
public interface CdaHandlerConfiguration {
	
    /**
	 * Get the shr-cdahandler.autocreate.concepts value
	 */
	public boolean getAutoCreateConcepts();
    
    /**
	 * Get the shr-cdahandler.autocreate.locations value
	 */
	public boolean getAutoCreateLocations();

	/**
	 * Get the shr-cdahandler.autocreate.metadata value
	 * @return
	 */
	public boolean getAutoCreateMetaData() ;

	/**
	 * Get the shr-cdahandler.autocreate.patientidtype value
	 */
	public boolean getAutoCreatePatientIdType();
	
	/**
	 * Get the shr-cdahandler.autocreate.patients value
	 */
	public boolean getAutoCreatePatients() ;
	
	/**
	 * Get the shr-cdahandler.autocreate.persons value
	 * @return
	 */
	public boolean getAutoCreatePersons() ;

	/**
	 * Get the shr-cdahandler.autocreate.providers value
	 */
	public boolean getAutoCreateProviders();

	/**
	 * Get the shr-cdahandler.autocreate.users value
	 * @return
	 */
	public boolean getAutoCreateUsers();	
	
	/**
	 * Get the EPID root
	 * @return
	 */
	public String getEpidRoot() ;
	
	/**
	 * Get the shr-cdahandler.idformat value
	 */
	public String getIdFormat() ;

	/**
	 * Get the shr-cdahandler.id.ecidRoot value
	 */
	public String getEcidRoot() ;

	/**
	 * Get the root of SHR records
	 */
	public String getShrRoot() ;
	
	/**
	 * Get the root of Visits
	 */
	public String getVisitRoot();
	
	/**
	 * Get the root of encounters
	 */
	public String getEncounterRoot(); 

	/**
	 * Get the root of Obs
	 */
	public String getObsRoot() ;

	/**
	 * Get the root of orders
	 */
	public String getOrderRoot();

	/**
	 * Get the root of problems
	 */
	public String getProblemRoot();

	/**
	 * Get the root of allergies
	 */
	public String getAllergyRoot();

	/**
	 * Get internal provider identifiers
	 */
	public String getProviderRoot();
	
	/**
	 * Get internal location identifier root
	 */
	public String getLocationRoot();

	/**
	 * Get internal patient root identifiers
	 */
	public String getPatientRoot(); 

	/**
	 * Get the root oid for Users
	 */
	public String getUserRoot() ;


	/**
	 * Get the shr-cdahandler.updatedExisting value
	 * 
	 * This affects the way that the SHR will persist entries / documents with identical IDs. If set to true
	 * then the SHR will simply perform an update. When false, all IDs must be unique or else a duplicate exception
	 * is thrown
	 * @return
	 */
	public boolean getUpdateExisting() ;

	/**
	 * Get the shr-cdahandler.validate.concept value
	 */
	public boolean getValidateConceptStructure(); 

	/**
	 * Get the shr-cdahandler.validate.cda value
	 * @return
	 */
	public boolean getValidationEnabled(); 
	

}
