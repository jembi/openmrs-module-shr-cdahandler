package org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc;

import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor;

/**
 * A template processor that supports the Assessment & Plan section 
 */
@ProcessTemplates(
	understands = {
			@TemplateId(root = CdaHandlerConstants.SCT_TEMPLATE_ASSESSMENT_AND_PLAN)
	})
public class AssessmentAndPlanSectionProcessor extends GenericLevel2SectionProcessor {

	/**
	 * Get the template name
	 */
	@Override
    public String getTemplateName() {
	    // TODO Auto-generated method stub
	    return super.getTemplateName();
    }

	/**
	 * Get the expected section code
	 */
	@Override
    protected CE<String> getExpectedSectionCode(Section section) {
	    // TODO Auto-generated method stub
	    return new CE<String>("51847-2", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "ASSESSMENT AND PLAN", null);
    }
	
	
	
}
