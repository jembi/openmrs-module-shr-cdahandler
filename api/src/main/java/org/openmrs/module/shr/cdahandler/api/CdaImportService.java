/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.shr.cdahandler.api;

import java.io.InputStream;
import java.util.List;

import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.openmrs.Concept;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Visit;
import org.openmrs.activelist.ActiveListItem;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.obs.ExtendedObs;
import org.springframework.transaction.annotation.Transactional;

/**
 * This service exposes module's core functionality. It is a Spring managed bean which is configured in moduleApplicationContext.xml.
 * <p>
 * It can be accessed only via Context:<br>
 * <code>
 * Context.getService(cdaAntepartumService.class).someMethod();
 * </code>
 * 
 * @see org.openmrs.api.context.Context
 */
@Transactional
public interface CdaImportService extends OpenmrsService {
     
	/**
	 * Import a document
	 */
	Visit importDocument(InputStream inputStream) throws DocumentImportException;

	/**
	 * Import a document
	 */
	Visit importDocument(ClinicalDocument inputStream) throws DocumentImportException;

	/**
	 * Subscribe to the import operation
	 * 
	 * @param templateId The identifier of the template to subscribe to, or null for all
	 */
	void subscribeImport(String templateId, CdaImportSubscriber singletonImporter);
	
	
	/**
	 * Get an order by AN
	 */
	List<Order> getOrdersByAccessionNumber(String an);
	
	/**
	 * Saves a concept without indexing
	 */
	Concept saveConcept(Concept concept);
	
	/**
	 * Get observation by accession number
	 */
	List<Obs> getObsByAccessionNumber(String an);

	/**
	 * Create a concept reference term without re-indexing
	 */
	ConceptReferenceTerm saveConceptReferenceTerm(ConceptReferenceTerm referenceTerm);

	/**
	 * Retrieves an extended observation 
	 */
	ExtendedObs getExtendedObs(Integer id);
	
	/**
	 * Get an active list item by the accession number of its start or stop obs
	 */
	<T extends ActiveListItem> List<T> getActiveListItemByAccessionNumber(String accessionNumber, Class<T> clazz);

	/**
	 * Get active list items to which the Obs is associted
	 */
	<T extends ActiveListItem> List<T> getActiveListItemByObs(Obs obs, Class<T> clazz);

	/**
	 * Get concept source by hl7
	 */
	ConceptSource getConceptSourceByHl7(String hl7);


}