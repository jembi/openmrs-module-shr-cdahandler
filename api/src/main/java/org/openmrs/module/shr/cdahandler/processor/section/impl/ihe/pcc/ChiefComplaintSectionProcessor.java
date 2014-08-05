package org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc;

import org.marc.everest.datatypes.generic.CE;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor;

/**
 * Template handler for Chief Complaint Section
 * 
 * From PCC:
 * This contains a narrative description of the patient's chief complaint
 * 
 * Constraints: PCC TF-2:6.3.3.1.3
 * 
 */
@ProcessTemplates(
	templateIds = {
			CdaHandlerConstants.SCT_TEMPLATE_CHIEF_COMPLAINT
	})
public class ChiefComplaintSectionProcessor extends GenericLevel2SectionProcessor {

	
	/**
	 * Get expected section code of 10154-3
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#getExpectedSectionCode(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section)
	 */
	@Override
    public CE<String> getExpectedSectionCode() {
		return new CE<String>("10154-3", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "CHIEF COMPLAINT", null);
    }

	/**
	 * Cheif complaint
	 */
	@Override
    public String getTemplateName() {
	    // TODO Auto-generated method stub
	    return "Cheif Complaint";
    }
	
	
}
