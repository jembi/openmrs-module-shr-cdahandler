package org.openmrs.module.shr.cdahandler.processor.util;

import java.util.List;

import org.jfree.util.Log;
import org.marc.everest.datatypes.AD;
import org.marc.everest.datatypes.ON;
import org.marc.everest.datatypes.TEL;
import org.marc.everest.datatypes.generic.SET;
import org.marc.everest.formatters.FormatterUtil;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.CustodianOrganization;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Organization;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.configuration.CdaHandlerConfiguration;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;

/**
 * A class which processes organization/location information 
 */
public final class LocationOrganizationProcessorUtil {
	
	/**
	 * Get the singleton instance
	 */
	public static LocationOrganizationProcessorUtil getInstance()
	{
		if(s_instance == null)
		{
			synchronized (s_lockObject) {
				if(s_instance == null) // Check to make sure another thread hasn't constructed
					s_instance = new LocationOrganizationProcessorUtil();
			}
		}
		return s_instance;
	}
	// singleton instance
	private static LocationOrganizationProcessorUtil s_instance;

	private static Object s_lockObject = new Object();
	// Utility classes
	private final CdaHandlerConfiguration m_configuration = CdaHandlerConfiguration.getInstance();
	private final OpenmrsMetadataUtil m_metaDataUtil = OpenmrsMetadataUtil.getInstance();


	private final DatatypeProcessorUtil m_datatypeUtil = DatatypeProcessorUtil.getInstance();
	
	/**
	 * Private ctor
	 */
	private LocationOrganizationProcessorUtil()
	{
		
	}

	/**
	 * Create a location in the oMRS data store from an organization
	 * 
	 * @param representedCustodianOrganization The HL7v3 RMIM representing the location / organization
	 * @param id The assigned identifier for the location
	 * @return The constructed location
	 * @throws DocumentImportException 
	 */
	// HACK: This should probably be it's own entity in the OpenMRS datastore as in the documentation it states
	// 		 that organizations could be tags on a location, however this is slightly different than the purpose
	//		 of the organization.
	private Location createLocation(Organization organization, String id) throws DocumentImportException {
		
		if(!this.m_configuration.getAutoCreateLocations())
			throw new IllegalStateException("Cannot create locations according to global properties");

		Location res = new Location();
		
		
		// Assign the name
		if(organization.getName() != null && !organization.getName().isNull() && !organization.getName().isEmpty())
			res.setName(organization.getName().get(0).toString());
		else
			res.setName("unnamed location");
		
		res.setDescription("Automatically created");
		
		// Assign the telecom
		if(organization.getAddr() != null && !organization.getAddr().isNull())
		{
			for(AD ad : organization.getAddr())
				if(!ad.isNull())
					this.parseAddressParts(ad, res);
		}
				
		// Telecom?
		if(organization.getTelecom() != null && !organization.getTelecom().isNull() )
		{
			for(TEL tel : organization.getTelecom())
			{
				if(tel.getValue() == null) continue;
				LocationAttribute telecomAttribute = new LocationAttribute();
				telecomAttribute.setAttributeType(this.m_metaDataUtil.getOrCreateLocationTelecomAttribute());
				telecomAttribute.setValueReferenceInternal(String.format("%s: %s", FormatterUtil.toWireFormat(tel.getUse()), tel.getValue()));
				res.addAttribute(telecomAttribute);
			}
		}

		// External id
		if(!id.equals(this.m_datatypeUtil.emptyIdString()))
		{
			LocationAttribute externalId = new LocationAttribute();
			externalId.setAttributeType(this.m_metaDataUtil.getOrCreateLocationExternalIdAttributeType());
			externalId.setValueReferenceInternal(id);
			res.addAttribute(externalId);
		}
		
		res = Context.getLocationService().saveLocation(res);
		return res;
    }

