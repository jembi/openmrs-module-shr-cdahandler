package org.openmrs.module.shr.cdahandler.api.db;

import java.util.List;

import org.openmrs.Concept;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.Obs;
import org.openmrs.Order;

/**
 * Represents a DAO for extended CDA properties
 */
public interface CdaImportServiceDAO {

	/**
	 * Saves concepts in a manner which is faster than the default OpenMRS implementation
	 */
	Concept saveConceptQuick(Concept concept);

	/**
	 * Get an order by accession number
	 */
	List<Order> getOrdersByAccessionNumber(String an, boolean includeVoided);

	/**
	 * Get an observation by accession number
	 */
	List<Obs> getObsByAccessionNumber(String an, boolean includeVoided);

	/**
	 * Create a reference term
	 */
	ConceptReferenceTerm saveReferenceTermQuick(ConceptReferenceTerm referenceTerm);
	
}
