package org.openmrs.module.shr.cdahandler.processor.document.impl;

import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;

/**
 * This is a generic document processor which can interpret any CDA
 * document with a structured body content (i.e. Level 2)
 * @author Justin Fyfe
 *
 */
public class GenericDocumentProcessor extends DocumentProcessorImpl {


	/**
	 * Implementation of validate for Processor
	 */
	@Override
	public ValidationIssueCollection validate(IGraphable object) {

		
		// Ensure that the document body is in fact structured
		ValidationIssueCollection validationErrors = super.validate(object);
		if(validationErrors.hasErrors()) return validationErrors;
		
		ClinicalDocument doc = (ClinicalDocument)object;
		
		// Must have component to be valid CDA
		if(doc.getComponent() == null || doc.getComponent().getNullFlavor() != null)
			validationErrors.error(String.format("Document %s is missing component", doc.getId().toString()));
		// Must have BodyChoice of StructuredBody
		else if(doc.getComponent().getBodyChoice() == null)
			validationErrors.error(String.format("Document %s is missing body", doc.getId().toString()));

		return validationErrors;
		
	}

	/**
	 * Get the template name .. Since this is a generic handler it has no template name
	 */
	@Override
	public String getTemplateName() {
		return null;
	}
	
	
}
