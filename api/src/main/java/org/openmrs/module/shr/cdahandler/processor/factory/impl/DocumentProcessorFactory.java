package org.openmrs.module.shr.cdahandler.processor.factory.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.formatters.FormatterUtil;
import org.marc.everest.rmim.uv.cdar2.rim.InfrastructureRoot;
import org.openmrs.module.shr.cdahandler.configuration.CdaHandlerConfiguration;
import org.openmrs.module.shr.cdahandler.processor.Processor;
import org.openmrs.module.shr.cdahandler.processor.document.DocumentProcessor;
import org.openmrs.module.shr.cdahandler.processor.document.impl.GenericDocumentProcessor;
import org.openmrs.module.shr.cdahandler.processor.factory.ProcessorFactory;

/**
 * Represents a factory that can construct an appropriate document 
 * processor to handle the object passed to it.
 * @author Justin Fyfe
 *
 */
public final class DocumentProcessorFactory implements ProcessorFactory {

	
	/**
	 * Gets or creates the singleton instance 
	 * @return
	 */
	public static DocumentProcessorFactory getInstance()
	{
		if(s_instance == null)
			synchronized (s_lockObject) {
				if(s_instance == null)
					s_instance = new DocumentProcessorFactory();
			}
		return s_instance;
	}
	// Singleton instance
	private static DocumentProcessorFactory s_instance;
	
	private static Object s_lockObject = new Object();
	// Log
	private final Log log = LogFactory.getLog(this.getClass());
	
	private final CdaHandlerConfiguration m_configuration = CdaHandlerConfiguration.getInstance();
	
	/**
	 * Constructs a document parser factory
	 */
	private DocumentProcessorFactory() 
	{
	}
	
	/**
	 * Create a parser that can handle the specified "object"
	 */
	@Override
	public DocumentProcessor createProcessor(InfrastructureRoot object) {
		ClasspathScannerUtil scanner = ClasspathScannerUtil.getInstance();
		Processor candidateProcessor = scanner.createProcessor(object.getTemplateId());
		
		// Log document processor
		if(candidateProcessor instanceof DocumentProcessor)
			log.info(String.format("Using template processor: '%s'", candidateProcessor.getTemplateName()));
		else 
		{
			candidateProcessor = new GenericDocumentProcessor();
			log.warn(String.format("Could not find a processor for document template %s ... Fallback processor: StructuredBodyDocumentProcessor", FormatterUtil.toWireFormat(object.getTemplateId())));
		}
		
		
		return (DocumentProcessor)candidateProcessor;
		
	}

}
