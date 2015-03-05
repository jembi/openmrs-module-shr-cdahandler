package org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor;

/**
 * Section processor Immunizations section
 * 
 * From PCC TF: 
 * The immunizations section shall contain a narrative description of the immunizations administered to the patient in the past. It shall include entries for medication administration as described in the Entry Content Modules. 
 * 
 * See: PCC TF-2: 6.3.3.3.5
 */
@ProcessTemplates(templateIds = { CdaHandlerConstants.SCT_TEMPLATE_IMMUNIZATIONS })
public class ImmunizationsSectionProcessor extends GenericLevel3SectionProcessor {
	
	/**
	 * Get the expected entries
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor#getExpectedEntries(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section)
	 */
	@Override
	protected List<String> getExpectedEntries(Section section) {
		return Arrays.asList(CdaHandlerConstants.ENT_TEMPLATE_IMMUNIZATIONS);
	}

	/**
     * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#getExpectedSectionCode()
     */
    @Override
    public CE<String> getExpectedSectionCode() {
	    return new CE<String>("11369-6", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "HISTORY OF IMMUNIZATIONS", null);
    }

	/**
     * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#getTemplateName()
     */
    @Override
    public String getTemplateName() {
    	return "Immunizations Section";
    }
	
	
}
