package org.openmrs.module.shr.cdahandler.processor.document.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Author;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Component3;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ContextControl;
import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;
import org.openmrs.module.shr.cdahandler.processor.context.DocumentProcessorContext;
import org.openmrs.module.shr.cdahandler.processor.factory.impl.SectionProcessorFactory;
import org.openmrs.module.shr.cdahandler.processor.section.SectionProcessor;

/**
 * This is a generic document processor which can interpret any CDA
 * document with a structured body content (i.e. Level 2)
 * @author Justin Fyfe
 *
 */
public class StructuredBodyDocumentProcessor extends DocumentProcessorImpl {

	/**
	 * Process the document data into a series of OpenMRS elements
	 * saving the result to the database
	 */
	@Override
	public Visit process(ClinicalDocument doc) throws DocumentParseException {
		DocumentProcessorContext parserContext = super.parseHeader(doc);
		Visit visitInformation = parserContext.getParsedVisit();

		visitInformation = Context.getVisitService().saveVisit(visitInformation);
		
		// Encounters
		Set<Encounter> encounters = new HashSet<Encounter>();
		
		// Iterate through sections saving them
		for(Component3 comp : doc.getComponent().getBodyChoiceIfStructuredBody().getComponent())
		{
			// empty section?
			if(comp == null || comp.getNullFlavor() != null ||
					comp.getSection() == null || comp.getSection().getNullFlavor() != null) 
				continue;
			
			Section section = comp.getSection();
			
			// TODO: Now process section
			SectionProcessorFactory factory = SectionProcessorFactory.getInstance();
			SectionProcessor processor = factory.createProcessor(section);
			processor.setContext(parserContext);
			
			Encounter sectionData = processor.process(section); 
			// Process
			if(sectionData != null)
				encounters.add(sectionData);
		}
		
		return visitInformation;
	}

	/**
	 * Implementation of validate for Processor
	 */
	@Override
	public Boolean validate(IGraphable object) {

		
		// Ensure that the document body is in fact structured
		Boolean isValid = super.validate(object);
		if(!isValid) return isValid;
		
		ClinicalDocument doc = (ClinicalDocument)object;
		
		// Must have component to be valid CDA
		if(doc.getComponent() == null || doc.getComponent().getNullFlavor() != null)
		{
			log.warn(String.format("Document %s is missing component", doc.getId().toString()));
			isValid = false;
		}
		// Must have BodyChoice of StructuredBody
		else if(doc.getComponent().getBodyChoice() == null || 
				doc.getComponent().getBodyChoice().isPOCD_MT000040UVNonXMLBody() || 
				doc.getComponent().getBodyChoice().getNullFlavor() != null)
		{
			log.warn(String.format("Document %s is missing body of structuredBody", doc.getId().toString()));
			isValid = false;
		}

		// Must have at least one section
		if(doc.getComponent().getBodyChoiceIfStructuredBody().getComponent().size() == 0 ||
				doc.getComponent().getBodyChoiceIfStructuredBody().getComponent().get(0).getNullFlavor() != null)
		{
			log.warn(String.format("Document %s must have at least one entry", doc.getId().toString()));
			isValid = false;
		}
		return isValid;
		
	}

	/**
	 * Get the template name .. Since this is a generic handler it has no template name
	 */
	@Override
	public String getTemplateName() {
		return null;
	}
	
	
}
