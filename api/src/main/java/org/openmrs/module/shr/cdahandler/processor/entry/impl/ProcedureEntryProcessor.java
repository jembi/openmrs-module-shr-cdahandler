package org.openmrs.module.shr.cdahandler.processor.entry.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.marc.everest.datatypes.ANY;
import org.marc.everest.datatypes.BL;
import org.marc.everest.datatypes.ST;
import org.marc.everest.datatypes.doc.StructDocNode;
import org.marc.everest.datatypes.generic.CD;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.datatypes.generic.CS;
import org.marc.everest.datatypes.generic.CV;
import org.marc.everest.formatters.FormatterUtil;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.EntryRelationship;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Observation;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Performer2;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Procedure;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Reference;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ActStatus;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ObservationInterpretation;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ParticipationAuthorOriginator;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActMoodDocumentObservation;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActRelationshipEntryRelationship;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActRelationshipExternalReference;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_DocumentProcedureMood;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Provider;
import org.openmrs.TestOrder;
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
import org.openmrs.module.shr.cdahandler.order.ProcedureOrder;
import org.openmrs.module.shr.cdahandler.processor.context.ProcessorContext;
import org.openmrs.module.shr.cdahandler.processor.util.AssignedEntityProcessorUtil;

/**
 * An entry processor that can handle the processing of procedures into obs
 */
public abstract class ProcedureEntryProcessor extends EntryProcessorImpl {

	// Provider utility
	protected final AssignedEntityProcessorUtil m_providerUtil = AssignedEntityProcessorUtil.getInstance();
	
