package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import java.util.List;

import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Observation;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Organizer;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.entry.impl.ObservationEntryProcessor;

/**
 * Simple observation processor
 */
@ProcessTemplates (
	templateIds = {
			CdaHandlerConstants.ENT_TEMPLATE_SIMPLE_OBSERVATION,
			CdaHandlerConstants.ENT_TEMPLATE_PROBLEM_OBSERVATION
	})
public class SimpleObservationEntryProcessor extends ObservationEntryProcessor {
	
	/**
	 * Gets the concept code from which values and concepts for this observation may be drawn
	 * Auto generated method comment
	 * 
	 * @return
	 */
	protected CE<String> getConceptSetCode()
	{
		IGraphable context = this.getContext().getRawObject();
		if(context instanceof Section)
			return ((Section)context).getCode();
		else if(context instanceof Organizer)
			return ((Organizer)context).getCode();
		else
			return null;
	}

	
	/**
	 * Get expected code
	 */
	@Override
    protected CE<String> getExpectedCode() {
		return null;
    }


	/**
	 * Get expected entry relationships
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.EntryProcessorImpl#getExpectedEntryRelationships()
	 */
	@Override
    protected List<String> getExpectedEntryRelationships() {
	    // TODO Auto-generated method stub
	    return null;
    }
	
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
		Observation observation = (Observation)object;
		if(observation.getId() == null || observation.getId().isEmpty())
			validationIssues.warn("IHE PCC TF-2: Each observation shall have an identifier");
		if(observation.getCode() == null || observation.getCode().isNull())
			validationIssues.error("IHE PCC TF-2: Observations shall have a code describing what is measured");
		if(observation.getStatusCode() == null)
			validationIssues.warn("IHE PCC TF-2: Observations shall have a status of completed");
		if(observation.getValue() == null)
			validationIssues.error("IHE PCC TF-2: Observation shall have a value appropriate with the observation type");
		if(observation.getEffectiveTime() == null || observation.getEffectiveTime().isNull())
			validationIssues.error("IHE PCC TF-2: Observations shall have an effective time");

		// Validate .. First we get the code from the organizer/container and figure out if this is an allowed value within its container
		CE<String> conceptGroupCode = this.getConceptSetCode();
		if(conceptGroupCode != null && this.m_configuration.getValidateConceptStructure())
	        super.validateConceptWithContainer(conceptGroupCode, observation, validationIssues);
		return validationIssues;
    }
	
}
