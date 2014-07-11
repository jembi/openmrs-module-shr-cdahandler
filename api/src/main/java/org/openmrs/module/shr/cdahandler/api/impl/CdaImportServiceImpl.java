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
import org.openmrs.module.shr.cdahandler.api.CdaImportService;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;
import org.openmrs.module.shr.cdahandler.processor.document.DocumentProcessor;
import org.openmrs.module.shr.cdahandler.processor.factory.impl.ClasspathScannerUtil;
import org.openmrs.module.shr.cdahandler.processor.factory.impl.DocumentProcessorFactory;
import org.openmrs.module.shr.cdahandler.processor.util.DatatypeProcessorUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsMetadataUtil;

/**
 * It is a default implementation of {@link CdaImportService}.
 */
public class CdaImportServiceImpl extends BaseOpenmrsService implements CdaImportService {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	
	
	/**
	 * Startup .. Might as well instantiate the ClassPathScannerUtil here
	 */
	@Override
    public void onStartup() {
	    super.onStartup();
	    log.info("Getting ClasspathScanner singleton");
	    ClasspathScannerUtil.getInstance();
    }



	@Override
	public Visit importDocument(InputStream doc) throws DocumentParseException 
	{

		// TODO: Store incoming to a temporary table for CDAs (like the HL7 queue)
		
		// Formatter
		XmlIts1Formatter formatter = new XmlIts1Formatter();
		formatter.addCachedClass(ClinicalDocument.class);
		formatter.getGraphAides().add(new DatatypeFormatter(R1FormatterCompatibilityMode.Canadian));
		formatter.setValidateConformance(false); // Don't validate to RMIM conformance
		
		// Parse the document
		IFormatterParseResult parseResult = formatter.parse(doc);
		// Output validation messages
		for(IResultDetail dtl : parseResult.getDetails())
		{
			if(dtl.getType() == ResultDetailType.ERROR)
				log.error(String.format("%s at %s", dtl.getMessage(), dtl.getLocation()), dtl.getException());
			else 
				log.warn(String.format("%s at %s", dtl.getMessage(), dtl.getLocation()), dtl.getException());
		}
		
		// Get the clinical document
		ClinicalDocument clinicalDocument = (ClinicalDocument)parseResult.getStructure();
		
		// Get the document parser
		DocumentProcessorFactory factory = DocumentProcessorFactory.getInstance();
		DocumentProcessor processor = factory.createProcessor(clinicalDocument);
		Visit visitInformation = processor.process(clinicalDocument);
		
		// Copy the original
		// TODO: Find out if we need this?
		// Format to the byte array output stream 
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
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
            }
		}

		return null;
	}
	
}
