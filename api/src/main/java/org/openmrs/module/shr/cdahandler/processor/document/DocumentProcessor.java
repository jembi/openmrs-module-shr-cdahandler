package org.openmrs.module.shr.cdahandler.processor.document;

import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.openmrs.Visit;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;
import org.openmrs.module.shr.cdahandler.processor.Processor;

/**
 * Represents a parser which is capable of processing a document
 * @author Justin
 *
 */
public interface DocumentProcessor extends Processor {
	
	/**
	 * Parses the specified document into a collection of BaseOpenMRS data elements
	 * @param doc The document to be parsed
	 * @return A collection of data containing all the interpreted information from the CDA
	 */
	// TODO: Can the Visit object do everything we need for this? I feel that for Allergies and Problems a Visit may not work as a diagnosis cannot be assigned to a visit, is this true?
	Visit process(ClinicalDocument doc) throws DocumentParseException;
	
}
