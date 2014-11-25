package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.marc.everest.datatypes.TS;
import org.marc.everest.datatypes.generic.SXCM;
import org.marc.everest.datatypes.interfaces.ISetComponent;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.EntryRelationship;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Observation;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.SubstanceAdministration;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.obs.ExtendedObs;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.context.ProcessorContext;
import org.openmrs.module.shr.cdahandler.processor.entry.impl.SubstanceAdministrationEntryProcessor;

/**
 * Entry processor for an immunization entry
 */
@ProcessTemplates(templateIds = {
		CdaHandlerConstants.ENT_TEMPLATE_IMMUNIZATIONS		
})
public class ImmunizationEntryProcessor extends SubstanceAdministrationEntryProcessor {

	/**
	 * Get the template name
	 */
	@Override
	public String getTemplateName() {
		return "Immunization Entry";
	}
	
	/**
	 * Process an administration as an order
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.SubstanceAdministrationEntryProcessor#processAdministrationAsOrder(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.SubstanceAdministration)
	 */
	@Override
	protected BaseOpenmrsData processAdministrationAsOrder(SubstanceAdministration administration)
	    throws DocumentImportException {
		throw new NotImplementedException();
	}
	
	/**
	 * Process an administration as an observation
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.SubstanceAdministrationEntryProcessor#processAdministrationAsObservation(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.SubstanceAdministration)
	 */
	@Override
	protected BaseOpenmrsData processAdministrationAsObservation(SubstanceAdministration administration)
	    throws DocumentImportException {
		
		ExtendedObs immunizationObs = super.createSubstanceAdministrationObs(administration, Context.getConceptService().getConcept(CdaHandlerConstants.CONCEPT_ID_IMMUNIZATION_HISTORY), Context.getConceptService().getConcept(CdaHandlerConstants.CONCEPT_ID_IMMUNIZATION_DRUG));
		
		// Set the effective time 
		for(ISetComponent<TS> eft : administration.getEffectiveTime())
			if(eft instanceof SXCM)
			{
				SXCM<TS> eftIvl = (SXCM<TS>)eft;
				if(eftIvl.isNull())
					immunizationObs.setObsDatePrecision(0); // null flavor
				else 
				{
					immunizationObs.setObsDatetime(eftIvl.getValue().getDateValue().getTime());
					immunizationObs.setObsDatePrecision(eftIvl.getValue().getDateValuePrecision());
					this.m_dataUtil.addSubObservationValue(immunizationObs, Context.getConceptService().getConcept(CdaHandlerConstants.CONCEPT_ID_IMMUNIZATION_DATE), eftIvl.getValue());
				}
			}
		
		// Now, is there a series number on this?
		for(EntryRelationship series : this.findEntryRelationship(administration, CdaHandlerConstants.ENT_TEMPLATE_IMMUNIZATION_SERIES))
		{
			Observation seriesObservation = series.getClinicalStatementIfObservation();
			if(seriesObservation == null)
				throw new DocumentImportException("Immunization series number must be an observation");
			else if(seriesObservation.getCode() == null || !seriesObservation.getCode().getCode().equals("30973-2"))
				throw new DocumentImportException("Immunization seires number must carry code of 30973-2");
			else
				this.m_dataUtil.addSubObservationValue(immunizationObs, Context.getConceptService().getConcept(CdaHandlerConstants.CONCEPT_ID_IMMUNIZATION_SEQUENCE), seriesObservation.getValue());
		}
		
		immunizationObs = (ExtendedObs)Context.getObsService().saveObs(immunizationObs, null);
		
		// Process entry relationships (these should be substance administrations) 
		// Representing them as a flat heirarchy
		ProcessorContext childContext = new ProcessorContext(administration, immunizationObs, this);
		super.processEntryRelationships(administration, childContext);

		return immunizationObs;
	}

	/**
	 * No expected entry relationships per se
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.EntryProcessorImpl#getExpectedEntryRelationships()
	 */
	@Override
	protected List<String> getExpectedEntryRelationships() {
		// TODO Auto-generated method stub
		return null;
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
		if(sbadm.getEffectiveTime().size() != 1 || ((SXCM)sbadm.getEffectiveTime().get(0)).getValue() == null)
			validationIssues.error("Immunizations section shall have only one effective time carrying a point in time which the immunization was given");
		if(sbadm.getConsumable() == null || sbadm.getConsumable().getNullFlavor() != null)
			validationIssues.error("Immunizations section must have a consumable");
		else if(sbadm.getConsumable().getManufacturedProduct() == null || sbadm.getConsumable().getManufacturedProduct().getNullFlavor() != null)
			validationIssues.error("Immunizations must have a manufacturedProduct node");
		
		// If route of administration is provided must be RouteOfAdministration code set
		if(sbadm.getRouteCode() != null && !sbadm.getRouteCode().isNull() && 
				!CdaHandlerConstants.CODE_SYSTEM_ROUTE_OF_ADMINISTRATION.equals(sbadm.getRouteCode().getCodeSystem()))
			validationIssues.error("If the route is known, the routeCode must be populated using the HL7 RouteOfAdministration valueset");
		return validationIssues;
    }
	

}
