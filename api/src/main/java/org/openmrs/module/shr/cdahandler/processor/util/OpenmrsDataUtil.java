package org.openmrs.module.shr.cdahandler.processor.util;

import java.io.ByteArrayInputStream;
import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.datatypes.ANY;
import org.marc.everest.datatypes.ED;
import org.marc.everest.datatypes.INT;
import org.marc.everest.datatypes.MO;
import org.marc.everest.datatypes.PQ;
import org.marc.everest.datatypes.SD;
import org.marc.everest.datatypes.ST;
import org.marc.everest.datatypes.TS;
import org.marc.everest.datatypes.generic.CD;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.datatypes.generic.CS;
import org.marc.everest.datatypes.generic.CV;
import org.marc.everest.datatypes.generic.RTO;
import org.marc.everest.interfaces.IEnumeratedVocabulary;
import org.marc.everest.util.SimpleSiUnitConverter;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.obs.ComplexData;

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
		String propertyValue = Context.getAdministrationService().getGlobalProperty(CdaHandlerConstants.PROP_AUTOCREATE_CONCEPTS);
		if(propertyValue != null  && !propertyValue.isEmpty())
			this.m_autoCreateConcepts = Boolean.parseBoolean(propertyValue);
		else
			Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(CdaHandlerConstants.PROP_AUTOCREATE_CONCEPTS, this.m_autoCreateConcepts.toString()));
		
		/**
		 * Initialize the converters for PQ
		 */
		PQ.getUnitConverters().add(new SimpleSiUnitConverter());
		PQ.getUnitConverters().add(new SimpleOpenmrsConceptUnitConverter());
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
	 * @throws DocumentImportException 
	 * @throws ParseException 
	 */
	public Obs getRmimValueObservation(String code, TS date, ANY value) throws DocumentImportException {

		OpenmrsConceptUtil conceptUtil = OpenmrsConceptUtil.getInstance();
		Obs res = new Obs();
		
		// Set concept
		Concept concept = conceptUtil.getOrCreateRMIMConcept(code, value);
		if(!concept.getDatatype().equals(conceptUtil.getConceptDatatype(value)))
			throw new DocumentImportException("Cannot store the specified type of data in the concept field");
		res.setConcept(concept);
		// Set date
		res.setObsDatetime(date.getDateValue().getTime());
		
		
		try
		{
			// Set value
			res = this.setObsValue(res, value);
		}
		catch(ParseException e)
		{
			throw new DocumentImportException("Could not set value", e);
		}
		
		// return back to the caller for further modification
		return res;
    }
	
	/**
	 * Set the observation value using an appropriate call
	 * @throws ParseException 
	 * @throws DocumentImportException 
	 */
	public Obs setObsValue(Obs observation, ANY value) throws ParseException, DocumentImportException
	{
		// TODO: PQ should technically be a numeric with unit ... hmm...
		if(value instanceof PQ)
		{
			PQ pqValue = (PQ)value;
			ConceptNumeric conceptNumeric = Context.getConceptService().getConceptNumeric(observation.getConcept().getId());
			pqValue = pqValue.convert(conceptNumeric.getUnits());
			log.debug(String.format("Convert '%s' to '%s' to match concept type", value, pqValue));
			pqValue = pqValue.convert(conceptNumeric.getUnits());
			observation.setValueNumeric(pqValue.toDouble());
		}
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
		else if(value instanceof CV)
		{
			OpenmrsConceptUtil conceptUtil = OpenmrsConceptUtil.getInstance();
			
			// Set code system if possible... 
			// Is the value an OpenMRS concept
			Concept concept = conceptUtil.getOrCreateConcept((CV<?>)value);
			conceptUtil.addAnswerToConcept(observation.getConcept(), concept);
			observation.setValueCoded(concept);
				
		}
		else
			throw new DocumentImportException("Cannot represent this concept!");
				
		return observation;
	}


}