	/**
	 * Get organization by an external id
	 */
	public Location getOrganizationByExternalId(String id) throws DocumentImportException
	{
		
		// TODO: This is an organization not a location so we need to get all locations belonging to the organization
		// TODO: For now this is stored just as a location with an externalId tag
		// HACK: The function that does this natively in OpenMRS is missing from 1.9 and is available in 1.10
		for(Location loc : Context.getLocationService().getAllLocations())
		{
			List<LocationAttribute> locAttributes = loc.getActiveAttributes(this.m_metaDataUtil.getOrCreateLocationExternalIdAttributeType());
			if(locAttributes.size() == 1 && locAttributes.get(0).getValueReference().equals(id)) 
				return loc; 
		}
		return  null;
	}
	/**
	 * Copy address parts from an AD into the specified location
	 * @throws DocumentImportException 
	 */
	private void parseAddressParts(AD addr, Location target) throws DocumentImportException {

		// Create a person address and then copy
		PersonAddress personAddress = this.m_datatypeUtil.parseAD(addr);
		target.setAddress1(personAddress.getAddress1());
		target.setAddress2(personAddress.getAddress2());
		target.setAddress3(personAddress.getAddress3());
		target.setAddress4(personAddress.getAddress4());
		target.setAddress5(personAddress.getAddress5());
		target.setAddress6(personAddress.getAddress6());
		target.setCityVillage(personAddress.getCityVillage());
		target.setStateProvince(personAddress.getStateProvince());
		target.setCountry(personAddress.getCountry());
		target.setCountyDistrict(personAddress.getCountyDistrict());
		target.setPostalCode(personAddress.getPostalCode());
    }

	/**
	 * Parse an HL7v3 Organization into an OpenMRS location
	 * with a tag representing the organization itself.
	 * 
	 * This method will create a default location for the organization
	 * if one does not exist and will create a tags representing organization
	 * identity 
	 * 
	 * @param representedCustodianOrganization The HL7v3 organization RMIM instance
	 * @return The parsed location
	 * @throws DocumentImportException 
	 */
	public Location processOrganization(CustodianOrganization representedCustodianOrganization) throws DocumentImportException {
		
		
		if (representedCustodianOrganization == null || representedCustodianOrganization.getNullFlavor() != null)
			throw new DocumentImportException("CustodianOrganization role is null");
		else if(representedCustodianOrganization.getId() == null || representedCustodianOrganization.getId().isNull() || representedCustodianOrganization.getId().isEmpty())
			throw new DocumentImportException("No identifiers found for organization");
		
		String id = this.m_datatypeUtil.formatIdentifier(representedCustodianOrganization.getId().get(0));
		
		Location res = null;

		// Was there an id?
		if (id.equals(this.m_datatypeUtil.emptyIdString())) 
			throw new DocumentImportException("No data specified for location id");
		else
			res = this.getOrganizationByExternalId(id);
		
		if (res==null && this.m_configuration.getAutoCreateLocations())
		{
			// Copy a CustodianOrganization to an Organization
			SET<ON> name = new SET<ON>();
			SET<TEL> tel = new SET<TEL>();
			SET<AD> addr = new SET<AD>();
			
			if(representedCustodianOrganization.getName() != null)
				name.add(representedCustodianOrganization.getName());
			if(representedCustodianOrganization.getAddr() != null)
				addr.add(representedCustodianOrganization.getAddr());
			if(representedCustodianOrganization.getTelecom() != null)
				tel.add(representedCustodianOrganization.getTelecom());
			Organization asOrganization = new Organization(
				representedCustodianOrganization.getId(),
				name,
				tel,
				addr,
				null,
				null
			);
			res = this.createLocation(asOrganization, id);
		}
		else if(res == null && !this.m_configuration.getAutoCreateLocations())
			throw new DocumentImportException(String.format("Unknown location %s", id));
		return res;
    }

	/**
	 * Parse an organization into a Location
	 * @throws DocumentImportException 
	 */
	public Location processOrganization(Organization organization) throws DocumentImportException {
		
		if (organization == null || organization.getNullFlavor() != null)
			throw new DocumentImportException("Organization role is null");
		else if((organization.getId() == null || organization.getId().isNull() || organization.getId().isEmpty()) &&
				(organization.getName() == null || organization.getName().isEmpty()))
			throw new DocumentImportException("No identifying information found for organization");
		
		String id = this.m_datatypeUtil.emptyIdString();
		
		Location res = null;
		// Find search criteria
		if(organization.getId() != null)
		{
			id = this.m_datatypeUtil.formatIdentifier(organization.getId().get(0));
			Log.debug(String.format("Finding organization by id %s", id));
			res = this.getOrganizationByExternalId(id);
		}
		else if(organization.getName() != null && !organization.getName().isEmpty())
		{
			String orgName = organization.getName().get(0).toString();
			Log.debug(String.format("Finding organization by name %s", orgName));
			res = Context.getLocationService().getLocation(orgName);
		}
		else
			throw new DocumentImportException("No data specified for location id");
		
		if (res==null && this.m_configuration.getAutoCreateLocations())
			res = this.createLocation(organization, id);
		else if(res == null && !this.m_configuration.getAutoCreateLocations())
			throw new DocumentImportException(String.format("Unknown location %s", id));
		return res;
		
    }

	
}
