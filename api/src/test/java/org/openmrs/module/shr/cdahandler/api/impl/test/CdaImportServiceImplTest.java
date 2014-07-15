package org.openmrs.module.shr.cdahandler.api.impl.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.SubstanceAdministration;
import org.openmrs.GlobalProperty;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.api.CdaImportService;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc.AntepartumHistoryAndPhysicalDocumentProcessor;
import org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc.MedicalDocumentsDocumentProcessor;
import org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc.MedicalSummaryDocumentProcessor;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;


/**
 * Test class for CdaImportServiceImpl
 */
public class CdaImportServiceImplTest extends BaseModuleContextSensitiveTest  {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	private CdaImportService m_service;
	
	@Before
	public void beforeEachTest() {

		this.m_service = Context.getService(CdaImportService.class);
		GlobalProperty saveDir = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR, "C:\\data\\");
		Context.getAdministrationService().saveGlobalProperty(saveDir);
		BasicConfigurator.configure();
		
		// TODO: Set properties
	}
	
	/**
	 * Do the parsing of a CDA
	 */
	private String doParseCda(String resourceName)
	{
		URL validAphpSample = this.getClass().getResource(resourceName);
		File fileUnderTest = new File(validAphpSample.getFile());
		FileInputStream fs = null;
		try
		{
			fs = new FileInputStream(fileUnderTest);
			Visit parsedVisit = this.m_service.importDocument(fs);
			assertEquals(parsedVisit, Context.getVisitService().getVisitByUuid(parsedVisit.getUuid()));
			return parsedVisit.getUuid();
		}
		catch(DocumentImportException e)
		{
			log.error("Error generated", e);
			Assert.fail();
			return null;
		}
        catch (FileNotFoundException e) {
	        // TODO Auto-generated catch block
	        log.error("Error generated", e);
	        Assert.fail();
	        return null;
        }

	}
	
	@Test
	public void shouldParseValidAphpTest() {
		String id = this.doParseCda("/validAphpSample.xml");
		assertEquals(new AntepartumHistoryAndPhysicalDocumentProcessor().getTemplateName(), Context.getVisitService().getVisitByUuid(id).getVisitType().getName());

	}

	@Test
	public void shouldParseValidLevel3Test() {
		String id = this.doParseCda("/validCdaLevel3Sample.xml");
		assertEquals(new MedicalDocumentsDocumentProcessor().getTemplateName(), Context.getVisitService().getVisitByUuid(id).getVisitType().getName());

	}

	@Test
	public void shouldParseValidCdaFromOscarTest() {
		String id = this.doParseCda("/cdaFromOscarEmr.xml");
		assertEquals(new MedicalSummaryDocumentProcessor().getTemplateName(), Context.getVisitService().getVisitByUuid(id).getVisitType().getName());
	}

	@Test
	public void shouldParseValidLevel3Test2() {
		String id = this.doParseCda("/validCdaLevel3Sample2.xml");
		assertEquals(new MedicalSummaryDocumentProcessor().getTemplateName(), Context.getVisitService().getVisitByUuid(id).getVisitType().getName());
	}
}
