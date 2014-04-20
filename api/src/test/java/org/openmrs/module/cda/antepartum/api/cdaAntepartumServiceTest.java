/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.cda.antepartum.api;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.h2.engine.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSource;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;
import org.openmrs.module.shr.cdahandler.api.cdaAntepartumService;
import org.openmrs.test.BaseModuleContextSensitiveTest;

/**
 * Tests {@link $ cdaAntepartumService}}.
 */
public class cdaAntepartumServiceTest extends BaseModuleContextSensitiveTest {
	
	@Before
	public void doSetUp() throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		
		Concept concept = new Concept();
		ConceptName cn = new ConceptName("HISTORY OF PREGNANCIES", Context.getLocale());
		concept.addName(cn);
		concept.setConceptId(1234567);
		concept.setDatatype(Context.getConceptService().getConceptDatatypeByName("Coded"));
		concept.setConceptClass(Context.getConceptService().getConceptClassByName("Finding"));
		
		ConceptSource newConceptSource = new ConceptSource();
		newConceptSource.setName("LOINC");
		newConceptSource.setDescription("LOINC-desc");
		newConceptSource.setHl7Code("hl7Code");
		newConceptSource.setCreator(Context.getAuthenticatedUser());
		Context.getConceptService().saveConceptSource(newConceptSource);
		
		ConceptMap map = new ConceptMap();
		map.setSourceCode("10162-6");
		map.setSource(newConceptSource);
		concept.addConceptMapping(map);
		
		concept = Context.getConceptService().saveConcept(concept);
		
		Concept newConcept = new Concept();
		ConceptName newCn = new ConceptName("BIRTHS LIVE (REPORTED)", Context.getLocale());
		newConcept.addName(newCn);
		newConcept.setConceptId(1234568);
		newConcept.setDatatype(Context.getConceptService().getConceptDatatypeByName("Coded"));
		newConcept.setConceptClass(Context.getConceptService().getConceptClassByName("Finding"));
		
		ConceptMap newMap = new ConceptMap();
		newMap.setSourceCode("11636-8");
		newMap.setSource(newConceptSource);
		newConcept.addConceptMapping(newMap);
		
		newConcept = Context.getConceptService().saveConcept(newConcept);
		
		Concept codedAnswer = new Concept();
		ConceptName answerName = new ConceptName("CODED ANSWER", Context.getLocale());
		codedAnswer.addName(answerName);
		codedAnswer.setConceptId(1234569);
		codedAnswer.setDatatype(Context.getConceptService().getConceptDatatypeByName("Coded"));
		codedAnswer.setConceptClass(Context.getConceptService().getConceptClassByName("Finding"));
		
		ConceptMap answerMap = new ConceptMap();
		answerMap.setSourceCode("1325");
		answerMap.setSource(newConceptSource);
		codedAnswer.addConceptMapping(answerMap);
		
		codedAnswer = Context.getConceptService().saveConcept(codedAnswer);
		
		Concept a = new Concept();
		ConceptName aName = new ConceptName("A", Context.getLocale());
		a.addName(aName);
		a.setConceptId(12345681);
		a.setDatatype(Context.getConceptService().getConceptDatatypeByName("Coded"));
		a.setConceptClass(Context.getConceptService().getConceptClassByName("Finding"));
		
		ConceptMap aMap = new ConceptMap();
		aMap.setSourceCode("11636-8-A");
		aMap.setSource(newConceptSource);
		a.addConceptMapping(aMap);
		
		a = Context.getConceptService().saveConcept(a);
		
		Concept b = new Concept();
		ConceptName bName = new ConceptName("B", Context.getLocale());
		b.addName(bName);
		b.setConceptId(12345682);
		b.setDatatype(Context.getConceptService().getConceptDatatypeByName("Coded"));
		b.setConceptClass(Context.getConceptService().getConceptClassByName("Finding"));
		
		ConceptMap bMap = new ConceptMap();
		bMap.setSourceCode("11636-8-B");
		bMap.setSource(newConceptSource);
		b.addConceptMapping(bMap);
		
		b = Context.getConceptService().saveConcept(b);
		
		Concept c = new Concept();
		ConceptName cName = new ConceptName("c", Context.getLocale());
		c.addName(cName);
		c.setConceptId(12345683);
		c.setDatatype(Context.getConceptService().getConceptDatatypeByName("Coded"));
		c.setConceptClass(Context.getConceptService().getConceptClassByName("Finding"));
		
		ConceptMap cMap = new ConceptMap();
		cMap.setSourceCode("11636-8-C");
		cMap.setSource(newConceptSource);
		c.addConceptMapping(cMap);
		
		c = Context.getConceptService().saveConcept(c);
		
		Assert.assertNotNull(concept);
	}
	
	@Test
	public void shouldSetupContext() {
		assertNotNull(Context.getService(cdaAntepartumService.class));
	}
	
	@Test
	public void shouldParseValidAPHP() throws DocumentParseException {
		InputStream sample = getClass().getClassLoader().getResourceAsStream("sampleAphp.xml");
		Encounter e = Context.getService(cdaAntepartumService.class).importAntepartumHistoryAndPhysical(sample);
		assertNotNull(e);
		assertEquals(e.getAllObs().size(), 5);
		Set<Obs> obsList = new HashSet<Obs>();
		obsList = e.getAllObs();
		
		Concept grouper = Context.getConceptService().getConceptByMapping("10162-6", "LOINC");
		Concept answer = Context.getConceptService().getConceptByMapping("1325", "LOINC");
		Concept cd = Context.getConceptService().getConceptByMapping("11636-8", "LOINC");
		Concept in = Context.getConceptService().getConceptByMapping("11636-8-A", "LOINC");
		Concept ts = Context.getConceptService().getConceptByMapping("11636-8-C", "LOINC");
		Concept st = Context.getConceptService().getConceptByMapping("11636-8-B", "LOINC");
		assertNotNull(grouper);
		System.out.println(grouper.getId());
		
		for (Obs obs : obsList) {
			
			if (obs.getConcept().getConceptId().equals(grouper.getId())) {
				assertTrue(obs.isObsGrouping());
			} else {
				assertFalse(obs.isObsGrouping());
			}
			
			if (obs.getConcept().getId().equals(cd.getId())) {
				assertEquals(obs.getValueCoded().getConceptId(), answer.getId());
			}
			if (obs.getConcept().getId().equals(in.getId())) {
				assertEquals(obs.getValueNumeric(),new Double(1));
				
			}
			
			if (obs.getConcept().getId().equals(st.getId())) {
				assertEquals(obs.getValueText(),"TEST");
				
			}
			
			if (obs.getConcept().getId().equals(ts.getId())) {
				//assertEquals(obs.getValueDate(),new Date(2011,0,01));
				
			}
			
		}
		
	}
}
