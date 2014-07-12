package org.openmrs.module.shr.cdahandler.processor.document.impl;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;

/**
 * Represents a processor that can import PCC Medical Summaries 
 */
@ProcessTemplates
( understands = {
		@TemplateId(root = "1.3.6.1.4.1.19376.1.5.3.1.1.2")
})
public class MedicalSummaryDocumentProcessor extends MedicalDocumentsDocumentProcessor {
	
	/**
	 * Validate
	 * @see org.openmrs.module.shr.cdahandler.processor.document.impl.MedicalDocumentsDocumentProcessor#validate(org.marc.everest.interfaces.IGraphable)
	 */
	@Override
    public Boolean validate(IGraphable object) {
	    Boolean isValid = super.validate(object);
	    if(!isValid) return false;
	    
	    ClinicalDocument doc = (ClinicalDocument)object;
	    if(doc.getComponent().getBodyChoiceIfStructuredBody() == null)
	    {
	    	log.error("Document must have a structuredBody");
	    	isValid = false;
	    }
	    else
	    {
	    	List<String> neededTemplates = Arrays.asList(new String[] {
		    		"1.3.6.1.4.1.19376.1.5.3.1.4.5.2", 
		    		"1.3.6.1.4.1.19376.1.5.3.1.4.5.3",
		    		"1.3.6.1.4.1.19376.1.5.3.1.4.7" 
		    }) ;
	    	
	    	neededTemplates = super.findMissingSections(doc, neededTemplates);
	    	for(String s : neededTemplates)
	    	{
	    		log.warn(String.format("Medical Summary missing required section %s", s));
	    	}
	    	
	    }
	    return isValid;
    }

	/**
	 * Get the name of the template
	 * @see org.openmrs.module.shr.cdahandler.processor.document.impl.MedicalDocumentsDocumentProcessor#getTemplateName()
	 */
	@Override
    public String getTemplateName() {
	    // TODO Auto-generated method stub
	    return "IHE Medical Summary";
    }

	
	
	
}
