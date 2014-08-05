package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.PN;
import org.marc.everest.datatypes.generic.CD;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Component4;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Organizer;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.RelatedSubject;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.DocumentValidationException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.entry.EntryProcessor;
import org.openmrs.module.shr.cdahandler.processor.entry.impl.OrganizerEntryProcessor;
import org.openmrs.module.shr.cdahandler.processor.factory.impl.EntryProcessorFactory;

/**
 * Processor for the Family History Organizer 
 * 
 *  See: PCC TF-2: 6.3.4.23
 */
@ProcessTemplates(
	templateIds = { CdaHandlerConstants.ENT_TEMPLATE_FAMILY_HISTORY_ORGANIZER }
	)
public class FamilyHistoryOrganizerEntryProcessor extends OrganizerEntryProcessor {
	
	/**
	 * Get the expected code, this is null as the code comes from a code system rather than a code
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.OrganizerEntryProcessor#getExpectedCode()
	 */
	@Override
	public CE<String> getExpectedCode() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Get the expected components
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.OrganizerEntryProcessor#getExpectedComponents()
	 */
	@Override
	protected List<String> getExpectedComponents() {
		return Arrays.asList(CdaHandlerConstants.ENT_TEMPLATE_FAMILY_HISTORY_OBSERVATION);
	}

	/**
	 * Get expected entry relationships
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.EntryProcessorImpl#getExpectedEntryRelationships()
	 */
	@Override
	protected List<String> getExpectedEntryRelationships() {
		return null;
	}
	
	/**
	 * Get the name of the template
	 * @see org.openmrs.module.shr.cdahandler.processor.Processor#getTemplateName()
	 */
	@Override
	public String getTemplateName() {
		return "Family History Organizer";
	}

	/**
	 * Process this organizer
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.OrganizerEntryProcessor#process(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement)
	 */
	@Override
    public BaseOpenmrsData process(ClinicalStatement entry) throws DocumentImportException {
		// First, we must assign a code to this organizer or else the underlying grouping will not work
		ValidationIssueCollection validationIssues = this.validate(entry);
		if(validationIssues.hasErrors())
			throw new DocumentValidationException(entry, validationIssues);
		
				
		// This organizer is a little different. The organizer maps to CIEL 160593
		Organizer organizer = (Organizer)entry;
		// Set organizer type to "Patient's Family History List"
		organizer.setCode(new CD<String>("160593", CdaHandlerConstants.CODE_SYSTEM_CIEL));
		Obs organizerObs = super.parseOrganizer(organizer);
		organizerObs.setValueText(null);
		
		// Process participant data
		RelatedSubject subject = organizer.getSubject().getRelatedSubject();

		// Family member relationship 
		Obs familyMemberRelationObs = new Obs(organizerObs.getPerson(), 
			Context.getConceptService().getConcept(1560), 
			organizerObs.getObsDatetime(), 
			organizerObs.getLocation());
		familyMemberRelationObs.setValueCoded(this.m_conceptUtil.getConceptOrEquivalent(subject.getCode()));
		organizerObs.addGroupMember(familyMemberRelationObs);
		
		// Id
		Obs familyMemberId = new Obs(organizerObs.getPerson(), 
			Context.getConceptService().getConcept(160752),
			organizerObs.getObsDatetime(),
			organizerObs.getLocation());
		familyMemberId.setValueText("TODO: See how to get extended elements");
		organizerObs.addGroupMember(familyMemberId);
		
		// Name
		if(subject.getSubject().getName() != null)
		{
			for(PN name : subject.getSubject().getName())
			{
				Obs familyMemberName = new Obs(organizerObs.getPerson(), 
					Context.getConceptService().getConcept(160750),
					organizerObs.getObsDatetime(),
					organizerObs.getLocation());
				familyMemberId.setValueText(name.toString());
				organizerObs.addGroupMember(familyMemberName);
			}
		}
				
		// Process components
		EntryProcessorFactory factory = EntryProcessorFactory.getInstance();
		for(Component4 comp : organizer.getComponent())
		{
			if(comp.getClinicalStatement() == null || comp.getClinicalStatement().getNullFlavor() != null)
				continue;
			
			// Process
			EntryProcessor processor = factory.createProcessor(comp.getClinicalStatement());
			BaseOpenmrsData data = processor.process(comp.getClinicalStatementIfObservation());
			if(data instanceof Obs)
				organizerObs.addGroupMember((Obs)data);
			
		}
		organizerObs = Context.getObsService().saveObs(organizerObs, null);
		return organizerObs;
    }

	
	/**
	 * Validate comes from HL7 FamilyMember 
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.OrganizerEntryProcessor#validate(org.marc.everest.interfaces.IGraphable)
	 */
	@Override
    public ValidationIssueCollection validate(IGraphable object) {
	    ValidationIssueCollection validationIssues = super.validate(object);
	    if(validationIssues.hasErrors()) return validationIssues;
	    
	    // Family history organizer must contain the family member that the relationship belongs to
	    Organizer organizer = (Organizer)object;
	    if(organizer.getSubject() == null || organizer.getSubject().getNullFlavor() != null)
	    	validationIssues.error("Family history organizer must carry a subject");
	    else if(organizer.getSubject().getRelatedSubject() == null)
	    	validationIssues.error("Family history organizer subject must carry a relatedSubject element");
	    else 
    	{
		    if(organizer.getSubject().getRelatedSubject().getCode() == null || organizer.getSubject().getRelatedSubject().getCode().isNull() || !organizer.getCode().getCodeSystem().equals(CdaHandlerConstants.CODE_SYSTEM_FAMILY_MEMBER))
		    	validationIssues.error("Family history organizer's relatedSubject must carry a code drawn from the HL7 Family Member domain");
	    	if(organizer.getSubject().getRelatedSubject().getSubject() == null)
	    		validationIssues.error("Family history organizer's relatedSubject must carry a subject");
	    	else
	    	{
	    		// TODO: Figure out extended namespaces in jEverest
	    		if(organizer.getSubject().getRelatedSubject().getSubject().getAdministrativeGenderCode() == null ||
	    				organizer.getSubject().getRelatedSubject().getSubject().getAdministrativeGenderCode().isNull())
	    			validationIssues.error("Family history organizer's relatedSubject.subject must carry an administrative gender code");
	    	}
    	}
	    
	    return validationIssues;
	    
    }

	
}
