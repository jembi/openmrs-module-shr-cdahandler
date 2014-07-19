package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import java.util.List;

import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Observation;
import org.openmrs.Concept;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.Processor;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;
import org.openmrs.module.shr.cdahandler.processor.entry.impl.ObservationEntryProcessor;
import org.openmrs.module.shr.cdahandler.processor.entry.impl.OrganizerEntryProcessor;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor;

/**
 * Simple observation processor
 */
@ProcessTemplates (
	process = {
			@TemplateId(root = CdaHandlerConstants.ENT_TEMPLATE_SIMPLE_OBSERVATION),
			@TemplateId(root = CdaHandlerConstants.ENT_TEMPLATE_PROBLEM_OBSERVATION)
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
		{
			try {
		        Concept conceptGroup = this.m_conceptUtil.getConcept(conceptGroupCode),
		        		codedObservationConcept = this.m_conceptUtil.getTypeSpecificConcept(observation.getCode(), observation.getValue());

		        // If we're not validating concept structure...
		        /*
		        if(!this.m_configuration.getValidateConceptStructure() && conceptGroup == null)
		        	conceptGroup = this.m_conceptUtil.createConcept(conceptGroupCode);
		        if(!this.m_configuration.getValidateConceptStructure() && codedObservationConcept == null)
		        	codedObservationConcept = this.m_conceptUtil.createConcept(observation.getCode(), observation.getValue());
		        */
		        
		        // First check, is the coded observation concept understood by OpenMRS?
	        	// Sometimes N/A applies for boolean as well as the observation is an indicator (i.e. the presence of this value
		        // indicates true for the question concept)
		        if(conceptGroup == null || codedObservationConcept == null)
		        {
 		        	validationIssues.error(String.format("The observation concept %s is not understood or is not registered for this value of type %s type (hint: units may be incompatible)", observation.getCode(), observation.getValue().getClass()));
		        }
		        // Next check that the concept is a valid pregnancy concept
	        	else if(!conceptGroup.getSetMembers().contains(codedObservationConcept))
	        	{
	        		StringBuilder allowedConcepts = new StringBuilder();
	        		for(Concept gm : conceptGroup.getSetMembers())
	        		{
	        			allowedConcepts.append(gm.toString());
	        			allowedConcepts.append(" ");
	        		}
	        		if(!this.m_configuration.getValidateConceptStructure())
			        	this.m_conceptUtil.addConceptToSet(conceptGroup, codedObservationConcept);
	        		else
	        			validationIssues.error(String.format("The code %s is not understood to be a valid concept according to the allowed values of %s = (%s)", codedObservationConcept, conceptGroup, allowedConcepts));
	        	}
	        }
	        catch (DocumentImportException e) {
	        	validationIssues.error(e.getMessage());
	        }
		}
		return validationIssues;
    }


	/**
	 * Get expected code
	 */
	@Override
    protected CE<String> getExpectedCode() {
		return null;
    }
	
	/**
	 * Gets the concept code from which values and concepts for this observation may be drawn
	 * Auto generated method comment
	 * 
	 * @return
	 */
	protected CE<String> getConceptSetCode()
	{
		Processor context = this.getContext().getProcessor();
		if(context instanceof GenericLevel2SectionProcessor)
			return ((GenericLevel2SectionProcessor)this.getContext().getProcessor()).getExpectedSectionCode();
		else if(context instanceof OrganizerEntryProcessor)
			return ((OrganizerEntryProcessor)this.getContext().getProcessor()).getExpectedCode();
		else
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
	
}
