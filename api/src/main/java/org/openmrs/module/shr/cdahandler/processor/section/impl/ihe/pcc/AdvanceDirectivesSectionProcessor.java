package org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc;

import org.marc.everest.datatypes.generic.CE;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor;

/**
 * Processor for Advance Directives Section
 * 
 * From PCC TF:
 * The advance directive section shall contain a narrative description of the list of documents (e.g., Durable Power of Attorney, Code Status) that define the patient’s expectations and requests for care along with the locations of the documents
 * 
 * See: PCC TF-2: 6.3.3.6.5
 */
public class AdvanceDirectivesSectionProcessor extends GenericLevel2SectionProcessor {

	/**
	 * Get template name
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#getTemplateName()
	 */
	@Override
    public String getTemplateName() {
		return "Advance Directives";
    }

	/**
	 * Get expected section code
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#getExpectedSectionCode()
	 */
	@Override
    public CE<String> getExpectedSectionCode() {
		return new CE<String>("42348-3", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "ADVNACE DIRECTIVES", null);
    }
	
}