	/**
	 * Process the clinical statement
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
		else if(!entry.isPOCD_MT000040UVProcedure())
			throw new DocumentImportException("Expected entry to be Procedure");
		
		// What is the mood code?
		Procedure procedure = (Procedure)entry;
		
		// Only store EVN or " I DID PERFORM " procedures, the other mood codes 
		// if encountered should create more appropriate data like an ORDER for INT (i.e. I intend to perform a procedure)
		if(procedure.getMoodCode().getCode().equals(x_DocumentProcedureMood.Eventoccurrence))
			return this.processEventOccurance(procedure);
		else if(procedure.getMoodCode().getCode().equals(x_DocumentProcedureMood.Intent))
			return this.processIntent(procedure);
		else
			throw new NotImplementedException("Only support procedures with moodCode = INT or EVN");
		
	}
	
	/**
	 * Process an intent to perform a procedure
	 * Auto generated method comment
	 * 
	 * @param procedure
	 * @return
	 * @throws DocumentImportException 
	 */
	protected BaseOpenmrsData processIntent(Procedure procedure) throws DocumentImportException {
		Encounter encounterInfo = (Encounter)this.getEncounterContext().getParsedObject();
		Obs parentObs = (Obs)this.getContext().getParsedObject();

		// Get current order and void if existing for an update
		Order previousOrder = super.voidOrThrowIfPreviousOrderExists(procedure.getReference(), encounterInfo.getPatient(), procedure.getId());

		// Now we create a new order
		ProcedureOrder res = new ProcedureOrder();
		// The type of procedure or administration done
		Concept orderConcept = null;
		if(procedure.getCode() != null && !procedure.getCode().isNull())
			orderConcept = this.m_conceptUtil.getOrCreateConceptAndEquivalents(procedure.getCode());
		else
			throw new DocumentImportException("Procedure order must have a code");
		
		// Set the procedure order fields
		res.setConcept(orderConcept);
		res.setPreviousOrder(previousOrder);
		res.setPatient(encounterInfo.getPatient());
		res.setDateCreated(encounterInfo.getDateCreated());
		res.setEncounter(encounterInfo);
		
		// Set the creator
		super.setCreator(res, procedure);
		
		// Is this a prescribe? 
		if(previousOrder != null)
			res.setAction(Action.REVISE);
		else
			res.setAction(Action.NEW);
			
		// Set the ID
		if(procedure.getId() != null && !procedure.getId().isNull())
			res.setAccessionNumber(this.m_datatypeUtil.formatIdentifier(procedure.getId().get(0)));
		
		// Effective time(s)
		Date discontinueDate = null;
		if(procedure.getEffectiveTime() != null && !procedure.getEffectiveTime().isNull())
		{
			if(procedure.getEffectiveTime().getLow() != null && !procedure.getEffectiveTime().getLow().isNull())
			{
				if(procedure.getStatusCode().getCode().equals(ActStatus.New))
					res.setScheduledDate(procedure.getEffectiveTime().getLow().getDateValue().getTime());
				else // Did occur
				{
					res.setDateActivated(procedure.getEffectiveTime().getLow().getDateValue().getTime());
					encounterInfo.setEncounterDatetime(res.getDateActivated());
					if(encounterInfo.getEncounterDatetime().before(encounterInfo.getVisit().getStartDatetime()))
						encounterInfo.getVisit().setStartDatetime(encounterInfo.getEncounterDatetime());
				}
			}
			if(procedure.getEffectiveTime().getHigh() != null && !procedure.getEffectiveTime().getHigh().isNull())
			{
				if(procedure.getStatusCode().getCode().equals(ActStatus.Active)  ||
						procedure.getStatusCode().getCode().equals(ActStatus.New))
					res.setAutoExpireDate(procedure.getEffectiveTime().getHigh().getDateValue().getTime());
				else
					discontinueDate = procedure.getEffectiveTime().getHigh().getDateValue().getTime();
			}
		}
		
		// Text?
		if(procedure.getText() != null && procedure.getText().getReference() != null)
		{
			StructDocNode node = this.getSection().getText().findNodeById(procedure.getText().getReference().getValue());
			if(node != null)
				res.setInstructions(node.toPlainString());
		}

		// Get orderer 
		if(procedure.getAuthor().size() == 1 &&
				procedure.getAuthor().get(0).getAssignedAuthor() != null
				)
			res.setOrderer(this.m_providerUtil.processProvider(procedure.getAuthor().get(0).getAssignedAuthor()));
		else 
			res.setOrderer(encounterInfo.getProvidersByRole(this.m_metadataUtil.getOrCreateEncounterRole(new CS<ParticipationAuthorOriginator>(ParticipationAuthorOriginator.Authororiginator))).iterator().next());
		
		// Priority
		if(procedure.getPriorityCode() != null)
			this.m_dataUtil.setOrderPriority(res, procedure.getPriorityCode().getCode());

		// Status
		if(procedure.getStatusCode() != null)
			res.setStatus(this.m_conceptUtil.getOrCreateConcept(new CV<String>(procedure.getStatusCode().getCode().getCode(), procedure.getStatusCode().getCode().getCodeSystem())));
		
		// Method?
		if(procedure.getApproachSiteCode() != null &&
				procedure.getApproachSiteCode().size() == 1)
		{
			res.setApproachSite(this.m_conceptUtil.getOrCreateConceptAndEquivalents(procedure.getApproachSiteCode().get(0)));
		}
		else if(procedure.getApproachSiteCode() != null)
			throw new NotImplementedException("Multiple approach site codes are not supported by this version of OpenSHR");

		// site?
		if(procedure.getTargetSiteCode() != null &&
				procedure.getTargetSiteCode().size() == 1)
		{
			res.setTargetSite(this.m_conceptUtil.getOrCreateConceptAndEquivalents(procedure.getTargetSiteCode().get(0)));
		}
		else if(procedure.getTargetSiteCode() != null)
			throw new NotImplementedException("Multiple targetSite codes are not supported by this version of OpenSHR");

		// Care setting (assumed no way to get this from CDA)
		res.setCareSetting(this.m_metadataUtil.getOrCreateInpatientCareSetting());

		// Order context
		OrderContext orderContext = new OrderContext();

		// Save the order 
		res = (ProcedureOrder)Context.getOrderService().saveOrder(res, orderContext);

		// Is this an event? If so it happened in the past and isn't active so we have to discontinue it
		if(discontinueDate != null)
			try
			{
				res = (ProcedureOrder)Context.getOrderService().discontinueOrder(res, procedure.getStatusCode().getCode().getCode(), discontinueDate, null, encounterInfo);
			}
			catch(Exception e)
			{
				throw new DocumentPersistenceException(e);
			}

		return res;
    }

