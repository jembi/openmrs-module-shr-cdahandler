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
package org.openmrs.module.shr.cdahandler.api.impl;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.datatypes.II;
import org.marc.everest.formatters.interfaces.IFormatterParseResult;
import org.marc.everest.formatters.xml.its1.XmlIts1Formatter;
import org.marc.everest.interfaces.IResultDetail;
import org.marc.everest.interfaces.ResultDetailType;
import org.marc.everest.resultdetails.DatatypeValidationResultDetail;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.shr.cdahandler.CdaImporter;
import org.openmrs.module.shr.cdahandler.api.CdaImportService;
import org.openmrs.module.shr.cdahandler.api.CdaImportSubscriber;
import org.openmrs.module.shr.cdahandler.api.db.CdaImportServiceDAO;
import org.openmrs.module.shr.cdahandler.everest.EverestUtil;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.DocumentValidationException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.obs.ExtendedObs;
import org.springframework.transaction.annotation.Transactional;

/**
 * It is a default implementation of {@link CdaImportService}.
 */
public class CdaImportServiceImpl extends BaseOpenmrsService implements CdaImportService {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	// Processor
	private CdaImporter m_processor = null;
	
	// The dao for the CdaImportService
	private CdaImportServiceDAO dao;
	
	// Subscribers
	protected final Map<String, Set<CdaImportSubscriber>> m_subscribers = new HashMap<String, Set<CdaImportSubscriber>>();
	
	/** 
	 * TODO: This needs to be more thread/process safe.. 
	 * @see org.openmrs.module.shr.cdahandler.api.CdaImportService#importDocument(java.io.InputStream)
	 */
	@Override
	public Visit importDocument(InputStream doc) throws DocumentImportException 
	{
		
		if(this.m_processor == null)
			this.m_processor = CdaImporter.getInstance();
	
		
		// Formatter
		XmlIts1Formatter formatter = EverestUtil.createFormatter();
		
		// Parse the document
		log.debug("Starting processing of document");
		IFormatterParseResult parseResult = formatter.parse(doc);
		log.debug("Process document complete.");

		// Validation messages?
		ValidationIssueCollection parsingIssues = new ValidationIssueCollection();
		for(IResultDetail dtl : parseResult.getDetails())
		{
			if(dtl.getType() == ResultDetailType.ERROR && !(dtl instanceof DatatypeValidationResultDetail))
			{
				parsingIssues.error(String.format("HL7v3 Validation: %s at %s", dtl.getMessage(), dtl.getLocation()));
				if(dtl.getException() != null)
				{
					log.error("Error", dtl.getException());
				}
			}
			else  
				parsingIssues.warn(String.format("HL7v3 Validation: %s at %s", dtl.getMessage(), dtl.getLocation()));
		}
		
		// Any serious validation has errors or structure is null?
		if(parsingIssues.hasErrors() || parseResult.getStructure() == null)
			throw new DocumentValidationException(parseResult.getStructure(), parsingIssues);
		
		// Get the clinical document
		ClinicalDocument clinicalDocument = (ClinicalDocument)parseResult.getStructure();

		// TODO: Store incoming to a temporary table for CDAs (like the HL7 queue)
		Visit retVal = this.m_processor.processCdaDocument(clinicalDocument);

		// Notify of successful import
		if(retVal != null)
		{
			Stack<CdaImportSubscriber> toBeNotified = new Stack<CdaImportSubscriber>();
			
			// The generic ones for all 
			Set<CdaImportSubscriber> candidates = this.m_subscribers.get("*");
			if(candidates != null)
				for(CdaImportSubscriber subscriber : candidates)
					toBeNotified.push(subscriber);
			
			// Notify the default always
			for(II templateId : clinicalDocument.getTemplateId())
			{
				candidates = this.m_subscribers.get(templateId.getRoot());
				if(candidates == null) continue; // no candidates
				
				for(CdaImportSubscriber subscriber : candidates)
					if(!toBeNotified.contains(subscriber))
						toBeNotified.push(subscriber);
			}
			
			// Notify the found subscribers
			while(!toBeNotified.isEmpty())
				toBeNotified.pop().onDocumentImported(clinicalDocument, retVal);
		}
		
		return retVal;
	}

	/**
	 * Subscribe to the import function
	 */
	@Override
    public void subscribeImport(String templateId, CdaImportSubscriber singletonImporter) {
	    
		// Ensure template ID has a value
		if(templateId == null)
	    	templateId = "*";
	    
		// Does the key exist?
		if(!this.m_subscribers.containsKey(templateId))
			this.m_subscribers.put(templateId, new HashSet<CdaImportSubscriber>());
		
		// Add
		if(!this.m_subscribers.get(templateId).contains(singletonImporter))
			this.m_subscribers.get(templateId).add(singletonImporter);
    }

	/**
	 * Get an active order by accession number
	 * @see org.openmrs.module.shr.cdahandler.api.CdaImportService#getOrderByAccessionNumber(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
    public List<Order> getOrdersByAccessionNumber(String an) {
		return this.dao.getOrdersByAccessionNumber(an, false);
    }

	/**
	 * Save a concept quickly (without reindexing);
	 * @see org.openmrs.module.shr.cdahandler.api.CdaImportService#saveConceptQuick(org.openmrs.Concept)
	 */
	@Override
    public Concept saveConcept(Concept concept) throws APIException {
		ConceptMapType defaultConceptMapType = null;
		for (ConceptMap map : concept.getConceptMappings()) {
			if (map.getConceptMapType() == null) {
				if (defaultConceptMapType == null) {
					defaultConceptMapType = Context.getConceptService().getDefaultConceptMapType();
				}
				map.setConceptMapType(defaultConceptMapType);
			}
		}

		concept.setDateChanged(new Date());
		concept.setChangedBy(Context.getAuthenticatedUser());
		
		// add/remove entries in the concept_word table (used for searching)
		return this.dao.saveConceptQuick(concept);
    }

	
    /**
     * @param dao the dao to set
     */
    public void setDao(CdaImportServiceDAO dao) {
    	this.dao = dao;
    }

	/**
	 * Get an obs by accession number
	 * @see org.openmrs.module.shr.cdahandler.api.CdaImportService#getObsByAccessionNumber(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
    public List<Obs> getObsByAccessionNumber(String an) {
		return this.dao.getObsByAccessionNumber(an, false);
    }

	@Override
    public ConceptReferenceTerm saveConceptReferenceTerm(ConceptReferenceTerm referenceTerm) {
		return this.dao.saveReferenceTermQuick(referenceTerm);
    }

	/**
	 * Get extended obs data by id
	 * @see org.openmrs.module.shr.cdahandler.api.CdaImportService#getExtendedObs(java.lang.Integer)
	 */
	@Override
    public ExtendedObs getExtendedObs(Integer id) {
		return this.dao.getExtendedObs(id);
    }

	
}
