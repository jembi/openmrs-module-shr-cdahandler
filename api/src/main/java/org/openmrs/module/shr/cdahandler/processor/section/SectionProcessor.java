package org.openmrs.module.shr.cdahandler.processor.section;

import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.Encounter;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;
import org.openmrs.module.shr.cdahandler.processor.Processor;

/**
 * Represents a parser that is capable of interpreting a section
 * @author Justin Fyfe
 *
 */
public interface SectionProcessor extends Processor {

	/**
	 * Parses the specified section into an encounter
	 * @param section The CDA section to be parsed
	 * @return An interpreted section represented as an encounter
	 */
	Encounter process(Section section) throws DocumentParseException;
	
}
