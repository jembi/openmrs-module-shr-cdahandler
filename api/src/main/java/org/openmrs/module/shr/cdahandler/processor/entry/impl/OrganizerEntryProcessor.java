package org.openmrs.module.shr.cdahandler.processor.entry.impl;

import java.util.List;

import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.ST;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.datatypes.generic.CV;
import org.marc.everest.formatters.FormatterUtil;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Component4;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Organizer;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Reference;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ActStatus;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActRelationshipExternalReference;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.DocumentValidationException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.obs.ExtendedObs;
import org.openmrs.module.shr.cdahandler.processor.context.ProcessorContext;
import org.openmrs.module.shr.cdahandler.processor.entry.EntryProcessor;
import org.openmrs.module.shr.cdahandler.processor.factory.impl.EntryProcessorFactory;
import org.openmrs.module.shr.cdahandler.processor.util.DatatypeProcessorUtil;

/**
 * Represents a generic class which can process organizers.
 * 
 * Organizers really don't need to be stored in oMRS, so we'll just process their components and cascade down the effective time
 */
public abstract class OrganizerEntryProcessor extends EntryProcessorImpl {

	/**
	 * Get the expected codes
	 */
	public abstract CE<String> getExpectedCode();

	/**
	 * Get the components expected in this organizer
	 */
	protected abstract List<String> getExpectedComponents();

	/**
	 * Returns true if the section contains the specified template
	 */
	public boolean hasComponent(Organizer organizer, String string) {
		II templateId = new II(string);
		DatatypeProcessorUtil processorUtil = DatatypeProcessorUtil.getInstance();
		for(Component4 ent : organizer.getComponent())
			if(processorUtil.hasTemplateId(ent.getClinicalStatement(), templateId))
					return true;
		return false;
				
    }
	
	/**
	 * Parse the organizer into an Obs group
	 * @throws DocumentImportException 
	 */
	protected Obs parseOrganizer(Organizer organizer) throws DocumentImportException {
		
		Encounter encounterInfo = (Encounter)this.getEncounterContext().getParsedObject();
		
		// References to previous organizer?
		Obs previousObs = this.voidOrThrowIfPreviousObsExists(organizer.getReference(), encounterInfo.getPatient(), organizer.getId());
				
		// Organizer obs
		ExtendedObs organizerObs = new ExtendedObs();
		Obs parentObs = (Obs)this.getContext().getParsedObject();
		
		// The value will be a classifier of the type of organizer
		ST value = new ST(organizer.getClassCode().getCode().getCode());
		
		// Previous version
		organizerObs.setPreviousVersion(previousObs);
		
		// Set the creator
		super.setCreator(organizerObs, organizer);

		// Concept
		if(organizer.getCode() != null)
		{
			Concept concept = this.m_conceptUtil.getConcept(organizer.getCode());
			if(concept == null && organizer.getCode() != null)
			{
				concept = this.m_conceptUtil.createConcept(organizer.getCode(), value);
				// Try to add this concept as a valid set member of the context
				this.m_conceptUtil.addConceptToSet(parentObs.getConcept(), concept);
			}
			else if(concept == null)
				throw new DocumentImportException("Cannot reliably establish the type of organizer concept to create");
			organizerObs.setConcept(concept);
		}

		// status
		if(organizer.getStatusCode() != null)
			organizerObs.setObsStatus(this.m_conceptUtil.getOrCreateConcept(new CV<ActStatus>(organizer.getStatusCode().getCode())));

		// Effective time is value
		if(organizer.getEffectiveTime() != null)
		{
			// Date precisions least descriptive
			if(organizer.getEffectiveTime().getValue() != null )
			{
				organizerObs.setObsDatetime(organizer.getEffectiveTime().getValue().getDateValue().getTime());
				organizerObs.setObsDatePrecision(organizer.getEffectiveTime().getValue().getDateValuePrecision());
			}
			// Low/high
			if(organizer.getEffectiveTime().getLow() != null && !organizer.getEffectiveTime().getLow().isNull())
			{
				organizerObs.setObsStartDate(organizer.getEffectiveTime().getLow().getDateValue().getTime());
				if(organizerObs.getObsDatePrecision() > organizer.getEffectiveTime().getLow().getDateValuePrecision())
					organizerObs.setObsDatePrecision(organizer.getEffectiveTime().getLow().getDateValuePrecision());
			}
			if(organizer.getEffectiveTime().getHigh() != null && !organizer.getEffectiveTime().getHigh().isNull())
			{
				organizerObs.setObsEndDate(organizer.getEffectiveTime().getHigh().getDateValue().getTime());
				if(organizerObs.getObsDatePrecision() > organizer.getEffectiveTime().getHigh().getDateValuePrecision())
					organizerObs.setObsDatePrecision(organizer.getEffectiveTime().getHigh().getDateValuePrecision());
			}
			
		}
		else
			organizerObs.setObsDatetime(encounterInfo.getEncounterDatetime());
		
		// Accession (ID) number
		if(organizer.getId() != null)
			organizerObs.setAccessionNumber(this.m_datatypeUtil.formatIdentifier(organizer.getId().get(0)));
		
		// Copy encounter info
		organizerObs.setEncounter(encounterInfo);
		organizerObs.setObsGroup(parentObs);
		organizerObs.setPerson(encounterInfo.getPatient());
		organizerObs.setDateCreated(encounterInfo.getDateCreated());
		organizerObs.setValueText(value.toString());
		
		return organizerObs;
		
    }
	
