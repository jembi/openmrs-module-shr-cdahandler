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
package org.openmrs.module.shr.cdahandler.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.formatters.FormatterUtil;
import org.marc.everest.formatters.interfaces.IFormatterParseResult;
import org.marc.everest.interfaces.IResultDetail;
import org.marc.everest.interfaces.ResultDetailType;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Component3;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.marc.everest.rmim.uv.cdar2.rim.InfrastructureRoot;
import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.api.CdaImportService;
import org.openmrs.module.shr.cdahandler.everest.EverestUtil;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.DocumentValidationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

/**
 * The main controller.
 */
@Controller
//@SessionAttributes("document")
public class CdaImportController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@RequestMapping(value = "/module/shr-cdahandler/import", method = RequestMethod.GET)
	public void importGET(ModelMap model) {
		if (model.get("document") == null)
			model.put("document", new Document(null));
	}
	
	@RequestMapping(value = "/module/shr-cdahandler/import", method = RequestMethod.POST)
	public ModelAndView importPOST(HttpServletRequest request, HttpServletResponse response,
	        @RequestParam(value = "importFile") MultipartFile file, @RequestParam("action") String action, @RequestParam("section") String[] section, @RequestParam("tempFile") String tempFile) throws Throwable
	         {
		
		Map<String, Object> model = new HashMap<String, Object>();
		Document document = new Document(tempFile);
		
		// Constitute the model
		if (action.equals("Upload") && (file == null || file.isEmpty()))
			return new ModelAndView("redirect:import.form");
		
		if(action.equals("Upload"))
		{
			log.info("User uploaded document " + file.getOriginalFilename());
			document.submitCda(file.getInputStream());
			//document.transformCDAtoHTML();
			model.put("document", document);
		}
		else if(action.equals("Import")) // Do the import
		{
			try {
				document.pruneCda(section);
	            Visit e = Context.getService(CdaImportService.class).importDocument(document.getInputStream());
				log.info("Successfully imported document. Generated visit with id ");
				document.transformCDAtoHTML();
				model.put("document", document);
			}
			catch(DocumentValidationException e)
			{
				// HACK:
				log.error("Error generated", e);
				
				for(IResultDetail dtl : e.getValidationIssues())
					if(dtl.getType() == ResultDetailType.ERROR)
						log.error(dtl.getMessage());
					else
						log.warn(dtl.getMessage());
				throw e;
				
			}

		}
		
		return new ModelAndView("/module/shr-cdahandler/import", model);
	}
	
	public static class Document {
		
		protected final Log log = LogFactory.getLog(getClass());
		private String html;
		private String tempFile;
		private String title;
		private List<Section> sections = new ArrayList<Section>();
		
		/**
		 * Constructs a new document object
		 * @param tempFile
		 */
		public Document(String tempFile)
		{
			this.tempFile = tempFile;
		}
		
		/**
		 * Return temporary file
		 */
		public String getTempFile()
		{
			return this.tempFile;
		}
		
		/**
		 * Get title
		 */
		public String getTitle() {
			return this.title;
		}
		
		/**
		 * Get sections
		 */
		public List<Section> getSections() { 
			return this.sections;
		}
		
		/**
		 * Get input stream
		 * @throws FileNotFoundException 
		 */
		public InputStream getInputStream() throws FileNotFoundException {
			return new FileInputStream(new File(this.tempFile));
        }

		/**
		 * Transform the CDA to HTML
		 */
		private void transformCDAtoHTML() throws TransformerException {
			TransformerFactory factory = TransformerFactory.newInstance();
			Source xslt = new StreamSource(getClass().getClassLoader().getResourceAsStream("cda.xsl"));
			Transformer transformer = factory.newTransformer(xslt);
			
			FileInputStream in = null;
			try
			{
				in = new FileInputStream(new File(this.tempFile));
				Source text = new StreamSource(in);
				StringWriter sw = new StringWriter();
				transformer.transform(text, new StreamResult(sw));
				html = sw.toString();
				applyFormatting();
				System.out.println(html);
			}
            catch (FileNotFoundException e) {
	            // TODO Auto-generated catch block
	            log.error("Error generated", e);
            }
			finally
			{
				if(in != null)
	                try {
	                    in.close();
                    }
                    catch (IOException e) {
	                    // TODO Auto-generated catch block
	                    log.error("Error generated", e);
                    }
			}

		}
		
		/**
		 * Prune the CDA of all sections except those in the list
		 */
		public void pruneCda(String[] sectionIds)
		{
			
            try {
            	InputStream is = this.getInputStream();
            	ClinicalDocument document = null;
				try
				{
	    			IFormatterParseResult parseResult = EverestUtil.createFormatter().parse(is);
	    			document = (ClinicalDocument)parseResult.getStructure();
	    			if(document == null)
	    			{
	    				for(IResultDetail dtl : parseResult.getDetails())
	    					log.error(String.format("%s : %s @ %s", dtl.getType(), dtl.getMessage(), dtl.getLocation()));
	    				throw new RuntimeException();
	    			}
	    			this.title = document.getTitle().getValue();
	    			List<Component3> garbagePail = new ArrayList<Component3>();
	    			
	    			if(document.getComponent().getBodyChoiceIfStructuredBody() != null)
	    				for(Component3 comp : document.getComponent().getBodyChoiceIfStructuredBody().getComponent())
	    				{
	    					// Remove any non-essential stuffs
	    					if(!Arrays.asList(sectionIds).contains(comp.getSection().getCode().getCode()))
	    					{
	    						log.warn(String.format("Prune %s", comp.getSection().getTitle()));
	    						garbagePail.add(comp);
	    					}
	    				}

	    			for(Component3 comp : garbagePail)
	    				document.getComponent().getBodyChoiceIfStructuredBody().getComponent().remove(comp);

				}
				finally
				{
					if(is != null)
						is.close();
				}

				// Output the document
				FileOutputStream fos = null;
				try
				{
					fos = new FileOutputStream(new File(this.tempFile));
					EverestUtil.createFormatter().graph(fos, document);
				}
				finally
				{
					if(fos != null)
						fos.close();
				}
				
            }
            catch (IOException e) {
	            // TODO Auto-generated catch block
	            log.error("Error generated", e);
            }
		}
		
		/** 
		 * Writes file to temp input stream 
		 */
		public void submitCda(InputStream inputStream) {
			
            try {
    			File temp = File.createTempFile("cda-submit", ".xml");
    			this.tempFile = temp.getAbsolutePath();
    			log.warn(String.format("Created temp file %s", this.tempFile));
    			FileOutputStream fos = null;
    			
    			try
    			{
    				fos = new FileOutputStream(temp);
    			
	    			// Copy steam<>stream
	    			int bufRead = 1024;
	    			byte[] buffer = new byte[1024];
	    			while(bufRead > 0)
	    			{
	    				bufRead = inputStream.read(buffer, 0, 1024);
	    				if(bufRead > 0)
	    					fos.write(buffer, 0, bufRead);
	    			}

    			}
    			finally
    			{
    				if(fos != null)
    					fos.close();
    			}
    			
    			InputStream is = this.getInputStream();
    			try
    			{
	    			IFormatterParseResult parseResult = EverestUtil.createFormatter().parse(is);
	    			ClinicalDocument document = (ClinicalDocument)parseResult.getStructure();
	    			if(document == null)
	    			{
	    				for(IResultDetail dtl : parseResult.getDetails())
	    					log.error(String.format("%s : %s @ %s", dtl.getType(), dtl.getMessage(), dtl.getLocation()));
	    				throw new RuntimeException();
	    			}

	    			this.title = document.getTitle().getValue();
	    			log.warn(title);
	    			if(document.getComponent().getBodyChoiceIfStructuredBody() != null)
	    				for(Component3 comp : document.getComponent().getBodyChoiceIfStructuredBody().getComponent())
	    				{
	    					this.sections.add(comp.getSection());
	    				}
    			}
    			finally
    			{
    				if(is != null)
    					is.close();
    			}
            }
            catch (IOException e) {
	            // TODO Auto-generated catch block
	            log.error("Error generated", e);
            }
            
		}

		private void applyFormatting() {
			html = html.substring(html.indexOf("<body>") + "<body>".length());
			html = html.substring(0, html.indexOf("</body>"));
		}
		
		public String getHtml() {
			return html;
		}
	}
}
