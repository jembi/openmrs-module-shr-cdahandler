package org.openmrs.module.shr.cdahandler.processor.entry;

import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Entry;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.module.shr.cdahandler.processor.Processor;

/**
 * Represents a parser that can interpret an entry
 * @author Justin
 *
 */
public interface EntryProcessor extends Processor {

	/**
	 * Parses an entry into an appropriate OpenMRS structure
	 * @param entry The CDA entry to be parsed
	 * @return An appropriate OpenMRS data object based on the entry content. 
	 */
	BaseOpenmrsData process(Entry entry);
}
