package org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.generic.CE;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;

/**
 * Represents a document processor that can import an ImmunizationConent (IC)
 */
@ProcessTemplates(templateIds = { CdaHandlerConstants.DOC_TEMPLATE_IMMUNIZATION_CONTENT })
public class ImmunizationContentDocumentProcessor extends MedicalSummaryDocumentProcessor {

	/**
     * @see org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc.MedicalSummaryDocumentProcessor#getExpectedCode()
     */
    @Override
    protected CE<String> getExpectedCode() {
	    return new CE<String>("11369-6", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "HISTORY OF IMMUNIZATIONS", null);
    }

	/**
     * @see org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc.MedicalSummaryDocumentProcessor#getExpectedSections()
     */
    @Override
    protected List<String> getExpectedSections() {
    	return Arrays.asList(
    		CdaHandlerConstants.SCT_TEMPLATE_IMMUNIZATIONS, 
    		CdaHandlerConstants.SCT_TEMPLATE_ACTIVE_PROBLEMS, 
    		CdaHandlerConstants.SCT_TEMPLATE_HISTORY_OF_PAST_ILLNESS, 
    		CdaHandlerConstants.SCT_TEMPLATE_ALLERGIES, 
    		CdaHandlerConstants.SCT_TEMPLATE_MEDICATIONS, 
    		CdaHandlerConstants.SCT_TEMPLATE_CODED_RESULTS, 
    		CdaHandlerConstants.SCT_TEMPLATE_CODED_VITAL_SIGNS, 
    		CdaHandlerConstants.SCT_TEMPLATE_PREGNANCY_HISTORY, 
    		CdaHandlerConstants.SCT_TEMPLATE_ADVANCE_DIRECTIVES);
    }

	/**
     * @see org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc.MedicalSummaryDocumentProcessor#getTemplateName()
     */
    @Override
    public String getTemplateName() {
	    return "Immunization Content";
    }

	
}
