package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.generic.CD;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Observation;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;

/**
 * A class that processes severity observations
 * 
 *  See: PCC TF-2:6.3.4.3
 */
@ProcessTemplates(
	process={
			@TemplateId(root = CdaHandlerConstants.ENT_TEMPLATE_SEVERITY_OBSERVATION)
	})
public class SeverityObservationEntryProcessor extends SimpleObservationEntryProcessor {

	// Allowed codes
	private static final List<String> s_allowedCodes = Arrays.asList("H","M","L"); 
	/**
	 * Get template name
	 */
	@Override
    public String getTemplateName() {
		return "Severity Observation";
    }

	/**
	 * Validate (value mostly)
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc.SimpleObservationEntryProcessor#validate(org.marc.everest.interfaces.IGraphable)
	 */
	@Override
    public ValidationIssueCollection validate(IGraphable object) {
		
		ValidationIssueCollection validationIssues = super.validate(object);
		if(validationIssues.hasErrors())
			return validationIssues;
		
		Observation obs = (Observation)object;
		if(!(obs.getValue() instanceof CD))
			validationIssues.error("Severity observation value must be CD");
		else
		{
			CD<String> obsValue = (CD<String>)obs.getValue();
			if(!obsValue.getCodeSystem().equals("2.16.840.1.113883.5.1063") ||
					!s_allowedCodes.contains( obsValue.getCode()))
				validationIssues.error("Value of severity observaton shall be drawn from SeverityObservation code system");
		}

		return validationIssues;
		
    }

	@Override
    protected CE<String> getExpectedCode() {
	    // TODO Auto-generated method stub
	    return new CE<String>("SEV", "2.16.840.1.113883.5.4", "ActCode", null, "Severity", null);
    }
	
	
}
