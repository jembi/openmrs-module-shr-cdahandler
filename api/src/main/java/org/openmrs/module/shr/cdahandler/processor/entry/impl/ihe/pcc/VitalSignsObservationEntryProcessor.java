package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import org.jfree.util.Log;
import org.marc.everest.datatypes.BL;
import org.marc.everest.datatypes.PQ;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.datatypes.generic.CV;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Observation;
import org.openmrs.Concept;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsConceptUtil;

/**
 * A template processor which can handle vital signs
 */
@ProcessTemplates(
	process = {
			@TemplateId(root = CdaHandlerConstants.ENT_TEMPLATE_VITAL_SIGNS_OBSERVATION)
	})
public class VitalSignsObservationEntryProcessor extends SimpleObservationEntryProcessor {


	/**
	 * Get the template name
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc.SimpleObservationEntryProcessor#getTemplateName()
	 */
	@Override
    public String getTemplateName() {
		return "Vital Signs Observation";
    }
	
}
