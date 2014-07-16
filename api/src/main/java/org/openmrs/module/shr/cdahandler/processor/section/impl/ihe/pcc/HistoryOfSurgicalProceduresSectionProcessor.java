package org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc;

import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor;

/**
 * A History of Surgical Procedures Section processor
 */
@ProcessTemplates(
	understands = {
			@TemplateId(root = CdaHandlerConstants.SCT_TEMPLATE_HISTORY_OF_SURGICAL_PROCEDURES)
	})
public class HistoryOfSurgicalProceduresSectionProcessor extends GenericLevel2SectionProcessor {

	/**
	 * Gets the name of the template
	 */
	@Override
    public String getTemplateName() {
		return "History of Surgical Procedures";
    }

	/**
	 * Gets the expected code for the section 
	 */
	@Override
    protected CE<String> getExpectedSectionCode(Section section) {
		return new CE<String>("10167-5", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "HISTORY OF SURGICAL PROCEDURES", null);
    }
	
	
}
