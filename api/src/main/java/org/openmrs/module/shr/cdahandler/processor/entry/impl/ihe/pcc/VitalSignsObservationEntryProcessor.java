package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import org.jfree.util.Log;
import org.marc.everest.datatypes.BL;
import org.marc.everest.datatypes.PQ;
import org.marc.everest.datatypes.generic.CV;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Observation;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;

/**
 * A template processor which can handle vital signs
 */
@ProcessTemplates(
	understands = {
			@TemplateId(root = CdaHandlerConstants.ENT_TEMPLATE_VITAL_SIGNS_OBSERVATION)
	})
public class VitalSignsObservationEntryProcessor extends SimpleObservationEntryProcessor {

	// Allowed codes with allowed units in original text
	private static CV<String>[] s_validCodes = new CV[]{
		new CV<String>("9279-1", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "RESPIRATION RATE", "/min"),
		new CV<String>("8867-4", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "HEART BEAT", "/min"),
		new CV<String>("2710-2", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "OXYGEN SATURATION", "%"),
		new CV<String>("8480-6", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "INTRAVASCULAR SYSTOLIC", "mm[Hg]|mmHg"),
		new CV<String>("8462-4", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "INTRAVASCULAR DIASTOLIC", "mm[Hg]|mmHg"),
		new CV<String>("8310-5", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "BODY TEMPURATURE", "Cel|[degF]"),
		new CV<String>("8302-2", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "BODY HEIGHT (MEASURED)", "m|cm|[in_us]|[in_uk]"),
		new CV<String>("8306-3", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "BODY HEIGHT^LYING", "m|cm|[in_us]|[in_uk]"),
		new CV<String>("8287-5", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "CIRCUMFERENC OCCIPITAL-FRONTAL (TAPE MEASURE)", "m|cm|[in_us]|[in_uk]"),
		new CV<String>("3141-9", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "BODY WEIGHT (MEASURED)", "kg|g|[lb_av]|[oz_av]")
	};
	
	/**
	 * Get the template name
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc.SimpleObservationEntryProcessor#getTemplateName()
	 */
	@Override
    public String getTemplateName() {
		return "Vital Signs Observation";
    }
	
	/**
	 * Validate
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc.SimpleObservationEntryProcessor#validate(org.marc.everest.interfaces.IGraphable)
	 */
	@Override
    public ValidationIssueCollection validate(IGraphable object) {
		ValidationIssueCollection validationIssues = super.validate(object);
		if(validationIssues.hasErrors()) return validationIssues;
		
		Observation observation = (Observation)object;

		// See if the code selected is among the valid codes 
		CV<String> validCode = null;
		for(CV<String> allowed : s_validCodes)
		{
			if(allowed.semanticEquals(observation.getCode()).toBoolean())
			{
				// One : Assign the display name if not present
				if(observation.getCode().getDisplayName() == null)
					observation.getCode().setDisplayName(allowed.getDisplayName());
				// Next: Ensure units match
				validCode = allowed;
			}
		}
		
		// Not valid?
		if(validCode == null)
			validationIssues.error("IHE PCC TF-2: Vital Signs code shall be drawn from PCC TF-2:6.3.4.22.3");
		// Is a PQ?
		if(observation.getValue() instanceof PQ)
		{
			PQ value = (PQ)observation.getValue();
			Boolean validUnit = false;
			for(String unit : validCode.getOriginalText().toSt().toString().split("\\|"))
				validUnit |= unit.equalsIgnoreCase(value.getUnit());
			if(!validUnit)
				validationIssues.error(String.format("Allowed units for %s are %s. Supplied unit %s is incompatible", validCode.getDisplayName(), validCode.getOriginalText().toSt(), value.getUnit()));
		}
		else
			validationIssues.error("IHE PCC TF-2: Vital Signs observation value shall be an instance of PQ");
		
		return validationIssues;
    }
	
}
