package org.openmrs.module.shr.cdahandler.processor.entry.impl;

import java.text.ParseException;

import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Observation;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ObservationInterpretation;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
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
		// Create the observation for this template
		Obs observation = this.parseObservation((Observation)entry);
		observation = Context.getObsService().saveObs(observation, null);
		return observation;
	}

	/**
	 * Parse the observation
	 * @throws DocumentImportException 
	 * @throws ParseException 
	 */
	protected Obs parseObservation(Observation entry) throws DocumentImportException {

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
		
		if(entry.getId() != null && !entry.getId().isNull())
			res.setAccessionNumber(datatypeUtil.formatIdentifier(entry.getId().get(0)));
		
		// Code 
		Concept concept = conceptUtil.getTypeSpecificConcept(entry.getCode(), entry.getValue());
		if(concept == null && entry.getValue() != null)
		{
			concept = conceptUtil.createConcept(entry.getCode(), entry.getValue());
			// Try to add this concept as a valid set member of the context
			conceptUtil.addConceptToSet(parentObs.getConcept(), concept);
		}
		else if(concept == null)
			throw new DocumentImportException("Cannot reliably establish the type of observation concept to create");
		else if(!concept.getDatatype().equals(conceptUtil.getConceptDatatype(entry.getValue())))
			throw new DocumentImportException("Cannot store the specified type of data in the concept field");
		res.setConcept(concept);
		
		// Effective time is value
		if(entry.getEffectiveTime() != null)
		{
			if(entry.getEffectiveTime().getValue() != null && !entry.getEffectiveTime().getValue().isNull())
				res.setObsDatetime(entry.getEffectiveTime().getValue().getDateValue().getTime());
			else 
				res.setObsDatetime(null); // the time isn't known or can't be represented in oMRS
			
		}
		else
			res.setObsDatetime(encounterInfo.getEncounterDatetime());
		
		// Repeat number ... as an obs.. 
		if(entry.getRepeatNumber() != null && entry.getRepeatNumber().getValue() != null)
		{
			Obs repeatObs = dataUtil.getRmimValueObservation(conceptUtil.getLocalizedString("obs.repeatNumber"), entry.getEffectiveTime().getValue(), entry.getRepeatNumber().getValue());
			repeatObs.setPerson(res.getPerson());
			repeatObs.setLocation(res.getLocation());
			repeatObs.setEncounter(res.getEncounter());
			res.addGroupMember(repeatObs);
		}
		
		// Method
		if(entry.getMethodCode() != null)
			for(CE<String> methodCode : entry.getMethodCode())
			{
				Obs methodObs = dataUtil.getRmimValueObservation(conceptUtil.getLocalizedString("obs.methodCode"), entry.getEffectiveTime().getValue(), methodCode);
				methodObs.setPerson(res.getPerson());
				methodObs.setLocation(res.getLocation());
				methodObs.setEncounter(res.getEncounter());
				res.addGroupMember(methodObs);
			}

		// Interpretation
		if(entry.getInterpretationCode() != null)
			for(CE<ObservationInterpretation> methodCode : entry.getInterpretationCode())
			{
				Obs interprationObs = dataUtil.getRmimValueObservation(conceptUtil.getLocalizedString("obs.interpretationCode"), entry.getEffectiveTime().getValue(), methodCode);
				interprationObs.setPerson(res.getPerson());
				interprationObs.setLocation(res.getLocation());
				interprationObs.setEncounter(res.getEncounter());
				res.addGroupMember(interprationObs);
			}

		// Get entry value
		try
		{
			dataUtil.setObsValue(res, entry.getValue());
		}
		catch(ParseException e)
		{
			throw new DocumentImportException("Could not set obs value", e);
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
		
		return validationIssues;
	}
}
