package org.openmrs.module.shr.cdahandler.processor.entry.impl;

import java.util.List;

import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.ST;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Act;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Component4;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Entry;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Organizer;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActClassDocumentEntryOrganizer;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.DocumentValidationException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.context.ProcessorContext;
import org.openmrs.module.shr.cdahandler.processor.entry.EntryProcessor;
import org.openmrs.module.shr.cdahandler.processor.factory.impl.EntryProcessorFactory;
import org.openmrs.module.shr.cdahandler.processor.util.DatatypeProcessorUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsConceptUtil;

/**
 * Represents a generic class which can process organizers.
 * 
 * Organizers really don't need to be stored in oMRS, so we'll just process their components and cascade down the effective time
 */
public abstract class OrganizerEntryProcessor extends EntryProcessorImpl {

	/**
	 * Process the organizer
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.EntryProcessorImpl#process(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement)
	 */
	@Override
	public BaseOpenmrsData process(ClinicalStatement entry) throws DocumentImportException {
		ValidationIssueCollection validationIssues = this.validate(entry);
		if(validationIssues.hasErrors())
			throw new DocumentValidationException("Cannot process an invalid entry", validationIssues);

		// We want to process the organizer as an Obs
		Organizer organizer = (Organizer)entry;
		Obs organizerObs = this.parseOrganizer(organizer);
		organizerObs = Context.getObsService().saveObs(organizerObs, null);
		
		// Cascade properties and process
		ProcessorContext organizerContext = new ProcessorContext(organizer, organizerObs, this);
		DatatypeProcessorUtil datatypeUtil = DatatypeProcessorUtil.getInstance();
		EntryProcessorFactory factory = EntryProcessorFactory.getInstance();
		
		// Iterate through components
		for(Component4 comp : organizer.getComponent())
		{
			if(comp.getClinicalStatement() == null || comp.getClinicalStatement().getNullFlavor() != null)
				continue;
			
			ClinicalStatement statement = comp.getClinicalStatement();
			
			// Cascade data elements through the participation
			datatypeUtil.cascade(organizer, statement, "id","effectiveTime","author");
			
			// Create processor and then process
			EntryProcessor processor = factory.createProcessor(statement);
			processor.setContext(organizerContext);
			processor.process(statement);
			
		}
		return organizerObs;
	}

	/**
	 * Parse the organizer into an Obs group
	 * @throws DocumentImportException 
	 */
	protected Obs parseOrganizer(Organizer organizer) throws DocumentImportException {
		
		// Helper utils
		OpenmrsConceptUtil conceptUtil = OpenmrsConceptUtil.getInstance();
		DatatypeProcessorUtil datatypeUtil = DatatypeProcessorUtil.getInstance();
		
		Encounter encounterInfo = (Encounter)this.getEncounterContext().getParsedObject();
		
		// Organizer obs
		Obs organizerObs = new Obs(),
				parentObs = (Obs)this.getContext().getParsedObject();
		
		// The value will be a classifier of the type of organizer
		ST value = new ST(organizer.getClassCode().getCode().getCode());

		// Concept
		Concept concept = conceptUtil.getConcept(organizer.getCode());
		if(concept == null && organizer.getCode() != null)
		{
			concept = conceptUtil.createConcept(organizer.getCode(), value);
			// Try to add this concept as a valid set member of the context
			conceptUtil.addConceptToSet(parentObs.getConcept(), concept);
		}
		else if(concept == null)
			throw new DocumentImportException("Cannot reliably establish the type of organizer concept to create");
		organizerObs.setConcept(concept);
		
		// Time?
		if(organizer.getEffectiveTime() != null)
		{
			if(organizer.getEffectiveTime().getValue() != null && !organizer.getEffectiveTime().getValue().isNull())
				organizerObs.setObsDatetime(organizer.getEffectiveTime().getValue().getDateValue().getTime());
			else
				organizerObs.setObsDatetime(null);
		}
		else
			organizerObs.setObsDatetime(encounterInfo.getEncounterDatetime());

		// Accession (ID) number
		if(organizer.getId() != null)
			organizerObs.setAccessionNumber(datatypeUtil.formatIdentifier(organizer.getId().get(0)));
		
		// Copy encounter info
		organizerObs.setEncounter(encounterInfo);
		organizerObs.setObsGroup(parentObs);
		organizerObs.setPerson(encounterInfo.getPatient());
		organizerObs.setDateCreated(encounterInfo.getDateCreated());
		organizerObs.setValueText(value.toString());
		
		return organizerObs;
		
    }

	/**
	 * Get the expected codes
	 */
	public abstract CE<String> getExpectedCode();
	
	/**
	 * Get the components expected in this organizer
	 */
	protected abstract List<String> getExpectedComponents();
	
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
			validationIssues.error("All Organizers must carry a code");
		
		// Get expected components
		List<String> expectedComponents = this.getExpectedComponents();
		if(expectedComponents != null)
			for(String comp : expectedComponents)
				if(!this.hasComponent(organizer, comp))
					validationIssues.error(String.format("Organizer must have component matching template %s", comp));
		return validationIssues;
    }
	
	/**
	 * Returns true if the section contains the specified template
	 */
	public boolean hasComponent(Organizer organizer, String string) {
		II templateId = new II(string);
		for(Component4 ent : organizer.getComponent())
			if(ent.getClinicalStatement() == null || ent.getClinicalStatement().getNullFlavor() != null ||
			ent.getClinicalStatement().getTemplateId() == null)
				continue;
			else if(ent.getClinicalStatement().getTemplateId().contains(templateId))
				return true;
		return false;
				
    }
	
}
