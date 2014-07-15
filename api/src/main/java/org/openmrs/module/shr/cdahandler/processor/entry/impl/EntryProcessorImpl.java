package org.openmrs.module.shr.cdahandler.processor.entry.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.annotations.Structure;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Encounter;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.context.ProcessorContext;
import org.openmrs.module.shr.cdahandler.processor.entry.EntryProcessor;

/**
 * Represents an implementation of an EntryProcessor
 */
public abstract class EntryProcessorImpl implements EntryProcessor {

	// Log
	protected final Log log = LogFactory.getLog(this.getClass());
	
	// The context within which this parser is operating
	protected ProcessorContext m_context;

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
}
