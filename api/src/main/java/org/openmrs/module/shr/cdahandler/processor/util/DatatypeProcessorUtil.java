package org.openmrs.module.shr.cdahandler.processor.util;

import org.marc.everest.datatypes.AD;
import org.marc.everest.datatypes.ADXP;
import org.marc.everest.datatypes.EN;
import org.marc.everest.datatypes.ENXP;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.TS;
import org.marc.everest.datatypes.generic.CS;
import org.marc.everest.datatypes.generic.CV;
import org.marc.everest.datatypes.generic.IVL;
import org.marc.everest.interfaces.IEnumeratedVocabulary;
import org.openmrs.GlobalProperty;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerGlobalPropertyNames;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;

/**
 * A class containing utilities for parsing v3 datatypes
 * @author Justin
 *
 */
public final class DatatypeProcessorUtil {

	// singleton instance
	private static DatatypeProcessorUtil s_instance;
	private static Object s_lockObject = new Object();

	// Identifier format
	private String m_idFormat = "%2$s^^^&%1$s&ISO";

	/**
	 * Private ctor
	 */
	private DatatypeProcessorUtil()
	{
		
	}
	
	/**
	 * Initialize instance
	 */
	private void initializeInstance()
	{
		String propertyValue = Context.getAdministrationService().getGlobalProperty(CdaHandlerGlobalPropertyNames.ID_FORMAT);
		if(propertyValue != null && !propertyValue.isEmpty())
			this.m_idFormat = propertyValue;
		else
			Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(CdaHandlerGlobalPropertyNames.ID_FORMAT, this.m_idFormat));
	}
	
	/**
	 * Get the singleton instance
	 */
	public static DatatypeProcessorUtil getInstance()
	{
		if(s_instance == null)
		{
			synchronized (s_lockObject) {
				if(s_instance == null) // Another thread might have created while we were waiting for a lock
				{
					s_instance = new DatatypeProcessorUtil();
					s_instance.initializeInstance();
				}
			}
		}
		return s_instance;
	}

	/**
	 * Format an ID into a string
	 * @return
	 */
	public String formatIdentifier(II id)
	{
		if(id == null) return "";
		return String.format(this.m_idFormat, id.getRoot(), id.getExtension());
	}
	
	/**
	 * Format a code
	 * @param code The code to format
	 * @return The formatted code identifier
	 */
	public String formatSimpleCode(CS<? extends IEnumeratedVocabulary> code)
	{
		if(code == null) return "";
		return String.format(this.m_idFormat, code.getCode().getCodeSystem(), code.getCode().getCode());
	}
	
	/**
	 * Format a coded value
	 * @param code The code to format
	 * @return The formatted code identifier
	 */
	public String formatCodeValue(CV<?> code)
	{
		if(code == null) return "";
		return String.format(this.m_idFormat, code.getCodeSystem(), code.getCode());
	}
	/**
	 * Parse a person name
	 * @param name The name to parse
	 * @return The parsed name
	 */
	@SuppressWarnings("incomplete-switch")
	public PersonName parseEN(EN en)
	{
		PersonName name = new PersonName();
		// Iterate through parts
		for(ENXP part : en.getParts())
			switch(part.getType().getCode())
			{
				case Family:
					if(name.getFamilyName() == null)
						name.setFamilyName(part.getValue());
					else if(name.getFamilyName2() == null)
						name.setFamilyName2(part.getValue());
					else
						name.setFamilyName2(name.getFamilyName2() + " " + part.getValue());
					break;
				case Given:
					if(name.getGivenName() == null)
						name.setGivenName(part.getValue());
					else if (name.getMiddleName() == null)
						name.setMiddleName(part.getValue());
					else
						name.setMiddleName(name.getMiddleName() + " " + part.getValue());
					break;
				case Prefix:
					if(name.getPrefix() == null)
						name.setPrefix(part.getValue());
					else
						name.setPrefix(part.getValue() + " " + part.getValue());
					break;
					// TODO: Suffix?
			}

		return name;
	}

	/**
	 * Parse an HL7v3 AD into an OpenMRS PersonAddress
	 * @param ad The HL7v3 AD to parse
	 * @return The parsed Address
	 * @throws DocumentParseException 
	 */
	@SuppressWarnings("incomplete-switch")
	public PersonAddress parseAD(AD ad) throws DocumentParseException {
		PersonAddress address = new PersonAddress();
		// Iterate through parts
		for(ADXP part : ad.getPart())
			switch(part.getPartType())
			{
				case AddressLine:
				case StreetAddressLine:
				case AdditionalLocator:
					if(address.getAddress1() == null)
						address.setAddress1(part.getValue());
					else if(address.getAddress2() == null)
						address.setAddress2(part.getValue());
					else if(address.getAddress3() == null)
						address.setAddress3(part.getValue());
					else if(address.getAddress4() == null)
						address.setAddress4(part.getValue());
					else if(address.getAddress5() == null)
						address.setAddress5(part.getValue());
					else if(address.getAddress6() == null)
						address.setAddress6(part.getValue());
					break;
				case Country:
					address.setCountry(part.getValue());
					break;
				case State:
					address.setStateProvince(part.getValue());
					break;
				case County:
				case Precinct:
					address.setCountyDistrict(part.getValue());
					break;
				case PostalCode:
					address.setPostalCode(part.getValue());
					break;
			}

		// Is there a simple useable period on here?
		if(ad.getUseablePeriod().getHull() instanceof IVL)
		{
			IVL<TS> useablePeriod = (IVL<TS>)ad.getUseablePeriod().getHull();
			if(useablePeriod.getLow() != null)
				address.setStartDate(useablePeriod.getLow().getDateValue().getTime());
			if(useablePeriod.getHigh() != null)
				address.setEndDate(useablePeriod.getHigh().getDateValue().getTime());
		}
		else
			throw new DocumentParseException("Complex GTS instances are not supported for usablePeriod. Please use GTS with IVL");
		return address;
	}

	
	
}
