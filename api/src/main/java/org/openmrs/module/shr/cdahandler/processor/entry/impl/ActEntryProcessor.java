package org.openmrs.module.shr.cdahandler.processor.entry.impl;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Act;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;

/**
 * The generic processor for Acts
 * 
 * Acts don't appear to have a natural home in OpenMRS however certain
 * types of Acts can be processed such as problems or allergies. Therefore
 * this implementation is pretty light
 */
public abstract class ActEntryProcessor extends EntryProcessorImpl {
	
	private final static List<String> s_expectedStates = Arrays.asList("active", "suspended", "aborted", "completed");
	
	/**
	 * Validate the observation
	 */
	@Override
    public ValidationIssueCollection validate(IGraphable object) {
		
		ValidationIssueCollection validationIssues = super.validate(object);
		if(validationIssues.hasErrors()) return validationIssues;
		
		// Validate now
		ClinicalStatement statement = (ClinicalStatement)object;
		// Must be an observation
		if(!statement.isPOCD_MT000040UVAct())
			validationIssues.error(super.getInvalidClinicalStatementErrorText(Act.class, statement.getClass()));

		Act act = (Act)statement;
		
		if(act.getId() == null || act.getId().isNull() || act.getId().isEmpty())
			validationIssues.error("Act is missing identifier");
		if(act.getStatusCode() == null || act.getStatusCode().isNull())
			validationIssues.error("Act must carry a statusCode");
		else if(!s_expectedStates.contains(act.getStatusCode().getCode().getCode()))
			validationIssues.error("Act statusCode must be one of : {active, suspended, aborted, completed}");
		

		return validationIssues;
	}
	
	

}
