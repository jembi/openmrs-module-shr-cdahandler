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
package org.openmrs.module.shr.cdahandler.api.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import org.openmrs.VisitAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.shr.cdahandler.CdaProcessor;
import org.openmrs.module.shr.cdahandler.api.CdaImportService;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.processor.document.DocumentProcessor;
import org.openmrs.module.shr.cdahandler.processor.factory.impl.*;
import org.openmrs.module.shr.cdahandler.processor.util.*;

/**
 * It is a default implementation of {@link CdaImportService}.
 */
public class CdaImportServiceImpl extends BaseOpenmrsService implements CdaImportService {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	// The processor for this service 
	private CdaProcessor m_processor;


	

	/** 
	 * TODO: This needs to be more thread/process safe.. 
	 * @see org.openmrs.module.shr.cdahandler.api.CdaImportService#importDocument(java.io.InputStream)
	 */
	@Override
	public Visit importDocument(InputStream doc) throws DocumentImportException 
	{
		if(this.m_processor == null)
			this.m_processor = CdaProcessor.getInstance();
		// TODO: Store incoming to a temporary table for CDAs (like the HL7 queue)
		return this.m_processor.processCdaDocument(doc);
		
	}
	
}
