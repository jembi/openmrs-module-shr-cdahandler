package org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor;

/**
 * This is a generic section handler that provides implementations for the expected sections
 * that would appear in a physical examination section.
 * 
 * See: PCC TF-2: 6.3.3.4.6 thru 6.3.3.4.29
 */
@ProcessTemplates(
	templateIds = {
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_ABDOMEN,
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_BREASTS,
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_CHEST_WALL,
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_EARS,
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_EARS_NOSE,
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_ENDOCRINE_SYSTEM,
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_EXTREMITIES,
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_EYES,
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_GENERAL_APPEARANCE,
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_GENITALIA,
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_HEAD,
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_HEART,
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_INTEGUMENTARY_SYSTEM,
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_LYMPHATIC,
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_MOUTH_THROAT,
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_MUSCULOSKELETAL,
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_NECK,
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_NEUROLOGIC,
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_NOSE,
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_PELVIS,
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_RECTUM,
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_RESPIRATORY_SYSTEM,
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_THORAX_LUNGS,
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_VESSELS,
			CdaHandlerConstants.SCT_TEMPLATE_EXAM_VISIBLE_IMPLANTED_DEVICES
	})
public class PhysicalExamSubSectionProcessor extends GenericLevel3SectionProcessor {
	
	@Override
	protected List<String> getExpectedEntries(Section section) {
		return Arrays.asList(CdaHandlerConstants.ENT_TEMPLATE_PROBLEM_OBSERVATION);
	}
	
}
