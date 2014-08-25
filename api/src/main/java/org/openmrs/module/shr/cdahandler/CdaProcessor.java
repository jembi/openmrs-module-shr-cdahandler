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
import org.marc.everest.resultdetails.DatatypeValidationResultDetail;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.openmrs.Visit;
import org.openmrs.module.shr.cdahandler.configuration.CdaHandlerConfiguration;
import org.openmrs.module.shr.cdahandler.everest.EverestUtil;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.DocumentValidationException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.document.DocumentProcessor;
import org.openmrs.module.shr.cdahandler.processor.factory.impl.ClasspathScannerUtil;
import org.openmrs.module.shr.cdahandler.processor.factory.impl.DocumentProcessorFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * A CDA processor singleton which can be called by the service or a task
 * meant to scoop up queued documents
 * 
 */
public final class CdaProcessor {

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
	
	// Log for this processor
	protected final Log log = LogFactory.getLog(this.getClass());
	private static CdaProcessor s_instance = null;
	
	private static Object s_lockObject = new Object();
	
	// Configuration
	private final CdaHandlerConfiguration m_configuration = CdaHandlerConfiguration.getInstance();

	/**
	 * CDA processor constructor
	 */
	protected CdaProcessor()
	{
	}
	
	/**
	 * Initialize any classes that need to be inialized for this instance
	 */
	private void initialize() {
		// Initialize singletons. Let them create any properties they need
	    log.info("Initialize singletons");
	    // Factories
	    ClasspathScannerUtil.getInstance();
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
	@Transactional(readOnly = true)
	public Visit processCdaDocument(InputStream doc) throws DocumentImportException {

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
				parsingIssues.error(String.format("HL7v3 Validation: %s at %s", dtl.getMessage(), dtl.getLocation()));
			else  
				parsingIssues.warn(String.format("HL7v3 Validation: %s at %s", dtl.getMessage(), dtl.getLocation()));
		}
		// Any serious validation has errors or structure is null?
		if(parsingIssues.hasErrors() && this.m_configuration.getValidationEnabled() || parseResult.getStructure() == null)
			throw new DocumentValidationException(parseResult.getStructure(), parsingIssues);
		
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
		return visitInformation;
    }
	
}
