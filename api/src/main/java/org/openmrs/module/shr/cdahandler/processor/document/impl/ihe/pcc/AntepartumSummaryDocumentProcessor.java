package org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;

/**
 * Represents an APS document processor
 */
@ProcessTemplates(templateIds = { CdaHandlerConstants.DOC_TEMPLATE_ANTEPARTUM_SUMMARY})
public class AntepartumSummaryDocumentProcessor extends MedicalSummaryDocumentProcessor {
	/**
	 * Get the expected sections
	 * @see org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc.HistoryAndPhysicalDocumentProcessor#getExpectedSections()
	 */
	@Override
    protected List<String> getExpectedSections() {
			List<String> retVal = new ArrayList<String>(super.getExpectedSections());
			retVal.addAll(Arrays.asList(
				CdaHandlerConstants.SCT_TEMPLATE_ESTIMATED_DELIVERY_DATES,
				CdaHandlerConstants.SCT_TEMPLATE_ANTEPARTUM_TEMPLATE_VISIT_SUMMARY_FLOWSHEET,
				CdaHandlerConstants.SCT_TEMPLATE_HISTORY_OF_SURGICAL_PROCEDURES,
				CdaHandlerConstants.SCT_TEMPLATE_CODED_ANTENATAL_TESTING_AND_SURVEILLANCE,
				CdaHandlerConstants.SCT_TEMPLATE_ALLERGIES,
				CdaHandlerConstants.SCT_TEMPLATE_MEDICATIONS,
				CdaHandlerConstants.SCT_TEMPLATE_CARE_PLAN,
				CdaHandlerConstants.SCT_TEMPLATE_ADVANCE_DIRECTIVES,
				CdaHandlerConstants.SCT_TEMPLATE_ACTIVE_PROBLEMS
					));   
			return retVal;
	}

	/**
	 * Get template name
	 */
	@Override
    public String getTemplateName() {
		return "Antepartum Summary";
    }

	
	/**
	 * Get expected code
	 * @see org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc.MedicalSummaryDocumentProcessor#getExpectedCode()
	 */
	@Override
    protected CE<String> getExpectedCode() {
		return new CE<String>("57055-6", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "ANTEPARTUM SUMMARY NOTE", null);
    }

	/**
	 * Validate (Includes several header templates)
	 * @see org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc.MedicalSummaryDocumentProcessor#validate(org.marc.everest.interfaces.IGraphable)
	 */
	@Override
    public ValidationIssueCollection validate(IGraphable object) {
		
		// Validate there is a spouse in the participant section
		ValidationIssueCollection validationIssues = super.validate(object);
	    if(validationIssues.hasErrors()) return validationIssues;
	    
	    ClinicalDocument doc = (ClinicalDocument)object;
	    return validationIssues;
    }
	
}
