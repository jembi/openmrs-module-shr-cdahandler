package org.openmrs.module.shr.cdahandler.processor.entry.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.marc.everest.datatypes.ANY;
import org.marc.everest.datatypes.BL;
import org.marc.everest.datatypes.ED;
import org.marc.everest.datatypes.INT;
import org.marc.everest.datatypes.PQ;
import org.marc.everest.datatypes.ST;
import org.marc.everest.datatypes.TEL;
import org.marc.everest.datatypes.doc.StructDocNode;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.datatypes.generic.CS;
import org.marc.everest.datatypes.generic.CV;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Observation;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Reference;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ActStatus;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ObservationInterpretation;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ParticipationAuthorOriginator;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActMoodDocumentObservation;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActRelationshipExternalReference;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptNumeric;
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
import org.openmrs.module.shr.cdahandler.obs.ExtendedObs;
import org.openmrs.module.shr.cdahandler.order.ObservationOrder;
import org.openmrs.module.shr.cdahandler.processor.context.ProcessorContext;
import org.openmrs.module.shr.cdahandler.processor.util.AssignedEntityProcessorUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsMetadataUtil;
import org.openmrs.util.OpenmrsConstants;


/**
 * Represents a processor for Observation Entries
 */
public abstract class ObservationEntryProcessor extends EntryProcessorImpl {

	// Metadata util
	protected final OpenmrsMetadataUtil m_metaDataUtil = OpenmrsMetadataUtil.getInstance();
	
	// Provider util
	protected final AssignedEntityProcessorUtil m_providerUtil = AssignedEntityProcessorUtil.getInstance();
	
	/**
	 * Gets the code expected to be on this observation (null if none specified)
	 * @return
	 */
	protected abstract CE<String> getExpectedCode();

	/**
	 * Process the observation
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.EntryProcessorImpl#process(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement)
	 */
	@Override
	public BaseOpenmrsData process(ClinicalStatement entry) throws DocumentImportException {
		
		// Validate
		if(this.m_configuration.getValidationEnabled())
		{
			ValidationIssueCollection issues = this.validate(entry);
			if(issues.hasErrors())
				throw new DocumentValidationException(entry, issues);
		}
		else if(!entry.isPOCD_MT000040UVObservation())
			throw new DocumentImportException("Expected entry to be an Observation");
		
		// Observation from entry
		Observation observation = (Observation)entry;

		// Only store EVN or " I DID OBSERVE " Observations, the other mood codes 
		// if encountered should create more appropriate data
		if(observation.getMoodCode().getCode().equals(x_ActMoodDocumentObservation.Intent))
			return this.processIntentOccurance(observation);
		else
			return this.processEventOccurance(observation);
	}


