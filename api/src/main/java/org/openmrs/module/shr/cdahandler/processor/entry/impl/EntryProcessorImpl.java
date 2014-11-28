package org.openmrs.module.shr.cdahandler.processor.entry.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.annotations.Structure;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.generic.SET;
import org.marc.everest.formatters.FormatterUtil;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.AssignedAuthor;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Author;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.EntryRelationship;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Reference;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActRelationshipExternalReference;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.configuration.CdaHandlerConfiguration;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.context.ProcessorContext;
import org.openmrs.module.shr.cdahandler.processor.entry.EntryProcessor;
import org.openmrs.module.shr.cdahandler.processor.factory.impl.EntryProcessorFactory;
import org.openmrs.module.shr.cdahandler.processor.util.AssignedEntityProcessorUtil;
import org.openmrs.module.shr.cdahandler.processor.util.DatatypeProcessorUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsConceptUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsDataUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsMetadataUtil;

/**
 * Represents an implementation of an EntryProcessor
 */
public abstract class EntryProcessorImpl implements EntryProcessor {

	// Log
	protected final Log log = LogFactory.getLog(this.getClass());
	
	// The context within which this parser is operating
	protected ProcessorContext m_context;

	// The Configuration and datatype utility
	protected final CdaHandlerConfiguration m_configuration = CdaHandlerConfiguration.getInstance();
	protected final DatatypeProcessorUtil m_datatypeUtil = DatatypeProcessorUtil.getInstance();
	protected final OpenmrsConceptUtil m_conceptUtil = OpenmrsConceptUtil.getInstance();
	protected final OpenmrsDataUtil m_dataUtil = OpenmrsDataUtil.getInstance();
	protected final AssignedEntityProcessorUtil m_assignedEntityUtil = AssignedEntityProcessorUtil.getInstance();
	protected final OpenmrsMetadataUtil m_metadataUtil = OpenmrsMetadataUtil.getInstance();

	/**
	 * Find an entry relationship
	 */
	protected final List<EntryRelationship> findEntryRelationship(ClinicalStatement statement, String templateIdRoot) {
		II templateId = new II(templateIdRoot);
		List<EntryRelationship> retVal = new ArrayList<EntryRelationship>();
		DatatypeProcessorUtil datatypeUtil = DatatypeProcessorUtil.getInstance();
		
		// See if the template can be found
		for(EntryRelationship ent : statement.getEntryRelationship())
			if(ent != null && datatypeUtil.hasTemplateId(ent, templateId))
				retVal.add(ent);
			else if(ent.getClinicalStatement() != null &&
					datatypeUtil.hasTemplateId(ent.getClinicalStatement(), templateId))
				retVal.add(ent);
		return retVal;
	}

	/**
	 * Gets the context under which this entry processor executes
	 */
	@Override
	public ProcessorContext getContext() {
		return this.m_context;
	}

	/**
	 * Gets the processor context which contains the encounter which an entry belongs 
	 */
	protected final ProcessorContext getEncounterContext()
	{
		ProcessorContext encounterContext = this.getContext();
		while(encounterContext.getParent() != null && !(encounterContext.getParsedObject() instanceof Encounter))
				encounterContext = encounterContext.getParent();
		return encounterContext;
	}

	/**
	 * Get the components expected in this act
	 */
	protected abstract List<String> getExpectedEntryRelationships();


	/**
	 * Get invalid clinical statemnet model text
	 */
	protected final String getInvalidClinicalStatementErrorText(Class<? extends IGraphable> expected, Class<? extends IGraphable> actual)
	{
		String expectedName = ((Structure)expected.getAnnotation(Structure.class)).name(),
				actualName = ((Structure)actual.getAnnotation(Structure.class)).name();
		return String.format("Invalid ClinicalStatement for this entry. Expected %s found %s", expectedName, actualName);
	}

	/**
	 * Returns true if the section contains the specified template
	 */
	public final boolean hasEntryRelationship(ClinicalStatement statement, String string) {
		return this.findEntryRelationship(statement, string).size() > 0;
    }
	
	/**
	 * Process the section
	 */
	@Override
	public abstract BaseOpenmrsData process(ClinicalStatement entry) throws DocumentImportException;
	
	/**
	 * Process entry relationships
	 * @throws DocumentImportException 
	 */
	protected void processEntryRelationships(ClinicalStatement entry, ProcessorContext childContext) throws DocumentImportException {
		this.processEntryRelationships(entry, childContext, ClinicalStatement.class);
    }

