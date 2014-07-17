package org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc;

import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor;

/**
 * A template processor which can handle History of Present Illness
 * 
 * From PCC:
 * The history of present illness section shall contain a narrative description of the sequence of events preceding the patient’s current complaints. 
 * 
 * See: PCC TF-2:6.3.3.2.1
 */
@ProcessTemplates(
	process = {
			@TemplateId(root = CdaHandlerConstants.SCT_TEMPLATE_HISTORY_OF_PRESENT_ILLNESS)
	})
public class HistoryOfPresentIllnessSectionProcessor extends GenericLevel2SectionProcessor {

	/**
	 * Get template name
	 */
	@Override
    public String getTemplateName() {
		return "History of Present Illness";
    }

	/**
	 * Get the expected code
	 */
	@Override
    public CE<String> getExpectedSectionCode() {
		return new CE<String>("10164-2", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "HISTORY OF PRESENT ILLNESS", null);
    }
	
	
}
