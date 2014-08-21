package org.openmrs.module.shr.cdahandler.contenthandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.CdaProcessor;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.DocumentValidationException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.contenthandler.api.Content;
import org.openmrs.module.shr.contenthandler.api.ContentHandler;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.XMLReader;

/**
 * Represents a content handler for CDA documents
 */
public class CdaContentHandler implements ContentHandler {
	
	// Log for this processor
	protected final Log log = LogFactory.getLog(this.getClass());

	// The lock object
	private static final Object s_lockObject = new Object();
	// The singleton instance
	private static CdaContentHandler s_instance = null;
	
	/**
	 * Get the singleton instance of the content handler
	 */
	public static CdaContentHandler getInstance() {
		if(s_instance == null)
			synchronized (s_lockObject) {
				if(s_instance == null)
					s_instance = new CdaContentHandler();
            }
		return s_instance;
	}
	
	/**
	 * This is a singleton so the clone will simply return the singleton
	 */
	@Override
	public ContentHandler cloneHandler() {
		return this;
	}
	
	/**
	 * Not supported yet
	 */
	@Override
	public Content fetchContent(String arg0) {
		throw new NotImplementedException();
	}
	
	/**
	 * Not supported yet
	 */
	@Override
	public Content fetchContent(int arg0) {
		throw new NotImplementedException();
	}
	
	/**
	 * Not supported yet
	 */
	@Override
	public List<Content> queryEncounters(Patient arg0, Date arg1, Date arg2) {
		throw new NotImplementedException();
	}
	
	/**
	 * Not supported yet
	 */
	@Override
	public List<Content> queryEncounters(Patient arg0, List<EncounterType> arg1, Date arg2, Date arg3) {
		throw new NotImplementedException();
	}
	
	/**
	 * Save content
	 */
	@Override
	@Transactional
	public Encounter saveContent(Patient patient, Map<EncounterRole, Set<Provider>> providerRole, EncounterType encounterType, Content content) {
		
		// TODO: Validate / add provider data to the header
		CdaProcessor processor = CdaProcessor.getInstance();
		try {

			// HACK: Handle the BOM or Java String PRoblems
			int offset = 0;
			byte[] data = content.getRawData();
			if(data[0] == 0x3F) 
				offset = 1;
			else if(data[0] == 0xEF && data[1] == 0xBB && data[2] == 0xBF) // UTF-8 BOM
				offset = 3;
			else if((data[0] == 0xFF && data[1] == 0xFE) || (data[0] == 0xFE || data[1] == 0xFF))
				offset = 2;
			log.info(new String(data, offset, data.length - offset));
			// Process the visit
	        Visit processedVisit = processor.processCdaDocument(new ByteArrayInputStream(data, offset, data.length - offset));
	        
	        ValidationIssueCollection issues = new ValidationIssueCollection();
	        Encounter lastEncounter = processedVisit.getEncounters().iterator().next(); // Assume the first encounter is the latest in the visit
	        
	        // Get the encounter for the processed visit and validate
	        for(Encounter enc : processedVisit.getEncounters())
	        {
	        	if(enc.getDateCreated().equals(processedVisit.getDateChanged()))
	        		lastEncounter = enc;
	        }
	        
        	// Validate that the patients match
        	if(!lastEncounter.getPatient().getId().equals(patient.getId()))
        		issues.error(String.format("Patient in meta-data doesn't match the patient in document. Expected %s but got %s", patient, lastEncounter.getPatient()));
        	for(Map.Entry<EncounterRole, Set<Provider>> providedProviderRole : providerRole.entrySet())
        	{
        		// Ensure the providers appear in the encounter provider list
	        	for(Provider providedProvider : providedProviderRole.getValue())
	        	{
	        		Boolean isProviderInRole = false;
		        	for(Map.Entry<EncounterRole, Set<Provider>> encounterProviderRoles : lastEncounter.getProvidersByRoles().entrySet())
		        		isProviderInRole |= encounterProviderRoles.getValue().contains(providedProvider);
		        	if(!isProviderInRole)
		        		issues.error(String.format("Provider %s is not identified in any roles within the CDA document", providedProvider));
	        	}
        		
        	}

	        // There are errors!
	        if(issues.hasErrors())
	        	throw new DocumentValidationException(null, issues);
	        else 
	        	return lastEncounter;
        }
        catch (IOException e) {
	        log.error("Error generated", e);
	        return null;
        }
        catch (DocumentImportException e) {
        	throw new RuntimeException(e);
        }
	}
	
}
