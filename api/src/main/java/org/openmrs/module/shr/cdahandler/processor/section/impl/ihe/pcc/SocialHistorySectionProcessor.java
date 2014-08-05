package org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor;

/**
 * Template processor that handles Social History sections (Level 2 or 3)
 * 
 * From PCC:
 * The social history section shall contain a narrative description of the person’s beliefs, home life, community life, work life, hobbies, and risky habits. 
 * 
 * See:PCC TF-2:6.3.3.2.14
 * See: PCC TF-2 CDA Suppl:6.3.3.2.36
 */
@ProcessTemplates(
	templateIds = {
			CdaHandlerConstants.SCT_TEMPLATE_SOCIAL_HISTORY,
			CdaHandlerConstants.SCT_TEMPLATE_CODED_SOCIAL_HISTORY
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
	 * Get the expected section code
	 */
	@Override
    public CE<String> getExpectedSectionCode() {
		return new CE<String>("29762-2", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "SOCIAL HISTORY", null);
    }

	/**
	 * Get the template name
	 */
	@Override
    public String getTemplateName() {
		return "Social History";
    }
	
	
	
}
