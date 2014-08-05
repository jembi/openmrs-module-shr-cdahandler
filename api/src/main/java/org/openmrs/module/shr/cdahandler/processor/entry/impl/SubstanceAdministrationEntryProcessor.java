package org.openmrs.module.shr.cdahandler.processor.entry.impl;

import java.util.Date;

import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.TS;
import org.marc.everest.datatypes.generic.EIVL;
import org.marc.everest.datatypes.generic.IVL;
import org.marc.everest.datatypes.generic.PIVL;
import org.marc.everest.datatypes.interfaces.ISetComponent;
import org.marc.everest.formatters.FormatterUtil;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.EntryRelationship;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ManufacturedProduct;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Material;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Reference;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.SubstanceAdministration;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Supply;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActRelationshipEntryRelationship;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_DocumentSubstanceMood;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.DrugOrder;
import org.openmrs.DrugOrder.DosingType;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Order.Action;
import org.openmrs.api.OrderContext;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.DocumentPersistenceException;
import org.openmrs.module.shr.cdahandler.exception.DocumentValidationException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.util.AssignedEntityProcessorUtil;

/**
 * A processor that can interpret SubstanceAdministrations
 */
public abstract class SubstanceAdministrationEntryProcessor extends EntryProcessorImpl {

	protected AssignedEntityProcessorUtil m_providerUtil = AssignedEntityProcessorUtil.getInstance();

