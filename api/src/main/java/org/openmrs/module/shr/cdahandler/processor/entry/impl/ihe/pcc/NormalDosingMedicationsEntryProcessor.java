package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.EntryRelationship;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.SubstanceAdministration;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;

/**
 * Normal Dosing of medications entry processor
 */
@ProcessTemplates(templateIds = { CdaHandlerConstants.ENT_TEMPLATE_MEDICATIONS_NORMAL_DOSING })
public class NormalDosingMedicationsEntryProcessor extends MedicationsEntryProcessor {

	/**
	 * Custom validation rules for the normal dosing
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc.MedicationsEntryProcessor#validate(org.marc.everest.interfaces.IGraphable)
	 */
	@Override
    public ValidationIssueCollection validate(IGraphable object) {
	    ValidationIssueCollection issues = super.validate(object);
	    if(issues.hasErrors())
	    	return issues;
	    
	    SubstanceAdministration administration = (SubstanceAdministration)object;
	    
	    // Must only have one frequency and dose with no subordinate substanceAdministration acts
	    if(administration.getEffectiveTime().size() != 2)
	    	issues.error("Normal dosing must only carry two repetitions of effectiveTime for date/time of administration and frequency");
	    
	    boolean hasSubordinateSbadm = false;
	    for(EntryRelationship er : administration.getEntryRelationship())
	    	hasSubordinateSbadm |= er.getClinicalStatement().isPOCD_MT000040UVSubstanceAdministration();
	    if(hasSubordinateSbadm)
	    	issues.error("Normal dosing may not carry subordinate substanceAdministrations");
	    
	    return issues;
    }
	
	
	
}
