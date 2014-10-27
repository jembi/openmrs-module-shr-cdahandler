package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import java.util.List;

import org.marc.everest.datatypes.TS;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.datatypes.generic.CV;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.EntryRelationship;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Observation;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActRelationshipEntryRelationship;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.context.ProcessorContext;
import org.openmrs.module.shr.cdahandler.processor.entry.EntryProcessor;
import org.openmrs.module.shr.cdahandler.processor.entry.impl.ObservationEntryProcessor;

/**
 * Estimated delivery date observation entry
 */
@ProcessTemplates(templateIds = { CdaHandlerConstants.ENT_TEMPLATE_DELIVERY_DATE_OBSERVATION })
public class EstimatedDeliveryDateObservationEntryProcessor extends SimpleObservationEntryProcessor {

	
	/**
	 * Get the expected code
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.ObservationEntryProcessor#getExpectedCode()
	 */
	@Override
	protected CE<String> getExpectedCode() {
		return new CE<String>("11778-8", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "DELIVERY DATE (CLINICAL ESTIMATE)", null);
	}

	/**
	 * No expected entry template Ids
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.EntryProcessorImpl#getExpectedEntryRelationships()
	 */
	@Override
	protected List<String> getExpectedEntryRelationships() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Get the name of the template processor
	 * @see org.openmrs.module.shr.cdahandler.processor.Processor#getTemplateName()
	 */
	@Override
	public String getTemplateName() {
		return "Estimated Delivery Date Observation";
	}

	/**
	 * Validate per 6.3.4.27 
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.ObservationEntryProcessor#validate(org.marc.everest.interfaces.IGraphable)
	 */
	@Override
    public ValidationIssueCollection validate(IGraphable object) {
	    ValidationIssueCollection validationIssues = super.validate(object);
	    if(validationIssues.hasErrors())
	    	return validationIssues;
	    
	    Observation obs = (Observation)object;
	    
	    // Author shall be present
	    if(obs.getAuthor().size() != 1 )
	    	validationIssues.error("Expected 1 author node on EDD observation");
	    if(!(obs.getValue() instanceof TS))
	    	validationIssues.error("EDD observation value shall be a point in time");
	    
	    // Estimated delivery date should have supporting observations, if
	    // present they shall have SPRT
	    for(EntryRelationship er : obs.getEntryRelationship())
	    {
			Observation firstNestedSupport = er.getClinicalStatementIfObservation();
	    	if(!er.getTypeCode().getCode().equals(x_ActRelationshipEntryRelationship.SPRT) || 
	    			firstNestedSupport == null)
	    		validationIssues.error("EDD supporting observations shall carry type code of SPRT");
	    	else
	    	{
	    		this.validateNestedObservation(obs.getCode(), firstNestedSupport, validationIssues);
	    		// Validate second nested
	    		for(EntryRelationship er2 : firstNestedSupport.getEntryRelationship())
	    		{
	    			Observation secondNestedSupport = er2.getClinicalStatementIfObservation();
	    	    	if(!er2.getTypeCode().getCode().equals(new x_ActRelationshipEntryRelationship("DRIV", x_ActRelationshipEntryRelationship.CAUS.getCodeSystem())) || 
	    	    			secondNestedSupport == null)
	    	    		validationIssues.error("EDD supporting observations shall carry type code of DRIV");
	    	    	else
	    	    		this.validateNestedObservation(firstNestedSupport.getCode(), secondNestedSupport, validationIssues);
	    	    		
	    		}

	    	}

	    }
	    
	    return validationIssues;
	    
    }
	
	/**
	 * Validated a nested observation
	 * Auto generated method comment
	 * 
	 * @param contextCode
	 * @param obs
	 * @param validationIssues
	 */
	private void validateNestedObservation(CV<String> contextCode, Observation obs, ValidationIssueCollection validationIssues)
	{
		// Validate the entry can be used in the code
		super.validateConceptWithContainer(obs.getCode(), obs, validationIssues);
		
		if(obs.getRepeatNumber() != null || 
				obs.getInterpretationCode() != null ||
						obs.getMethodCode() != null)
			validationIssues.error("Supporting observations shall not include repeatNumber, interpretationCode, or methodCode");

	}
}
