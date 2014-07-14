package org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc;

import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.module.shr.cdahandler.CdaHandlerOids;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor;

/**
 * Template handler for Chief Complaint Section
 */
@ProcessTemplates(
	understands = {
			@TemplateId(root = CdaHandlerOids.SCT_TEMPLATE_CHIEF_COMPLAINT)
	})
public class ChiefComplaintSectionProcessor extends GenericLevel2SectionProcessor {

	/**
	 * Get expected section code of 10154-3
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#getExpectedSectionCode(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section)
	 */
	@Override
    protected CE<String> getExpectedSectionCode(Section section) {
		return new CE<String>("10154-3", CdaHandlerOids.CODE_SYSTEM_LOINC, "LOINC", null, "CHIEF COMPLAINT", null);
    }
	
	
	
}
