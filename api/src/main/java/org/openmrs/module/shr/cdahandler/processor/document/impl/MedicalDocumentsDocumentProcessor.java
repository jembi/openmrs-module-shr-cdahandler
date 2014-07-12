package org.openmrs.module.shr.cdahandler.processor.document.impl;

import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.TS;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Author;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Patient;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.RecordTarget;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;

/**
 * Represents a processor that can import PCC Medical Documents 
 */
@ProcessTemplates
( understands = {
		@TemplateId(root = "1.3.6.1.4.1.19376.1.5.3.1.1.1")
})
public class MedicalDocumentsDocumentProcessor extends DocumentProcessorImpl {

	/**
	 * Validate those constraints which are required to ensure successful processing
	 * of this document
	 */
	@Override
    public Boolean validate(IGraphable object) {
	    Boolean isValid = super.validate(object);
	    if(!isValid) return false;
	    
	    ClinicalDocument doc = (ClinicalDocument)object;
	    // CONF-HP-1 
	    if(!doc.getTemplateId().contains(new II("2.16.840.1.113883.10.20.3")))
	    	log.warn("CONF-HP-1 : Documents SHALL indicate conformance to template 2.16.840.1.113883.10.20.3");
	    // CONF-HP-16
	    if(!doc.getTypeId().getExtension().equals(("POCD_HD000040")))
    		log.warn("CONF-HP-16 : Document shall have TypeID with extension of POCD_HD000040");
	    // CONF-HP-10
	    if(doc.getEffectiveTime() == null || doc.getEffectiveTime().getDateValuePrecision() < TS.DAY)
	    {
	    	log.error("CONF-HP-10 : Times in effectiveTime shall be precise to the day");
	    	isValid = false;
	    }
	    // CONF-HP-17
	    if(doc.getId() == null || doc.getId().isNull() ||
	    		(!II.isRootOid(doc.getId()) && !II.isRootUuid(doc.getId())))
    	{
	    	log.error("CONF-HP-17 : ClinicalDocument/id SHALL be present and @root shall be a syntactically correct UUID or OID");
	    	isValid = false;
    	}
	    // CONF-HP-21
	    if(doc.getCode() == null || doc.getCode().isNull() || doc.getCode().getCode() == null)
	    {
	    	log.error("CONF-HP-21 : ClinicalDocument/code SHALL be present and SHALL specify the type of clinical document");
	    	isValid = false;
	    }
	    // CONF-HP-23
	    if(doc.getEffectiveTime() == null || doc.getEffectiveTime().isNull())
	    {
	    	log.error("CONF-HP-23 : ClinicalDocument/effectiveTime SHALL be present and specifies the creation time of the document");
	    	isValid = false;
    	}
	    
	    // CONF-HP-31
	    if(doc.getRecordTarget().size() == 0)
	    {
	    	log.error("CONF-HP-31 : At least one recordTarget/patientRole element SHALL be present");
	    	isValid = false;
	    }
	    else
	    {
	    	for(RecordTarget rct : doc.getRecordTarget())
	    	{
	    		if(rct.getPatientRole() == null || rct.getPatientRole().getNullFlavor() != null)
	    		{
	    	    	log.error("CONF-HP-31 : At least one recordTarget/patientRole element SHALL be present");
	    	    	isValid = false;
	    		}
	    		
	    		// Need to have a patient on all patient roles
	    		if(rct.getPatientRole().getPatient() == null)
    			{
	    			log.error("CONF-HP-32 : patient/birthTime element SHALL be present");
    				log.error("CONF-HP-33 : patient/administrativeGender SHALL be present. If unknown, it SHALL be represented using a flavor of null");
    				isValid = false;
    			}
	    		else {
	    			
	    			Patient pat = rct.getPatientRole().getPatient();
	    			
	    			// CONF-HP-32
	    			if(pat.getBirthTime() == null || !pat.getBirthTime().isNull() && pat.getBirthTime().getDateValuePrecision() < TS.YEAR) 
		    		{
		    			log.error("CONF-HP-32 : patient/birthTime element SHALL be present and SHALL be precise to at least the year. If unknown it SHALL be represented using a nullFlavor");
		    			isValid = false;
		    		}
	    			// CONF-HP-33
	    			if(pat.getAdministrativeGenderCode() == null &&
	    					(pat.getAdministrativeGenderCode().getCode() != null) ^ (pat.getAdministrativeGenderCode().isNull()))
	    			{
	    				log.error("CONF-HP-33 : patient/administrativeGender SHALL be present. If unknown, it SHALL be represented using a flavor of null");
	    				isValid = false;
	    			}
	    		}
	    	}
	    }
	    
	    // Author constraints
	    for(Author aut : doc.getAuthor())
	    {
	    	if(aut.getTime() == null)
	    		log.warn("CONF-HP-37 : The author/time element represents the start time of the author's participation in the creation of the document and SHALL be present");
	    	if(aut.getAssignedAuthor() == null || aut.getAssignedAuthor().getAssignedAuthorChoice() == null)
	    	{
	    		log.error("CONF-HP-39 : An assignedAuthor element SHALL contain an assignedPerson or assignAuthoringDevice element");
	    		isValid = false;
	    	}
	    	else if(aut.getAssignedAuthor().getId() == null)
	    	{
	    		log.error("CONF-HP-38 : An assignedAuthor/id SHALL be present");
	    		isValid = false;
	    	}
	    }
	    
	    return isValid;
    }

	/**
	 * Get template name
	 * @see org.openmrs.module.shr.cdahandler.processor.document.impl.DocumentProcessorImpl#getTemplateName()
	 */
	@Override
    public String getTemplateName() {
	    return "IHE Medical Document";
    }

	
	
	
}
