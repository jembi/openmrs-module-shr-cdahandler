package org.openmrs.module.shr.cdahandler.processor.factory.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.formatters.FormatterUtil;
import org.marc.everest.rmim.uv.cdar2.rim.InfrastructureRoot;
import org.openmrs.module.shr.cdahandler.configuration.CdaHandlerConfiguration;
import org.openmrs.module.shr.cdahandler.configuration.CdaHandlerConfigurationFactory;
import org.openmrs.module.shr.cdahandler.processor.Processor;
import org.openmrs.module.shr.cdahandler.processor.factory.ProcessorFactory;
import org.openmrs.module.shr.cdahandler.processor.section.SectionProcessor;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor;

/**
 * Represents a factory that creates processors capable of 
 * interpreting sections
 * @author Justin Fyfe
 *
 */
public final class SectionProcessorFactory implements ProcessorFactory {

	/**
	 * Gets or creates the singleton instance 
	 * @return
	 */
	public static SectionProcessorFactory getInstance()
	{
		if(s_instance == null)
			synchronized (s_lockObject) {
				if(s_instance == null)
					s_instance = new SectionProcessorFactory();
			}
		return s_instance;
	}
	// Singleton instance
	private static SectionProcessorFactory s_instance;
	
	private static Object s_lockObject = new Object();
	// Log
	private final Log log = LogFactory.getLog(this.getClass());
	 
	private final CdaHandlerConfiguration m_configuration = CdaHandlerConfigurationFactory.getInstance();
	
	/**
	 * Constructs a document parser factory
	 */
	private SectionProcessorFactory() 
	{
	}
	
	/**
	 * Create a parser that can handle the specified "object"
	 */
	@Override
	public SectionProcessor createProcessor(InfrastructureRoot object) {
		ClasspathScannerUtil scanner = ClasspathScannerUtil.getInstance();
		Processor candidateProcessor = scanner.createProcessor(object.getTemplateId());
		
		// Return document processor
		if(candidateProcessor instanceof SectionProcessor)
			log.info(String.format("Using template processor: '%s'", candidateProcessor.getTemplateName()));
		else
		{
			candidateProcessor = new GenericLevel2SectionProcessor();
			log.warn(String.format("Could not find a processor for section template %s ... Fallback processor: StructuredBodyDocumentProcessor", FormatterUtil.toWireFormat(object.getTemplateId())));
		}

		
		return (SectionProcessor)candidateProcessor;
	}

}
