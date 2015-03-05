package org.openmrs.module.shr.cdahandler.processor.section.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.Obs;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.context.ProcessorContext;
import org.openmrs.module.shr.cdahandler.processor.section.SectionProcessor;

/**
 * Section processor implementation that can interpret 
 * @author Justin Fyfe
 *
 */
public abstract class SectionProcessorImpl implements SectionProcessor {


	// Log
	protected final Log log = LogFactory.getLog(this.getClass());
	
	// The context within which this parser is operating
	protected ProcessorContext m_context;

	/**
	 * Gets the context under which this section processor executes
	 */
	@Override
	public ProcessorContext getContext() {
		return this.m_context;
	}

	/**
	 * Process the section
	 */
	@Override
	public abstract Obs process(Section section) throws DocumentImportException;

	/**
	 * Sets the context under which this section processor executes
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
		if(!(object instanceof Section))
			validationIssues.error(String.format("Expected Section got %s", object.getClass()));
		return validationIssues;
	}
	
	
}
