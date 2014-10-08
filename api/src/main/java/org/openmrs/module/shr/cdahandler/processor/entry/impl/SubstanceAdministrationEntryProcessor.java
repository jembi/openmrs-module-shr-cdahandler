package org.openmrs.module.shr.cdahandler.processor.entry.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.marc.everest.datatypes.ANY;
import org.marc.everest.datatypes.BL;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.ST;
import org.marc.everest.datatypes.TS;
import org.marc.everest.datatypes.doc.StructDocNode;
import org.marc.everest.datatypes.generic.CS;
import org.marc.everest.datatypes.generic.CV;
import org.marc.everest.datatypes.generic.EIVL;
import org.marc.everest.datatypes.generic.IVL;
import org.marc.everest.datatypes.generic.PIVL;
import org.marc.everest.datatypes.generic.SET;
import org.marc.everest.datatypes.interfaces.ISetComponent;
import org.marc.everest.formatters.FormatterUtil;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Act;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.EntryRelationship;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.LabeledDrug;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ManufacturedProduct;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Material;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Performer2;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Precondition;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Reference;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.SubstanceAdministration;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Supply;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ActStatus;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ParticipationAuthorOriginator;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActRelationshipEntryRelationship;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActRelationshipExternalReference;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_DocumentSubstanceMood;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Order.Action;
import org.openmrs.Provider;
import org.openmrs.api.OrderContext;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.DocumentPersistenceException;
import org.openmrs.module.shr.cdahandler.exception.DocumentValidationException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.obs.ExtendedObs;
import org.openmrs.module.shr.cdahandler.processor.context.ProcessorContext;
import org.openmrs.module.shr.cdahandler.processor.util.AssignedEntityProcessorUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.validator.ObsValidator;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.EscapedErrors;

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

	/**
	 * Creates the substance administration obs with the appropriate container
	 * and date identifier obs
	 * @param drugObsConcept = The concept under which the manufactured material is assigned
	 * @return 
	 * @throws DocumentImportException 
	 */
	protected ExtendedObs createSubstanceAdministrationObs(SubstanceAdministration administration, Concept obsConcept, Concept drugObsConcept) throws DocumentImportException {

		Encounter encounterInfo = (Encounter)this.getEncounterContext().getParsedObject();
		Obs parentObs = (Obs)this.getContext().getParsedObject();
		
		// Get current order and void if existing for an update
		Obs previousHistoryObs = super.voidOrThrowIfPreviousObsExists(administration.getReference(), encounterInfo.getPatient(), administration.getId());
		
		
		// Obs for the medication history entry
		ExtendedObs medicationHistoryObs = new ExtendedObs();
		medicationHistoryObs.setConcept(obsConcept);
		medicationHistoryObs.setObsGroup(parentObs);
		medicationHistoryObs.setPreviousVersion(previousHistoryObs);
		medicationHistoryObs.setPerson(encounterInfo.getPatient());
		medicationHistoryObs.setLocation(encounterInfo.getLocation());
		
		// Set mood code
		medicationHistoryObs.setObsMood(this.m_conceptUtil.getOrCreateConcept(new CV<x_DocumentSubstanceMood>(administration.getMoodCode().getCode())));
		// Set the status of the obs
		medicationHistoryObs.setObsStatus(this.m_conceptUtil.getOrCreateConcept(new CV<ActStatus>(administration.getStatusCode().getCode())));
		medicationHistoryObs.setObsDatetime(encounterInfo.getEncounterDatetime());
		
		medicationHistoryObs.setEncounter(encounterInfo);
		medicationHistoryObs.setDateCreated(encounterInfo.getDateCreated());
		super.setCreator(medicationHistoryObs, administration);				

		// Procedure?
		if(administration.getCode() != null && !administration.getCode().isNull() && administration.getCode().getCode() != null)
			this.m_dataUtil.addSubObservationValue(medicationHistoryObs, Context.getConceptService().getConcept(CdaHandlerConstants.CONCEPT_ID_PROCEDURE), administration.getCode());
		
		// Dosage? 
		if(administration.getDoseQuantity() != null)
		{
			if(administration.getDoseQuantity().getValue() != null && administration.getDoseQuantity().getValue().getUnit() == null )
				this.m_dataUtil.addSubObservationValue(medicationHistoryObs, Context.getConceptService().getConcept(CdaHandlerConstants.CONCEPT_ID_MEDICATION_QUANTITY), administration.getDoseQuantity().getValue().getValue());
			else
				this.m_dataUtil.addSubObservationValue(medicationHistoryObs, Context.getConceptService().getConcept(CdaHandlerConstants.CONCEPT_ID_MEDICATION_STRENGTH), administration.getDoseQuantity().toString());
		}
			
		// Now form
		if(administration.getAdministrationUnitCode() != null && !administration.getAdministrationUnitCode().isNull())
		{
			Concept formCode = this.m_conceptUtil.getOrCreateDrugAdministrationFormConcept(administration.getAdministrationUnitCode());
			this.m_dataUtil.addSubObservationValue(medicationHistoryObs, Context.getConceptService().getConcept(CdaHandlerConstants.CONCEPT_ID_MEDICATION_FORM), formCode);
		}
		
		// Now route
		if(administration.getRouteCode() != null && !administration.getRouteCode().isNull())
		{
			Concept routeCodeConcept = this.m_conceptUtil.getOrCreateDrugRouteConcept();
			this.m_dataUtil.addSubObservationValue(medicationHistoryObs, routeCodeConcept, administration.getRouteCode());
		}

		// Get the section text
		Section parentSection = super.getSection();

		// Text and comments
		if(administration.getText() != null && administration.getText().getReference() != null)
		{
			StructDocNode textNode = parentSection.getText().findNodeById(administration.getText().getReference().getValue());
			if(textNode != null)
				this.m_dataUtil.addSubObservationValue(medicationHistoryObs, Context.getConceptService().getConcept(CdaHandlerConstants.CONCEPT_ID_MEDICATION_TEXT), textNode.toPlainString());
		}
		
		// Instructions as a sub-observation
		// TODO: Make this more appropriate sub-obs
		for(EntryRelationship er : this.findEntryRelationship(administration, CdaHandlerConstants.ENT_TEMPLATE_MEDICATION_INSTRUCTIONS))
		{
			// Get the text
			Act instructionAct = er.getClinicalStatementIfAct();
			if(instructionAct == null || instructionAct.getText() == null) continue;
			// Get instruction
			if(instructionAct.getText().getReference() != null)
			{
				StructDocNode instructionNode = parentSection.getText().findNodeById(instructionAct.getText().getReference().getValue());
				if(instructionNode != null)
					this.m_dataUtil.addSubObservationValue(medicationHistoryObs, Context.getConceptService().getConcept(CdaHandlerConstants.CONCEPT_ID_MEDICATION_TEXT), String.format("Instructions: %s", instructionNode.toPlainString()));
			}
			break;
		}		

		// Drug time ... 
		Drug administeredDrug = null;
		if(administration.getConsumable() != null && administration.getConsumable().getNullFlavor() == null &&
				administration.getConsumable().getManufacturedProduct() != null && administration.getConsumable().getManufacturedProduct().getNullFlavor() == null)
		{
			ManufacturedProduct product = administration.getConsumable().getManufacturedProduct();
			if(product.getManufacturedDrugOrOtherMaterialIfManufacturedLabeledDrug() != null) // A labelled drug
			{
				throw new DocumentImportException("Must use a manufacturedMaterial on consumable");
			}
			else
			{
				Material drugMaterial =  product.getManufacturedDrugOrOtherMaterialIfManufacturedMaterial();
				
				if(drugMaterial.getCode() != null)
				{
					
					administeredDrug = this.m_conceptUtil.getOrCreateDrugFromConcept(drugMaterial.getCode(), drugMaterial.getName(), administration.getAdministrationUnitCode());
					this.m_dataUtil.addSubObservationValue(medicationHistoryObs, drugObsConcept, administeredDrug);

				}
				else
					throw new DocumentImportException("Drug must carry a coded identifier for the product administered");
			}
		}
		
		// Negation indicator?
		if(BL.TRUE.equals(administration.getNegationInd())) 
			this.m_dataUtil.addSubObservationValue(medicationHistoryObs, Context.getConceptService().getConcept(CdaHandlerConstants.CONCEPT_ID_SIGN_SYMPTOM_PRESENT), Context.getConceptService().getConcept(Integer.valueOf(Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_FALSE_CONCEPT))));
		
		// Prescription (supply) entry relationship?
		for(EntryRelationship er : this.findEntryRelationship(administration, CdaHandlerConstants.ENT_TEMPLATE_SUPPLY))
		{
			// TODO: Move this to an entry processor
			Supply supply = er.getClinicalStatementIfSupply();
			if(er.getTypeCode().getCode().equals(x_ActRelationshipEntryRelationship.REFR) && supply != null)
			{
				
				ExtendedObs supplyObs = new ExtendedObs();
				supplyObs.setEncounter(encounterInfo);
				supplyObs.setConcept(Context.getConceptService().getConcept(CdaHandlerConstants.CONCEPT_ID_SUPPLY));
				supplyObs.setObsDatetime(medicationHistoryObs.getObsDatetime());
				medicationHistoryObs.setObsMood(this.m_conceptUtil.getOrCreateConcept(new CV<x_DocumentSubstanceMood>(supply.getMoodCode().getCode())));
				super.setCreator(supplyObs, supply);
				
				// Supply obs
				if(supply.getId() != null && !supply.getId().isEmpty())
					supplyObs.setAccessionNumber(this.m_datatypeUtil.formatIdentifier(supply.getId().get(0)));
				
				// Repeat
				if(supply.getRepeatNumber() != null && supply.getRepeatNumber().getValue() != null && !supply.getRepeatNumber().getValue().isNull())
					supplyObs.setObsRepeatNumber(supply.getRepeatNumber().getValue().getValue());
				
				// Medications?
				if(administeredDrug != null)
					this.m_dataUtil.addSubObservationValue(supplyObs, Context.getConceptService().getConcept(CdaHandlerConstants.CONCEPT_ID_MEDICATION_DRUG), administeredDrug);
				
				// Quantity? 
				if(supply.getQuantity() != null)
				{
					if(supply.getQuantity().getUnit() == null && supply.getQuantity().getValue() != null)
						this.m_dataUtil.addSubObservationValue(supplyObs, Context.getConceptService().getConcept(CdaHandlerConstants.CONCEPT_ID_MEDICATION_DISPENSED), supply.getQuantity().getValue());
					else
						this.m_dataUtil.addSubObservationValue(supplyObs, Context.getConceptService().getConcept(CdaHandlerConstants.CONCEPT_ID_MEDICATION_STRENGTH), supply.getQuantity().toString());
				}
				
				// Fill
				if(er.getSequenceNumber() != null)
					this.m_dataUtil.addSubObservationValue(supplyObs, Context.getConceptService().getConcept(CdaHandlerConstants.CONCEPT_ID_MEDICATION_TREATMENT_NUMBER), er.getSequenceNumber());
				
				// Date that it was filled
				for(Performer2 prf : supply.getPerformer())
				{
					// Now we want to get two pieces of info off this 1) who dispensed
					if(prf.getAssignedEntity() != null)
					{
						Provider provider = this.m_providerUtil.processProvider(prf.getAssignedEntity());
						this.m_dataUtil.addSubObservationValue(supplyObs, Context.getConceptService().getConcept(CdaHandlerConstants.CONCEPT_ID_PROVIDER_NAME), provider.getIdentifier());
					}
					// 2) the date of the dispense
					if(prf.getTime() != null && !prf.getTime().isNull())
						this.m_dataUtil.addSubObservationValue(supplyObs, Context.getConceptService().getConcept(CdaHandlerConstants.CONCEPT_ID_DATE_OF_EVENT), prf.getTime().getValue());
				}
				
				// TODO: Instructions
				medicationHistoryObs.addGroupMember(supplyObs);
				
			}
		}
		
		// reasoning will link to another obs
		// TODO: Map this to 160742 concept by walking down the CDA tree
		for(EntryRelationship er : this.findEntryRelationship(administration, CdaHandlerConstants.ENT_TEMPLATE_INTERNAL_REFERENCE))
			if(er.getTypeCode().getCode().equals(x_ActRelationshipEntryRelationship.HasReason))
			{
				ST reasonText = new ST(this.m_datatypeUtil.formatIdentifier(er.getClinicalStatementIfAct().getId().get(0)));
				this.m_dataUtil.addSubObservationValue(medicationHistoryObs, this.m_conceptUtil.getOrCreateRMIMConcept(CdaHandlerConstants.RMIM_CONCEPT_UUID_REASON, reasonText), reasonText);
			}
		
		// Conditions, or dosing instructions
		for(Precondition condition : administration.getPrecondition())
		{
			if(condition.getNullFlavor() != null || condition.getCriterion() == null ||
					condition.getCriterion().getNullFlavor() != null ||
					condition.getCriterion().getText() == null ||
					condition.getCriterion().getText().getReference() == null)
				continue;
			
			// Add instructions
			StructDocNode instructionsNode = this.getSection().getText().findNodeById(condition.getCriterion().getText().getReference().getValue());
			if(instructionsNode != null)
				this.m_dataUtil.addSubObservationValue(medicationHistoryObs, Context.getConceptService().getConcept(CdaHandlerConstants.CONCEPT_ID_MEDICATION_TEXT), String.format("Pre-Condition: %s", instructionsNode.toPlainString()));
		}
	 
		return medicationHistoryObs;
    }

	



	
	
}
