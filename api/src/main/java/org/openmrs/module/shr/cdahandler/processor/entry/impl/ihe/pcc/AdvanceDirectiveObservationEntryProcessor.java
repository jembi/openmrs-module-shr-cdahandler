package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import java.util.List;

import org.marc.everest.datatypes.BL;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Observation;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.entry.impl.ObservationEntryProcessor;

/**
 * Advance directive observation entry processor
 */
@ProcessTemplates(templateIds = { 
		CdaHandlerConstants.ENT_TEMPLATE_ADVANCE_DIRECTIVE_OBSERVATION
	})
public class AdvanceDirectiveObservationEntryProcessor extends SimpleObservationEntryProcessor {

	/**
	 * Get the template name
	 * Auto generated method comment
	 * 
	 * @return
	 */
	@Override
	public String getTemplateName() {
		return "Advance Directive Observation";
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
	    	issues.error("Advance directive must be an observation");
	    else
	    {
	    	Observation obs = (Observation)object;
	    	if(obs.getCode() == null || !CdaHandlerConstants.CODE_SYSTEM_SNOMED.equals(obs.getCode().getCodeSystem()))
	    		issues.error("Advance directive observation 'code' element must be drawn from SNOMED-CT");
	    		
	    	if("71388002".equals(obs.getCode().getCode()))
	    	{
	    		if(obs.getValue() != null) // Other directive, we'll mark it as TRUE
	    			issues.error("When 'other directive' is specified the use of the 'value' is prohibited");
	    		else
	    			obs.setValue(BL.TRUE);
	    	}
	    	else
	    		if(!(obs.getValue() instanceof BL))
	    			issues.error("Advance directive's value must be a BL");
	    }
	    return issues;
    }

	
}
