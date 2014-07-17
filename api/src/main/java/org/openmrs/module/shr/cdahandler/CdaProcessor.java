package org.openmrs.module.shr.cdahandler;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.formatters.interfaces.IFormatterParseResult;
import org.marc.everest.formatters.xml.datatypes.r1.DatatypeFormatter;
import org.marc.everest.formatters.xml.datatypes.r1.R1FormatterCompatibilityMode;
import org.marc.everest.formatters.xml.its1.XmlIts1Formatter;
import org.marc.everest.interfaces.IResultDetail;
import org.marc.everest.interfaces.ResultDetailType;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.DocumentValidationException;
import org.openmrs.module.shr.cdahandler.processor.document.DocumentProcessor;
import org.openmrs.module.shr.cdahandler.processor.factory.impl.ClasspathScannerUtil;
import org.openmrs.module.shr.cdahandler.processor.factory.impl.DocumentProcessorFactory;
import org.openmrs.module.shr.cdahandler.processor.factory.impl.EntryProcessorFactory;
import org.openmrs.module.shr.cdahandler.processor.factory.impl.SectionProcessorFactory;
import org.openmrs.module.shr.cdahandler.processor.util.AssignedEntityProcessorUtil;
import org.openmrs.module.shr.cdahandler.processor.util.DatatypeProcessorUtil;
import org.openmrs.module.shr.cdahandler.processor.util.LocationOrganizationProcessorUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsConceptUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsMetadataUtil;
import org.openmrs.module.shr.cdahandler.processor.util.PatientRoleProcessorUtil;
import org.openmrs.module.shr.cdahandler.processor.util.PersonProcessorUtil;

/**
 * A CDA processor singleton which can be called by the service or a task
 * meant to scoop up queued documents
 * 
 */
public class CdaProcessor {

	// Log for this processor
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private static CdaProcessor s_instance = null;
	private static Object s_lockObject = new Object();
	
	/**
	 * CDA processor constructor
	 */
	protected CdaProcessor()
	{
	}

	/**
	 * Get (and initialize) an instance of the CdaProcessor 
	 */
	public static CdaProcessor getInstance()
	{
		if(s_instance == null)
			synchronized (s_lockObject) {
				if(s_instance == null)
				{
					s_instance = new CdaProcessor();
					s_instance.initialize();
				}
            }
		return s_instance;
	}
	
	/**
	 * Initialize any classes that need to be inialized for this instance
	 */
	private void initialize() {
		// Initialize singletons. Let them create any properties they need
	    log.info("Initialize singletons");
	    
	    // Factories
	    ClasspathScannerUtil.getInstance();
	    DocumentProcessorFactory.getInstance();
	    EntryProcessorFactory.getInstance();
	    SectionProcessorFactory.getInstance();
	    
	    // Utils
	    AssignedEntityProcessorUtil.getInstance();
	    DatatypeProcessorUtil.getInstance();
	    LocationOrganizationProcessorUtil.getInstance();
	    OpenmrsConceptUtil.getInstance();
	    OpenmrsMetadataUtil.getInstance();
	    PatientRoleProcessorUtil.getInstance();
	    PersonProcessorUtil.getInstance();	    
    }

	/**
	 * Processes a single CDA document
	 * 
	 * This method *should* be called using CDAs that have come from a processing queue. 
	 * TODO: Is there a mechanism to ensure duplicate concepts/providers/etc. aren't created in the oMRS database, currently
	 * it is possible to have multiple processes accessing the service at once... Is there any other way to do this in oMRS natively
	 * (such as doing transactions, etc.)
	 * @throws DocumentImportException 
	 */
	public Visit processCdaDocument(InputStream doc) throws DocumentImportException {
		
		try
		{
			// Formatter
			XmlIts1Formatter formatter = new XmlIts1Formatter();
			formatter.addCachedClass(ClinicalDocument.class);
			formatter.getGraphAides().add(new DatatypeFormatter(R1FormatterCompatibilityMode.Canadian));
			formatter.setValidateConformance(false); // Don't validate to RMIM conformance
			
			// Parse the document
			log.debug("Starting processing of document");
			IFormatterParseResult parseResult = formatter.parse(doc);
			log.debug("Process document complete.");
			// Output validation messages
			for(IResultDetail dtl : parseResult.getDetails())
			{
				if(dtl.getType() == ResultDetailType.ERROR && dtl.getException() != null)
					log.error(String.format("%s at %s", dtl.getMessage(), dtl.getLocation()), dtl.getException());
				else if(dtl.getException() != null) 
					log.debug(String.format("%s at %s", dtl.getMessage(), dtl.getLocation()), dtl.getException());
			}
			
			// Get the clinical document
			ClinicalDocument clinicalDocument = (ClinicalDocument)parseResult.getStructure();

			// Get the document parser
			DocumentProcessorFactory factory = DocumentProcessorFactory.getInstance();
			DocumentProcessor processor = factory.createProcessor(clinicalDocument);
			Visit visitInformation = processor.process(clinicalDocument);
			// Copy the original
			// TODO: Find out if we need this?
			// Format to the byte array output stream 
			/*ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try
			{
	
				formatter.graph(baos, clinicalDocument);
				VisitAttribute original = new VisitAttribute();
				original.setAttributeType(OpenmrsMetadataUtil.getInstance().getVisitOriginalCopyAttributeType());
				original.setValue(baos.toString());
				visitInformation.addAttribute(original);
				visitInformation = Context.getVisitService().saveVisit(visitInformation);
			}
			finally
			{
				try {
		            baos.close();
	            }
	            catch (IOException e) {
		            log.error("Error generated", e);
					return null;
	            }
			}*/
			Context.flushSession();
			return visitInformation;
		}
		catch(DocumentValidationException e)
		{
			// TODO: How to rollback?
			
			for(IResultDetail dtl : e.getValidationIssues())
				log.error(dtl.getMessage());
			throw e;
		}
	    
    }
	
}