	/**
	 * Process the observation as an order. Usually these are tests
	 * @throws DocumentImportException 
	 */
	protected BaseOpenmrsData processIntentOccurance(Observation observation) throws DocumentImportException {
		
		Encounter encounterInfo = (Encounter)this.getEncounterContext().getParsedObject();

		// Get current order and void if existing for an update
		Order previousOrder = super.voidOrThrowIfPreviousOrderExists(observation.getReference(), encounterInfo.getPatient(), observation.getId());

		// Now we create a new order
		ObservationOrder res = new ObservationOrder();
		// The type of procedure or administration done
		Concept orderConcept = null;
		if(observation.getCode() != null && !observation.getCode().isNull())
		{
			orderConcept = null;
			if(observation.getValue() != null)
			{
				orderConcept = this.m_conceptUtil.getTypeSpecificConcept(observation.getCode(), observation.getValue());
				if(orderConcept == null)
					this.m_conceptUtil.createConcept(observation.getCode(), observation.getValue());
			}
			else
				orderConcept = this.m_conceptUtil.getOrCreateConceptAndEquivalents(observation.getCode());
		}
		else
			throw new DocumentImportException("Observation must have a code");
		
		
		res.setConcept(orderConcept);
		res.setPreviousOrder(previousOrder);
		res.setPatient(encounterInfo.getPatient());
		res.setDateCreated(encounterInfo.getDateCreated());
		res.setEncounter(encounterInfo);
		
		// Set the creator
		super.setCreator(res, observation);
		
		// Is this a prescribe? 
		if(previousOrder != null)
			res.setAction(Action.REVISE);
		else
			res.setAction(Action.NEW);
			
		// Set the ID
		if(observation.getId() != null && !observation.getId().isNull())
			res.setAccessionNumber(this.m_datatypeUtil.formatIdentifier(observation.getId().get(0)));
		
		// Effective time(s)
		Date discontinueDate = null;
		if(observation.getEffectiveTime() != null && !observation.getEffectiveTime().isNull())
		{
			if(observation.getEffectiveTime().getLow() != null && !observation.getEffectiveTime().getLow().isNull())
			{
				if(observation.getStatusCode().getCode().equals(ActStatus.New))
					res.setScheduledDate(observation.getEffectiveTime().getLow().getDateValue().getTime());
				else // Did occur
				{
					res.setDateActivated(observation.getEffectiveTime().getLow().getDateValue().getTime());
					encounterInfo.setEncounterDatetime(res.getDateActivated());
					if(encounterInfo.getEncounterDatetime().before(encounterInfo.getVisit().getStartDatetime()))
						encounterInfo.getVisit().setStartDatetime(encounterInfo.getEncounterDatetime());
					
				}
			}
			if(observation.getEffectiveTime().getHigh() != null && !observation.getEffectiveTime().getHigh().isNull())
			{
				if(observation.getStatusCode().getCode().equals(ActStatus.Active)  ||
						observation.getStatusCode().getCode().equals(ActStatus.New))
					res.setAutoExpireDate(observation.getEffectiveTime().getHigh().getDateValue().getTime());
				else
					discontinueDate = observation.getEffectiveTime().getHigh().getDateValue().getTime();
			}
		}
		
		// Text?
		if(observation.getText() != null && observation.getText().getReference() != null)
		{
			StructDocNode node = this.getSection().getText().findNodeById(observation.getText().getReference().getValue());
			if(node != null)
				res.setInstructions(node.toPlainString());
		}

		// Get orderer 
		if(observation.getAuthor().size() == 1 &&
				observation.getAuthor().get(0).getAssignedAuthor() != null
				)
			res.setOrderer(this.m_providerUtil.processProvider(observation.getAuthor().get(0).getAssignedAuthor()));
		else 
			res.setOrderer(encounterInfo.getProvidersByRole(this.m_metadataUtil.getOrCreateEncounterRole(new CS<ParticipationAuthorOriginator>(ParticipationAuthorOriginator.Authororiginator))).iterator().next());


		// Method?
		if(observation.getMethodCode() != null &&
				observation.getMethodCode().size() == 1)
		{
			res.setMethod(this.m_conceptUtil.getOrCreateConceptAndEquivalents(observation.getMethodCode().get(0)));
		}
		else if(observation.getMethodCode() != null)
			throw new NotImplementedException("Multiple method codes are not supported by this version of OpenSHR");

		// site?
		if(observation.getTargetSiteCode() != null &&
				observation.getTargetSiteCode().size() == 1)
		{
			res.setTargetSite(this.m_conceptUtil.getOrCreateConceptAndEquivalents(observation.getTargetSiteCode().get(0)));
		}
		else if(observation.getTargetSiteCode() != null)
			throw new NotImplementedException("Multiple targetSite codes are not supported by this version of OpenSHR");

		// Goal value?
		if(observation.getValue() instanceof CV)
			res.setGoalCoded(this.m_conceptUtil.getOrCreateConcept((CV<?>)observation.getValue()));
		else if(observation.getValue() instanceof PQ)
		{
			// Units?
			PQ pqValue = (PQ)observation.getValue();
			ConceptNumeric conceptNumeric = Context.getConceptService().getConceptNumeric(orderConcept.getId());
			String conceptUnits = this.m_conceptUtil.getUcumUnitCode(conceptNumeric);
			if(!conceptUnits.equals(pqValue.getUnit()))
				pqValue = pqValue.convert(conceptUnits);
			res.setGoalNumeric(pqValue.toDouble());
		}
		else if(observation.getValue() instanceof INT)
			res.setGoalNumeric(((INT)observation.getValue()).toDouble());
		else if(observation.getValue() instanceof ST || observation.getValue() instanceof TEL || observation.getValue() instanceof ED)
			res.setGoalText(observation.getValue().toString());
		res.setCareSetting(this.m_metadataUtil.getOrCreateInpatientCareSetting());

		// Priority
		if(observation.getPriorityCode() != null)
			this.m_dataUtil.setOrderPriority(res, observation.getPriorityCode().getCode());

		// Order context
		OrderContext orderContext = new OrderContext();
		
		// Save the order 
		res = (ObservationOrder)Context.getOrderService().saveOrder(res, orderContext);

		// Is this an event? If so it happened in the past and isn't active so we have to discontinue it
		if(discontinueDate != null)
			try
			{
				res = (ObservationOrder)Context.getOrderService().discontinueOrder(res, observation.getStatusCode().getCode().getCode(), discontinueDate, null, encounterInfo);
			}
			catch(Exception e)
			{
				throw new DocumentPersistenceException(e);
			}

		return res;
    }



