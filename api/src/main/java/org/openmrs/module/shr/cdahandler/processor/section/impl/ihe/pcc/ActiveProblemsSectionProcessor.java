package org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor;

/**
 * Active problems list
 * 
 * From PCC:
 * The active problem section shall contain a narrative description of the conditions currently being monitored for the patient. It shall include entries for patient conditions as described in the Entry Content Module. 
 * 
 * See: PCC TF-2: 6.3.3.2.3
 */
@ProcessTemplates(
	process = {
			@TemplateId(root = CdaHandlerConstants.SCT_TEMPLATE_ACTIVE_PROBLEMS)
	})
public class ActiveProblemsSectionProcessor extends GenericLevel3SectionProcessor {
	
	/**
	 * Problem concern entry
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor#getExpectedEntries(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section)
	 */
	@Override
	protected List<String> getExpectedEntries(Section section) {
		return Arrays.asList(CdaHandlerConstants.ENT_TEMPLATE_PROBLEM_CONCERN);
	}

	/**
	 * Get the template name
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#getTemplateName()
	 */
	@Override
    public String getTemplateName() {
		return "Active Problems";
    }

	/**
	 * Get the expected code
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#getExpectedSectionCode()
	 */
	@Override
    public CE<String> getExpectedSectionCode() {
	    return new CE<String>("11450-4", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "PROBLEM LIST", null);
    }
	
	
}
