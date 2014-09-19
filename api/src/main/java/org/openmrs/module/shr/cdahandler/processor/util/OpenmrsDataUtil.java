package org.openmrs.module.shr.cdahandler.processor.util;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;

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
import org.marc.everest.datatypes.TEL;
import org.marc.everest.datatypes.TS;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.datatypes.generic.CS;
import org.marc.everest.datatypes.generic.CV;
import org.marc.everest.datatypes.generic.RTO;
import org.marc.everest.datatypes.generic.SET;
import org.marc.everest.interfaces.IEnumeratedVocabulary;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Author;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ActPriority;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.Order.Urgency;
import org.openmrs.activelist.Allergy;
import org.openmrs.activelist.Problem;
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
	public Obs createRmimValueObservation(String code, TS date, ANY value) throws DocumentImportException {

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
		else if(value instanceof II)
			observation.setValueText(this.m_datatypeUtil.formatIdentifier((II)value));
		else if(value instanceof INT)
			observation.setValueNumeric(((INT) value).toDouble());
		else if(value instanceof TS)
			observation.setValueDatetime(((TS) value).getDateValue().getTime());
		else if(value instanceof ST || value instanceof TEL)
			observation.setValueText(value.toString());
		else if(value instanceof BL)
			observation.setValueBoolean(((BL)value).toBoolean());
		else if(value instanceof ED)
		{
			// HACK: Find a better way of doing this
			String title = UUID.randomUUID().toString() + " -- " + URLEncoder.encode(((ED)value).getMediaType()) + ".bin";
			ByteArrayInputStream textStream = new ByteArrayInputStream(((ED)value).getData());
			ComplexData complexData = new ComplexData(title, textStream);
			observation.setComplexData(complexData);
		}
		else if(value instanceof SD)
		{
			ByteArrayInputStream textStream = new ByteArrayInputStream(((SD) value).toString().getBytes());
			ComplexData complexData = new ComplexData("observationdata", textStream);
			observation.setComplexData(complexData);
		}
		else if(value instanceof CS || value instanceof CO)
		{
			
			CE<?> codeValue = null;
			if(value instanceof CO)
			{
				if(((CO)value).getValue() != null)
					observation.setValueNumeric(((CO)value).toDouble());
				else
					codeValue = ((CO)value).getCode();
			}
			else
			{
				CS<?> csValue = (CS<?>)value;
				if(value.getDataType().equals(CS.class) && csValue.getCode() instanceof IEnumeratedVocabulary)
					codeValue = new CE<String>(csValue.getCode().toString(), ((IEnumeratedVocabulary)csValue.getCode()).getCodeSystem());
				else
					codeValue = (CE<?>)value;
			}
			
			// Coded 
			if(codeValue != null)
			{
				Concept concept = this.m_conceptUtil.getTypeSpecificConcept(codeValue, null);
				if(concept == null) // Maybe an inappropriate concept then?
					concept = this.m_conceptUtil.getOrCreateConcept(codeValue);
				
				log.debug(String.format("Adding %s to %s", concept, observation.getConcept()));
				this.m_conceptUtil.addAnswerToConcept(observation.getConcept(), concept);
				observation.setValueCoded(concept);
			}
		}
		else
			throw new DocumentImportException("Cannot represent this concept!");
				
		return observation;
	}


	/**
	 * Find an obs by the set of ids
	 */
	public <T extends BaseOpenmrsData> T findExistingItem(SET<II> ids, String shrRoot, List<T> existingCollection) {

		T retVal = null;
		
		for(T itm : existingCollection)
			for(II id : ids)
				if(this.m_datatypeUtil.formatIdentifier(id).equals(this.m_datatypeUtil.emptyIdString()))
					continue; // no id ... typically this is <id/> elements which aren't valid but we'll process them anyways
				else if(id.getRoot().equals(shrRoot) && itm.getId().toString().equals(id.getExtension()))
					return itm;
				else if(itm instanceof Obs &&
						((Obs)itm).getAccessionNumber() != null &&
						((Obs)itm).getAccessionNumber().equals(this.m_datatypeUtil.formatIdentifier(id)))
					return itm;
				else if(itm instanceof Order &&
						((Order)itm).getAccessionNumber() != null &&
						((Order)itm).getAccessionNumber().equals(this.m_datatypeUtil.formatIdentifier(id)))
					return itm;
				
		return null;
    }

	/**
	 * Find an existing obs 
	 */
	public Obs findExistingObs(SET<II> ids, Patient patient)
	{
		return this.findExistingItem(ids, this.m_configuration.getObsRoot(), Context.getObsService().getObservationsByPerson(patient));
	}

	/**
	 * Find an existing obs 
	 */
	public Order findExistingOrder(SET<II> ids, Patient patient)
	{
		return this.findExistingItem(ids, this.m_configuration.getOrderRoot(), Context.getOrderService().getAllOrdersByPatient(patient));
	}

	/**
	 * Find an existing obs 
	 */
	public Allergy findExistingAllergy(SET<II> ids, Patient patient)
	{
		return (Allergy)this.findExistingItem(ids, this.m_configuration.getAllergyRoot(), Context.getActiveListService().getActiveListItems(patient, Allergy.ACTIVE_LIST_TYPE));
	}


	/**
	 * Find an existing obs 
	 */
	public Problem findExistingProblem(SET<II> ids, Patient patient)
	{
		return (Problem)this.findExistingItem(ids, this.m_configuration.getAllergyRoot(), Context.getActiveListService().getActiveListItems(patient, Problem.ACTIVE_LIST_TYPE));
	}


	/**
	 * Adds the specified obs to the parentObs ensuring that the concept is a valid concept for the parent obs
	 * @throws DocumentImportException 
	 */
	public Obs addSubObservationValue(Obs parentObs, Concept obsConcept, Object value) throws DocumentImportException {
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
			this.setObsValue(res, (ANY)value);
		else if(value instanceof Concept)
			res.setValueCoded((Concept)value);
		else if(value instanceof String)
			res.setValueText(value.toString());
		
		parentObs.addGroupMember(res);
		//res.setObsGroup(parentObs);
		//res = Context.getObsService().saveObs(res, null);
		return res;
    }


	/**
	 * Set properties on the order as described by the priority code
	 * @throws DocumentImportException 
	 */
	public void setOrderPriority(Order order, ActPriority priorityCode) throws DocumentImportException {
		if(priorityCode.equals(ActPriority.ASAP))
		{
			order.setUrgency(Urgency.STAT);
			order.setCommentToFulfiller("ASAP");
		}
		else if(priorityCode.equals(ActPriority.AsNeeded) && order instanceof DrugOrder)
			((DrugOrder)order).setAsNeeded(true);
		else if(priorityCode.equals(ActPriority.CallbackForScheduling))
			order.setInstructions("Callback for scheduling");
		else if(priorityCode.equals(ActPriority.Stat))
			order.setUrgency(Urgency.STAT);
		else if(priorityCode.equals(ActPriority.TimingCritical))
			order.setUrgency(Urgency.ON_SCHEDULED_DATE);
		else if(priorityCode.equals(ActPriority.Routine))
			order.setUrgency(Urgency.ROUTINE);
		else if(priorityCode.equals(ActPriority.Emergency))
		{
			order.setCommentToFulfiller("Emergency");
			order.setUrgency(Urgency.STAT);
		}
		else
			throw new DocumentImportException(String.format("OpenSHR has no mechanism to represent priority code of %s", priorityCode.getCode()));
    }
}
