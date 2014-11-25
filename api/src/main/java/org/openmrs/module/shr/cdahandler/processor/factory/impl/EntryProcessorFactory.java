package org.openmrs.module.shr.cdahandler.processor.factory.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.formatters.FormatterUtil;
import org.marc.everest.rmim.uv.cdar2.rim.InfrastructureRoot;
import org.openmrs.module.shr.cdahandler.configuration.CdaHandlerConfiguration;
import org.openmrs.module.shr.cdahandler.configuration.CdaHandlerConfigurationFactory;
import org.openmrs.module.shr.cdahandler.processor.Processor;
import org.openmrs.module.shr.cdahandler.processor.entry.EntryProcessor;
import org.openmrs.module.shr.cdahandler.processor.factory.ProcessorFactory;

/**
 * Represents a processor factory for entries
 */
public final class EntryProcessorFactory implements ProcessorFactory {
	
	/**
	 * Gets or creates the singleton instance 
	 * @return
	 */
	public static EntryProcessorFactory getInstance()
	{
		if(s_instance == null)
			synchronized (s_lockObject) {
				if(s_instance == null)
					s_instance = new EntryProcessorFactory();
			}
		return s_instance;
	}
	// Singleton instance
	private static EntryProcessorFactory s_instance;
	
	private static Object s_lockObject = new Object(); 
	
	// Get configuration
	private final CdaHandlerConfiguration m_configuration = CdaHandlerConfigurationFactory.getInstance();
	 
	// Log
	private final Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * Constructs a document parser factory
	 */
	private EntryProcessorFactory() 
	{
	}
	
	/**
	 * Create a parser that can handle the specified "object"
	 */
	@Override
	public EntryProcessor createProcessor(InfrastructureRoot object) {
		ClasspathScannerUtil scanner = ClasspathScannerUtil.getInstance();
		
		Processor candidateProcessor = null;
		if(object.getTemplateId() != null)
			candidateProcessor = scanner.createProcessor(object.getTemplateId());
		
		// Log the processor selected
		if(candidateProcessor instanceof EntryProcessor)
			log.info(String.format("Using template processor: '%s'", candidateProcessor.getTemplateName()));
		else 
			log.warn(String.format("Could not find a processor for entry template %s ...", FormatterUtil.toWireFormat(object.getTemplateId())));
		
		return (EntryProcessor)candidateProcessor;
		
	}

	
}
