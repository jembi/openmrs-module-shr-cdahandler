package org.openmrs.module.shr.cdahandler.processor.entry.impl;

import java.text.ParseException;
import java.util.List;

import org.marc.everest.datatypes.ANY;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.datatypes.generic.CV;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Observation;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ObservationInterpretation;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptNumeric;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.DocumentPersistenceException;
import org.openmrs.module.shr.cdahandler.exception.DocumentValidationException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.util.DatatypeProcessorUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsConceptUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsDataUtil;


/**
 * Represents a processor for Observation Entries
 */
public abstract class ObservationEntryProcessor extends EntryProcessorImpl {
	
	
	/**
	 * Process the observation
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.EntryProcessorImpl#process(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement)
	 */
	@Override
	public BaseOpenmrsData process(ClinicalStatement entry) throws DocumentImportException {
		
		ValidationIssueCollection validationIssues = this.validate(entry);
		if(validationIssues.hasErrors())
			throw new DocumentValidationException("Cannot process an invalid entry", validationIssues);

		// Observation from entry
		Observation observation = (Observation)entry;
		
		// Create concept and datatype services
		OpenmrsConceptUtil conceptUtil = OpenmrsConceptUtil.getInstance();
		DatatypeProcessorUtil datatypeUtil = DatatypeProcessorUtil.getInstance();
		OpenmrsDataUtil dataUtil = OpenmrsDataUtil.getInstance();
		Encounter encounterInfo = (Encounter)this.getEncounterContext().getParsedObject();
		Obs parentObs = (Obs)this.getContext().getParsedObject();
		
		// TODO: Get an existing obs and do an update to the obs? or void it because the new encounter supersedes it..
		Obs res = new Obs();
		
		res.setObsGroup(parentObs);
		// Now ... Get the encounter and copy cascade what can be cascaded
		res.setPerson(encounterInfo.getPatient());
		res.setLocation(encounterInfo.getLocation());
		res.setDateCreated(encounterInfo.getDateCreated());
		res.setEncounter(encounterInfo);
		
		if(observation.getId() != null && !observation.getId().isNull())
			res.setAccessionNumber(datatypeUtil.formatIdentifier(observation.getId().get(0)));
		
		// Value may be changed
		ANY value = observation.getValue();
		// Code 
		Concept concept = conceptUtil.getTypeSpecificConcept(observation.getCode(), observation.getValue());
		if(concept == null && observation.getValue() != null)
		{
			concept = conceptUtil.createConcept(observation.getCode(), observation.getValue());
			// Try to add this concept as a valid set member of the context
			conceptUtil.addConceptToSet(parentObs.getConcept(), concept);
		}
		else if(concept == null)
			throw new DocumentImportException(String.format("Cannot reliably establish the type of observation concept to create based on code %s", observation.getCode()));
		else if(concept.getDatatype().getUuid().equals(ConceptDatatype.N_A_UUID) && (observation.getValue() == null || observation.getValue() instanceof CV)) // NA is an indicator .. We need to figure out what the question was?
		{
			List<Concept> questions = Context.getConceptService().getConceptsByAnswer(concept);
			if(questions.size() == 1)// There is only one question so we set the value
			{
				concept = questions.get(0);
				value = observation.getCode();
			}
			else
				throw new DocumentImportException(String.format("Cannot store data of type %s in a concept field assigned %s", conceptUtil.getConceptDatatype(observation.getValue()).getName(), concept.getDatatype().getName()));
			
		}
		else if(!concept.getDatatype().equals(conceptUtil.getConceptDatatype(observation.getValue())))
			throw new DocumentImportException(String.format("Cannot store data of type %s in a concept field assigned %s", conceptUtil.getConceptDatatype(observation.getValue()).getName(), concept.getDatatype().getName()));
		
		res.setConcept(concept);
		
		// Effective time is value
		if(observation.getEffectiveTime() != null)
		{
			if(observation.getEffectiveTime().getValue() != null && !observation.getEffectiveTime().getValue().isNull())
				res.setObsDatetime(observation.getEffectiveTime().getValue().getDateValue().getTime());
			else 
				res.setObsDatetime(null); // the time isn't known or can't be represented in oMRS
			
		}
		else
			res.setObsDatetime(encounterInfo.getEncounterDatetime());

		// Get entry value
		try
		{
			res = dataUtil.setObsValue(res, value);
			res = Context.getObsService().saveObs(res, null);
		}
		catch(Exception e)
		{
			throw new DocumentPersistenceException("Could not set obs value", e);
		}
		
		
		// Repeat number ... as an obs.. 
		if(observation.getRepeatNumber() != null && observation.getRepeatNumber().getValue() != null)
		{
			Obs repeatObs = dataUtil.getRmimValueObservation(conceptUtil.getLocalizedString("obs.repeatNumber"), observation.getEffectiveTime().getValue(), observation.getRepeatNumber().getValue());
			repeatObs.setPerson(res.getPerson());
			repeatObs.setLocation(res.getLocation());
			repeatObs.setEncounter(res.getEncounter());
			repeatObs.setObsGroup(res);
			Context.getObsService().saveObs(repeatObs, null);
		}
		
		// Method
		if(observation.getMethodCode() != null)
			for(CE<String> methodCode : observation.getMethodCode())
			{
				Obs methodObs = dataUtil.getRmimValueObservation(conceptUtil.getLocalizedString("obs.methodCode"), observation.getEffectiveTime().getValue(), methodCode);
				methodObs.setPerson(res.getPerson());
				methodObs.setLocation(res.getLocation());
				methodObs.setEncounter(res.getEncounter());
				methodObs.setObsGroup(res);
				Context.getObsService().saveObs(methodObs, null);
			}

		// Interpretation
		if(observation.getInterpretationCode() != null)
			for(CE<ObservationInterpretation> interpretationCode : observation.getInterpretationCode())
			{
				Obs interpretationObs = dataUtil.getRmimValueObservation(conceptUtil.getLocalizedString("obs.interpretationCode"), observation.getEffectiveTime().getValue(), interpretationCode);
				interpretationObs.setPerson(res.getPerson());
				interpretationObs.setLocation(res.getLocation());
				interpretationObs.setEncounter(res.getEncounter());
				interpretationObs.setObsGroup(res);
				Context.getObsService().saveObs(interpretationObs, null);
			}

		return res;
	}




	/**
	 * Gets the code expected to be on this observation (null if none specified)
	 * @return
	 */
	protected abstract CE<String> getExpectedCode();
	
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
			validationIssues.error(super.getInvalidClinicalStatementErrorText(Observation.class, statement.getClass()));

		Observation obs = (Observation)statement;
		CE<String> expectedCode = this.getExpectedCode();
		if(expectedCode != null && (obs.getCode() == null || !obs.getCode().semanticEquals(expectedCode).toBoolean()))
			validationIssues.warn(String.format("Observation carries code %s but expected %s", obs.getCode(), expectedCode.getCode()));
		else if(expectedCode != null && obs.getCode().getDisplayName() == null)
			obs.getCode().setDisplayName(expectedCode.getDisplayName());
		if(obs.getCode() == null)
			validationIssues.error("All observations must have a code to be imported");

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
}
