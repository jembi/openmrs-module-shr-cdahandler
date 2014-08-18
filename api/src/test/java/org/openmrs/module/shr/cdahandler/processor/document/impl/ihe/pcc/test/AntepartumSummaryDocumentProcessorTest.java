package org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc.test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Component3;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Component4;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Component5;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Entry;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.EntryRelationship;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.Encounter;
import org.openmrs.GlobalProperty;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.api.CdaImportService;
import org.openmrs.module.shr.cdahandler.api.impl.test.util.CdaDocumentCreatorUtil;
import org.openmrs.module.shr.cdahandler.configuration.CdaHandlerConfiguration;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc.AntepartumSummaryDocumentProcessor;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;


public class AntepartumSummaryDocumentProcessorTest extends BaseModuleContextSensitiveTest  {

	private static final String ACTIVE_LIST_INITIAL_XML = "include/ActiveListTest.xml";
	protected final Log log = LogFactory.getLog(this.getClass());
	
	@Before
	public void beforeEachTest() throws Exception {

		GlobalProperty saveDir = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR, "C:\\data\\");
		Context.getAdministrationService().setGlobalProperty(CdaHandlerConfiguration.PROP_VALIDATE_CONCEPT_STRUCTURE, "false");
		Context.getAdministrationService().saveGlobalProperty(saveDir);
		executeDataSet(ACTIVE_LIST_INITIAL_XML);
		// TODO: Set properties
	}
	
	/**
	 * Create an parse an APS document
	 */
	@Test
	public void shouldParseValidAps() {
		ClinicalDocument documentUnderTest = CdaDocumentCreatorUtil.createApsDocument();
		CdaDocumentCreatorUtil.logDocument(documentUnderTest);
		try {
	        Visit visit = new AntepartumSummaryDocumentProcessor().process(documentUnderTest);
	        assertEquals("Antepartum Summary", visit.getVisitType().getName());
	        assertEquals(1, visit.getEncounters().size());
	        
	        Encounter mainEncounter = visit.getEncounters().iterator().next();
	        // Assert there are the correct number of items
	        int expectedObsCount = documentUnderTest.getComponent().getBodyChoiceIfStructuredBody().getComponent().size();
	        assertEquals(expectedObsCount, mainEncounter.getObsAtTopLevel(true).size());
	        
        }
		catch(Exception e)
		{
	        log.error("Error generated", e);
        	fail(e.getMessage());
			
		}
	}
	
}