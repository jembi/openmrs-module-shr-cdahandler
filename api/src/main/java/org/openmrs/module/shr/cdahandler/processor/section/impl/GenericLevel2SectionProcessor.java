package org.openmrs.module.shr.cdahandler.processor.section.impl;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;
import org.openmrs.module.shr.cdahandler.processor.context.DocumentProcessorContext;
import org.openmrs.module.shr.cdahandler.processor.context.ProcessorContext;
import org.openmrs.module.shr.cdahandler.processor.util.DatatypeProcessorUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsConceptUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsMetadataUtil;
import org.openmrs.notification.Note;
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
	public Encounter process(Section section) throws DocumentParseException {
		
		ProcessorContext context = this.parseSectionElements(section);
		
		Encounter encounterInfo = (Encounter) context.getParsedObject();
		
		// Process the encounter information
		encounterInfo = Context.getEncounterService().saveEncounter(encounterInfo);
		return encounterInfo;
	}
	
	/**
	 * Parses the section elements into a PRocessorContext
	 * 
	 * @param section The section from which section elements should be parsed
	 * @return The constructed processor context
	 */
	protected ProcessorContext parseSectionElements(Section section) throws DocumentParseException {
		if (!this.validate(section))
			throw new DocumentParseException("Cannot process an invalid document");
		
		Encounter encounterInfo = new Encounter();
		
		// Construct the current context
		ProcessorContext currentContext = new ProcessorContext(section, encounterInfo, this);
		
		// Get the datatype processor util
		DatatypeProcessorUtil datatypeProcessorUtil = DatatypeProcessorUtil.getInstance();
		OpenmrsMetadataUtil openmrsMetadataUtil = OpenmrsMetadataUtil.getInstance();
		OpenmrsConceptUtil openmrsConceptUtil = OpenmrsConceptUtil.getInstance();
		
		// Process encounter type
		if (section.getCode() != null && !section.getCode().isNull())
			encounterInfo.setEncounterType(openmrsMetadataUtil.getEncounterType(section.getCode()));
		
		// TODO: Context conduction rules, do we care for CDA?
		// Current implementation does not really care
		DocumentProcessorContext rootContext = (DocumentProcessorContext) currentContext.getRootContext();
		for (Entry<EncounterRole, Set<Provider>> entry : rootContext.getProviders().entrySet())
			for (Provider pvdr : entry.getValue())
				encounterInfo.addProvider(entry.getKey(), pvdr);
		
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
		
		// TODO: Add a note for text .. This is currently an obs because   
		// This could be an obs attached to the encounter (would make more sense)
		if (section.getText() != null && section.getText().getContent().size() > 0) {
			
			Obs noteObs = new Obs();
			
			noteObs.setConcept(openmrsConceptUtil.getRMIMConcept(String.format("%s %s",
				encounterInfo.getEncounterType().getName(),
				openmrsMetadataUtil.getInternationalizedString("obs.section.text"))));
			noteObs.setAccessionNumber(datatypeProcessorUtil.formatIdentifier(section.getId()));
			noteObs.setDateCreated(encounterInfo.getDateCreated());
			noteObs.setObsDatetime(rootContext.getParsedVisit().getStartDatetime());
			noteObs.setLocation(rootContext.getParsedVisit().getLocation());
			noteObs.setVoided(false);
			noteObs.setPerson(rootContext.getParsedVisit().getPatient());
			noteObs.setEncounter(encounterInfo);
			noteObs.setObsDatetime(encounterInfo.getDateCreated());
			
			String sectionText = section.getText().toString();
			ByteArrayInputStream textStream = new ByteArrayInputStream(sectionText.getBytes());
			ComplexData complexData = new ComplexData("summary.htm", textStream);
			log.debug(String.format("Setting text to %s", sectionText));
			noteObs.setComplexData(complexData);

//			Context.getObsService().saveObs(noteObs, null);
		
			encounterInfo.addObs(noteObs);

		}
		
		return currentContext;
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
	
}
