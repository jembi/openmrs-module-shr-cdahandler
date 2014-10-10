package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.BL;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.INT;
import org.marc.everest.datatypes.PN;
import org.marc.everest.datatypes.PQ;
import org.marc.everest.datatypes.ST;
import org.marc.everest.datatypes.doc.StructDocElementNode;
import org.marc.everest.datatypes.doc.StructDocNode;
import org.marc.everest.datatypes.generic.CD;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Component4;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.EntryRelationship;
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
import org.openmrs.module.shr.cdahandler.obs.ExtendedObs;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.context.ProcessorContext;
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
			
			ExtendedObs familyHistoryObs = (ExtendedObs)super.parseOrganizer(organizer);
			familyHistoryObs.setConcept(Context.getConceptService().getConcept(160593));

			// Date of diagnosis
			if(componentObservation.getEffectiveTime() != null)
			{
				// Set the effective time of the family history obs
				if(componentObservation.getEffectiveTime() != null)
				{
					// Date precisions least descriptive
					if(componentObservation.getEffectiveTime().getValue() != null )
					{
						familyHistoryObs.setObsDatetime(componentObservation.getEffectiveTime().getValue().getDateValue().getTime());
						familyHistoryObs.setObsDatePrecision(componentObservation.getEffectiveTime().getValue().getDateValuePrecision());
					}
					// Low/high
					if(componentObservation.getEffectiveTime().getLow() != null && !componentObservation.getEffectiveTime().getLow().isNull())
					{
						familyHistoryObs.setObsStartDate(componentObservation.getEffectiveTime().getLow().getDateValue().getTime());
						if(familyHistoryObs.getObsDatePrecision() > componentObservation.getEffectiveTime().getLow().getDateValuePrecision())
							familyHistoryObs.setObsDatePrecision(componentObservation.getEffectiveTime().getLow().getDateValuePrecision());
					}
					if(componentObservation.getEffectiveTime().getHigh() != null && !componentObservation.getEffectiveTime().getHigh().isNull())
					{
						familyHistoryObs.setObsEndDate(componentObservation.getEffectiveTime().getHigh().getDateValue().getTime());
						if(familyHistoryObs.getObsDatePrecision() > componentObservation.getEffectiveTime().getHigh().getDateValuePrecision())
							familyHistoryObs.setObsDatePrecision(componentObservation.getEffectiveTime().getHigh().getDateValuePrecision());
					}
					
				}
				else
					familyHistoryObs.setObsDatetime(encounterInfo.getEncounterDatetime());
			}
			else
				familyHistoryObs.setObsDatePrecision(0);
			
			// Interpretation
			if(componentObservation.getInterpretationCode() != null && !componentObservation.getInterpretationCode().isEmpty())
				familyHistoryObs.setObsInterpretation(this.m_conceptUtil.getOrCreateConcept(componentObservation.getInterpretationCode().get(0)));

			// Comment?
			if(componentObservation.getText() != null && componentObservation.getText().getReference() != null)
			{
				StructDocNode node = this.getSection().getText().findNodeById(componentObservation.getText().getReference().getValue());
				if(node != null)
					familyHistoryObs.setComment(node.toPlainString());
			}
			
			familyHistoryObs = (ExtendedObs)Context.getObsService().saveObs(familyHistoryObs, null);
			
			// Process participant data
			RelatedSubject subject = organizer.getSubject().getRelatedSubject();

			// Family member relationship
			if(subject.getSubject() instanceof SdtcSubjectPerson)
			{
				SdtcSubjectPerson person = (SdtcSubjectPerson)subject.getSubject();
				if(person.getId() != null)
					for(II id : person.getId())
					{
						Obs idObs = this.m_dataUtil.createSubObservationValue(familyHistoryObs, Context.getConceptService().getConcept(160752), this.m_datatypeUtil.formatIdentifier(id));
						idObs.setObsGroup(familyHistoryObs);
						Context.getObsService().saveObs(idObs, null);
					}
				
				// Name
				if(person.getName() != null)
					for(PN name : person.getName())
					{
						Obs nameObs =this.m_dataUtil.createSubObservationValue(familyHistoryObs, Context.getConceptService().getConcept(160750), name.toString());
						nameObs.setObsGroup(familyHistoryObs);
						Context.getObsService().saveObs(nameObs, null);
					}
				
				
				if(person.getBirthTime() != null && !person.getBirthTime().isNull())
				{
					Obs dobObs = this.m_dataUtil.createSubObservationValue(familyHistoryObs, Context.getConceptService().getConcept(160751), person.getBirthTime());
					dobObs.setObsGroup(familyHistoryObs);
					dobObs.setComment(person.getBirthTime().getDateValuePrecision().toString());
					Context.getObsService().saveObs(dobObs, null);
				}
					
				
				// Age observation
				List<EntryRelationship> ageObservation = super.findEntryRelationship(componentObservation, CdaHandlerConstants.ENT_TEMPLATE_CCD_AGE_OBSERVATION);
				if(ageObservation.size() == 1)
				{
					INT age =  (INT)ageObservation.get(0).getClinicalStatementIfObservation().getValue();
					Obs ageObs = this.m_dataUtil.createSubObservationValue(familyHistoryObs, Context.getConceptService().getConcept(160617), age);
					ageObs.setObsGroup(familyHistoryObs);
					Context.getObsService().saveObs(ageObs, null);
					componentObservation.getEntryRelationship().remove(ageObservation.get(0));
				}
				/*
				else if(person.getBirthTime() != null && componentObservation.getEffectiveTime().getValue() != null) // Not required in PCC but still useful
				{
					PQ age =  componentObservation.getEffectiveTime().getValue().subtract(person.getBirthTime()).convert("a");
					Obs ageObs = this.m_dataUtil.createSubObservationValue(familyHistoryObs, Context.getConceptService().getConcept(160617), age);
					ageObs.setObsGroup(familyHistoryObs);
					Context.getObsService().saveObs(ageObs, null);
				}*/
			}
	
			if(subject.getCode() != null)
			{
				Obs relationObs = this.m_dataUtil.createSubObservationValue(familyHistoryObs, Context.getConceptService().getConcept(1560), subject.getCode());
				relationObs.setObsGroup(familyHistoryObs);
				Context.getObsService().saveObs(relationObs, null);
			}
			else
				throw new DocumentImportException("Family member must have a relationship type specified");

			
			// Present?
			if(BL.TRUE.equals(componentObservation.getNegationInd()))
			{
				Obs negateObs = this.m_dataUtil.createSubObservationValue(familyHistoryObs, Context.getConceptService().getConcept(1729), Context.getConceptService().getConcept(Integer.valueOf(Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_FALSE_CONCEPT))));
				negateObs.setObsGroup(familyHistoryObs);
				Context.getObsService().saveObs(negateObs, null);
			}
			

			// Death indicator?
			if(this.m_datatypeUtil.hasTemplateId(componentObservation, new II(CdaHandlerConstants.ENT_TEMPLATE_CCD_DEATH_OBSERVATION)))
			{
				Obs dxObs = this.m_dataUtil.createSubObservationValue(familyHistoryObs, Context.getConceptService().getConcept(160592), Context.getConceptService().getConcept(160432));
				dxObs.setObsGroup(familyHistoryObs);
				Context.getObsService().saveObs(dxObs, null);
			}

			// Write the diagnosis
			if (componentObservation.getCode() != null)
			{
				Obs dxObs = this.m_dataUtil.createSubObservationValue(parentObs, this.m_conceptUtil.getOrCreateConceptAndEquivalents(componentObservation.getCode()), componentObservation.getValue());
				dxObs.setObsGroup(familyHistoryObs);
				Context.getObsService().saveObs(dxObs, null);
			}
			
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
		    if(organizer.getSubject().getRelatedSubject().getCode() == null || organizer.getSubject().getRelatedSubject().getCode().isNull() || !organizer.getSubject().getRelatedSubject().getCode().getCodeSystem().equals(CdaHandlerConstants.CODE_SYSTEM_FAMILY_MEMBER))
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
