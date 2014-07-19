package org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;

/**
 * Represents a processor for APHP
 */
@ProcessTemplates(
	templateIds = {
			CdaHandlerConstants.DOC_TEMPLATE_ANTEPARTUM_HISTORY_AND_PHYSICAL
	})
public class AntepartumHistoryAndPhysicalDocumentProcessor extends HistoryAndPhysicalDocumentProcessor {

	/**
	 * Get template name
	 */
	@Override
    public String getTemplateName() {
		return "Antepartum History & Physical";
    }

	/**
	 * Get the expected sections
	 * @see org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc.HistoryAndPhysicalDocumentProcessor#getExpectedSections()
	 */
	@Override
    protected List<String> getExpectedSections() {
			List<String> retVal = new ArrayList<String>(super.getExpectedSections());
			retVal.addAll(Arrays.asList(
				CdaHandlerConstants.SCT_TEMPLATE_CODED_HISTORY_OF_INFECTION,
				CdaHandlerConstants.SCT_TEMPLATE_PREGNANCY_HISTORY,
				CdaHandlerConstants.SCT_TEMPLATE_CODED_SOCIAL_HISTORY,
				CdaHandlerConstants.SCT_TEMPLATE_CODED_FAMILY_MEDICAL_HISTORY,
				CdaHandlerConstants.SCT_TEMPLATE_CODED_PHYISCAL_EXAM,
				CdaHandlerConstants.SCT_TEMPLATE_HISTORY_OF_SURGICAL_PROCEDURES
					));   
			return retVal;
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
