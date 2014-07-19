package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;


/**
 * Pregnancy observation entry processor
 * 
 * See: PCC TF-2:6.3.4.25
 */
public class PregnancyObservationEntryProcessor extends SimpleObservationEntryProcessor {
	
	/**
	 * Gets the name of the template handler
	 */
	@Override
	public String getTemplateName() {
		return "Pregnancy Observation";
	}
	
}
