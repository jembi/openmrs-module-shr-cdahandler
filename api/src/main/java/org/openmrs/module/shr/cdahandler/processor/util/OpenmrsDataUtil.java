package org.openmrs.module.shr.cdahandler.processor.util;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.datatypes.ANY;
import org.marc.everest.datatypes.ED;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.INT;
import org.marc.everest.datatypes.MO;
import org.marc.everest.datatypes.PQ;
import org.marc.everest.datatypes.SD;
import org.marc.everest.datatypes.ST;
import org.marc.everest.datatypes.TS;
import org.marc.everest.datatypes.generic.CD;
import org.marc.everest.datatypes.generic.CV;
import org.marc.everest.datatypes.generic.RTO;
import org.marc.everest.datatypes.generic.SET;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerGlobalPropertyNames;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;
import org.openmrs.obs.ComplexData;

import ca.uhn.hl7v2.model.DataTypeUtil;

/**
 * Data utilities for OpenMRS
 */
public class OpenmrsDataUtil {
	
	// Log
	protected final Log log = LogFactory.getLog(this.getClass());

	// singleton instance
	private static OpenmrsDataUtil s_instance;
	private static Object s_lockObject = new Object();
	
	// Auto create concepts
	private Boolean m_autoCreateConcepts = true;
	
	/**
	 * Private ctor
	 */
	protected OpenmrsDataUtil()
	{
	}
	
	/**
	 * Initialize instance
	 */
	private void initializeInstance()
	{
		String propertyValue = Context.getAdministrationService().getGlobalProperty(CdaHandlerGlobalPropertyNames.AUTOCREATE_CONCEPTS);
		if(propertyValue != null  && !propertyValue.isEmpty())
			this.m_autoCreateConcepts = Boolean.parseBoolean(propertyValue);
		else
			Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(CdaHandlerGlobalPropertyNames.AUTOCREATE_CONCEPTS, this.m_autoCreateConcepts.toString()));
		
	}
	
	/**
	 * Get the singleton instance
	 */
	public static OpenmrsDataUtil getInstance()
	{
		if(s_instance == null)
		{
			synchronized (s_lockObject) {
				if(s_instance == null) // Another thread might have created while we were waiting for a lock
				{
					s_instance = new OpenmrsDataUtil();
					s_instance.initializeInstance();
				}
			}
		}
		return s_instance;
	}

	
	/**
	 * Creates a simple observation representing an RMIM type with type and value 
	 * @throws DocumentParseException 
	 * @throws ParseException 
	 */
	public Obs getRmimValueObservation(String code, TS date, ANY value) throws DocumentParseException {

		OpenmrsConceptUtil conceptUtil = OpenmrsConceptUtil.getInstance();
		Obs res = new Obs();
		
		// Set concept
		Concept concept = conceptUtil.getOrCreateRMIMConcept(code, value);
		if(!concept.getDatatype().equals(conceptUtil.getConceptDatatype(value)))
			throw new DocumentParseException("Cannot store the specified type of data in the concept field");
		
		// Set date
		res.setObsDatetime(date.getDateValue().getTime());
		
		try
		{
			// Set value
			this.setObsValue(res, value);
		}
		catch(ParseException e)
		{
			throw new DocumentParseException("Could not set value", e);
		}
		
		// return back to the caller for further modification
		return res;
    }
	
	/**
	 * Set the observation value using an appropriate call
	 * @throws ParseException 
	 * @throws DocumentParseException 
	 */
	public Obs setObsValue(Obs observation, ANY value) throws ParseException, DocumentParseException
	{
		// TODO: PQ should technically be a numeric with unit ... hmm...
		if(value instanceof PQ)
			observation.setValueNumeric(((PQ)value).getValue().doubleValue());
		else if(value instanceof RTO || value instanceof MO)
			observation.setValueAsString(value.toString());
		else if(value instanceof INT)
			observation.setValueNumeric(((INT) value).toDouble());
		else if(value instanceof TS)
			observation.setValueDatetime(((TS) value).getDateValue().getTime());
		else if(value instanceof ST)
			observation.setValueText(value.toString());
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
		else if(value instanceof CD)
		{
			OpenmrsConceptUtil conceptUtil = OpenmrsConceptUtil.getInstance();
			DatatypeProcessorUtil datatypeUtil = DatatypeProcessorUtil.getInstance();
			
			// Now comes a tricky bit
			// Is the value an OpenMRS concept
			Concept concept = conceptUtil.getOrCreateConceptAndEquivalents((CD<?>)value);
			
			// Is the concept in the list of answers?
			ConceptAnswer answer = null;
			for(ConceptAnswer ans : observation.getConcept().getAnswers())
				if(ans.equals(concept))
					answer = ans;
			if(answer == null && this.m_autoCreateConcepts)
			{
				answer = new ConceptAnswer();
				answer.setAnswerConcept(concept);
				answer.setConcept(observation.getConcept());
				observation.getConcept().addAnswer(answer);
				Context.getConceptService().saveConcept(observation.getConcept());
			}
			else if(answer == null)
				throw new DocumentParseException(String.format("Cannot assign code %s to observation concept %s as it is not a valid value", datatypeUtil.formatCodeValue((CV<?>)value), observation.getConcept().getName()));
			// Set the value
			observation.setValueCoded(concept);
				
		}
				
		return observation;
	}


}