	/**
	 * Process entry relationship of the specified types
	 * @throws DocumentImportException 
	 */
	protected void processEntryRelationships(ClinicalStatement entry, ProcessorContext childContext,
                                          Class<? extends ClinicalStatement> filterType) throws DocumentImportException {
		EntryProcessorFactory factory = EntryProcessorFactory.getInstance();
		for(EntryRelationship relationship : entry.getEntryRelationship())
		{
			if(relationship == null || relationship.getClinicalStatement() == null ||
					relationship.getClinicalStatement().getNullFlavor() != null)
				continue;
			
			if(filterType.isAssignableFrom(relationship.getClinicalStatement().getClass()))
			{
				this.m_datatypeUtil.cascade(entry, relationship.getClinicalStatement(), "effectiveTime");
				EntryProcessor processor = factory.createProcessor(relationship.getClinicalStatement());
				if(processor != null)
				{
					processor.setContext(childContext);
					processor.process(relationship.getClinicalStatement());
				}
			}
		}
    }

	/**
	 * Sets the context under which this entry processor executes
	 */
	@Override
	public void setContext(ProcessorContext context) {
		this.m_context = context;
	}

	/**
	 * Set the creator on the openMrs data
	 * @throws DocumentImportException 
	 */
	public void setCreator(BaseOpenmrsData data, ClinicalStatement statement) throws DocumentImportException {

		// Created by different?
		Encounter encounterInfo = (Encounter)this.getEncounterContext().getParsedObject();
		if(statement.getAuthor().size() == 1 && statement.getAuthor().get(0).getAssignedAuthor() != null)
		{
			AssignedAuthor headerAuthor = this.findAuthorFromHeader(statement.getAuthor().get(0).getAssignedAuthor().getId());
			// Get the provider
			Provider createdByProvider = this.m_assignedEntityUtil.processProvider(headerAuthor);
			User createdBy = this.m_dataUtil.getUser(createdByProvider);
			data.setCreator(createdBy);
			if(statement.getAuthor().get(0).getTime() != null &&
					!statement.getAuthor().get(0).getTime().isNull())
				data.setDateCreated(statement.getAuthor().get(0).getTime().getDateValue().getTime());
		}
		else if (statement.getAuthor().size() > 1)
			throw new DocumentImportException("Observations can only have one author");
		else
			data.setCreator(encounterInfo.getCreator());
	    
    }

	/**
	 * Clone an encounter for registering a sub-encounter
	 * @throws DocumentImportException 
	 */
	protected Encounter createEncounter(ClinicalStatement statement) throws DocumentImportException {
		Encounter retVal = new Encounter();
		this.setCreator(retVal, statement);
		
		// Get encounter root
		Encounter rootEncounter = (Encounter)this.getEncounterContext().getParsedObject();
		retVal.setVisit(rootEncounter.getVisit());
		retVal.setLocation(rootEncounter.getLocation());
		retVal.setPatient(rootEncounter.getPatient());
		
		// Parse the authors of this document
		for(Author aut : statement.getAuthor())
		{
			//AssignedAuthor headerAuthor = this.findAuthorFromHeader(aut.getAssignedAuthor().getId());
			// This element represents the time that the author started participating in the creation of the clinical document .. Is it important?
			Provider provider = this.m_assignedEntityUtil.processProvider(aut.getAssignedAuthor());
			EncounterRole role = this.m_metadataUtil.getOrCreateEncounterRole(aut.getTypeCode());
			retVal.addProvider(role, provider);
		}
		
		return retVal;
    }

	/**
	 * Find the assigned author in the header
	 * @throws DocumentImportException 
	 */
	private AssignedAuthor findAuthorFromHeader(SET<II> authorIds) throws DocumentImportException {
		// Get the author from the CDA header
		ClinicalDocument documentContext = (ClinicalDocument)this.getContext().getRootContext().getRawObject();
		AssignedAuthor headerAuthor = null;
		for(II id : authorIds)
		{
			for(Author aut : documentContext.getAuthor())
				if(aut.getAssignedAuthor().getId().contains(id))
				{
					headerAuthor = aut.getAssignedAuthor();
					break;
				}
			if(headerAuthor != null) break;
		}
		
		if(headerAuthor == null)
			throw new DocumentImportException("Author in entry must appear in the header as well");
		return headerAuthor;
    }

