package org.openmrs.module.shr.cdahandler.processor.util;

import java.io.ByteArrayInputStream;
import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.datatypes.ANY;
import org.marc.everest.datatypes.BL;
import org.marc.everest.datatypes.CO;
import org.marc.everest.datatypes.ED;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.INT;
import org.marc.everest.datatypes.MO;
import org.marc.everest.datatypes.PQ;
import org.marc.everest.datatypes.SD;
import org.marc.everest.datatypes.ST;
import org.marc.everest.datatypes.TS;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.datatypes.generic.CV;
import org.marc.everest.datatypes.generic.RTO;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Author;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.openmrs.module.shr.cdahandler.configuration.CdaHandlerConfiguration;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.obs.ComplexData;

/**
 * Data utilities for OpenMRS
 */
public final class OpenmrsDataUtil {
	
	/**
	 * Get the singleton instance
	 */
	public static OpenmrsDataUtil getInstance()
	{
		if(s_instance == null)
		{
			synchronized (s_lockObject) {
				if(s_instance == null) // Another thread might have created while we were waiting for a lock
					s_instance = new OpenmrsDataUtil();
			}
		}
		return s_instance;
	}

	// Log
	protected final Log log = LogFactory.getLog(this.getClass());
	// singleton instance
	private static OpenmrsDataUtil s_instance;

	private static Object s_lockObject = new Object();
	// Util classes
	private final CdaHandlerConfiguration m_configuration = CdaHandlerConfiguration.getInstance();
	private final OpenmrsConceptUtil m_conceptUtil = OpenmrsConceptUtil.getInstance();
	
	
	private final DatatypeProcessorUtil m_datatypeUtil = DatatypeProcessorUtil.getInstance();
	
	/**
	 * Private ctor
	 */
	protected OpenmrsDataUtil()
	{
	}

	
	/**
	 * Creates a simple observation representing an RMIM type with type and value 
	 * @throws DocumentImportException 
	 * @throws ParseException 
	 */
	public Obs getRmimValueObservation(String code, TS date, ANY value) throws DocumentImportException {

		Obs res = new Obs();
		
		// Set concept
		Concept concept = this.m_conceptUtil.getOrCreateRMIMConcept(code, value);
		if(!concept.getDatatype().equals(this.m_conceptUtil.getConceptDatatype(value)))
			throw new DocumentImportException("Cannot store the specified type of data in the concept field");
		res.setConcept(concept);
		// Set date
		res.setObsDatetime(date.getDateValue().getTime());
		res = this.setObsValue(res, value);
		// return back to the caller for further modification
		return res;
    }
	
	/**
	 * Get the user from the provider
	 */
	public User getUser(Provider provider) {
		for(User user : Context.getUserService().getUsersByPerson(provider.getPerson(), false))
			return user;
		return null;
    }

	/**
	 * Get a visit by its id
	 * @return
	 * @throws DocumentImportException 
	 * @throws InvalidCustomValueException 
	 */
	public Visit getVisitById(II id, Patient patient) throws InvalidCustomValueException, DocumentImportException {
		for(Visit visit : Context.getVisitService().getActiveVisitsByPatient(patient))
		{
			for(VisitAttribute attr : visit.getAttributes())
				if(attr.getAttributeType().equals(this.m_conceptUtil.getOrCreateVisitExternalIdAttributeType()) &&
					attr.getValue().equals(this.m_datatypeUtil.formatIdentifier(id)))
					return visit;
		}
		return null;
    }

	/**
	 * Set the observation value using an appropriate call
	 * @throws ParseException 
	 * @throws DocumentImportException 
	 */
	public Obs setObsValue(Obs observation, ANY value) throws DocumentImportException
	{
		// TODO: PQ should technically be a numeric with unit ... hmm...
		if(value instanceof PQ)
		{
			PQ pqValue = (PQ)value;
			ConceptNumeric conceptNumeric = Context.getConceptService().getConceptNumeric(observation.getConcept().getId());
			String conceptUnits = this.m_conceptUtil.getUcumUnitCode(conceptNumeric);
			if(!conceptUnits.equals(pqValue.getUnit()))
				pqValue = pqValue.convert(conceptUnits);
			log.debug(String.format("Storing value '%s' (original: '%s') to match concept type", pqValue, value));
			observation.setValueNumeric(pqValue.toDouble());
			
		}
		else if(value instanceof RTO || value instanceof MO)
			observation.setValueText(value.toString());
		else if(value instanceof INT)
			observation.setValueNumeric(((INT) value).toDouble());
		else if(value instanceof TS)
			observation.setValueDatetime(((TS) value).getDateValue().getTime());
		else if(value instanceof ST)
			observation.setValueText(value.toString());
		else if(value instanceof BL)
			observation.setValueBoolean(((BL)value).toBoolean());
		else if(value instanceof ED)
		{
			ByteArrayInputStream textStream = new ByteArrayInputStream(((ED) value).getData());
			ComplexData complexData = new ComplexData("observationdata", textStream);
			observation.setComplexData(complexData);
		}
		else if(value instanceof SD)
		{
			ByteArrayInputStream textStream = new ByteArrayInputStream(((SD) value).toString().getBytes());
			ComplexData complexData = new ComplexData("observationdata", textStream);
			observation.setComplexData(complexData);
		}
		else if(value instanceof CE || value instanceof CO)
		{
			
			CE<String> codeValue = null;
			if(value instanceof CO)
				codeValue = ((CO)value).getCode();
			else
				codeValue = (CE<String>)value;

			// Coded 
			if(codeValue != null)
			{
				Concept concept = this.m_conceptUtil.getTypeSpecificConcept(codeValue, null);
				if(concept == null) // Maybe an inappropriate concept then?
					concept = this.m_conceptUtil.getOrCreateConcept(codeValue);
				
				this.m_conceptUtil.addAnswerToConcept(observation.getConcept(), concept);
				observation.setValueCoded(concept);
			}
			else // ordinal
				observation.setValueNumeric(((CO)value).toDouble());
		}
		else
			throw new DocumentImportException("Cannot represent this concept!");
				
		return observation;
	}


}
