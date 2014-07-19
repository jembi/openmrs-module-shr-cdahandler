package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;

/**
 * Processor for the family history observation
 * 
 * See: PCC TF-2: 6.3.4.57
 */
@ProcessTemplates(
	templateIds = {
			CdaHandlerConstants.ENT_TEMPLATE_FAMILY_HISTORY_OBSERVATION
	})
public class FamilyHistoryObservationEntryProcessor extends SimpleObservationEntryProcessor {

	/**
	 * Get the name of the family history observation
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc.SimpleObservationEntryProcessor#getTemplateName()
	 */
	@Override
    public String getTemplateName() {
	    // TODO Auto-generated method stub
	    return "Family History Observation";
    }
	
	
	
}
