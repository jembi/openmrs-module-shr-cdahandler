package org.openmrs.module.shr.cdahandler.processor.context;

import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.StructuredBody;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Encounter;
import org.openmrs.module.shr.cdahandler.processor.Processor;

/**
 * Represents a parsing context. 
 * 
 * This type of class allows parsers for entries and sections
 * to determine the context of the document within which they were 
 * called (this may affect behavior)
 * @author Justin Fyfe
 *
 */
public class ProcessorContext {


	private Processor m_parser;
	private IGraphable m_object;
	private ProcessorContext m_parent;
	private BaseOpenmrsData m_parsedObject;
	
	/**
	 * Creates a new parser context
	 * @param currentObject The current object being parsed
	 * @param parser The parser being used
	 */
	public ProcessorContext(IGraphable rawObject, BaseOpenmrsData parsedObject, Processor parser)
	{
		this.m_parser = parser;
		this.m_object = rawObject;
		this.m_parent = parser.getContext();
		this.m_parsedObject = parsedObject;
	}
	
	/**
	 * Create a new parser context
	 */
	public ProcessorContext(IGraphable rawObject, BaseOpenmrsData parsedObject,
        Processor processor, ProcessorContext parent) {
		this(rawObject, parsedObject, processor);
		this.m_parent = parent;
    }

	/**
	 * Gets the parent context of this context object
	 */
	public ProcessorContext getParent() { return this.m_parent; }
	/**
	 * Gets the current object being parsed
	 */
	public IGraphable getRawObject() { return this.m_object; }
	/**
	 * Gets the parser that is being used in this context
	 */
	public Processor getProcessor() { return this.m_parser; }
	/**
	 * Gets the OpenMRS object that this context is currently building 
	 */
	public BaseOpenmrsData getParsedObject() { return this.m_parsedObject; }
	
	/**
	 * Gets the root context
	 * @return The root context
	 */
	public ProcessorContext getRootContext() {
		ProcessorContext context = this;
		while(context.getParent() != null)
			context = context.getParent();
		return context;
	}
}
