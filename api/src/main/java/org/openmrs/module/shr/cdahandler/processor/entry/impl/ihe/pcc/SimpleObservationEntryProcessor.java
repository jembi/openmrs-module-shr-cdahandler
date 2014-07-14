package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import java.text.ParseException;

import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Observation;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ActStatus;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ObservationInterpretation;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerOids;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;
import org.openmrs.module.shr.cdahandler.processor.entry.impl.EntryProcessorImpl;
import org.openmrs.module.shr.cdahandler.processor.util.DatatypeProcessorUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsConceptUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsDataUtil;

/**
 * Simple observation processor
 */
@ProcessTemplates (
	understands = {
			@TemplateId(root = CdaHandlerOids.ENT_TEMPLATE_SIMPLE_OBSERVATION)
	})
public class SimpleObservationEntryProcessor extends EntryProcessorImpl {
	
	/**
	 * Gets the template name
	 * @see org.openmrs.module.shr.cdahandler.processor.Processor#getTemplateName()
	 */
	@Override
	public String getTemplateName() {
		return "Simple Observations";
	}
	
	/**
	 * Process the observation
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.EntryProcessorImpl#process(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement)
	 */
	@Override
	public BaseOpenmrsData process(ClinicalStatement entry) throws DocumentParseException {
		
		if(!this.validate(entry))
			throw new DocumentParseException("Cannot process an invalid entry");
		// Create the observation for this template
		Obs observation = this.parseObservation((Observation)entry);
		observation = Context.getObsService().saveObs(observation, this.getTemplateName());
		return observation;
	}

	/**
	 * Parse the observation
	 * @throws DocumentParseException 
	 * @throws ParseException 
	 */
	protected Obs parseObservation(Observation entry) throws DocumentParseException {

		// Create concept and datatype services
		OpenmrsConceptUtil conceptUtil = OpenmrsConceptUtil.getInstance();
		DatatypeProcessorUtil datatypeUtil = DatatypeProcessorUtil.getInstance();
		OpenmrsDataUtil dataUtil = OpenmrsDataUtil.getInstance();
		Encounter encounterInfo = (Encounter)this.getEncounterContext().getParsedObject();
		
		// TODO: Get an existing obs and do an update to the obs? or void it because the new encounter supersedes it..
		Obs res = new Obs();
		
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
		}
		else if(concept == null)
			throw new DocumentParseException("Cannot reliably establish the type of observation concept to create");
		else if(!concept.getDatatype().equals(conceptUtil.getConceptDatatype(entry.getValue())))
			throw new DocumentParseException("Cannot store the specified type of data in the concept field");
		res.setConcept(concept);
		
		// Effective time is value
		if(entry.getEffectiveTime() != null && entry.getEffectiveTime().getValue() != null)
			res.setObsDatetime(entry.getEffectiveTime().getValue().getDateValue().getTime());
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
			throw new DocumentParseException("Could not set obs value", e);
		}
		
		return res;
    }

	/**
	 * Validate this entry
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.EntryProcessorImpl#validate(org.marc.everest.interfaces.IGraphable)
	 */
	@Override
    public Boolean validate(IGraphable object) {
		Boolean isValid = super.validate(object);
		if(!isValid) return false;
		
		// Validate now
		ClinicalStatement statement = (ClinicalStatement)object;
		
		// Must be an observation
		if(!statement.isPOCD_MT000040UVObservation())
		{
			log.error(super.getInvalidClinicalStatementErrorText(Observation.class, statement.getClass()));
			isValid = false;
		}
		// Must have a code
		Observation obs = (Observation)statement;
		if(obs.getId() == null || obs.getId().isEmpty())
		{
			log.error("IHE PCC TF-2: Each observation shall have an identifier");
			isValid = false;
		}
		if(obs.getCode() == null || obs.getCode().isNull())
		{
			log.error("IHE PCC TF-2: Observations shall have a code describing what is measured");
			isValid = false;
		}
		if(obs.getStatusCode() == null)
		{
			log.warn("IHE PCC TF-2: Observations shall have a status of completed");
			obs.setStatusCode(ActStatus.Completed);
		}
		if(obs.getValue() == null)
		{
			log.error("IHE PCC TF-2: Observation shall have a value appropriate with the observation type");
			isValid = false;
		}
		if(obs.getEffectiveTime() == null || obs.getEffectiveTime().getValue() == null)
		{
			log.error("IHE PCC TF-2: Observations shall have an effective time");
			isValid = false;
		}
		return isValid;
    }
	
	
	
}
