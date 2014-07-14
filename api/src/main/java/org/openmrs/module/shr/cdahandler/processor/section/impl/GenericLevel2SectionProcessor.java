package org.openmrs.module.shr.cdahandler.processor.section.impl;

import java.io.ByteArrayInputStream;
import java.util.Map.Entry;
import java.util.Set;

import org.marc.everest.datatypes.SD;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Obs;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;
import org.openmrs.module.shr.cdahandler.processor.context.DocumentProcessorContext;
import org.openmrs.module.shr.cdahandler.processor.context.ProcessorContext;
import org.openmrs.module.shr.cdahandler.processor.util.DatatypeProcessorUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsConceptUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsMetadataUtil;
import org.openmrs.obs.ComplexData;

/**
 * Represents a generic level 2 section processor implementation
 * 
 * @author Justin Fyfe
 */
public class GenericLevel2SectionProcessor extends SectionProcessorImpl {
	
	/**
	 * Process the section
	 */
	@Override
	public BaseOpenmrsData process(Section section) throws DocumentParseException {

		// Validate the section
		if (!this.validate(section))
			throw new DocumentParseException("Cannot process an invalid section!");

		BaseOpenmrsData res = null;
		
		// This is a root section? If yes we save as an encounter
		if(this.getContext().getRawObject() instanceof ClinicalDocument)
		{
			Encounter encounterInfo = this.parseSectionElementsAsEncounter(section);
			res = Context.getEncounterService().saveEncounter(encounterInfo);
		}
		else // This is a subsection .. Hmm... We need to save this as a 
		{
			Obs sectionObs = this.parseSectionElementsAsObs(section);
			res = Context.getObsService().saveObs(sectionObs, null);
		}
		return res;
	}
	
