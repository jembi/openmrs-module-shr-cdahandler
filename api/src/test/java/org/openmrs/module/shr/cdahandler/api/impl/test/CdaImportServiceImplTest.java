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
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.api.CdaImportService;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;
import org.openmrs.test.BaseModuleContextSensitiveTest;


/**
 * Test class for CdaImportServiceImpl
 */
public class CdaImportServiceImplTest extends BaseModuleContextSensitiveTest  {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	private CdaImportService m_service;
	
	@Before
	public void beforeEachTest() {
		this.m_service = Context.getService(CdaImportService.class);
		BasicConfigurator.configure();
		// TODO: Set properties
	}
	
	@Test
	public void shouldParseValidAphpTest() {
		URL validAphpSample = this.getClass().getResource("/validAphpSample.xml");
		File fileUnderTest = new File(validAphpSample.getFile());
		FileInputStream fs = null;
		try
		{
			fs = new FileInputStream(fileUnderTest);
			Visit parsedVisit = this.m_service.importDocument(fs);
			
		}
		catch(DocumentParseException e)
		{
			log.error("Error generated", e);
			Assert.fail();
		}
        catch (FileNotFoundException e) {
	        // TODO Auto-generated catch block
	        log.error("Error generated", e);
	        Assert.fail();
        }
	}
	
}