	/**
	 * Process an observation in a manner where the observation 
	 * DID OCCUR (moodCode = EVN)
	 * @throws DocumentImportException 
	 */
	protected BaseOpenmrsData processEventOccurance(Observation observation) throws DocumentImportException {

		// Create concept and datatype services
		Encounter encounterInfo = (Encounter)this.getEncounterContext().getParsedObject();
		Obs parentObs = (Obs)this.getContext().getParsedObject();
		
		// TODO: Get an existing obs and do an update to the obs? or void it because the new encounter supersedes it..
		// Void any existing obs that have the same id
		Obs previousObs = super.voidOrThrowIfPreviousObsExists(observation.getReference(), encounterInfo.getPatient(), observation.getId());

		
		ExtendedObs res = new ExtendedObs();
		res.setPreviousVersion(previousObs);
		res.setObsGroup(parentObs);
		// Now ... Get the encounter and copy cascade what can be cascaded
		res.setPerson(encounterInfo.getPatient());
		res.setLocation(encounterInfo.getLocation());
		res.setDateCreated(encounterInfo.getDateCreated());
		res.setEncounter(encounterInfo);
		
		
		if(observation.getId() != null && !observation.getId().isNull())
			res.setAccessionNumber(this.m_datatypeUtil.formatIdentifier(observation.getId().get(0)));
		
		// Set the creator
		super.setCreator(res, observation);
		
		// Value may be changed
		ANY value = observation.getValue();
		
		// Code 
		Concept concept = this.m_conceptUtil.getTypeSpecificConcept(observation.getCode(), observation.getValue());
		if(concept == null && observation.getValue() != null && this.m_configuration.getAutoCreateConcepts())
		{
			concept = this.m_conceptUtil.createConcept(observation.getCode(), observation.getValue());
			// Try to add this concept as a valid set member of the context
			this.m_conceptUtil.addConceptToSet(parentObs.getConcept(), concept);
		}
		else if(concept == null)
			throw new DocumentImportException(String.format("Cannot reliably establish the type of observation concept to create based on code %s", observation.getCode()));
		else if(concept.getDatatype().getUuid().equals(ConceptDatatype.N_A_UUID) && 
				(observation.getValue() == null || 
				observation.getValue() instanceof CV || 
				observation.getValue() instanceof BL)) // NA is an indicator .. We need to figure out what the question was?
		{
			// Get potential question
			List<Concept> questions = Context.getConceptService().getConceptsByAnswer(concept);
			value = observation.getCode();
			if(questions.size() == 1)// There is only one question so we set the value
				concept = questions.get(0);
			else // Create a question indicator
				concept = this.m_conceptUtil.getOrCreateRMIMConcept(concept.getDisplayString() + " Indicator", value);
		}
		else if(!concept.getDatatype().equals(this.m_conceptUtil.getConceptDatatype(observation.getValue())))
			throw new DocumentImportException(String.format("Cannot store data of type %s in a concept field assigned %s", this.m_conceptUtil.getConceptDatatype(observation.getValue()).getName(), concept.getDatatype().getName()));
		res.setConcept(concept);
		
		// status
		if(observation.getStatusCode() != null)
			res.setObsStatus(this.m_conceptUtil.getOrCreateConcept(new CV<ActStatus>(observation.getStatusCode().getCode())));
		
		// Effective time is value
		if(observation.getEffectiveTime() != null)
		{
			// Date precisions least descriptive
			if(observation.getEffectiveTime().getValue() != null )
			{
				res.setObsDatetime(observation.getEffectiveTime().getValue().getDateValue().getTime());
				res.setObsDatePrecision(observation.getEffectiveTime().getValue().getDateValuePrecision());
			}
			// Low/high
			if(observation.getEffectiveTime().getLow() != null && !observation.getEffectiveTime().getLow().isNull())
			{
				res.setObsStartDate(observation.getEffectiveTime().getLow().getDateValue().getTime());
				if(res.getObsDatePrecision() > observation.getEffectiveTime().getLow().getDateValuePrecision())
					res.setObsDatePrecision(observation.getEffectiveTime().getLow().getDateValuePrecision());
			}
			if(observation.getEffectiveTime().getHigh() != null && !observation.getEffectiveTime().getHigh().isNull())
			{
				res.setObsEndDate(observation.getEffectiveTime().getHigh().getDateValue().getTime());
				if(res.getObsDatePrecision() > observation.getEffectiveTime().getHigh().getDateValuePrecision())
					res.setObsDatePrecision(observation.getEffectiveTime().getHigh().getDateValuePrecision());
			}
			
		}
		else
			res.setObsDatetime(encounterInfo.getEncounterDatetime());

		if(res.getObsDatetime() == null && res.getObsStartDate() == null && res.getObsEndDate() == null)
			res.setObsDatePrecision(0);
		if(res.getObsDatetime() == null)
			res.setObsDatetime(encounterInfo.getEncounterDatetime());

		// Comments?
		if(observation.getText() != null)
		{
			if(observation.getText().getReference() != null) // Reference
			{
				ProcessorContext sectionContext = this.getContext();
				while(!(sectionContext.getRawObject() instanceof Section))
					sectionContext = sectionContext.getParent();
				
				// Now find the text
				StructDocNode referencedNode = ((Section)sectionContext.getRawObject()).getText().findNodeById(observation.getText().getReference().getValue());
				if(referencedNode != null)
				{
					res.setComment(referencedNode.toPlainString());
				}
			}
		}

		// TODO: Move these sub-observations into proper Obs 
		// values via an extended Obs (which I'm not sure how to do)
		if(!observation.getMoodCode().getCode().equals(x_ActMoodDocumentObservation.Eventoccurrence))
			res.setObsMood(this.m_conceptUtil.getOrCreateConcept(new CE<x_ActMoodDocumentObservation>(observation.getMoodCode().getCode())));

		// Interpretation
		if(observation.getInterpretationCode() != null)
			for(CE<ObservationInterpretation> interpretationCode : observation.getInterpretationCode())
			{
				res.setObsInterpretation(this.m_conceptUtil.getOrCreateConcept(interpretationCode));
				break;
			}
				
		// Repeat number ... as an obs.. 
		if(observation.getRepeatNumber() != null && observation.getRepeatNumber().getValue() != null)
			res.setObsRepeatNumber(observation.getRepeatNumber().getValue().toInteger());

		// Get entry value
		res = (ExtendedObs)this.m_dataUtil.setObsValue(res, value);
		Context.getObsService().saveObs(res, null);
				
		// Is this really an indicator that is enabled?
		if(BL.FALSE.equals(observation.getValue()) || BL.TRUE.equals(observation.getNegationInd()))
		{
			Obs sub = this.m_dataUtil.addSubObservationValue(res, Context.getConceptService().getConcept(1729), Context.getConceptService().getConcept(Integer.valueOf(Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_FALSE_CONCEPT))));
			sub.setObsGroup(res);
			Context.getObsService().saveObs(sub, null);
		}
		
