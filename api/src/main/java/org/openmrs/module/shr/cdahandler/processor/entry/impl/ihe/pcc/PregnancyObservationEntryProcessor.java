package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;


/**
 * Pregnancy observation entry processor
 * 
 * See: PCC TF-2:6.3.4.25
 */
@ProcessTemplates(templateIds = { CdaHandlerConstants.ENT_TEMPLATE_PREGNANCY_OBSERVATION})
public class PregnancyObservationEntryProcessor extends SimpleObservationEntryProcessor {
	
	/**
	 * Gets the name of the template handler
	 */
	@Override
	public String getTemplateName() {
		return "Pregnancy Observation";
	}
	
}
