package org.openmrs.module.shr.cdahandler.processor.entry.impl;

import java.util.List;

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
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Procedure;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Reference;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ActStatus;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ObservationInterpretation;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActMoodDocumentObservation;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActRelationshipEntryRelationship;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActRelationshipExternalReference;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.DocumentValidationException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.context.ProcessorContext;

/**
 * An entry processor that can handle the processing of procedures into obs
 */
public abstract class ProcedureEntryProcessor extends EntryProcessorImpl {

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
		if(procedure.getMoodCode().getCode().equals(x_ActMoodDocumentObservation.Eventoccurrence))
			return this.processEventOccurance(procedure);
		else
			throw new DocumentImportException(String.format("Don't yet understand mood code '%s'", procedure.getMoodCode().getCode()));
		
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
		Obs previousObs = null;

		// References to previous observation?
		for(Reference reference : procedure.getReference())
			if(reference.getExternalActChoiceIfExternalAct() == null ||
				!reference.getTypeCode().getCode().equals(x_ActRelationshipExternalReference.RPLC))
				continue;
			else 
				previousObs = this.m_dataUtil.findExistingObs(reference.getExternalActChoiceIfExternalAct().getId(), encounterInfo.getPatient());

		if(previousObs != null)
			Context.getObsService().voidObs(previousObs, "Replaced");
		
		// Validate no duplicates 
		if(procedure.getId() != null &&
				this.m_dataUtil.findExistingObs(procedure.getId(), encounterInfo.getPatient()) != null)
			throw new DocumentImportException(String.format("Duplicate procedure %s. If you intend to replace it please use the replacement mechanism for CDA", FormatterUtil.toWireFormat(procedure.getId())));
		
		// Create the observation
		Obs res = new Obs();
		res.setPreviousVersion(previousObs);
		res.setObsGroup(parentObs);
		
		// Now ... Get the encounter and copy cascade what can be cascaded
		res.setPerson(encounterInfo.getPatient());
		res.setLocation(encounterInfo.getLocation());
		res.setDateCreated(encounterInfo.getDateCreated());
		res.setEncounter(encounterInfo);
		
		// Set the accession number
		if(procedure.getId() != null && !procedure.getId().isNull())
			res.setAccessionNumber(this.m_datatypeUtil.formatIdentifier(procedure.getId().get(0)));
		
		// Set the creator
		super.setCreator(res, procedure);
		
		// The concept for the procedure is "Procedure History"
		res.setConcept(Context.getConceptService().getConcept(160714));

		// The procedure performed is a sub-observation
		if(procedure.getCode() != null && procedure.getCode().isNull())
			this.m_dataUtil.addSubObservationValue(res, Context.getConceptService().getConcept(1651), procedure.getCode());
		
		// The procedure date/time
		if(procedure.getEffectiveTime() != null && !procedure.getEffectiveTime().isNull())
		{
			if(procedure.getEffectiveTime().getValue() != null)
				this.m_dataUtil.addSubObservationValue(res, Context.getConceptService().getConcept(160715), procedure.getEffectiveTime().getValue());
			else 
			{
				if(procedure.getEffectiveTime().getLow() != null && !procedure.getEffectiveTime().getLow().isNull())
					this.m_dataUtil.addSubObservationValue(res, Context.getConceptService().getConcept(160715), procedure.getEffectiveTime().getLow());
				if(procedure.getEffectiveTime().getHigh() != null && !procedure.getEffectiveTime().getHigh().isNull())
					this.m_dataUtil.addSubObservationValue(res, Context.getConceptService().getConcept(160715), procedure.getEffectiveTime().getHigh());
			}
		}
		
		// Comment
		if(procedure.getText() != null && !procedure.getText().isNull())
		{
			StructDocNode node = this.getSection().getText().findNodeById(procedure.getText().getReference().getValue());
			if(node != null)
				this.m_dataUtil.addSubObservationValue(res, Context.getConceptService().getConcept(160716), node.toPlainString());
		}
		
		// Status of the procedure
		if(procedure.getStatusCode() != null && !procedure.getStatusCode().isNull())
		{
			CV<ActStatus> statusCode = new CV<ActStatus>(procedure.getStatusCode().getCode());
			this.m_dataUtil.addSubObservationValue(res, this.m_conceptUtil.getOrCreateRMIMConcept("Status", statusCode), statusCode);
		}
		else
		{
			CV<ActStatus> completeCode = new CV<ActStatus>(ActStatus.Completed);
			this.m_dataUtil.addSubObservationValue(res, this.m_conceptUtil.getOrCreateRMIMConcept("Status", completeCode), completeCode);
		}
		
		// Approach site
		if(procedure.getApproachSiteCode() != null && !procedure.getApproachSiteCode().isNull())
			for(CD<String> code : procedure.getApproachSiteCode())
				this.m_dataUtil.addSubObservationValue(res, this.m_conceptUtil.getOrCreateRMIMConcept("Approach Site", code), code);

		// Target site
		if(procedure.getTargetSiteCode() != null && !procedure.getTargetSiteCode().isNull())
			for(CD<String> code : procedure.getTargetSiteCode())
				this.m_dataUtil.addSubObservationValue(res, this.m_conceptUtil.getOrCreateRMIMConcept("Target Site", code), code);

		// reasoning will link to another obs and indicates the reason for the procedure
		for(EntryRelationship er : this.findEntryRelationship(procedure, CdaHandlerConstants.ENT_TEMPLATE_INTERNAL_REFERENCE))
			if(er.getTypeCode().getCode().equals(x_ActRelationshipEntryRelationship.HasReason))
			{
				ST reasonText = new ST(this.m_datatypeUtil.formatIdentifier(er.getClinicalStatementIfAct().getId().get(0)));
				this.m_dataUtil.addSubObservationValue(res, this.m_conceptUtil.getOrCreateRMIMConcept("Reason", reasonText), reasonText);
			}
		
		// Save
		res = Context.getObsService().saveObs(res, null);
		
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
