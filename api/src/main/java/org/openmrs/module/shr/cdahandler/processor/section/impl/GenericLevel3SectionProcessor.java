package org.openmrs.module.shr.cdahandler.processor.section.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.formatters.FormatterUtil;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Component5;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Entry;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.context.ProcessorContext;
import org.openmrs.module.shr.cdahandler.processor.entry.EntryProcessor;
import org.openmrs.module.shr.cdahandler.processor.factory.ProcessorFactory;
import org.openmrs.module.shr.cdahandler.processor.factory.impl.EntryProcessorFactory;
import org.openmrs.module.shr.cdahandler.processor.factory.impl.SectionProcessorFactory;
import org.openmrs.module.shr.cdahandler.processor.section.SectionProcessor;

/**
 * Represents a section processor that iterates through entries in the 
 * section. 
 * 
 * The reason this logic is not included in the GenericLevel2 processor 
 * is that we don't want to process entries without having some sort of 
 * knowledge of whether the section is complete. This is also why
 * the class is abstract. It should be extended by implementations 
 * of section processors for each level 3 section.
 * 
 */
public abstract class GenericLevel3SectionProcessor extends GenericLevel2SectionProcessor {

	
	/**
	 * Process the entries in this section
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#process(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section)
	 */
	@Override
    public Obs process(Section section) throws DocumentImportException {
	    
		// Validate the section done by super
		Obs level2Data = super.process(section); // Process the level 2 portions
		ProcessorContext parseContext = new ProcessorContext(section, level2Data, this);
		
		ProcessorFactory factory = EntryProcessorFactory.getInstance();

	    // Process entries
	    for(Entry ent : section.getEntry())
	    {
	    	// Check there is actually data in the entry
	    	if(ent == null || ent.getNullFlavor() != null ||
	    			ent.getClinicalStatement() == null || ent.getClinicalStatement().getNullFlavor() != null)
	    	{
	    		log.warn(String.format("Entry is missing clinical statement data in section %s", section.getId()));
	    		continue;
	    	}
	    	
	    	ClinicalStatement statement = ent.getClinicalStatement();
	    	
	    	// Get the processor
	    	EntryProcessor processor = (EntryProcessor)factory.createProcessor(statement);
	    	if(processor == null) // No processor found!
	    	{
	    		log.warn(String.format("No processor found for entry type %s", FormatterUtil.toWireFormat(ent.getClinicalStatement().getTemplateId())));
	    		continue;
	    	}
	    	else
    		{
	    		processor.setContext(parseContext);
	    		processor.process(statement);
    		}
	    	
	    }

	    // Switch the factory 
	    factory = SectionProcessorFactory.getInstance();
	    
	    // Process sub-sections ... These are interesting as they do represent encounters
	    // However section processor are expecting to create entries 
	    for(Component5 comp : section.getComponent())
	    {
	    	if(comp == null || comp.getNullFlavor() != null ||
	    			comp.getSection() == null || comp.getSection().getNullFlavor() != null)
	    	{
	    		log.warn(String.format("Component is missing section in section %s", section.getId()));
	    		continue;
	    	}
	    
			Section subSection = comp.getSection();
			
			// Now process section
			SectionProcessor processor = (SectionProcessor)factory.createProcessor(subSection);
			processor.setContext(parseContext);
			processor.process(subSection);
			
	    }
	    
	    // Save now
	    //if(level2Data instanceof Encounter)
	    //	level2Data = Context.getEncounterService().saveEncounter((Encounter)level2Data);
	    //else
	    //level2Data = Context.getObsService().saveObs(level2Data, "Processed entries");

	    return level2Data;
    }

	/**
	 * Validate this is in fact a level 3 section
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#validate(org.marc.everest.interfaces.IGraphable)
	 */
	@Override
    public ValidationIssueCollection validate(IGraphable object) {
		
	    ValidationIssueCollection validationIssues = super.validate(object);
	    if(validationIssues.hasErrors()) return validationIssues;
	    Section section = (Section)object;
	    
	    // Validate the section has components / entries 
	    // and/or sub-sections
	    List<String> expectedEntries = this.getExpectedEntries(section);

	    // Assert entry
		if(expectedEntries != null)
			// Must have vital sign organizer
			for(String entry : expectedEntries)
				if(section.getEntry().size() == 0 && !this.hasEntry(section, entry))
					validationIssues.error(String.format("Section %s expects entry of %s", this.getTemplateName(), entry));

		
	    return validationIssues;
    }

	/**
	 * Gets a list of expected entries
	 */
	protected abstract List<String> getExpectedEntries(Section section);

	/**
	 * Returns true if the section contains the specified template
	 */
	public boolean hasEntry(Section section, String string) {
		II templateId = new II(string);
		for(Entry ent : section.getEntry())
			if(ent.getClinicalStatement() == null || ent.getClinicalStatement().getNullFlavor() != null ||
			ent.getClinicalStatement().getTemplateId() == null)
				continue;
			else if(ent.getClinicalStatement().getTemplateId().contains(templateId))
				return true;
		return false;
				
    }

	
}
