package org.openmrs.module.shr.cdahandler.processor.section.impl;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.configuration.CdaHandlerConfiguration;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.DocumentValidationException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.context.ProcessorContext;
import org.openmrs.module.shr.cdahandler.processor.util.DatatypeProcessorUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsConceptUtil;
import org.openmrs.obs.ComplexData;

/**
 * Represents a generic level 2 section processor implementation
 * 
 * @author Justin Fyfe
 */
public class GenericLevel2SectionProcessor extends SectionProcessorImpl {

	// Get the processor utils
	protected final DatatypeProcessorUtil m_datatypeProcessorUtil = DatatypeProcessorUtil.getInstance();
	protected final OpenmrsConceptUtil m_openmrsConceptUtil = OpenmrsConceptUtil.getInstance();
	protected final CdaHandlerConfiguration m_configuration = CdaHandlerConfiguration.getInstance();
	
	/**
	 * Gets a list of Expected code (anything for generic)
	 */
	public CE<String> getExpectedSectionCode() 
	{
		return null;
	}
	
	/**
	 * Gets the name of this template 
	 * @see org.openmrs.module.shr.cdahandler.processor.Processor#getTemplateName()
	 */
	@Override
    public String getTemplateName() {
	    return null;
    }

	/**
	 * Parse section elements as an obs group 
	 * Auto generated method comment
	 * 
	 * @param section The section whose elements should be parsed
	 * @return The parsed obs
	 */
	protected Obs parseSectionElements(Section section) throws DocumentImportException {

		Obs obsGrouper = new Obs();
		
		// Is a sub-section? If so assign the obs to the group above
		if(this.getContext().getParsedObject() instanceof Obs)
			obsGrouper.setObsGroup((Obs)this.getContext().getParsedObject());
		
		// TODO: Context conduction rules, do we care for CDA?
		// Get the context that has the counter
		ProcessorContext encounterContext = this.getContext();
		while(encounterContext.getParent() != null && !(encounterContext.getParsedObject() instanceof Encounter))
				encounterContext = encounterContext.getParent();
		Encounter encounterInfo = (Encounter)encounterContext.getParsedObject();

		// Process encounter type
		if (section.getCode() != null && !section.getCode().isNull())
		{
			Concept concept = this.m_openmrsConceptUtil.getConcept(section.getCode(), section.getText());
			if(concept == null)
				concept = this.m_openmrsConceptUtil.createConcept(section.getCode(), section.getText());
			obsGrouper.setConcept(concept);
		}
		// Patient from visit
		obsGrouper.setPerson(encounterInfo.getPatient());

		// Add visit
		obsGrouper.setEncounter(encounterInfo);
		obsGrouper.setLocation(encounterInfo.getLocation());
		
		// Created on...
		obsGrouper.setDateCreated(encounterInfo.getDateCreated());
		obsGrouper.setCreator(encounterInfo.getCreator());
		// If we have a visit start time this would represent the encounter time as well
		if (encounterInfo.getEncounterDatetime() != null)
			obsGrouper.setObsDatetime(encounterInfo.getEncounterDatetime());
		
		// TODO: ID of section
		obsGrouper.setAccessionNumber(this.m_datatypeProcessorUtil.formatIdentifier(section.getId()));
		
		// TODO: Add a note for text .. This is currently an obs because notes are to the patient not the encounter   
		// This could be an obs attached to the encounter (would make more sense)
		if (section.getText() != null && section.getText().getContent().size() > 0) {
			String title = UUID.randomUUID().toString() + ".xml";
			log.debug(String.format("Saving as %s", title));
			String sectionText = section.getText().toString();
			ByteArrayInputStream textStream = new ByteArrayInputStream(sectionText.getBytes());
			ComplexData complexData = new ComplexData(title, textStream);
			obsGrouper.setComplexData(complexData);
			
		}
		
		// Add the obs
		
		return obsGrouper;

	}

	/**
	 * Process the section
	 */
	@Override
	public Obs process(Section section) throws DocumentImportException {

		// Validate
		if(this.m_configuration.getValidationEnabled())
		{
			ValidationIssueCollection issues = this.validate(section);
			if(issues.hasErrors())
				throw new DocumentValidationException(section, issues);
		}

		Obs res = this.parseSectionElements(section);
		res = Context.getObsService().saveObs(res, null);

		return res;
	}
	
	/**
	 * Validate this section processor can perform the necessary function
	 */
	@Override
	public ValidationIssueCollection validate(IGraphable object) {
		// TODO Auto-generated method stub
		ValidationIssueCollection validationIssues = super.validate(object);
		if (validationIssues.hasErrors())
			return validationIssues;
		
		Section section = (Section) object;
		// CONF-HP-69 A section element shall have a code
		if (section.getCode() == null || section.getCode().isNull()) 
			validationIssues.warn(String
			        .format("CONF-HP-69 : Section element SHALL have a code element @ %s", section.getId().toString()));
		else
		{
		
		    CE<String> expectedCode = this.getExpectedSectionCode();
	
			// Assert code
		    if(expectedCode != null &&
		    		(section.getCode() == null || 
		    		section.getCode().isNull() ||
		    		!section.getCode().semanticEquals(expectedCode).toBoolean()))
		    	validationIssues.error(String.format("Template %s must carry code of %s in code system %s", this.getTemplateName(), expectedCode.getCode(), expectedCode.getCodeSystem()));
			else if(expectedCode != null && section.getCode().getDisplayName() == null)
				section.getCode().setDisplayName(expectedCode.getDisplayName());
	    	
		}
		
		// CONF-HP-70
		/*if ((section.getText() == null || section.getText().isNull()) && (section.getComponent().size() == 0)) {
			log.warn(String
			        .format(
			            "CONF-HP-70 : Section element SHALL contain at least one text element or one or more component sections @ %s",
			            section.getId().toString()));
			isValid = false;
		} else if (section.getText() != null && section.getComponent().size() == 0
		        && section.getText().getContent().size() == 0) {
			log.warn(String.format("CONF-HP-71 : All text or component elements SHALL contain content"));
			isValid = false;
		}*/
		
		return validationIssues;
	}

}
