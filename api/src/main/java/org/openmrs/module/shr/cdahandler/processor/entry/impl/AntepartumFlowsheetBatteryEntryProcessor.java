package org.openmrs.module.shr.cdahandler.processor.entry.impl;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Organizer;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActClassDocumentEntryOrganizer;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;

/**
 * Antepartum Flowsheet Battery Entry Processor
 */
@ProcessTemplates(templateIds = { CdaHandlerConstants.ENT_TEMPLATE_ANTEPARTUM_FLOWSHEET_PANEL })
public class AntepartumFlowsheetBatteryEntryProcessor extends OrganizerEntryProcessor {

	/**
	 * Get the template name
	 */
	@Override
	public String getTemplateName() {
		return "Antepartum Flowsheet Battery";
	}
	
	/**
	 * Get the expected code
	 */
	@Override
	public CE<String> getExpectedCode() {
		return new CE<String>("57061-4", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "ACOG VISIT SUMMARY BATTERY", null);
	}
	
	/**
	 * Get expected components
	 */
	@Override
	protected List<String> getExpectedComponents() {
		return Arrays.asList(CdaHandlerConstants.ENT_TEMPLATE_SIMPLE_OBSERVATION);
	}
	
	/**
	 * Get expected relationships to other items
	 * @return
	 */
	@Override
	protected List<String> getExpectedEntryRelationships() {
		return null;
	}

	/**
	 * Additional validation rules
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.OrganizerEntryProcessor#validate(org.marc.everest.interfaces.IGraphable)
	 */
	@Override
    public ValidationIssueCollection validate(IGraphable object) {
		ValidationIssueCollection validationIssues = super.validate(object);
		if(validationIssues.hasErrors()) return validationIssues;
		
		Organizer statement = (Organizer)object;
		if(!statement.getClassCode().getCode().equals(x_ActClassDocumentEntryOrganizer.BATTERY))
			validationIssues.error("Antepartum Flowsheet Panel class code must be BATTERY");
		
		return validationIssues;
    }
	

}
