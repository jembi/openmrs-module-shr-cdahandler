package org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.generic.CE;
import org.openmrs.module.shr.cdahandler.CdaHandlerOids;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;

/**
 * A history and physical document processor
 */
@ProcessTemplates(
	understands = {
			@TemplateId(root = CdaHandlerOids.DOC_TEMPLATE_HISTORY_PHYSICAL)
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
			CdaHandlerOids.SCT_TEMPLATE_CHIEF_COMPLAINT,
			CdaHandlerOids.SCT_TEMPLATE_HISTORY_OF_PRESENT_ILLNESS,
			CdaHandlerOids.SCT_TEMPLATE_HISTORY_OF_PAST_ILLNESS,
			CdaHandlerOids.SCT_TEMPLATE_MEDICATIONS,
			CdaHandlerOids.SCT_TEMPLATE_ALLERGIES,
			CdaHandlerOids.SCT_TEMPLATE_SOCIAL_HISTORY,
			CdaHandlerOids.SCT_TEMPLATE_FAMILY_HISTORY,
			CdaHandlerOids.SCT_TEMPLATE_REVIEW_OF_SYSTEMS,
			CdaHandlerOids.SCT_TEMPLATE_DETAILED_PHYSICAL_EXAM,
			CdaHandlerOids.SCT_TEMPLATE_CODED_RESULTS,
			CdaHandlerOids.SCT_TEMPLATE_ASSESSMENT_AND_PLAN
				);
    }

	/**
	 * Get the expected code
	 */
	@Override
    protected CE<String> getExpectedCode() {
		return new CE<String>("34117-2", CdaHandlerOids.CODE_SYSTEM_LOINC, "LOINC", null, "HISTORY AND PHYSICAL", null);
    }
	
	
	
}
