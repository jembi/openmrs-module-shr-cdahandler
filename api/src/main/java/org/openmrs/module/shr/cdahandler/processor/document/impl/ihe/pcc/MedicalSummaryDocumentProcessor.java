package org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Component3;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;

/**
 * Represents a processor that can import PCC Medical Summaries 
 */
@ProcessTemplates
( templateIds = {
		CdaHandlerConstants.DOC_TEMPLATE_MEDICAL_SUMMARY
})
public class MedicalSummaryDocumentProcessor extends MedicalDocumentsDocumentProcessor {
	
	/**
	 * Gets the code expected on the document
	 */
	protected CE<String> getExpectedCode() {
	    return null;
    }

	/**
	 * Gets the sections expected to be present in this document
	 */
	protected List<String> getExpectedSections()
	{
		return Arrays.asList(new String[] {
	    		CdaHandlerConstants.SCT_TEMPLATE_MEDICATIONS 
	    }) ;
	}

	/**
	 * Get the name of the template
	 * @see org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc.MedicalDocumentsDocumentProcessor#getTemplateName()
	 */
	@Override
    public String getTemplateName() {
	    // TODO Auto-generated method stub
	    return "Medical Summary";
    }

	/**
	 * Validate
	 * @see org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc.MedicalDocumentsDocumentProcessor#validate(org.marc.everest.interfaces.IGraphable)
	 */
	@Override
    public ValidationIssueCollection validate(IGraphable object) {
		ValidationIssueCollection validationIssues = super.validate(object);
	    if(validationIssues.hasErrors()) return validationIssues;
	    
	    ClinicalDocument doc = (ClinicalDocument)object;
	    if(doc.getComponent().getBodyChoiceIfStructuredBody() == null)
	    	validationIssues.error("Document must have a structuredBody");
	    else
	    {
	    	List<String> neededTemplates = new ArrayList<String>(this.getExpectedSections());
			
	    	// Find all missing required sections
	    	for(Component3 comp : doc.getComponent().getBodyChoiceIfStructuredBody().getComponent())
	    	{
	    		if(comp == null || comp.getNullFlavor() != null || 
	    				comp.getSection() == null || comp.getSection().getNullFlavor() != null)
	    			validationIssues.error("Each component must have a section");
	    		else
	    			for(II templateId : comp.getSection().getTemplateId())
	    				neededTemplates.remove(templateId.getRoot());
	    	}

	    	// Output errors
	    	for(String s : neededTemplates)
	    		validationIssues.warn(String.format("%s missing required section %s", this.getTemplateName(), s));
	    	
	    }
	    
	    // Expected code
	    CE<String> expectedCode = this.getExpectedCode();
	    // Assert code
	    if(expectedCode != null &&
	    		(doc.getCode() == null || 
	    		doc.getCode().isNull() ||
	    		!doc.getCode().semanticEquals(expectedCode).toBoolean()))
	    	validationIssues.error(String.format("Template %s must carry code of %s in code system %s", this.getTemplateName(), expectedCode.getCode(), expectedCode.getCodeSystem()));
		else if(doc.getCode().getDisplayName() == null)
			doc.getCode().setDisplayName(expectedCode.getDisplayName());
	    
	    return validationIssues;
    }

}
