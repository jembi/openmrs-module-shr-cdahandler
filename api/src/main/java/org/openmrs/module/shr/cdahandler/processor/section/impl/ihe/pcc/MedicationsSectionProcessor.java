package org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor;

/**
 * Processor for the Medications Section
 * 
 * From PCC TF:
 * The medications section shall contain a description of the relevant medications for the patient, e.g., an ambulatory prescription list. It shall include entries for medications as described in the Entry Content Module. 
 * 
 * See: PCC TF-2: 6.3.3.3.1
 */
@ProcessTemplates(templateIds = { CdaHandlerConstants.SCT_TEMPLATE_MEDICATIONS })
public class MedicationsSectionProcessor extends GenericLevel3SectionProcessor {
	
	/**
	 * Get the expected entries
	 */
	@Override
	protected List<String> getExpectedEntries(Section section) {
		return Arrays.asList(CdaHandlerConstants.ENT_TEMPLATE_MEDICATIONS);
	}

	/**
	 * Get the expected section code
	 */
	@Override
    public CE<String> getExpectedSectionCode() {
		return new CE<String>("10160-0", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "HISTORY OF MEDICATION USE", null);
    }

	/**
	 * Get the template name
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#getTemplateName()
	 */
	@Override
    public String getTemplateName() {
		return "Medications Section";
    }
	
	
	
}
