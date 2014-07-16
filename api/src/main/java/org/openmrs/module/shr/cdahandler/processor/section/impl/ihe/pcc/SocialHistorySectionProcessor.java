package org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor;

/**
 * Template processor that handles Social History sections (Level 2 or 3)
 */
@ProcessTemplates(
	understands = {
			@TemplateId(root = CdaHandlerConstants.SCT_TEMPLATE_SOCIAL_HISTORY),
			@TemplateId(root = CdaHandlerConstants.SCT_TEMPLATE_CODED_SOCIAL_HISTORY)
	})
public class SocialHistorySectionProcessor extends GenericLevel3SectionProcessor {
	
	/**
	 * Get expected entries
	 */
	@Override
	protected List<String> getExpectedEntries(Section section) {
		if(section.getTemplateId().contains(new II(CdaHandlerConstants.SCT_TEMPLATE_CODED_SOCIAL_HISTORY)))
			return Arrays.asList(CdaHandlerConstants.ENT_TEMPLATE_SOCIAL_HISTORY_OBSERVATION);
		return null;
	}

	/**
	 * Get the template name
	 */
	@Override
    public String getTemplateName() {
		return "Social History";
    }

	/**
	 * Get the expected section code
	 */
	@Override
    protected CE<String> getExpectedSectionCode(Section section) {
		return new CE<String>("29762-2", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "SOCIAL HISTORY", null);
    }
	
	
	
}
