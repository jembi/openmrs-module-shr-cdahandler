package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import java.util.List;

import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.SubstanceAdministration;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.entry.impl.SubstanceAdministrationEntryProcessor;

/**
 * Medications Entry Processor
 */
@ProcessTemplates(templateIds = {
		CdaHandlerConstants.ENT_TEMPLATE_MEDICATIONS
})
public class MedicationsEntryProcessor extends SubstanceAdministrationEntryProcessor {
	
	/**
	 * Get the expected entry relationships
	 */
	@Override
	protected List<String> getExpectedEntryRelationships() {
		return null;
	}
	
	/**
	 * Get the template name
	 */
	@Override
	public String getTemplateName() {
		return "Medications";
	}

	/**
	 * Validate the instance
	 */
	@Override
    public ValidationIssueCollection validate(IGraphable object) {
		ValidationIssueCollection validationIssues = super.validate(object);
		if(validationIssues.hasErrors())
			return validationIssues;
		
		// Now validate the explicit requirements
		SubstanceAdministration sbadm = (SubstanceAdministration)object;
		if(sbadm.getEffectiveTime().size() < 2)
			validationIssues.error("Medications section shall have at minimum two effectiveTime elements representing the date and frequency of administration");
		if(sbadm.getDoseQuantity() == null || sbadm.getDoseQuantity().isNull())
			validationIssues.error("Medications section shall have a dose quantity");
		if(sbadm.getConsumable() == null || sbadm.getConsumable().getNullFlavor() != null)
			validationIssues.error("Medications section must have a consumable");
		else if(sbadm.getConsumable().getManufacturedProduct() == null || sbadm.getConsumable().getManufacturedProduct().getNullFlavor() != null)
			validationIssues.error("Consumable must have a manufacturedProduct node");
		return validationIssues;
    }

	
	
}

