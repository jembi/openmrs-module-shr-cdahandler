package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.generic.CE;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;
import org.openmrs.module.shr.cdahandler.processor.entry.impl.OrganizerEntryProcessor;

/**
 * A class that processes a vital signs organizer
 */
@ProcessTemplates(
	process = {
			@TemplateId(root = CdaHandlerConstants.ENT_TEMPLATE_VITAL_SIGNS_ORGANIZER)
	})
public class VitalSignsOrganizerEntryProcessor extends OrganizerEntryProcessor {
	
	/**
	 * Get template name
	 */
	@Override
	public String getTemplateName() {
		return "Vital Signs Organizer";
	}

	/**
	 * Get the expected code
	 */
	@Override
    public CE<String> getExpectedCode() {
		return new CE<String>("46680005", CdaHandlerConstants.CODE_SYSTEM_SNOMED, "SNOMED CT", null, "Vital Signs", null);
    }

	/**
	 * Get expected components
	 */
	@Override
    protected List<String> getExpectedComponents() {
		return Arrays.asList(CdaHandlerConstants.ENT_TEMPLATE_VITAL_SIGNS_OBSERVATION);
    }

	/**
	 * Get expected entry relationships
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.EntryProcessorImpl#getExpectedEntryRelationships()
	 */
	@Override
    protected List<String> getExpectedEntryRelationships() {
	    return null;
    }
	
}
