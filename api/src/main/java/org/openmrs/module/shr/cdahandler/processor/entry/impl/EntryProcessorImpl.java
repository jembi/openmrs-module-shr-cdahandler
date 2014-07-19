package org.openmrs.module.shr.cdahandler.processor.entry.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.annotations.Structure;
import org.marc.everest.datatypes.II;
import org.marc.everest.formatters.FormatterUtil;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.EntryRelationship;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Encounter;
import org.openmrs.module.shr.cdahandler.configuration.CdaHandlerConfiguration;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.context.ProcessorContext;
import org.openmrs.module.shr.cdahandler.processor.entry.EntryProcessor;
import org.openmrs.module.shr.cdahandler.processor.factory.impl.EntryProcessorFactory;
import org.openmrs.module.shr.cdahandler.processor.util.DatatypeProcessorUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsConceptUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsDataUtil;

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
	
	/**
	 * Gets the context under which this entry processor executes
	 */
	@Override
	public ProcessorContext getContext() {
		return this.m_context;
	}

	/**
	 * Sets the context under which this entry processor executes
	 */
	@Override
	public void setContext(ProcessorContext context) {
		this.m_context = context;
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
	 * Process the section
	 */
	@Override
	public abstract BaseOpenmrsData process(ClinicalStatement entry) throws DocumentImportException;


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
	 * Get invalid clinical statemnet model text
	 */
	protected final String getInvalidClinicalStatementErrorText(Class<? extends IGraphable> expected, Class<? extends IGraphable> actual)
	{
		String expectedName = ((Structure)expected.getAnnotation(Structure.class)).name(),
				actualName = ((Structure)actual.getAnnotation(Structure.class)).name();
		return String.format("Invalid ClinicalStatement for this entry. Expected %s found %s", expectedName, actualName);
	}
	
	/**
	 * Get the components expected in this act
	 */
	protected abstract List<String> getExpectedEntryRelationships();
	
	/**
	 * Returns true if the section contains the specified template
	 */
	public final boolean hasEntryRelationship(ClinicalStatement statement, String string) {
		return this.findEntryRelationship(statement, string).size() > 0;
    }

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
	 * Process entry relationships
	 * @throws DocumentImportException 
	 */
	protected void processEntryRelationships(ClinicalStatement entry, ProcessorContext childContext) throws DocumentImportException {

		EntryProcessorFactory factory = EntryProcessorFactory.getInstance();
		for(EntryRelationship relationship : entry.getEntryRelationship())
		{
			if(relationship == null || relationship.getClinicalStatement() == null ||
					relationship.getClinicalStatement().getNullFlavor() != null)
				continue;
			
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