		// Method
		if(observation.getMethodCode() != null)
			for(CE<String> methodCode : observation.getMethodCode())
			{
				Obs sub = this.m_dataUtil.addSubObservationValue(res, this.m_conceptUtil.getOrCreateRMIMConcept(CdaHandlerConstants.RMIM_CONCEPT_UUID_METHOD, methodCode), methodCode);
				sub.setObsGroup(res);
				Context.getObsService().saveObs(sub, null);
			}

		// References
		for(Reference ref : observation.getReference())
		{
			if(ref.getTypeCode().getCode().equals(x_ActRelationshipExternalReference.REFR))
			{
				if(ref.getExternalActChoiceIfExternalDocument() != null &&
						ref.getExternalActChoiceIfExternalDocument().getId() != null &&
						!ref.getExternalActChoiceIfExternalDocument().getId().isEmpty())
					this.m_dataUtil.addSubObservationValue(res, this.m_conceptUtil.getOrCreateRMIMConcept(CdaHandlerConstants.RMIM_CONCEPT_UUID_REFERENCE, new ST()), this.m_datatypeUtil.formatIdentifier(ref.getExternalActChoiceIfExternalDocument().getId().get(0)));
			}
		}
		// Process any components
		ProcessorContext childContext = new ProcessorContext(observation, res, this);
		super.processEntryRelationships(observation, childContext);
		
