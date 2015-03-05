package org.openmrs.module.shr.cdahandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.openmrs.Visit;
import org.openmrs.module.shr.cdahandler.configuration.CdaHandlerConfiguration;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.processor.document.DocumentProcessor;
import org.openmrs.module.shr.cdahandler.processor.factory.impl.ClasspathScannerUtil;
import org.openmrs.module.shr.cdahandler.processor.factory.impl.DocumentProcessorFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * A CDA processor singleton which can be called by the service or a task
 * meant to scoop up queued documents
 * 
 */
public final class CdaImporter {

	/**
	 * Get (and initialize) an instance of the CdaProcessor 
	 */
	public static CdaImporter getInstance()
	{
		if(s_instance == null)
			synchronized (s_lockObject) {
				if(s_instance == null)
				{
					s_instance = new CdaImporter();
					s_instance.initialize();
				}
            }
		return s_instance;
	}
	
	// Log for this processor
	protected final Log log = LogFactory.getLog(this.getClass());
	private static CdaImporter s_instance = null;
	
	private static Object s_lockObject = new Object();
	
	// Configuration
	private final CdaHandlerConfiguration m_configuration = CdaHandlerConfiguration.getInstance();

	/**
	 * CDA processor constructor
	 */
	protected CdaImporter()
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
	public Visit processCdaDocument(ClinicalDocument doc) throws DocumentImportException {

	
		// Get the document parser
		DocumentProcessorFactory factory = DocumentProcessorFactory.getInstance();
		DocumentProcessor processor = factory.createProcessor(doc);

		Visit visitInformation = processor.process(doc);
		
		
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
