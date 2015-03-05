package org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.II;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor;

/**
 * Physical examination section processor
 * 
 * From PCC TF:
 * The Coded Detailed Physical Examination section shall contain a narrative description of the patient’s physical findings. It shall include subsections, if known, for the exams that are performed.
 * 
 * See: PCC TF-2:6.3.3.4.2
 * PCC TF-2 CDA Suppl:6.3.3.4.30
 */
@ProcessTemplates(
	templateIds = {
		CdaHandlerConstants.SCT_TEMPLATE_DETAILED_PHYSICAL_EXAM,
		CdaHandlerConstants.SCT_TEMPLATE_CODED_PHYISCAL_EXAM
	})
public class PhysicalExaminationSectionProcessor extends GenericLevel3SectionProcessor {
	
	/**
	 * Get the expected sections
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor#getExpectedEntries(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section)
	 */
	@Override
	protected List<String> getExpectedEntries(Section section) {
		return null;
	}

	/**
	 * Gets the expected sub-sections
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor#getExpectedSubSections(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section)
	 */
	@Override
    protected List<String> getExpectedSubSections(Section section) {
		if(section.getTemplateId().contains(new II(CdaHandlerConstants.SCT_TEMPLATE_CODED_PHYISCAL_EXAM)))
			return Arrays.asList(
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
					);
		return null;
    }

	/**
	 * Get the template name
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#getTemplateName()
	 */
	@Override
    public String getTemplateName() {
		return "Physical Examination";
    }
	
	
	
}