	/**
	 * Process the event occurance of a procedure
	 * @throws DocumentImportException 
	 */
	protected BaseOpenmrsData processEventOccurance(Procedure procedure) throws DocumentImportException {
		// Create concept and datatype services
		Encounter encounterInfo = (Encounter)this.getEncounterContext().getParsedObject();
		Obs parentObs = (Obs)this.getContext().getParsedObject();
		
		// TODO: Get an existing obs and do an update the obs? or void it because the new encounter supersedes it..
		// Void any existing obs that have the same id
		Obs previousObs = super.voidOrThrowIfPreviousObsExists(procedure.getReference(), encounterInfo.getPatient(), procedure.getId());
		
		// Create the observation
		ExtendedObs res = new ExtendedObs();
		res.setPreviousVersion(previousObs);
		res.setObsGroup(parentObs);
		
		// Now ... Get the encounter and copy cascade what can be cascaded
		res.setPerson(encounterInfo.getPatient());
		res.setLocation(encounterInfo.getLocation());
		res.setDateCreated(encounterInfo.getDateCreated());
		res.setEncounter(encounterInfo);
		
		// Mood code in-case it is PRMS, RQO, or some other non EVN code
		if(procedure.getMoodCode() != null)
			res.setObsMood(this.m_conceptUtil.getOrCreateConcept(new CV<String>(procedure.getMoodCode().getCode().getCode(), procedure.getMoodCode().getCode().getCodeSystem())));
		
		// Set the accession number
		if(procedure.getId() != null && !procedure.getId().isNull())
			res.setAccessionNumber(this.m_datatypeUtil.formatIdentifier(procedure.getId().get(0)));
		
		// Set the creator
		super.setCreator(res, procedure);
		
		// The concept for the procedure is "Procedure History"
		res.setConcept(Context.getConceptService().getConcept(CdaHandlerConstants.CONCEPT_ID_PROCEDURE_HISTORY));

		// The procedure performed is a sub-observation
		if(procedure.getCode() != null && procedure.getCode().isNull())
			this.m_dataUtil.addSubObservationValue(res, Context.getConceptService().getConcept(CdaHandlerConstants.CONCEPT_ID_PROCEDURE), procedure.getCode());
		
		// The procedure date/time
		if(procedure.getEffectiveTime() != null && !procedure.getEffectiveTime().isNull())
		{
			if(procedure.getEffectiveTime().getValue() != null)
			{
				this.m_dataUtil.addSubObservationValue(res, Context.getConceptService().getConcept(CdaHandlerConstants.CONCEPT_ID_PROCEDURE_DATE), procedure.getEffectiveTime().getValue());
				res.setObsDatetime(procedure.getEffectiveTime().getValue().getDateValue().getTime());
				res.setObsDatePrecision(procedure.getEffectiveTime().getValue().getDateValuePrecision());
			}
			if(procedure.getEffectiveTime().getLow() != null && !procedure.getEffectiveTime().getLow().isNull())
			{
				res.setObsStartDate(procedure.getEffectiveTime().getLow().getDateValue().getTime());
				if(procedure.getEffectiveTime().getLow().getDateValuePrecision() < res.getObsDatePrecision())
					res.setObsDatePrecision(procedure.getEffectiveTime().getLow().getDateValuePrecision());
			}
			if(procedure.getEffectiveTime().getHigh() != null && !procedure.getEffectiveTime().getHigh().isNull())
			{
				res.setObsEndDate(procedure.getEffectiveTime().getHigh().getDateValue().getTime());
				if(procedure.getEffectiveTime().getHigh().getDateValuePrecision() < res.getObsDatePrecision())
					res.setObsDatePrecision(procedure.getEffectiveTime().getHigh().getDateValuePrecision());
			}
		}

		// Get the performer
		for(Performer2 prf : procedure.getPerformer())
		{
			if(prf.getAssignedEntity() != null)
			{
				Provider pvdr = this.m_assignedEntityUtil.processProvider(prf.getAssignedEntity());
				this.m_dataUtil.addSubObservationValue(res, Context.getConceptService().getConcept(CdaHandlerConstants.CONCEPT_ID_PROVIDER_NAME), pvdr.getIdentifier());
			}
		}
		
		// Comment
		if(procedure.getText() != null && !procedure.getText().isNull())
		{
			StructDocNode node = this.getSection().getText().findNodeById(procedure.getText().getReference().getValue());
			if(node != null)
				res.setComment(node.toPlainString());
		}
		
		// Status of the procedure
		if(procedure.getStatusCode() != null && !procedure.getStatusCode().isNull())
		{
			CV<ActStatus> statusCode = new CV<ActStatus>(procedure.getStatusCode().getCode());
			res.setObsStatus(this.m_conceptUtil.getOrCreateConcept(statusCode));
		}
		else
		{
			CV<ActStatus> completeCode = new CV<ActStatus>(ActStatus.Completed);
			res.setObsStatus(this.m_conceptUtil.getOrCreateConcept(completeCode));
		}
		
		
		// Approach site
		if(procedure.getApproachSiteCode() != null && !procedure.getApproachSiteCode().isNull())
			for(CD<String> code : procedure.getApproachSiteCode())
				this.m_dataUtil.addSubObservationValue(res, this.m_conceptUtil.getOrCreateRMIMConcept(CdaHandlerConstants.RMIM_CONCEPT_UUID_APPROACH_SITE, code), code);

		// Target site
		if(procedure.getTargetSiteCode() != null && !procedure.getTargetSiteCode().isNull())
			for(CD<String> code : procedure.getTargetSiteCode())
				this.m_dataUtil.addSubObservationValue(res, this.m_conceptUtil.getOrCreateRMIMConcept(CdaHandlerConstants.RMIM_CONCEPT_UUID_TARGET_SITE, code), code);

		// reasoning will link to another obs and indicates the reason for the procedure
		for(EntryRelationship er : this.findEntryRelationship(procedure, CdaHandlerConstants.ENT_TEMPLATE_INTERNAL_REFERENCE))
			if(er.getTypeCode().getCode().equals(x_ActRelationshipEntryRelationship.HasReason))
			{
				ST reasonText = new ST(this.m_datatypeUtil.formatIdentifier(er.getClinicalStatementIfAct().getId().get(0)));
				this.m_dataUtil.addSubObservationValue(res, this.m_conceptUtil.getOrCreateRMIMConcept(CdaHandlerConstants.RMIM_CONCEPT_UUID_REASON, reasonText), reasonText);
			}
			else if(er.getTypeCode().getCode().equals(x_ActRelationshipEntryRelationship.HasComponent) && BL.TRUE.equals(er.getInversionInd())) 
			{
				ST referenceText = new ST(this.m_datatypeUtil.formatIdentifier(er.getClinicalStatementIfAct().getId().get(0)));
				this.m_dataUtil.addSubObservationValue(res, this.m_conceptUtil.getOrCreateRMIMConcept(CdaHandlerConstants.RMIM_CONCEPT_UUID_REFERENCE, referenceText), referenceText);
			}
		
			
		
		// Save
		res = (ExtendedObs)Context.getObsService().saveObs(res, null);
		
		
		// Process any components
		ProcessorContext childContext = new ProcessorContext(procedure, res, this);
		super.processEntryRelationships(procedure, childContext);
				
		return res;
    }

	/**
	 * Validate the template
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.EntryProcessorImpl#validate(org.marc.everest.interfaces.IGraphable)
	 */
	@Override
    public ValidationIssueCollection validate(IGraphable object) {
	    ValidationIssueCollection issues = super.validate(object);
	    if(issues.hasErrors()) return issues;
	    
	    // Should be a procedure
	    ClinicalStatement statement = (ClinicalStatement)object;
	    if(!statement.isPOCD_MT000040UVProcedure())
	    	issues.error(super.getInvalidClinicalStatementErrorText(Procedure.class, statement.getClass()));
	    else
	    {
	    	Procedure procedure = (Procedure)statement;
	    	
	    	// Must have a code and id
	    	if(procedure.getId() == null || procedure.getId().isNull() || procedure.getId().isEmpty())
	    		issues.error("Procedure entry must carry an identifier");
			if(procedure.getCode() == null)
				issues.error("All procedures must have a code specifying the type of procedure performed");
			
	    }
	    
	    
	    return issues;
    }
	
	
}
