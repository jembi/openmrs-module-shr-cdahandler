package org.openmrs.module.shr.cdahandler.processor.entry.impl;

import java.util.Date;

import org.marc.everest.datatypes.ANY;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.TS;
import org.marc.everest.datatypes.generic.CS;
import org.marc.everest.datatypes.generic.EIVL;
import org.marc.everest.datatypes.generic.IVL;
import org.marc.everest.datatypes.generic.PIVL;
import org.marc.everest.datatypes.interfaces.ISetComponent;
import org.marc.everest.formatters.FormatterUtil;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Act;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.EntryRelationship;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ManufacturedProduct;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Material;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Reference;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.SubstanceAdministration;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Supply;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ParticipationAuthorOriginator;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActRelationshipEntryRelationship;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_DocumentSubstanceMood;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.CareSetting.CareSettingType;
import org.openmrs.DrugOrder.DosingType;
import org.openmrs.Encounter;
import org.openmrs.Obs;
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
	 * Adds the specified obs to the parentObs ensuring that the concept is a valid concept for the parent obs
	 * @throws DocumentImportException 
	 */
	protected Obs processMedicationObservationValue(Obs parentObs, Concept obsConcept, Object value) throws DocumentImportException {
		// Create the result
		Obs res = new Obs(parentObs.getPerson(), 
			obsConcept, 
			parentObs.getObsDatetime(), 
			parentObs.getLocation());
		res.setEncounter(parentObs.getEncounter());
		res.setDateCreated(parentObs.getDateCreated());
		res.setCreator(parentObs.getCreator());
		res.setLocation(parentObs.getLocation());
		// Ensure obsConcept is a valid set member of parentObs.getConcept
		this.m_conceptUtil.addConceptToSet(parentObs.getConcept(), obsConcept);

		// Set the value
		if(value instanceof ANY)
			this.m_dataUtil.setObsValue(res, (ANY)value);
		else if(value instanceof Concept)
			res.setValueCoded((Concept)value);
		else if(value instanceof String)
			res.setValueText(value.toString());
		
		//res.setObsGroup(parentObs);
		res = Context.getObsService().saveObs(res, null);
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
