package org.openmrs.module.shr.cdahandler.api;

import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.openmrs.Visit;

/**
 * Represents an import subscriber. That is; a class which wishes to know
 * when a CDA document is imported (for things like auditing, warehousing, ODD)
 */
public interface CdaImportSubscriber {
	
	/**
	 * Indicates a document was imported successfully by the processor
	 */
	void onDocumentImported(ClinicalDocument rawDocument, Visit processedVisit);
	
}
