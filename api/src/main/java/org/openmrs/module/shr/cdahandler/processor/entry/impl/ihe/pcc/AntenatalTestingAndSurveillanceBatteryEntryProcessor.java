package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.generic.CE;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.entry.impl.OrganizerEntryProcessor;

/**
 * Entry processor for Antenatal Testing & Surveillance Battery
 */
@ProcessTemplates(templateIds = {CdaHandlerConstants.ENT_TEMPLATE_ANTENATAL_TESTING_BATTERY})
public class AntenatalTestingAndSurveillanceBatteryEntryProcessor extends OrganizerEntryProcessor {
	
	/**
	 * Gets the name of the template
	 * @see org.openmrs.module.shr.cdahandler.processor.Processor#getTemplateName()
	 */
	@Override
	public String getTemplateName() {
		return "Antenatal Testing & Surveillance Battery";
	}
	
	/**
	 * Get the expected code for the section
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.OrganizerEntryProcessor#getExpectedCode()
	 */
	@Override
	public CE<String> getExpectedCode() {
		return new CE<String>("XX-ANTENATALTESTINGBATTERY", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "ANTENATAL TESTING AND SURVEILLANCE BATTERY", null);
	}

	/**
	 * Get the expected components within the organizer
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.OrganizerEntryProcessor#getExpectedComponents()
	 */
	@Override
	protected List<String> getExpectedComponents() {
		return Arrays.asList(CdaHandlerConstants.ENT_TEMPLATE_SIMPLE_OBSERVATION);
	}

	/**
	 * Get the expected entry relationships
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.EntryProcessorImpl#getExpectedEntryRelationships()
	 */
	@Override
	protected List<String> getExpectedEntryRelationships() {
		return null;
	}
	
}
