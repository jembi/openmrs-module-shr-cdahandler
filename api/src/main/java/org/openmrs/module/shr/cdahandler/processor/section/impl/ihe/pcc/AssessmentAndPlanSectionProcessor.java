package org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc;

import org.marc.everest.datatypes.generic.CE;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor;

/**
 * A template processor that supports the Assessment & Plan section 
 * 
 * From PCC:
 * The assessment and plan section shall contain a narrative description of the assessment of the patient condition and expectations for care including proposals, goals, and order requests for monitoring, tracking, or improving the condition of the patient. 
 * 
 * See: PCC TF-2:6.3.3.6.2
 */
@ProcessTemplates(
	process = {
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
    public CE<String> getExpectedSectionCode() {
	    // TODO Auto-generated method stub
	    return new CE<String>("51847-2", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "ASSESSMENT AND PLAN", null);
    }
	
	
	
}
