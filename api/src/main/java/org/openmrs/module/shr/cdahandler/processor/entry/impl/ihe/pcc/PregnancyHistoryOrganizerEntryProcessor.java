package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import java.util.List;

import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Component4;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Organizer;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.entry.impl.OrganizerEntryProcessor;

/**
 * Processor for the Pregnancy History organizer
 * 
 * See: PCC TF-2: 6.3.4.26
 */
@ProcessTemplates(
	templateIds = {
			CdaHandlerConstants.ENT_TEMPLATE_PREGNANCY_HISTORY_ORGANIZER
	})
public class PregnancyHistoryOrganizerEntryProcessor extends OrganizerEntryProcessor {
	
	/**
	 * Get expected code(s)
	 */
	@Override
	public CE<String> getExpectedCode() {
		return new CE<String>("118185001", CdaHandlerConstants.CODE_SYSTEM_SNOMED, "SNOMED CT", null, "Pregnancy Finding", null);
	}
	
	
	/**
	 * Get expected sections .. 
	 */
	@Override
	protected List<String> getExpectedComponents() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Get expected entry relationships
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.EntryProcessorImpl#getExpectedEntryRelationships()
	 */
	@Override
    protected List<String> getExpectedEntryRelationships() {
	    // TODO Auto-generated method stub
	    return null;
    }


	/**
	 * Gets the template name
	 */
	@Override
	public String getTemplateName() {
		return "Pregnancy History Organizer";
	}


	/** 
	 * Validate the section
	 */
	@Override
    public ValidationIssueCollection validate(IGraphable object) {
		ValidationIssueCollection validationIssues = super.validate(object);
		if(validationIssues.hasErrors()) 
			return validationIssues;
		
		// This organizer must contain either a pregnancy observation or a birth organizer
		Organizer organizer = (Organizer)object;
		if(organizer.getComponent().size() == 0)
			validationIssues.error("The Pregnancy History Organizer shall contain either Birth Event Organizers or Pregnancy observations");
		else
			for(Component4 comp : organizer.getComponent())
			{
				if(comp.getClinicalStatement() == null || comp.getClinicalStatement().getNullFlavor() != null)
					validationIssues.error("Component within organizer is missing ClinicalStatement");
				else if(comp.getClinicalStatement().getTemplateId().contains(new II(CdaHandlerConstants.ENT_TEMPLATE_PREGNANCY_OBSERVATION)))
					;
				else if(comp.getClinicalStatement().getTemplateId().contains(new II(CdaHandlerConstants.ENT_TEMPLATE_BIRTH_EVENT_ORGANIZER)) &&
						comp.getSequenceNumber() == null || !comp.getSequenceNumber().isNull())
					validationIssues.error("When present a BirthEventOrganizer shall have a sequence number or shall be represented using a nullFlavor");
				else
					validationIssues.error("A pregnancy history organizer shall only contain either a Pregnancy Observation or a Birth Event Organizer entry");
			}
		return validationIssues;
    }

	
}
