package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import java.text.ParseException;

import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.datatypes.generic.CV;
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
import org.openmrs.module.shr.cdahandler.processor.Processor;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;
import org.openmrs.module.shr.cdahandler.processor.entry.impl.EntryProcessorImpl;
import org.openmrs.module.shr.cdahandler.processor.entry.impl.ObservationEntryProcessor;
import org.openmrs.module.shr.cdahandler.processor.entry.impl.OrganizerEntryProcessor;
import org.openmrs.module.shr.cdahandler.processor.section.SectionProcessor;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor;
import org.openmrs.module.shr.cdahandler.processor.util.DatatypeProcessorUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsConceptUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsDataUtil;

/**
 * Simple observation processor
 */
@ProcessTemplates (
	process = {
			@TemplateId(root = CdaHandlerConstants.ENT_TEMPLATE_SIMPLE_OBSERVATION)
	})
public class SimpleObservationEntryProcessor extends ObservationEntryProcessor {
	
	// Changes the validation behavior to auto-create concepts
	private static Boolean s_isInTestMode = null;

	/**
	 * Static initializer
	 */
	public SimpleObservationEntryProcessor() {
		// HACK: To allow for test mode
		if(s_isInTestMode == null)
		{
			String value = Context.getAdministrationService().getGlobalProperty(CdaHandlerConstants.PROP_TEST_MODE);
			if(value != null && !value.isEmpty())
				s_isInTestMode = Boolean.parseBoolean(value);
			else
			{
				Context.getAdministrationService().setGlobalProperty(CdaHandlerConstants.PROP_TEST_MODE, "false");
				s_isInTestMode = false;
			}
		}
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
			validationIssues.error("IHE PCC TF-2: Each observation shall have an identifier");
		if(observation.getCode() == null || observation.getCode().isNull())
			validationIssues.error("IHE PCC TF-2: Observations shall have a code describing what is measured");
		if(observation.getStatusCode() == null)
			validationIssues.warn("IHE PCC TF-2: Observations shall have a status of completed");
		if(observation.getValue() == null)
			validationIssues.error("IHE PCC TF-2: Observation shall have a value appropriate with the observation type");
		if(observation.getEffectiveTime() == null || observation.getEffectiveTime().getValue() == null)
			validationIssues.error("IHE PCC TF-2: Observations shall have an effective time");

		// Validate .. First we get the pregnancy history code from the organizer
		CE<String> conceptGroupCode = this.getConceptSetCode();
		if(conceptGroupCode != null)
		{
			OpenmrsConceptUtil conceptUtil = OpenmrsConceptUtil.getInstance();
			try {
		        Concept conceptGroup = conceptUtil.getConcept(conceptGroupCode),
		        		codedObservationConcept = conceptUtil.getTypeSpecificConcept(observation.getCode(), observation.getValue());

		        // If we're in test mode forgive this and create the codes
		        if(s_isInTestMode && conceptGroup == null)
		        	conceptGroup = conceptUtil.createConcept(conceptGroupCode);
		        if(s_isInTestMode && codedObservationConcept == null)
		        {
		        	codedObservationConcept = conceptUtil.createConcept(observation.getCode(), observation.getValue());
		        	conceptUtil.addConceptToSet(conceptGroup, codedObservationConcept);
		        }
		        
		        
		        // First check, is the coded observation concept understood by OpenMRS?
		        if(conceptGroup == null || codedObservationConcept == null)
		        	validationIssues.error(String.format("The observation concept %s is not understood or is not registered for this value of type %s type (hint: units may be incompatible)", observation.getCode(), observation.getValue().getClass()));
		        // Next check that the concept is a valid pregnancy concept
	        	else if(!conceptGroup.getSetMembers().contains(codedObservationConcept))
	        	{
	        		StringBuilder allowedConcepts = new StringBuilder();
	        		for(Concept gm : conceptGroup.getSetMembers())
	        		{
	        			allowedConcepts.append(gm.toString());
	        			allowedConcepts.append(" ");
	        		}
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
	
}
