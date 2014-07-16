package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import java.text.ParseException;

import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Observation;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ActStatus;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ObservationInterpretation;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.DocumentValidationException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;
import org.openmrs.module.shr.cdahandler.processor.entry.impl.EntryProcessorImpl;
import org.openmrs.module.shr.cdahandler.processor.entry.impl.ObservationEntryProcessor;
import org.openmrs.module.shr.cdahandler.processor.util.DatatypeProcessorUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsConceptUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsDataUtil;

/**
 * Simple observation processor
 */
@ProcessTemplates (
	understands = {
			@TemplateId(root = CdaHandlerConstants.ENT_TEMPLATE_SIMPLE_OBSERVATION)
	})
public class SimpleObservationEntryProcessor extends ObservationEntryProcessor {
	
	/**
	 * Gets the template name
	 * @see org.openmrs.module.shr.cdahandler.processor.Processor#getTemplateName()
	 */
	@Override
	public String getTemplateName() {
		return "Simple Observations";
	}

	
	/**
	 * Validate this entry
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.EntryProcessorImpl#validate(org.marc.everest.interfaces.IGraphable)
	 */
	@Override
    public ValidationIssueCollection validate(IGraphable object) {
		ValidationIssueCollection validationIssues = super.validate(object);
		if(validationIssues.hasErrors()) return validationIssues;
		
		// Must have a code
		Observation obs = (Observation)object;
		if(obs.getId() == null || obs.getId().isEmpty())
			validationIssues.error("IHE PCC TF-2: Each observation shall have an identifier");
		if(obs.getCode() == null || obs.getCode().isNull())
			validationIssues.error("IHE PCC TF-2: Observations shall have a code describing what is measured");
		if(obs.getStatusCode() == null)
			validationIssues.warn("IHE PCC TF-2: Observations shall have a status of completed");
		if(obs.getValue() == null)
			validationIssues.error("IHE PCC TF-2: Observation shall have a value appropriate with the observation type");
		if(obs.getEffectiveTime() == null || obs.getEffectiveTime().getValue() == null)
			validationIssues.error("IHE PCC TF-2: Observations shall have an effective time");
		return validationIssues;
    }


	/**
	 * Get expected code
	 */
	@Override
    protected CE<String> getExpectedCode() {
		return null;
    }
	
	
	
}
