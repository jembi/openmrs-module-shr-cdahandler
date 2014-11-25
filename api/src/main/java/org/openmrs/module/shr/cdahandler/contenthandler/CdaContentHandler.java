package org.openmrs.module.shr.cdahandler.contenthandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.formatters.interfaces.IFormatterParseResult;
import org.marc.everest.formatters.interfaces.IXmlStructureFormatter;
import org.marc.everest.interfaces.IResultDetail;
import org.marc.everest.interfaces.ResultDetailType;
import org.marc.everest.resultdetails.DatatypeValidationResultDetail;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.api.CdaImportService;
import org.openmrs.module.shr.cdahandler.everest.EverestUtil;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.DocumentValidationException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.util.PatientRoleProcessorUtil;
import org.openmrs.module.shr.contenthandler.api.Content;
import org.openmrs.module.shr.contenthandler.api.ContentHandler;
import org.springframework.transaction.annotation.Transactional;

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
	
	// Patient utility 
	private PatientRoleProcessorUtil m_patientUtil = PatientRoleProcessorUtil.getInstance();
	
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
	 * Save content
	 */
	@Override
	@Transactional
	public Encounter saveContent(Patient patient, Map<EncounterRole, Set<Provider>> providerRole, EncounterType encounterType, Content content) {
		
		// TODO: Validate / add provider data to the header
		CdaImportService importService = Context.getService(CdaImportService.class);
		try {

			// HACK: Handle the BOM or Java String Problems
			int offset = 0;
			byte[] data = content.getRawData();
			if(data[0] == 0x3F) 
				offset = 1;
			else if(data[0] == 0xEF && data[1] == 0xBB && data[2] == 0xBF) // UTF-8 BOM
				offset = 3;
			else if((data[0] == 0xFF && data[1] == 0xFE) || (data[0] == 0xFE || data[1] == 0xFF))
				offset = 2;
			log.info(new String(data, offset, data.length - offset));

			// Process the content
			IXmlStructureFormatter formatter = EverestUtil.createFormatter();
			IFormatterParseResult parseResult = formatter.parse(new ByteArrayInputStream(data, offset, data.length - offset));
			ValidationIssueCollection parseIssues = new ValidationIssueCollection();
			for(IResultDetail dtl : parseResult.getDetails())
			{
				if(dtl.getType() == ResultDetailType.ERROR && !(dtl instanceof DatatypeValidationResultDetail))
					parseIssues.error(String.format("HL7 Processing Error: %s at %s", dtl.getMessage(), dtl.getLocation()));
				else
					parseIssues.warn(String.format("HL7 Processing Warning: %s at %s", dtl.getMessage(), dtl.getLocation()));
			}
			
			// Now validate that the record target matches the XDS meta-data
			ClinicalDocument document = (ClinicalDocument)parseResult.getStructure();
			
			// Validate record target
			if(document == null || document.getRecordTarget().size() != 1)
				parseIssues.error("Missing recordTarget on ClinicalDocument");
			else
			{
				PatientIdentifier pid = this.m_patientUtil.getApplicablePatientIdentifier(document.getRecordTarget().get(0).getPatientRole().getId());
				boolean containsPid = false;
				for(PatientIdentifier xdsPid : patient.getIdentifiers())
					containsPid |= xdsPid.getIdentifier().equals(pid.getIdentifier()) && xdsPid.getIdentifierType().getId().equals(pid.getIdentifierType().getId());
				if(!containsPid)
					parseIssues.error(String.format("Patient identifier '%s^^^&%s&ISO' in recordTarget must match patientIdentifier provided in XDS meta-data", pid.getIdentifier(), pid.getIdentifierType().getName()));
			}
			
			if(parseIssues.hasErrors() || document == null)
				throw new DocumentValidationException(parseResult.getStructure(), parseIssues);
			
			// Parse the visit
	        Visit processedVisit = importService.importDocument((ClinicalDocument)parseResult.getStructure());
	        
	        Encounter lastEncounter = processedVisit.getEncounters().iterator().next(); // Assume the first encounter is the latest in the visit
	        
	        // Get the encounter for the processed visit and validate
	        for(Encounter enc : processedVisit.getEncounters())
	        {
	        	if(enc.getDateCreated().equals(processedVisit.getDateChanged()))
	        		lastEncounter = enc;
	        }
	        
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
