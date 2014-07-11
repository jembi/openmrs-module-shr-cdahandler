package org.openmrs.module.shr.cdahandler.processor;

import org.marc.everest.interfaces.IGraphable;
import org.openmrs.module.shr.cdahandler.processor.context.ProcessorContext;

/**
 * An interface describing the functionality of a processor implementation
 * be it for Document, Section or Entries.
 * @author Justin Fyfe
 *
 */
public interface Processor {

	/**
	 * Get the current context of the processor
	 */
	ProcessorContext getContext();
	
	/**
	 * Sets the context of the processor
	 */
	void setContext(ProcessorContext context);
	
	/**
	 * Gets the name of the template the processor handles
	 * @return
	 */
	String getTemplateName();
	
	/**
	 * Validate an object can be processed
	 * @param object The object to be processed
	 * @return The validation result
	 */
	Boolean validate(IGraphable object);
}
