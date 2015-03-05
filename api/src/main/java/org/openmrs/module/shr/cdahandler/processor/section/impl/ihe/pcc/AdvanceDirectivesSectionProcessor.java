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
 * Section processor for advance directives or coded advance directives
 * 
 * From PCC TF:
 * The advance directive section shall include entries for references to consent and advance directive documents (e.g., Durable Power of Attorney, Code Status) when known as described in the Entry Content Modules. 
 * 
 * See: PCC TF-2: 6.3.3.6.6
 */
@ProcessTemplates(templateIds = { 
		CdaHandlerConstants.SCT_TEMPLATE_ADVANCE_DIRECTIVES, 
		CdaHandlerConstants.SCT_TEMPLATE_CODED_ADVANCE_DIRECTIVES
		})
public class AdvanceDirectivesSectionProcessor extends GenericLevel3SectionProcessor {

	/**
	 * Get the expected entries
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor#getExpectedEntries(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section)
	 */
	@Override
	protected List<String> getExpectedEntries(Section section) {
		if(m_datatypeProcessorUtil.hasTemplateId(section, new II(CdaHandlerConstants.SCT_TEMPLATE_CODED_ADVANCE_DIRECTIVES)))
			return Arrays.asList(CdaHandlerConstants.ENT_TEMPLATE_ADVANCE_DIRECTIVE_OBSERVATION);
		return null;
	}

	/**
     * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#getExpectedSectionCode()
     */
    @Override
    public CE<String> getExpectedSectionCode() {
	    return new CE<String>("42348-3", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "ADVANCE DIRECTIVES", null);
    }

	/**
     * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#getTemplateName()
     */
    @Override
    public String getTemplateName() {
    	return "Advance Directives";
    }
	
	
	
}
