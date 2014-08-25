package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.BL;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.PN;
import org.marc.everest.datatypes.PQ;
import org.marc.everest.datatypes.doc.StructDocNode;
import org.marc.everest.datatypes.generic.CD;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Component4;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Observation;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Organizer;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.RelatedSubject;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.everest.sdtc.SdtcSubjectPerson;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.DocumentValidationException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.entry.EntryProcessor;
import org.openmrs.module.shr.cdahandler.processor.entry.impl.OrganizerEntryProcessor;
import org.openmrs.module.shr.cdahandler.processor.factory.impl.EntryProcessorFactory;
import org.openmrs.util.OpenmrsConstants;

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
		else if(!entry.isPOCD_MT000040UVOrganizer())
			throw new DocumentImportException("Expected family history to be an Organizer");
		
		Encounter encounterInfo = (Encounter)this.getEncounterContext().getParsedObject();
		Organizer organizer = (Organizer)entry;
		organizer.setCode(new CD<String>("160593", CdaHandlerConstants.CODE_SYSTEM_CIEL));
		
		// This organizer is a little different. The organizer maps to CIEL 160593
		for(Component4 component : organizer.getComponent())
		{
			Observation componentObservation = component.getClinicalStatementIfObservation();
			// Don't process
			if(componentObservation == null || !this.m_datatypeUtil.hasTemplateId(componentObservation, new II(CdaHandlerConstants.ENT_TEMPLATE_FAMILY_HISTORY_OBSERVATION)))
			{
				log.warn("Cannot process family history component");
				continue;
			}
			
			// Cascade anything we can
			this.m_datatypeUtil.cascade(organizer, componentObservation, "effectiveTime","author", "reference");

			// Previous obs
			Obs parentObs = (Obs)this.getContext().getParsedObject();
			
			// Process the organizer components
			Obs familyHistoryObs = super.parseOrganizer(organizer);
			
			// Process participant data
			RelatedSubject subject = organizer.getSubject().getRelatedSubject();

			// Family member relationship
			if(subject.getSubject() instanceof SdtcSubjectPerson)
			{
				SdtcSubjectPerson person = (SdtcSubjectPerson)subject.getSubject();
				if(person.getId() != null)
					for(II id : person.getId())
						this.m_dataUtil.addSubObservationValue(familyHistoryObs, Context.getConceptService().getConcept(160752), this.m_datatypeUtil.formatIdentifier(id));
				if(person.getBirthTime() != null && componentObservation.getEffectiveTime().getValue() != null) // Not required in PCC but still useful
				{
					PQ age =  componentObservation.getEffectiveTime().getValue().subtract(person.getBirthTime()).convert("a");
					this.m_dataUtil.addSubObservationValue(familyHistoryObs, Context.getConceptService().getConcept(160617), age);
				}
				if(person.getBirthTime() != null)
					this.m_dataUtil.addSubObservationValue(familyHistoryObs, Context.getConceptService().getConcept(160751), person.getBirthTime());
			}
	
			if(subject.getCode() != null)
				this.m_dataUtil.addSubObservationValue(familyHistoryObs, Context.getConceptService().getConcept(1560), subject.getCode());		
	
			// Name
			if(subject.getSubject().getName() != null)
				for(PN name : subject.getSubject().getName())
					this.m_dataUtil.addSubObservationValue(familyHistoryObs, Context.getConceptService().getConcept(160750), name.toString());

			// Date of diagnosis
			if(componentObservation.getEffectiveTime() != null)
			{
				if(componentObservation.getEffectiveTime().getValue() != null)
					this.m_dataUtil.addSubObservationValue(familyHistoryObs, Context.getConceptService().getConcept(159948), componentObservation.getEffectiveTime().getValue());
				else
				{
					// did it start?
					if(componentObservation.getEffectiveTime().getLow() != null)
					{
						Obs sub = this.m_dataUtil.addSubObservationValue(familyHistoryObs, Context.getConceptService().getConcept(159948), componentObservation.getEffectiveTime().getLow());
						sub.setComment("From");
					}
					if(componentObservation.getEffectiveTime().getHigh() != null)
					{
						Obs sub = this.m_dataUtil.addSubObservationValue(familyHistoryObs, Context.getConceptService().getConcept(159948), componentObservation.getEffectiveTime().getHigh());
						sub.setComment("Until");
					}
				}
			}
			
			// Present?
			if(BL.TRUE.equals(componentObservation.getNegationInd()))
				this.m_dataUtil.addSubObservationValue(familyHistoryObs, Context.getConceptService().getConcept(1729), Context.getConceptService().getConcept(Integer.valueOf(Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_FALSE_CONCEPT))));

			// TODO: Map this more accurately
			if(componentObservation.getCode() != null && 
					componentObservation.getCode().getCodeSystem().equals(CdaHandlerConstants.CODE_SYSTEM_SNOMED) && 
					componentObservation.getCode().getCode().equals("282291009"))
				this.m_dataUtil.addSubObservationValue(familyHistoryObs, Context.getConceptService().getConcept(160592), componentObservation.getValue());
			else if (componentObservation.getCode() != null)
				this.m_dataUtil.addSubObservationValue(parentObs, this.m_conceptUtil.getOrCreateConceptAndEquivalents(componentObservation.getCode()), componentObservation.getValue());
			
			// Comment?
			if(componentObservation.getText() != null && componentObservation.getText().getReference() != null)
			{
				StructDocNode node = this.getSection().getText().findNodeById(componentObservation.getText().getReference().getValue());
				if(node != null)
					familyHistoryObs.setComment(node.toPlainString());
			}
			
			familyHistoryObs = Context.getObsService().saveObs(familyHistoryObs, null);
			
		}
		
		// Nothing?
		return null;
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
