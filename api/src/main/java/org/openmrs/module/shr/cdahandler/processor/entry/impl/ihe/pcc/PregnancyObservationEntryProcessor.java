package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Observation;
import org.openmrs.Concept;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.entry.impl.ObservationEntryProcessor;
import org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc.PregnancyHistorySectionProcessor;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsConceptUtil;

/**
 * Pregnancy observation entry processor
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
