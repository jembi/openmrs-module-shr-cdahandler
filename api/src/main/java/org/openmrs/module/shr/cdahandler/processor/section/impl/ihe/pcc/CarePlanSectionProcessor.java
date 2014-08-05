package org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc;

import org.marc.everest.datatypes.generic.CE;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor;

/**
 * Processor for Care Plan Section
 * 
 * From PCC:
 * The care plan section shall contain a narrative description of the expectations for care including proposals, goals, and order requests for monitoring, tracking, or improving the condition of the patient. 
 * 
 * See: PCC TF-2: 6.3.3.6.1
 */
public class CarePlanSectionProcessor extends GenericLevel2SectionProcessor {

	/**
	 * Get the expected section code
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#getExpectedSectionCode()
	 */
	@Override
    public CE<String> getExpectedSectionCode() {
		return new CE<String>("61145-9", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "PATIENT PLAN OF CARE", null);
    }

	/**
	 * Get template name
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#getTemplateName()
	 */
	@Override
    public String getTemplateName() {
		return "Care Plan";
    }
	
}