		return res;
	}
	
	/**
	 * Validate the observation
	 */
	@Override
    public ValidationIssueCollection validate(IGraphable object) {
		
		ValidationIssueCollection validationIssues = super.validate(object);
		if(validationIssues.hasErrors()) return validationIssues;
		
		// Validate now
		ClinicalStatement statement = (ClinicalStatement)object;
		// Must be an observation
		if(!statement.isPOCD_MT000040UVObservation())
		{
			validationIssues.error(super.getInvalidClinicalStatementErrorText(Observation.class, statement.getClass()));
			return validationIssues;
		}
		
		Observation obs = (Observation)statement;
		CE<String> expectedCode = this.getExpectedCode();
		if(expectedCode != null && (obs.getCode() == null || !obs.getCode().semanticEquals(expectedCode).toBoolean()))
			validationIssues.warn(String.format("Observation carries code %s but expected %s", obs.getCode(), expectedCode.getCode()));
		else if(expectedCode != null && obs.getCode().getDisplayName() == null)
			obs.getCode().setDisplayName(expectedCode.getDisplayName());
		if(obs.getCode() == null)
			validationIssues.error("All observations must have a code to be imported");
		if(obs.getValue() instanceof CE && ((CE)obs.getValue()).isNull() )
			validationIssues.error("In order to persist coded values the value must be populated with data");
			
		
		if(obs.getInterpretationCode() != null)
			for(CE<ObservationInterpretation> code : obs.getInterpretationCode())
				if(code.getCode() != null && code.getCode().getCodeSystem() == null)
					validationIssues.error("Interpretation code must be drawn from ObservationInterpretation");
		if(obs.getMethodCode() != null)
			for(CE<String> code : obs.getMethodCode())
				if(code.getCode() != null && code.getCodeSystem() == null)
					validationIssues.error("MethodCode must specify the valuset from which it was drawn via CodeSystem attribute");
		
		
		return validationIssues;
	}


	/**
	 * Validates that the provided observation can be used in the 
	 * parentCode adding validation messages to validationIssues if necessary
	 */
	public void validateConceptWithContainer(CE<String> parentCode, Observation observation,
                                             ValidationIssueCollection validationIssues) {
		try {
	        Concept conceptGroup = this.m_conceptUtil.getConcept(parentCode, observation.getValue()),
	        		codedObservationConcept = this.m_conceptUtil.getTypeSpecificConcept(observation.getCode(), observation.getValue());
        // If we're not validating concept structure...
        
        if(!this.m_configuration.getValidateConceptStructure() && conceptGroup == null)
        	conceptGroup = this.m_conceptUtil.createConcept(parentCode);
        if(!this.m_configuration.getValidateConceptStructure() && codedObservationConcept == null)
        	codedObservationConcept = this.m_conceptUtil.createConcept(observation.getCode(), observation.getValue());
        
        
        // First check, is the coded observation concept understood by OpenMRS?
    	// Sometimes N/A applies for boolean as well as the observation is an indicator (i.e. the presence of this value
        // indicates true for the question concept)
        if(conceptGroup == null || codedObservationConcept == null)
        {
	        validationIssues.error(String.format("The observation concept %s is not understood or is not registered for this value of type %s type (hint: units may be incompatible)", observation.getCode(), observation.getValue().getClass()));
        }
        // Next check that the concept is a valid pregnancy concept
    	else if(!conceptGroup.getSetMembers().contains(codedObservationConcept))
    	{
    		StringBuilder allowedConcepts = new StringBuilder();
    		for(Concept gm : conceptGroup.getSetMembers())
    		{
    			allowedConcepts.append(gm.toString());
    			allowedConcepts.append(" ");
    		}
    		if(!this.m_configuration.getValidateConceptStructure())
	        	this.m_conceptUtil.addConceptToSet(conceptGroup, codedObservationConcept);
    		else
    			validationIssues.error(String.format("The code %s is not understood to be a valid concept according to the allowed values of %s = (%s)", codedObservationConcept, conceptGroup, allowedConcepts));
    	}
    }
    catch (DocumentImportException e) {
    	validationIssues.error(e.getMessage());
    }

    }
}
