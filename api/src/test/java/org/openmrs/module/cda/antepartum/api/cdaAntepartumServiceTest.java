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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;
import org.openmrs.module.shr.cdahandler.api.cdaAntepartumService;
import org.openmrs.test.BaseModuleContextSensitiveTest;

/**
 * Tests {@link ${cdaAntepartumService}}.
 */
public class  cdaAntepartumServiceTest extends BaseModuleContextSensitiveTest {
	
	@Before
	public void doSetUp() throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
	}
	
	@Test
	public void shouldSetupContext() {
		assertNotNull(Context.getService(cdaAntepartumService.class));
	}
	
	@Test
	public void shouldParseValidAPHP() throws DocumentParseException {
		/*InputStream sample = getClass().getClassLoader().getResourceAsStream("sampleAphp.xml");
		Encounter e = Context.getService(cdaAntepartumService.class).importAntepartumHistoryAndPhysical(sample);
		assertNotNull(e);*/
	}
}
