package org.openmrs.module.shr.cdahandler.processor.section.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Author;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Encounter;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;
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
	public Boolean validate(IGraphable object)
	{
		return object instanceof Section;
	}

	/**
	 * Process the section
	 */
	@Override
	public abstract BaseOpenmrsData process(Section section) throws DocumentParseException;
	
	
}
