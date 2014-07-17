package org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc;

import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor;

/**
 * A processor that can handle a Review of Systems section
 * 
 * From PCC:
 * The review of systems section shall contain a narrative description of the responses the patient gave to a set of routine questions on the functions of each anatomic body system. 
 * 
 * See: PCC TF-2:6.3.3.2.16
 */
@ProcessTemplates(
	process = {
			@TemplateId(root = CdaHandlerConstants.SCT_TEMPLATE_REVIEW_OF_SYSTEMS)
	})
public class ReviewOfSystemsSectionProcessor extends GenericLevel2SectionProcessor {

	/**
	 * Get template name
	 */
	@Override
    public String getTemplateName() {
		return "Review of Systems";
    }

	/**
	 * Get the expected section code
	 */
	@Override
    public CE<String> getExpectedSectionCode() {
		return new CE<String>("10187-3", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "REVIEW OF SYSTEMS", null);
    }
	
	
}
