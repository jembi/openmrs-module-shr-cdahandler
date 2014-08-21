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
package org.openmrs.module.shr.cdahandler;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.shr.cdahandler.contenthandler.CdaContentHandler;
import org.openmrs.module.shr.cdahandler.processor.document.DocumentProcessor;
import org.openmrs.module.shr.cdahandler.processor.document.impl.ihe.pcc.AntepartumSummaryDocumentProcessor;
import org.openmrs.module.shr.contenthandler.api.AlreadyRegisteredException;
import org.openmrs.module.shr.contenthandler.api.CodedValue;
import org.openmrs.module.shr.contenthandler.api.ContentHandlerService;
import org.openmrs.module.shr.contenthandler.api.InvalidCodedValueException;

/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */
public class ShrCdaHandlerActivator implements ModuleActivator {
	
	protected Log log = LogFactory.getLog(getClass());
	
	// Format codes this handler supports
	protected final Map<String, String> m_formatTypeCodes = new HashMap<String, String>()
			{{
				put("urn:ihe:pcc:xds-ms:2007", "*");
				put("urn:ihe:pcc:hp:2008", "34117-2");
				put("urn:ihe:pcc:aphp:2008", "34117-2");
				put("urn:ihe:pcc:aps:2007", "57055-6");
				put("urn:ihe:pcc:ape:2008", "34895-3");
				put("urn:ihe:pcc:lds:2009", "57057-2");
				put("urn:ihe:pcc:mds:2009", "57058-0");
			}};
	

	/**
	 * @see ModuleActivator#contextRefreshed()
	 */
	public void contextRefreshed() {
		this.registerContentHandler();
		log.info("SHR CDA Handler Module refreshed");
	}
	
	/**
	 * Register the content handler
	 */
	private void registerContentHandler() {
		// Register the format codes
		ContentHandlerService contentHandler = Context.getService(ContentHandlerService.class);
		for(Map.Entry<String, String> formatTypeCode : this.m_formatTypeCodes.entrySet())
		{
			CodedValue formatCode = new CodedValue(formatTypeCode.getKey(), "IHE Format Codes");
			CodedValue typeCode = new CodedValue(formatTypeCode.getValue(), "LOINC");
			try {
				if(contentHandler.getContentHandler(typeCode, formatCode) == null)
					contentHandler.registerContentHandler(typeCode, formatCode, CdaContentHandler.getInstance());
	            
            }
            catch (AlreadyRegisteredException e) {
	            log.error("Error generated", e);
            }
            catch (InvalidCodedValueException e) {
	            log.error("Error generated", e);
            }
		}
    }

	/**
	 * @see ModuleActivator#started()
	 */
	public void started() {
		this.registerContentHandler();
		log.info("SHR CDA Handler Module started");
		
	}
	
	/**
	 * @see ModuleActivator#stopped()
	 */
	public void stopped() {
		log.info("SHR CDA Handler Module stopped");
	}
	
	/**
	 * @see ModuleActivator#willRefreshContext()
	 */
	public void willRefreshContext() {
		log.info("Refreshing SHR CDA Handler Module");
	}
	
	/**
	 * @see ModuleActivator#willStart()
	 */
	public void willStart() {
		log.info("Starting SHR CDA Handler Module");
	}
	
	/**
	 * @see ModuleActivator#willStop()
	 */
	public void willStop() {
		log.info("Stopping SHR CDA Handler Module");
	}
		
}