	/**
	 * Validate that the section can be processed
	 */
	@Override
	public ValidationIssueCollection validate(IGraphable object)
	{
		ValidationIssueCollection validationIssues = new ValidationIssueCollection();
		if(!(object instanceof ClinicalStatement))
			validationIssues.error(String.format("Expected ClinicalStatement got %s", object.getClass()));
		
		// Cast to clinical statement
		ClinicalStatement statement = (ClinicalStatement)object;
		
		// Get expected entries
		List<String> expectedEntries = this.getExpectedEntryRelationships();
		if(expectedEntries != null)
			for(String comp : expectedEntries)
				if(!this.hasEntryRelationship(statement, comp))
					validationIssues.error(String.format("ClinicalStatement of type %s must have component matching template %s", FormatterUtil.toWireFormat(statement.getTemplateId()), comp));

		return validationIssues;
	}
	
	/**
	 * Get the CDA section to which this belongs
	 */
	public Section getSection() {
		ProcessorContext context = this.getContext();
		while(!(context.getRawObject() instanceof Section))
			context = context.getParent();
		return (Section)context.getRawObject();
    }

	/**
	 * Void an existing obs or throw an exception if duplicate identifiers are not allowed
	 * @throws DocumentImportException 
	 */
	protected Obs voidOrThrowIfPreviousObsExists(ArrayList<Reference> statementReferences, Patient patient, SET<II> statementIds) throws DocumentImportException {
		Obs previousObs = null;
		// References to previous observation?
		for(Reference reference : statementReferences)
			if(reference.getExternalActChoiceIfExternalAct() == null ||
				!reference.getTypeCode().getCode().equals(x_ActRelationshipExternalReference.RPLC))
				continue;
			else 
				previousObs = this.m_dataUtil.findExistingObs(reference.getExternalActChoiceIfExternalAct().getId(), patient);

		if(previousObs != null)
			Context.getObsService().voidObs(previousObs, "Replaced");
		
		// Validate no duplicates on AN
		if(statementIds != null)
		{
			Obs existingObs = this.m_dataUtil.findExistingObs(statementIds, patient) ;
			
			//    An replacement from the auto-replace
			 if(existingObs != null && this.m_configuration.getUpdateExisting())
			{
				log.debug(String.format("Voiding existing obs %s", existingObs));
				existingObs.setDateVoided(new Date());
				existingObs.setVoided(true);
				existingObs.setVoidedBy(Context.getAuthenticatedUser());
				existingObs.setVoidReason("Auto-Replaced");
				Context.getObsService().voidObs(existingObs, "Auto-Replaced");
				previousObs = Context.getObsService().getObs(existingObs.getId()); 
			}
			else if(existingObs != null)
				throw new DocumentImportException(String.format("Duplicate entry %s. If you intend to replace it please use the replacement mechanism for CDA", FormatterUtil.toWireFormat(statementIds)));
		}

		return previousObs;
	}


	/**
	 * Void an existing observation is applicable, otherwise throw
	 * @throws DocumentImportException 
	 */
	protected Order voidOrThrowIfPreviousOrderExists(ArrayList<Reference> statementReferences, Patient patient, SET<II> statementIds) throws DocumentImportException {
		
		Order previousOrder = null;
		
		// References to previous order
		for(Reference reference : statementReferences)
			if(reference.getExternalActChoiceIfExternalAct() == null ||
				!reference.getTypeCode().getCode().equals(x_ActRelationshipExternalReference.RPLC))
				continue;
			else 
				previousOrder = this.m_dataUtil.findExistingOrder(reference.getExternalActChoiceIfExternalAct().getId(), patient);

		if(previousOrder != null)
			Context.getOrderService().voidOrder(previousOrder, "Replaced");
		
		// Validate no duplicates on AN
		if(statementIds != null)
		{
			Order existingOrder = this.m_dataUtil.findExistingOrder(statementIds, patient) ;
			
			// An replacement from the auto-replace
			if(existingOrder != null && this.m_configuration.getUpdateExisting())
			{
				Context.getOrderService().voidOrder(existingOrder, "Auto-Replaced");
				previousOrder = Context.getOrderService().getOrder(existingOrder.getId()); 
			}
			else if(existingOrder != null)
				throw new DocumentImportException(String.format("Duplicate entry %s. If you intend to replace it please use the replacement mechanism for CDA", FormatterUtil.toWireFormat(statementIds)));
		}

		return previousOrder;
    }
}

