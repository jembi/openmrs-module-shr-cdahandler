package org.openmrs.module.shr.cdahandler.processor.util;

import org.marc.everest.datatypes.PN;
import org.openmrs.GlobalProperty;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerGlobalPropertyNames;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;

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
		String propertyValue = Context.getAdministrationService().getGlobalProperty(CdaHandlerGlobalPropertyNames.AUTOCREATE_PERSONS);
		if(propertyValue != null && !propertyValue.isEmpty())
			this.m_autoCreatePersons = Boolean.parseBoolean(propertyValue);
		else
			Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(CdaHandlerGlobalPropertyNames.AUTOCREATE_PERSONS, this.m_autoCreatePersons.toString()));
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
			org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Person person) throws DocumentParseException {
		
		if(person == null || person.getNullFlavor() != null)
			throw new DocumentParseException("Cannot parse a null person relationship");
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
	
}
