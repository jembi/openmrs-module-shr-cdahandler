package org.openmrs.module.shr.cdahandler.api.db;

import java.util.List;

import org.openmrs.Concept;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.activelist.ActiveListItem;
import org.openmrs.module.shr.cdahandler.obs.ExtendedObs;

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

	/**
	 * Get extended obs data by id
	 */
	ExtendedObs getExtendedObs(Integer id);
	
	/**
	 * Get ActiveListItem by the accession number of the start or stop obs
	 */
	<T extends ActiveListItem> List<T> getActiveListItemByAccessionNumber(String accessionNumber, Class<T> clazz);
	
	/**
	 * Get active list item by obs
	 */
	<T extends ActiveListItem> List<T> getActiveListItemByObs(Obs obs, Class<T> clazz);
}
