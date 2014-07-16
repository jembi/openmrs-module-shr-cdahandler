package org.openmrs.module.shr.cdahandler.api.processor.util;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.marc.everest.datatypes.PQ;
import org.marc.everest.datatypes.SD;
import org.marc.everest.datatypes.generic.CE;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsConceptUtil;
import org.openmrs.test.BaseModuleContextSensitiveTest;


public class OpenmrsConceptUtilTest extends BaseModuleContextSensitiveTest {
	
	// Log
	protected final Log log = LogFactory.getLog(this.getClass());

	private Concept m_weightConcept = null;
	private ConceptReferenceTerm m_weightTerm = null;
	private ConceptSource m_loincSource = null;
	private OpenmrsConceptUtil m_conceptUtil = null;
	private CE<String> m_loincWeightTerm = new CE<String>("3141-9", CdaHandlerConstants.CODE_SYSTEM_LOINC, "LOINC", null, "BODY WEIGHT (MEASURED)", null);
	private CE<String> m_loincHeightTerm = new CE<String>("8302-2", CdaHandlerConstants.CODE_SYSTEM_LOINC, "LOINC", null, "BODY HEIGHT (MEASURED)", null);
	private CE<String> m_vitalSignsTerm = new CE<String>("8716-3", CdaHandlerConstants.CODE_SYSTEM_LOINC, "LOINC", null, "VITAL SIGNS", null);
	
	@Before
	public void setupConcepts()
	{
		// Create our sample set
		
		// Create concept Source
		this.m_loincSource = new ConceptSource();
		this.m_loincSource.setName("LOINC");
		this.m_loincSource.setHl7Code("LN");
		Context.getConceptService().saveConceptSource(this.m_loincSource);

		// Create reference term
		this.m_weightTerm = new ConceptReferenceTerm();
		this.m_weightTerm.setCode("3141-9");
		this.m_weightTerm.setDescription("BODY WEIGHT (MEASURED)");
		this.m_weightTerm.setConceptSource(this.m_loincSource);
		Context.getConceptService().saveConceptReferenceTerm(this.m_weightTerm);
		
		// A concept for Weight in KG that has a map to 
		this.m_weightConcept = new ConceptNumeric();
		this.m_weightConcept.setConceptClass(Context.getConceptService().getConceptClassByName("Misc"));
		this.m_weightConcept.addName(new ConceptName("A Weight Concept in Kg", Context.getLocale()));
		this.m_weightConcept.setPreferredName(this.m_weightConcept.getName());
		((ConceptNumeric)this.m_weightConcept).setUnits("kg");
		// Map 
		ConceptMap map = new ConceptMap();
		map.setConcept(this.m_weightConcept);
		map.setConceptReferenceTerm(this.m_weightTerm);
		map.setConceptMapType(Context.getConceptService().getConceptMapTypeByName("same-as"));
		this.m_weightConcept.addConceptMapping(map);
		Context.getConceptService().saveConcept(this.m_weightConcept);
		
		this.m_conceptUtil = OpenmrsConceptUtil.getInstance();
	}

	/**
	 * Get concepts
	 */
	@Test
	public void testGetConcepts() {

		try {
			List<Concept> matches = this.m_conceptUtil.getConcepts(m_loincWeightTerm);
			assertEquals(1, matches.size());
	        assertEquals(this.m_weightConcept, matches.get(0) );
        }
        catch (DocumentImportException e) {
	        // TODO Auto-generated catch block
	        log.error("Error generated", e);
	        fail();
        }
		
	}

	/**
	 * Test GetConcept points to WEIGHT
	 */
	@Test
	public void testGetConcept() {
		try {
	        assertEquals(this.m_weightConcept, this.m_conceptUtil.getConcept(m_loincWeightTerm));
        }
        catch (DocumentImportException e) {
	        log.error(e);
        }
	}

	/**
	 * Get concept datatype
	 */
	@Test
	public void testGetConceptDatatypeSD() {
		assertEquals(ConceptDatatype.COMPLEX_UUID, this.m_conceptUtil.getConceptDatatype(new SD()).getUuid());
	}

	/**
	 * Get concept datatype
	 */
	@Test
	public void testGetConceptDatatypePQ() {
		assertEquals(ConceptDatatype.NUMERIC_UUID, this.m_conceptUtil.getConceptDatatype(new PQ()).getUuid());
	}

	/**
	 * Create a concept
	 */
	@Test
	public void testCreateConceptCVOfQ() {
		
		try {
	        Concept createdConcept = this.m_conceptUtil.createConcept(m_vitalSignsTerm);
	        assertEquals(1, createdConcept.getConceptMappings().size());
	        assertEquals(this.m_loincSource, createdConcept.getConceptMappings().iterator().next().getConceptReferenceTerm().getConceptSource());
	        assertEquals("VITAL SIGNS", createdConcept.getPreferredName(Context.getLocale()).getName());
        }
        catch (DocumentImportException e) {
	        // TODO Auto-generated catch block
	        log.error("Error generated", e);
        }
	}

	/**
	 * Test creating a concept for unit of measure
	 * Auto generated method comment
	 *
	 */
	@Test
	public void testCreateConceptCVOfQANY() {
		try {
	        Concept createdConcept = this.m_conceptUtil.createConcept(m_loincHeightTerm, new PQ(BigDecimal.ONE, "cm"));
	        assertEquals(1, createdConcept.getConceptMappings().size());
	        assertEquals(this.m_loincSource, createdConcept.getConceptMappings().iterator().next().getConceptReferenceTerm().getConceptSource());
	        assertEquals("BODY HEIGHT (MEASURED)", createdConcept.getConceptMappings().iterator().next().getConceptReferenceTerm().getName());
	        assertEquals("BODY HEIGHT (MEASURED) (cm)", createdConcept.getPreferredName(Context.getLocale()).getName());
	        assertEquals("cm", ((ConceptNumeric)createdConcept).getUnits());
        }
        catch (DocumentImportException e) {
	        // TODO Auto-generated catch block
	        log.error("Error generated", e);
        }
	}

	/**
	 * Get a type specific concept matching specific
	 */
	@Test
	public void testGetTypeSpecificConceptMatchExisting() {
		try {
	        Concept concept = this.m_conceptUtil.getTypeSpecificConcept(this.m_loincWeightTerm, new PQ(BigDecimal.ONE, "kg"));
	        assertEquals(concept, this.m_weightConcept);
        }
        catch (DocumentImportException e) {
	        // TODO Auto-generated catch block
	        log.error("Error generated", e);
        }
	}

	/**
	 * Get a type specific concept new
	 */
	@Test
	public void testGetTypeSpecificConceptNew() {
		try {
	        Concept concept = this.m_conceptUtil.getTypeSpecificConcept(this.m_loincWeightTerm, new PQ(BigDecimal.ONE, "[lbs_i]"));
	        assertNull(concept);

        }
        catch (DocumentImportException e) {
	        // TODO Auto-generated catch block
	        log.error("Error generated", e);
        }
	}

}
