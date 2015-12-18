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
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.datatypes.II;
import org.marc.everest.formatters.interfaces.IFormatterParseResult;
import org.marc.everest.formatters.xml.its1.XmlIts1Formatter;
import org.marc.everest.interfaces.IResultDetail;
import org.marc.everest.interfaces.ResultDetailType;
import org.marc.everest.resultdetails.DatatypeValidationResultDetail;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.openmrs.*;
import org.openmrs.activelist.ActiveListItem;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
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
		return this.importDocument((ClinicalDocument)parseResult.getStructure());

	}
	
	/**
	 * Import the parsed clinical document
	 * Auto generated method comment
	 * 
	 * @param clinicalDocument
	 * @return
	 * @throws DocumentImportException
	 */
	@Override
	public Visit importDocument(ClinicalDocument clinicalDocument) throws DocumentImportException
	{
		if(this.m_processor == null)
			this.m_processor = CdaImporter.getInstance();
	
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
	 * @see org.openmrs.module.shr.cdahandler.api.CdaImportService#getOrdersByAccessionNumber(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
    public List<Order> getOrdersByAccessionNumber(String an) {
		return this.dao.getOrdersByAccessionNumber(an, false);
    }

	/**
	 * Save a concept quickly (without reindexing);
	 * @see org.openmrs.module.shr.cdahandler.api.CdaImportService#saveConcept(org.openmrs.Concept)
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

	/**
	 * Get active list item by the accession number of their start/stop obs
	 */
	@Override
    public <T extends ActiveListItem> List<T> getActiveListItemByAccessionNumber(String accessionNumber, Class<T> clazz) {
	    return this.dao.getActiveListItemByAccessionNumber(accessionNumber, clazz);
    }

	/**
	 * Get active list item by obs
	 * @see org.openmrs.module.shr.cdahandler.api.CdaImportService#getActiveListItemByObs(org.openmrs.Obs, java.lang.Class)
	 */
	@Override
    public <T extends ActiveListItem> List<T> getActiveListItemByObs(Obs obs, Class<T> clazz) {
		return this.dao.getActiveListItemByObs(obs, clazz);
    }

	/**
	 * Get concept source by HL7
	 * @see org.openmrs.module.shr.cdahandler.api.CdaImportService#getConceptSourceByHl7(java.lang.String)
	 */
	@Override
    public ConceptSource getConceptSourceByHl7(String hl7) {
		return this.dao.getConceptSourceByHl7(hl7);
    }

    private static final Map<String, List<Integer>> conceptCache = Collections.synchronizedMap(new HashMap<String, List<Integer>>());
    private static final String GP_CACHE_MAPPED_CONCEPTS = "shr-cdahandler.cacheMappedConcepts";
    private static Boolean cacheMappedConcepts = null;

	/**
	 * Get concept by mapping
	 * @see org.openmrs.module.shr.cdahandler.api.CdaImportService#getConceptsByMapping(ConceptReferenceTerm, java.lang.String)
	 */
	@Override
    public List<Concept> getConceptsByMapping(ConceptReferenceTerm term, String strength) {
		if (term==null || term.getCode()==null || term.getCode().trim().isEmpty() || term.getConceptSource()==null) {
			return Collections.emptyList();
		}

		synchronized (this) {
            if (cacheMappedConcepts == null) {
                if (Context.getAdministrationService().getGlobalProperty(GP_CACHE_MAPPED_CONCEPTS).equalsIgnoreCase("true")) {
                    cacheMappedConcepts = true;
                } else {
                    cacheMappedConcepts = false;
                }
            }
        }

        ConceptService cs = Context.getConceptService();
        List<Concept> terms = null;
        String key = null;

        if (cacheMappedConcepts) {
            key = term.getCode() + ':' + term.getConceptSource().getName() + ':' + strength;
            List<Integer> conceptIds = conceptCache.get(key);
            if (conceptIds != null) {
                terms = new ArrayList<Concept>();
                for (Integer conceptId : conceptIds) {
                    terms.add(cs.getConcept(conceptId));
                }
            }
        }

        if (terms == null) {
            terms = cs.getConceptsByMapping(term.getCode(), term.getConceptSource().getName(), false);
            if (cacheMappedConcepts) {
                List<Integer> conceptIds = new ArrayList<Integer>();
                for (Concept c : terms) {
                    conceptIds.add(c.getConceptId());
                }
                conceptCache.put(key, conceptIds);
            }
        }

		List<Concept> retVal = new ArrayList<Concept>();
		
		for(Concept concept : terms)
		{
			for(ConceptMap map : concept.getConceptMappings())
				if(map.getConceptReferenceTerm().getId().equals(term.getId()) &&
						map.getConceptMapType().getName().toLowerCase().equals(strength.toLowerCase()))
					retVal.add(concept);
		}

		return retVal;
    }
	
}
