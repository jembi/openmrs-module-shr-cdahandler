package org.openmrs.module.shr.cdahandler.processor.entry.impl.ccd;

import java.util.List;

import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.datatypes.generic.CV;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Observation;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.entry.impl.ObservationEntryProcessor;

/**
 * Status observation entry 
 */
@ProcessTemplates(templateIds = { 
		CdaHandlerConstants.ENT_TEMPLATE_CCD_STATUS_OBSERVATION,
		CdaHandlerConstants.ENT_TEMPLATE_CCD_ADVANCE_DIRECTIVE_STATUS
		})
public class StatusObservationEntryProcessor extends ObservationEntryProcessor {
	
	/**
	 * Get the template name
	 * @see org.openmrs.module.shr.cdahandler.processor.Processor#getTemplateName()
	 */
	@Override
	public String getTemplateName() {
		return "Status Observation";
	}
	/**
	 * Get the expected code
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.ObservationEntryProcessor#getExpectedCode()
	 */
	@Override
	protected CE<String> getExpectedCode() {
		return new CE<String>("33999-4", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "Status", null);
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
     * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.ObservationEntryProcessor#validate(org.marc.everest.interfaces.IGraphable)
     */
    @Override
    public ValidationIssueCollection validate(IGraphable object) {
	    ValidationIssueCollection issues = super.validate(object);
	    if(issues.hasErrors())
	    	return issues;
	    
	    if(!(object instanceof Observation))
	    	issues.error("Status observation must be an Observation");
	    else
	    {
	    	Observation obs = (Observation)object;
	    	if(obs.getValue() == null || !(obs.getValue() instanceof CE))
	    		issues.error("Status value must be of type CD");
	    }
	    return issues;
    }
	
	
	
}
