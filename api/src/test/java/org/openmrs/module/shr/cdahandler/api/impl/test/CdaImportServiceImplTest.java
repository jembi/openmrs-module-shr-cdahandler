package org.openmrs.module.shr.cdahandler.api.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.marc.everest.datatypes.TS;
import org.openmrs.GlobalProperty;
import org.openmrs.Relationship;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
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
		Context.getAdministrationService().setGlobalProperty(CdaHandlerConstants.PROP_TEST_MODE, "true");
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
	public void shouldParseValidAphpPovichTest() {
		String id = this.doParseCda("/validAphpSamplePovich.xml");
		assertEquals(new AntepartumHistoryAndPhysicalDocumentProcessor().getTemplateName(), Context.getVisitService().getVisitByUuid(id).getVisitType().getName());

	}

	@Test
	public void shouldParseValidAphpFullTest() {
		String id = this.doParseCda("/validAphpSampleFullSections.xml");
		assertEquals(new AntepartumHistoryAndPhysicalDocumentProcessor().getTemplateName(), Context.getVisitService().getVisitByUuid(id).getVisitType().getName());

	}



	@Test
	public void shouldParseValidAphpPovichTest2() {
		String id = this.doParseCda("/validAphpSamplePovich.xml");
		Visit visit1 = Context.getVisitService().getVisitByUuid(id);
		id = this.doParseCda("/validAphpSamplePovich2.xml");
		assertEquals(new AntepartumHistoryAndPhysicalDocumentProcessor().getTemplateName(), Context.getVisitService().getVisitByUuid(id).getVisitType().getName());
//		List<Relationship> relPerson = Context.getPersonService().getRelationships(visit1.getPatient(), Context.getPersonService().findPeople("Thomas Caster", false).iterator().next(), Context.getPersonService().getRelationshipTypeByName("xx-fatherofbaby^^^&2.16.840.1.113883.6.96&ISO"));
//		Calendar endTime = Calendar.getInstance();
//		endTime.setTime(relPerson.get(0).getEndDate());
//		assertTrue(TS.valueOf("20140609").toIvl().contains(new TS(endTime)));
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
	public void shouldParseValidCdaFromOscar2Test() {
		String id = this.doParseCda("/cdaFromOscarEmr2.xml");
		assertEquals(new MedicalSummaryDocumentProcessor().getTemplateName(), Context.getVisitService().getVisitByUuid(id).getVisitType().getName());
	}
	
	@Test
	public void shouldParseValidLevel3Test2() {
		String id = this.doParseCda("/validCdaLevel3Sample2.xml");
		assertEquals(new MedicalSummaryDocumentProcessor().getTemplateName(), Context.getVisitService().getVisitByUuid(id).getVisitType().getName());
	}

	@Test
	public void shouldParseCdaFromHl7() {
		String id = this.doParseCda("/cdaFromHl7.xml");
		assertEquals(new MedicalSummaryDocumentProcessor().getTemplateName(), Context.getVisitService().getVisitByUuid(id).getVisitType().getName());
	}

}
