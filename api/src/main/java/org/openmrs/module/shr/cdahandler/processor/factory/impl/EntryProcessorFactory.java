package org.openmrs.module.shr.cdahandler.processor.factory.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.formatters.FormatterUtil;
import org.marc.everest.rmim.uv.cdar2.rim.InfrastructureRoot;
import org.openmrs.module.shr.cdahandler.processor.Processor;
import org.openmrs.module.shr.cdahandler.processor.document.DocumentProcessor;
import org.openmrs.module.shr.cdahandler.processor.document.impl.StructuredBodyDocumentProcessor;
import org.openmrs.module.shr.cdahandler.processor.entry.EntryProcessor;
import org.openmrs.module.shr.cdahandler.processor.factory.ProcessorFactory;

/**
 * Represents a processor factory for entries
 */
public class EntryProcessorFactory implements ProcessorFactory {
	
	// Singleton instance
	private static EntryProcessorFactory s_instance;
	private static Object s_lockObject = new Object();
	
	// Log
	protected final Log log = LogFactory.getLog(this.getClass());
	 
	/**
	 * Constructs a document parser factory
	 */
	private EntryProcessorFactory() 
	{
	}
	
	/**
	 * Gets or creates the singleton instance 
	 * @return
	 */
	public final static EntryProcessorFactory getInstance()
	{
		if(s_instance == null)
			synchronized (s_lockObject) {
				if(s_instance == null)
					s_instance = new EntryProcessorFactory();
			}
		return s_instance;
	}
	
	/**
	 * Create a parser that can handle the specified "object"
	 */
	@Override
	public EntryProcessor createProcessor(InfrastructureRoot object) {
		ClasspathScannerUtil scanner = ClasspathScannerUtil.getInstance();
		Processor candidateProcessor = scanner.createProcessor(object.getTemplateId());
		
		// Return document processor
		if(candidateProcessor instanceof DocumentProcessor && candidateProcessor.validate(object))
		{
			log.info(("Using template processor: '%s'"));
			return (EntryProcessor)candidateProcessor;
		}
		else 
		{
			log.warn(String.format("Could not find a processor for entry template %s ... Fallback processor: StructuredBodyDocumentProcessor", FormatterUtil.toWireFormat(object.getTemplateId())));
			return null;
		}
	}

	
}
