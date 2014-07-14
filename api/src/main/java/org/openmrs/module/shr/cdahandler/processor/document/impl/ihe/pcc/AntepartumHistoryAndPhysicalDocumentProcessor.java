package org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.openmrs.module.shr.cdahandler.CdaHandlerOids;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;

/**
 * Represents a processor for APHP
 */
@ProcessTemplates(
	understands = {
			@TemplateId(root = CdaHandlerOids.DOC_TEMPLATE_ANTEPARTUM_HISTORY_AND_PHYSICAL)
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
				CdaHandlerOids.SCT_TEMPLATE_CODED_HISTORY_OF_INFECTION,
				CdaHandlerOids.SCT_TEMPLATE_PREGNANCY_HISTORY,
				CdaHandlerOids.SCT_TEMPLATE_CODED_SOCIAL_HISTORY,
				CdaHandlerOids.SCT_TEMPLATE_CODED_FAMILY_MEDICAL_HISTORY,
				CdaHandlerOids.SCT_TEMPLATE_CODED_PHYISCAL_EXAM,
				CdaHandlerOids.SCT_TEMPLATE_HISTORY_OF_SURGICAL_PROCEDURES
					));   
			return retVal;
	}


	/**
	 * Validate (Includes several header templates)
	 * @see org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc.MedicalSummaryDocumentProcessor#validate(org.marc.everest.interfaces.IGraphable)
	 */
	@Override
    public Boolean validate(IGraphable object) {
		
		// Validate there is a spouse in the participant section
	    Boolean isValid = super.validate(object);
	    if(!isValid) return false;
	    
	    ClinicalDocument doc = (ClinicalDocument)object;
	    return true;
    }
	
	
	
}
