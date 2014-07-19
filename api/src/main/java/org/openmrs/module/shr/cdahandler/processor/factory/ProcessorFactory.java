package org.openmrs.module.shr.cdahandler.processor.factory;

import org.marc.everest.rmim.uv.cdar2.rim.InfrastructureRoot;
import org.openmrs.module.shr.cdahandler.processor.Processor;

/**
 * Parser factory instance
 * @author Justin Fyfe
 *
 */
public interface ProcessorFactory {

	/**
	 * Creates a parser for the specified object instance and validates the instance
	 * @param object The object which should be used to determine the parser to create
	 * @return The constructed parser 
	 */
	Processor createProcessor(InfrastructureRoot object);	
	
}