	/**
	 * Parse section elements as an obs group 
	 * Auto generated method comment
	 * 
	 * @param section The section whose elements should be parsed
	 * @return The parsed obs
	 */
	protected Obs parseSectionElementsAsObs(Section section) throws DocumentParseException {

		Obs obsGrouper = new Obs();
		
		// Get the processor utils
		DatatypeProcessorUtil datatypeProcessorUtil = DatatypeProcessorUtil.getInstance();
		OpenmrsConceptUtil openmrsConceptUtil = OpenmrsConceptUtil.getInstance();
		
		// TODO: Context conduction rules, do we care for CDA?
		// Get the context that has the counter
		ProcessorContext encounterContext = this.getContext();
		while(encounterContext.getParent() != null && !(encounterContext.getParsedObject() instanceof Encounter))
				encounterContext = encounterContext.getParent();
		Encounter encounterInfo = (Encounter)encounterContext.getParsedObject();

		// Process encounter type
		if (section.getCode() != null && !section.getCode().isNull())
		{
			Concept concept = openmrsConceptUtil.getConceptOrEquivalent(section.getCode());
			if(concept == null)
				concept = openmrsConceptUtil.createConcept(section.getCode(), section.getText());
			obsGrouper.setConcept(concept);
		}
		
		// Patient from visit
		obsGrouper.setPerson(encounterInfo.getPatient());

		// Add visit
		obsGrouper.setEncounter(encounterInfo);
		obsGrouper.setLocation(encounterInfo.getLocation());
		
		// Created on...
		obsGrouper.setDateCreated(encounterInfo.getDateCreated());
		
		// If we have a visit start time this would represent the encounter time as well
		if (encounterInfo.getEncounterDatetime() != null)
			obsGrouper.setObsDatetime(encounterInfo.getEncounterDatetime());
		
		// TODO: ID of section
		obsGrouper.setAccessionNumber(datatypeProcessorUtil.formatIdentifier(section.getId()));
		
		// TODO: Add a note for text .. This is currently an obs because notes are to the patient not the encounter   
		// This could be an obs attached to the encounter (would make more sense)
		if (section.getText() != null && section.getText().getContent().size() > 0) {
			String title = section.getTitle() + ".xml";
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
	 * Parses the section elements into an encounter (this is done when we're at the root (visit) stage)
	 * 
	 * @param section The section from which section elements should be parsed
	 * @return The constructed processor context
	 */
	protected Encounter parseSectionElementsAsEncounter(Section section) throws DocumentParseException {
		
		Encounter encounterInfo = new Encounter();
		
	
		// Get the processor utils
		OpenmrsMetadataUtil openmrsMetadataUtil = OpenmrsMetadataUtil.getInstance();
		DatatypeProcessorUtil datatypeProcessorUtil = DatatypeProcessorUtil.getInstance();

		// Process encounter type
		if (section.getCode() != null && !section.getCode().isNull())
			encounterInfo.setEncounterType(openmrsMetadataUtil.getOrCreateEncounterType(section.getCode()));
		
		// TODO: Context conduction rules, do we care for CDA?
		// Current implementation does not really care
		DocumentProcessorContext rootContext = (DocumentProcessorContext)this.getContext().getRootContext();
		
		// If there are no observations we must do this as level2 authors are indistinguishable from one another
		// however level3 is based on the author relationship in their entry
		this.addAuthors(section, encounterInfo);
		
		// Patient from visit
		encounterInfo.setPatient(rootContext.getParsedVisit().getPatient());
		// Add visit
		encounterInfo.setVisit(rootContext.getParsedVisit());
		encounterInfo.setLocation(rootContext.getParsedVisit().getLocation());
		
		// Created on...
		encounterInfo.setDateCreated(rootContext.getParsedVisit().getDateCreated());
		encounterInfo.setDateChanged(rootContext.getParsedVisit().getDateCreated());
		
		// If we have a visit start time this would represent the encounter time as well
		if (rootContext.getParsedVisit().getStartDatetime() != null)
			encounterInfo.setEncounterDatetime(rootContext.getParsedVisit().getStartDatetime());
		
		// TODO: ID of section
		
		// TODO: Add a note for text .. This is currently an obs because notes are to the patient not the encounter   
		// This could be an obs attached to the encounter (would make more sense)
		if (section.getText() != null && section.getText().getContent().size() > 0) {
			
			Obs noteObs = this.parseSectionText(section.getText(), encounterInfo);
			noteObs.setAccessionNumber(datatypeProcessorUtil.formatIdentifier(section.getId()));
			encounterInfo.addObs(noteObs);
			
		}
		
		return encounterInfo;
	}
	
	/**
	 * Add applicable authors to this document
	 * @throws DocumentParseException 
	 */
	protected void addAuthors(Section section, Encounter encounterInfo) throws DocumentParseException {

		DocumentProcessorContext rootContext = (DocumentProcessorContext) this.getContext().getRootContext();
		for (Entry<EncounterRole, Set<Provider>> entry : rootContext.getProviders().entrySet())
			for (Provider pvdr : entry.getValue())
				encounterInfo.addProvider(entry.getKey(), pvdr);
	    
    }

	/**
	 * Parse section text into an observation
	 * @throws DocumentParseException 
	 */
	protected Obs parseSectionText(SD text, Encounter encounterInfo) throws DocumentParseException {

		OpenmrsMetadataUtil openmrsMetadataUtil = OpenmrsMetadataUtil.getInstance();
		OpenmrsConceptUtil openmrsConceptUtil = OpenmrsConceptUtil.getInstance();

		Obs noteObs = new Obs();
		
		noteObs.setConcept(openmrsConceptUtil.getOrCreateRMIMConcept(
			openmrsMetadataUtil.getLocalizedString("obs.section.text"), text));
		noteObs.setDateCreated(encounterInfo.getDateCreated());
		noteObs.setLocation(encounterInfo.getLocation());
		noteObs.setVoided(false);
		noteObs.setPerson(encounterInfo.getPatient());
		noteObs.setEncounter(encounterInfo);
		noteObs.setObsDatetime(encounterInfo.getEncounterDatetime());
		
		// HACK: Remove xmlns as the StructDoc.Text nodes are really just HTML
		String sectionText = text.toString().replaceAll("xmlns\\=\".*\"", "");
		ByteArrayInputStream textStream = new ByteArrayInputStream(sectionText.getBytes());
		ComplexData complexData = new ComplexData("summary.htm", textStream);
		noteObs.setComplexData(complexData);

		return noteObs;
    }

	/**
	 * Validate this section processor can perform the necessary function
	 */
	@Override
	public Boolean validate(IGraphable object) {
		// TODO Auto-generated method stub
		Boolean isValid = super.validate(object);
		if (!isValid)
			return isValid;
		
		Section section = (Section) object;
		// CONF-HP-69 A section element shall have a code
		if (section.getCode() == null || section.getCode().isNull()) {
			log.warn(String
			        .format("CONF-HP-69 : Section element SHALL have a code element @ %s", section.getId().toString()));
			isValid = false;
		}
		else
		{
		
		    CE<String> expectedCode = this.getExpectedSectionCode(section);
	
			// Assert code
		    if(expectedCode != null &&
		    		(section.getCode() == null || 
		    		section.getCode().isNull() ||
		    		!section.getCode().semanticEquals(expectedCode).toBoolean()))
			{
				log.error(String.format("Template %s must carry code of %s in code system %s", this.getTemplateName(), expectedCode.getCode(), expectedCode.getCodeSystem()));
				isValid = false;
			}
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
		
		return isValid;
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
	 * Gets a list of Expected code (anything for generic)
	 */
	protected CE<String> getExpectedSectionCode(Section section) 
	{
		return null;
	}

}