	/**
	 * Process the entry into a 
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.EntryProcessorImpl#process(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement)
	 */
	@Override
	public BaseOpenmrsData process(ClinicalStatement entry) throws DocumentImportException {
		
		// We create a drug order in the OpenMRS datastore for substance administrations
		if(this.m_configuration.getValidationEnabled())
		{
			ValidationIssueCollection validationIssues = this.validate(entry);
			if(validationIssues.hasErrors())
				throw new DocumentValidationException(entry, validationIssues);
		}

		SubstanceAdministration administration = (SubstanceAdministration)entry;
		
		// We create a drug order if one does not already exist!
		Encounter encounterInfo = (Encounter)this.getEncounterContext().getParsedObject();
		// Get current order and void if existing for an update
		Order previousOrder = null;
		
		// References to previous order
		for(Reference reference : administration.getReference())
			if(reference.getExternalActChoiceIfExternalAct() == null)
				continue;
			else
				for(Order currentOrder : Context.getOrderService().getAllOrdersByPatient(encounterInfo.getPatient()))
				{
					for(II id : reference.getExternalActChoiceIfExternalAct().getId())
						if(currentOrder.getAccessionNumber() != null
						&& currentOrder.getAccessionNumber().equals(this.m_datatypeUtil.formatIdentifier(id)))
						{
							previousOrder = currentOrder;
							Context.getOrderService().voidOrder(currentOrder, String.format("replaced in %s", encounterInfo));
						}
				}
		
		// Validate no duplicates on AN
		if(administration.getId() != null)
			for(Order currentOrder : Context.getOrderService().getAllOrdersByPatient(encounterInfo.getPatient()))
			{
				for(II id : administration.getId())
					if(currentOrder.getAccessionNumber() != null
					&& currentOrder.getAccessionNumber().equals(this.m_datatypeUtil.formatIdentifier(id)))
						throw new DocumentImportException(String.format("Duplicate order number %s", id));
			}			
		
		// Now we create a new order
		DrugOrder res = new DrugOrder();
	
		res.setPreviousOrder(previousOrder);
		res.setPatient(encounterInfo.getPatient());
		res.setDateCreated(encounterInfo.getDateCreated());
		res.setEncounter(encounterInfo);
		
		// Set the creator
		super.setCreator(res, administration, encounterInfo);
		
		// Is this a prescribe? 
		if(previousOrder != null)
			res.setAction(Action.REVISE);
		else
			res.setAction(Action.NEW);
			
		// Set the ID
		if(administration.getId() != null && !administration.getId().isNull())
			res.setAccessionNumber(this.m_datatypeUtil.formatIdentifier(administration.getId().get(0)));
		
		// The type of procedure or administration done
		if(administration.getCode() != null && !administration.getCode().isNull())
			res.setConcept(this.m_conceptUtil.getOrCreateConceptAndEquivalents(administration.getCode()));
		
		// Effective time(s)
		Date discontinueDate = null;
		for(ISetComponent<TS> eft : administration.getEffectiveTime())
		{
			// TODO: Is there a better way to represent these frequencies in oMRS than as serializations of the data types? Like a coded list?
			if(eft instanceof IVL) // The effective range time
			{
				IVL<TS> effectiveRange = ((IVL<TS>)eft).toBoundIvl();
				if(effectiveRange.getLow() != null && !effectiveRange.getLow().isNull())
					res.setStartDate(effectiveRange.getLow().getDateValue().getTime());
				if(effectiveRange.getHigh() != null && !effectiveRange.getHigh().isNull())
				{
					if(administration.getMoodCode().getCode().equals(x_DocumentSubstanceMood.Eventoccurrence))
						discontinueDate = effectiveRange.getHigh().getDateValue().getTime();
					else
						res.setAutoExpireDate(effectiveRange.getHigh().getDateValue().getTime());
				}
			}
			else if(eft instanceof EIVL) // Event based, for example: After Meals
			{
				EIVL<TS> frequency = (EIVL<TS>)eft;
				res.setDosingInstructions(String.format("Event:%s", frequency.getEvent().getCode().getCode()));
			}
			else if(eft instanceof TS) // Happened at one instant in time or will happen at one instant in time = Scheduled date
			{
				TS frequency = (TS)eft;
				res.setScheduledDate(frequency.getDateValue().getTime());
			}
			else if(eft instanceof PIVL) // period
			{
				PIVL<TS> frequency = (PIVL<TS>)eft;
				// Frequency needs to be expressed in terms of repeats per day 
				if(frequency.getPhase() != null)
				{
					frequency.setPhase(frequency.getPhase().toBoundIvl());
					res.setDosingInstructions(String.format("Frequency interval:%s", frequency.getPhase().toString()));
				}
				// Period to repeats per day
				if(frequency.getPeriod() != null)
					res.setFrequency(this.m_datatypeUtil.getOrCreateOrderFrequency(frequency.getPeriod()));
			}
			else
				throw new DocumentPersistenceException(String.format("Cannot represent administration frequency of %s", FormatterUtil.createXsiTypeName(eft)));
		}
		
		
		// Dosage?
		if(administration.getDoseQuantity().getValue() != null)
		{
			res.setDosingType(DosingType.SIMPLE);
				res.setDose(administration.getDoseQuantity().getValue().getValue().doubleValue());
			if(administration.getDoseQuantity().getValue().getUnit() != null)
				res.setDoseUnits(this.m_conceptUtil.getOrCreateUcumConcept(administration.getDoseQuantity().getValue().getUnit()));
		}
		else // TODO: Make these Obs maybe?
			throw new DocumentPersistenceException("OpenSHR only supports explicit (non-ranged) doses for administrations");
		
		// Now route
		if(administration.getRouteCode() != null && !administration.getRouteCode().isNull())
			res.setRoute(this.m_conceptUtil.getOrCreateConceptAndEquivalents(administration.getRouteCode()));
		
		// Drug time ... 
		if(administration.getConsumable() != null && administration.getConsumable().getNullFlavor() == null &&
				administration.getConsumable().getManufacturedProduct() != null && administration.getConsumable().getManufacturedProduct().getNullFlavor() == null)
		{
			ManufacturedProduct product = administration.getConsumable().getManufacturedProduct();
			if(product.getManufacturedDrugOrOtherMaterialIfManufacturedLabeledDrug() != null) // A labelled drug
				;
			else
			{
				Material drugMaterial =  product.getManufacturedDrugOrOtherMaterialIfManufacturedMaterial();
				if(drugMaterial.getCode() != null)
					res.setDrug(this.m_conceptUtil.getOrCreateDrugFromConcept(drugMaterial.getCode(), drugMaterial.getName()));
				else
					throw new DocumentImportException("Drug must carry a coded identifier for the product administered");
			}
		}
		
		// reason will link to another obs
		StringBuilder reason = new StringBuilder(); 
		for(EntryRelationship er : this.findEntryRelationship(administration, CdaHandlerConstants.ENT_TEMPLATE_INTERNAL_REFERENCE))
			if(er.getTypeCode().getCode().equals(x_ActRelationshipEntryRelationship.HasReason))
				reason.append(String.format("%s;",this.m_datatypeUtil.formatIdentifier(er.getClinicalStatementIfAct().getId().get(0))));
		if(!reason.toString().isEmpty())
			res.setOrderReasonNonCoded(reason.toString());

		// This is the person who performed the dispense or is intended to perform the dispense
		OrderContext orderContext = new OrderContext();

		// Prescription (supply) entry relationship?
		for(EntryRelationship er : this.findEntryRelationship(administration, CdaHandlerConstants.ENT_TEMPLATE_SUPPLY))
			if(er.getTypeCode().getCode().equals(x_ActRelationshipEntryRelationship.REFR))
			{
				
				Supply supply = er.getClinicalStatementIfSupply();
				
				// Repeat number
				if(supply.getRepeatNumber() != null && !supply.getRepeatNumber().isNull() &&
						supply.getRepeatNumber().getValue() != null)
					res.setNumRefills(supply.getRepeatNumber().getValue().toInteger());

				// Quantity
				if(supply.getQuantity() != null && !supply.getQuantity().isNull())
				{
					res.setQuantity(supply.getQuantity().getValue().doubleValue());
					res.setQuantityUnits(this.m_conceptUtil.getOrCreateUcumConcept(supply.getQuantity().getUnit()));
				}
					
				// Author, who prescribed?
				if(supply.getAuthor().size() > 0)
					res.setOrderer(this.m_providerUtil.processProvider(supply.getAuthor().get(0).getAssignedAuthor()));
				
				// Performer, who performed the fulfillment?
				if(supply.getPerformer().size() > 0)
				{
					orderContext.setAttribute("PRF", this.m_providerUtil.processProvider(supply.getPerformer().get(0).getAssignedEntity()));
				}
			}
		
		// Save the order 
		res = (DrugOrder)Context.getOrderService().saveOrder(res, null);

		// Is this an event? If so it happened in the past and isn't active so we have to discontinue it
		if(administration.getMoodCode().getCode().equals(x_DocumentSubstanceMood.Eventoccurrence))
			try
			{
				res = (DrugOrder)Context.getOrderService().discontinueOrder(res, "MoodCode=EVN", discontinueDate, null, encounterInfo);
			}
			catch(Exception e)
			{
				throw new DocumentPersistenceException(e);
			}

		return res;
		
	}

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
