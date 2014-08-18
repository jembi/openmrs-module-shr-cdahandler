package org.openmrs.module.shr.cdahandler.processor.entry.impl;

import org.marc.everest.datatypes.ANY;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.SubstanceAdministration;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_DocumentSubstanceMood;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.DocumentValidationException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.util.AssignedEntityProcessorUtil;

/**
 * A processor that can interpret SubstanceAdministrations
 */
public abstract class SubstanceAdministrationEntryProcessor extends EntryProcessorImpl {

	protected AssignedEntityProcessorUtil m_providerUtil = AssignedEntityProcessorUtil.getInstance();

	/**
	 * Process
	 */
	@Override
    public BaseOpenmrsData process(ClinicalStatement entry) throws DocumentImportException {
		if(this.m_configuration.getValidationEnabled())
		{
			ValidationIssueCollection issues = this.validate(entry);
			if(issues.hasErrors())
				throw new DocumentValidationException(entry, issues);
		}
		else if(!entry.isPOCD_MT000040UVSubstanceAdministration())
			throw new DocumentImportException("Expected entry to be SubstanceAdministration");
		
		SubstanceAdministration sbadm = (SubstanceAdministration)entry;
		if(sbadm.getMoodCode().getCode().equals(x_DocumentSubstanceMood.Intent)) // Prescribe
			return this.processAdministrationAsOrder(sbadm);
		else
			return this.processAdministrationAsObservation(sbadm);
    }

	/**
	 * Process the substance administration as an order
	 */
	protected abstract BaseOpenmrsData processAdministrationAsOrder(SubstanceAdministration administration) throws DocumentImportException;

	/**
	 * PRocess the substance administration as an observation
	 */
	protected abstract BaseOpenmrsData processAdministrationAsObservation(SubstanceAdministration administration) throws DocumentImportException;
	
	/**
	 * Validate a substance administration can be processed
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.EntryProcessorImpl#validate(org.marc.everest.interfaces.IGraphable)
	 */
	@Override
    public ValidationIssueCollection validate(IGraphable object) {
		ValidationIssueCollection validationIssues = super.validate(object);
		if(validationIssues.hasErrors()) return validationIssues;
		
		// Clinical statement type check
		ClinicalStatement statement = (ClinicalStatement)object;
		if(!statement.isPOCD_MT000040UVSubstanceAdministration())
		{
			validationIssues.error(String.format("Expected entry of type SubstanceAdministration, got %s", object.getClass().getName()));
			return validationIssues;
		}
		
		// Must have a substance administration
		SubstanceAdministration administration = (SubstanceAdministration)object;
		if(administration.getEffectiveTime().size() == 0)
			validationIssues.error("Substance administration must have an effective time");
		if(administration.getConsumable() == null || administration.getConsumable().getNullFlavor() != null ||
				administration.getConsumable().getManufacturedProduct() == null || administration.getConsumable().getManufacturedProduct().getNullFlavor() != null)
			validationIssues.error("Substance administration must have a consumable product");
		
		return validationIssues;
    }

	
	
}
