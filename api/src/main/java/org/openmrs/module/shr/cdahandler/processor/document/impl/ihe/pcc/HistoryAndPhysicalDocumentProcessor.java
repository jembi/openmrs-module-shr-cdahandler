package org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.generic.CE;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;

/**
 * A history and physical document processor
 */
@ProcessTemplates(
	templateIds = {
			CdaHandlerConstants.DOC_TEMPLATE_HISTORY_PHYSICAL
	})

public class HistoryAndPhysicalDocumentProcessor extends MedicalSummaryDocumentProcessor {


	/**
	 * Get template name
	 * @see org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc.MedicalSummaryDocumentProcessor#getTemplateName()
	 */
	@Override
    public String getTemplateName() {
		return "History & Physical";
    }

	/**
	 * Get expected sections
	 * @see org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc.MedicalSummaryDocumentProcessor#getExpectedSections()
	 */
	@Override
    protected List<String> getExpectedSections() {
		return Arrays.asList(
			CdaHandlerConstants.SCT_TEMPLATE_CHIEF_COMPLAINT,
			CdaHandlerConstants.SCT_TEMPLATE_HISTORY_OF_PRESENT_ILLNESS,
			CdaHandlerConstants.SCT_TEMPLATE_HISTORY_OF_PAST_ILLNESS,
			CdaHandlerConstants.SCT_TEMPLATE_MEDICATIONS,
			CdaHandlerConstants.SCT_TEMPLATE_ALLERGIES,
			CdaHandlerConstants.SCT_TEMPLATE_SOCIAL_HISTORY,
			CdaHandlerConstants.SCT_TEMPLATE_FAMILY_HISTORY,
			CdaHandlerConstants.SCT_TEMPLATE_REVIEW_OF_SYSTEMS,
			CdaHandlerConstants.SCT_TEMPLATE_DETAILED_PHYSICAL_EXAM,
			CdaHandlerConstants.SCT_TEMPLATE_CODED_RESULTS,
			CdaHandlerConstants.SCT_TEMPLATE_ASSESSMENT_AND_PLAN
				);
    }

	/**
	 * Get the expected code
	 */
	@Override
    protected CE<String> getExpectedCode() {
		return new CE<String>("34117-2", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "HISTORY AND PHYSICAL", null);
    }
	
	
	
}