	/**
	 * Process the organizer
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
		else if(!entry.isPOCD_MT000040UVOrganizer())
			throw new DocumentImportException("Expected entry to be an Organizer");
		
		// We want to process the organizer as an Obs
		Organizer organizer = (Organizer)entry;
		Obs organizerObs = this.parseOrganizer(organizer);
		organizerObs = Context.getObsService().saveObs(organizerObs, null);
		
		// Cascade properties and process
		ProcessorContext organizerContext = new ProcessorContext(organizer, organizerObs, this);
		EntryProcessorFactory factory = EntryProcessorFactory.getInstance();
 
		// Iterate through components
		for(Component4 comp : organizer.getComponent())
		{
			if(comp.getClinicalStatement() == null || comp.getClinicalStatement().getNullFlavor() != null)
				continue;
			
			ClinicalStatement statement = comp.getClinicalStatement();
			
			// Cascade data elements through the participation
			this.m_datatypeUtil.cascade(organizer, statement, "effectiveTime","author");
			
			// Create processor and then process
			EntryProcessor processor = factory.createProcessor(statement);
			if(processor == null)
	    	{
	    		log.warn(String.format("No processor found for entry type %s", FormatterUtil.toWireFormat(comp.getClinicalStatement().getTemplateId())));
	    		continue;
	    	}
			
			processor.setContext(organizerContext);
			processor.process(statement);
			
		}

		
		// Process entry relationships
		super.processEntryRelationships(entry, organizerContext);
		
		return organizerObs;
	}
	
	/**
	 * Validate the organizer
	 */
	@Override
    public ValidationIssueCollection validate(IGraphable object) {
		ValidationIssueCollection validationIssues = super.validate(object);
		if(validationIssues.hasErrors())
			return validationIssues;
		
		// Assert statement is an organizer
		ClinicalStatement statement = (ClinicalStatement)object;
		if(!statement.isPOCD_MT000040UVOrganizer())
			validationIssues.error(super.getInvalidClinicalStatementErrorText(Organizer.class, object.getClass()));
		
		Organizer organizer = (Organizer)statement;
		CE<String> expectedCode = this.getExpectedCode();
		if(expectedCode != null && (organizer.getCode() == null || !organizer.getCode().semanticEquals(expectedCode).toBoolean()))
			validationIssues.error(String.format("Expected code %s however Organizer carries code %s", expectedCode.getCode(), organizer.getCode().getCode()));
		else if(expectedCode != null && organizer.getCode().getDisplayName() == null)
			organizer.getCode().setDisplayName(expectedCode.getDisplayName());
		else if(organizer.getCode() == null)
			validationIssues.warn("All Organizers should carry a code");
		
		// Get expected components
		List<String> expectedComponents = this.getExpectedComponents();
		if(expectedComponents != null)
			for(String comp : expectedComponents)
				if(!this.hasComponent(organizer, comp))
					validationIssues.error(String.format("Organizer must have component matching template %s", comp));
		return validationIssues;
    }
	
}
