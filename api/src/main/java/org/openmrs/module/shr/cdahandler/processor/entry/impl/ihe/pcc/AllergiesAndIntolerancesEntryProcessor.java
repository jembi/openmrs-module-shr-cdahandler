package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.BL;
import org.marc.everest.datatypes.generic.CD;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Observation;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ActStatus;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;;
/**
 * Represents an observation processor for Allergies and Intolerances section
 */
@ProcessTemplates(
	process={
			@TemplateId(root = CdaHandlerConstants.ENT_TEMPLATE_ALLERGY_AND_INTOLERANCE_OBSERVATION)
	})
public class AllergiesAndIntolerancesEntryProcessor extends SimpleObservationEntryProcessor {

	private static final List<String> s_allowedTypes = Arrays.asList(
		"ALG",
		"OINT",
		"DALG",
		"EALG",
		"FALG",
		"DINT",
		"EINT",
		"FINT",
		"DNAINT",
		"ENAINT",
		"FNAINT"
		);
	
	/**
	 * Get template name
	 * @see org.openmrs.module.shr.cdahandler.processor.Processor#getTemplateName()
	 */
	@Override
    public String getTemplateName() {
		return "Allergies and Intolerances";
    }

	/**
	 * Get the expected code
	 */
	@Override
    protected CE<String> getExpectedCode() {
	    return null;
    }

	/**
	 * Get the expected entry relationships
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.EntryProcessorImpl#getExpectedEntryRelationships()
	 */
	@Override
    protected List<String> getExpectedEntryRelationships() {
		return null;
    }

	/**
	 * Validate this message
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.ObservationEntryProcessor#validate(org.marc.everest.interfaces.IGraphable)
	 */
	@Override
    public ValidationIssueCollection validate(IGraphable object) {
		ValidationIssueCollection validationIssues = super.validate(object);
		if(validationIssues.hasErrors())
			return validationIssues;
		
		Observation observation = (Observation)object;
		if(observation.getNegationInd() != null && !observation.getNegationInd().equals(BL.FALSE))
			validationIssues.error("Negation on Allergy is not supported");
		if(observation.getCode() == null || observation.getCode().isNull() ||
				!observation.getCode().getCodeSystem().equals(CdaHandlerConstants.CODE_SYSTEM_ACT_CODE) ||
				!s_allowedTypes.contains(observation.getCode().getCode()))
			validationIssues.error("Allergy and intolerances section must carry code from ObservationIntoleranceType");
		if(observation.getStatusCode().getCode() != ActStatus.Completed)
			validationIssues.error("Status code must carry value 'completed'");
		if(!(observation.getValue() instanceof CD))
			validationIssues.error("Value must be of type CD");
		
		return validationIssues;
    }

	/**
	 * Special processing of the allergy observation
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.ObservationEntryProcessor#process(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement)
	 */
	@Override
    public BaseOpenmrsData process(ClinicalStatement entry) throws DocumentImportException {
	    return super.process(entry);
    }
	
	
}
