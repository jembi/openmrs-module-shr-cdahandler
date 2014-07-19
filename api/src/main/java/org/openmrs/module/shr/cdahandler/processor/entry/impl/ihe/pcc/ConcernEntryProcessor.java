package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.NullFlavor;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Act;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.EntryRelationship;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ActStatus;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.activelist.ActiveListItem;
import org.openmrs.activelist.Allergy;
import org.openmrs.activelist.Problem;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.DocumentValidationException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;
import org.openmrs.module.shr.cdahandler.processor.entry.impl.ActEntryProcessor;

/**
 * Processes concern entries
 * 
 * See: PCC TF-2: 6.3.4.11
 */
@ProcessTemplates(
	process = {
			@TemplateId(root = CdaHandlerConstants.ENT_TEMPLATE_CONCERN_ENTRY)
	})
public class ConcernEntryProcessor extends ActEntryProcessor {

	/**
	 * Get the template name
	 * @see org.openmrs.module.shr.cdahandler.processor.Processor#getTemplateName()
	 */
	@Override
	public String getTemplateName() {
		return "Concern Entry";
	}
	
	/**
	 * Get the expected entries .. there are none other than there should be more than one
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.ActEntryProcessor#getExpectedEntries()
	 */
	@Override
	protected List<String> getExpectedEntryRelationships() {
		return null;
	}
	
	/**
	 * The concern gets placed in two places in the OpenMRS datamodel:
	 * 
	 *  First: It is placed as an observation within the encounter being reported for this document import
	 *  Second: As an active list item
	 *  
	 *  This method will only return the observation (or a group of them if more than one exists)
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.EntryProcessorImpl#process(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement)
	 */
	@Override
	public BaseOpenmrsData process(ClinicalStatement entry) throws DocumentImportException {
		
		// Validate
		if(this.m_configuration.getValidationEnabled())
		{
			ValidationIssueCollection issues = this.validate(entry);
			if(issues.hasErrors())
				throw new DocumentValidationException(entry, issues);
		}

		// This is an interesting thought, according to the spec
		// There may be multiple entries each of which identify the problems of concern
		// Hmm... I think the only way this can be done is to process each of the
		//        entries as an observation saving to the encounter, then creating a 
		//		  problem for each of the observations that exist. Ensuring that the 
		// 		  the problem list item doesn't already exist via accession number?
		Act act = (Act)entry;
		for(EntryRelationship relationship : act.getEntryRelationship())
		{
			
			if(relationship == null || relationship.getNullFlavor() != null ||
					relationship.getClinicalStatement() == null ||
					relationship.getClinicalStatement().getNullFlavor() != null)
				continue;
		
			// Process the active list item
			ActiveListItem listItem = this.parseActContents(act, relationship.getClinicalStatement());
			if(listItem != null)
				Context.getActiveListService().saveActiveListItem(listItem);
		}
		
		return null;
	}

	/**
	 * Parse act contents into a list item... This technically can't be done at this 
	 * level because a concern entry doesn't have enough information information about
	 * the type of concern to know which active list item to create  
	 */
	protected ActiveListItem parseActContents(Act act, ClinicalStatement obs) throws DocumentImportException {
		return null;
    }

	/**
	 * Gets the current list item based on the patient and type
	 */
	protected <T extends ActiveListItem> T getCurrentActiveListItem(Class<T> clazz, Act act)
	{
		// Get the encounter context
		Encounter encounterInfo = (Encounter)super.getEncounterContext().getParsedObject();
		
		// Already reported as a problem?
		for(T prob : Context.getActiveListService().getActiveListItems(clazz, encounterInfo.getPatient(), null))
		{
			// First, does the UUID match? And also, is it an active problem?
			for(II id : act.getId())
				if(prob.getStartObs().getAccessionNumber().equals(this.m_datatypeUtil.formatIdentifier(id))) // Is the ID mentioned in the comments?
					return prob;
					break;
		}
		
		return null;

	}
	
	
	/**
	 * Validate this section adheres to the ConcernEntry
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.ActEntryProcessor#validate(org.marc.everest.interfaces.IGraphable)
	 */
	@Override
    public ValidationIssueCollection validate(IGraphable object) {
	    ValidationIssueCollection validationIssues = super.validate(object);
	    if(validationIssues.hasErrors()) return validationIssues;
	    
	    // Shall have a code with nullflavor NA
	    Act act = (Act)object;
	    if(act.getCode() != null && act.getCode().getNullFlavor().getCode() != NullFlavor.NotApplicable)
	    	validationIssues.error("Act must carry a code with nullFlavor = 'NA'");
	    if(act.getEffectiveTime() == null || act.getEffectiveTime().isNull())
	    	validationIssues.error("Act must carry an effective time");
	    else if(act.getEffectiveTime().getLow() == null || act.getEffectiveTime().getLow().isNull())
	    	validationIssues.warn("Act's effectiveTime element must be populated with a Low value");
	    else
	    {
	    	Boolean isHighNull = act.getEffectiveTime().getHigh() == null || act.getEffectiveTime().isNull();
	    	ActStatus status = act.getStatusCode().getCode();
	    	if(isHighNull && (status == ActStatus.Aborted || status == ActStatus.Completed))
	    		validationIssues.error("Act's effectiveTime element must be populated with a High value when status code implies the act is completed (completed, aborted)");
	    	else if(!isHighNull && (status == ActStatus.Active || status == ActStatus.Suspended))
	    		validationIssues.error("Act's effectiveTime element must not be populated with a High value when status code implies the act is still ongoing (active, suspended)");
	    }
	    
	    return validationIssues;
    }

	/**
	 * Calculate the current status
	 */
	public ActStatus calculateCurrentStatus(ActiveListItem res) {
		if(res.getStartDate() == null && res.getEndDate() == null)
			return ActStatus.New;
		else if(res.getStartDate() != null && res.getEndDate() == null)
			return ActStatus.Active;
		else if(res.getVoided() && res.getEndDate() != null)
			return ActStatus.Aborted;
		else if(res.getVoided() && res.getEndDate() == null)
			return ActStatus.Suspended;
		else if(res.getEndDate() != null)
			return ActStatus.Completed;
		else
			return null;
    }

	/**
	 * Processes common list contents for the specified class
	 * Auto generated method comment
	 * 
	 * @param act
	 * @param res
	 * @return
	 * @throws DocumentImportException 
	 */
	public <T extends ActiveListItem> T createActiveListItem(Act act, Obs obs, Class<T> clazz) throws DocumentImportException {
		
		// Get the encounter context
		Encounter encounterInfo = (Encounter)super.getEncounterContext().getParsedObject();

		// Set base result properties
		T res = this.getCurrentActiveListItem(clazz, act);
		if(res == null || 
				res instanceof Problem && !((Problem)res).getProblem().equals(obs.getValueCoded()) ||
				res instanceof Allergy && !((Allergy)res).getAllergen().equals(obs.getValueCoded()))
	        try {
	            res = clazz.newInstance();
            }
            catch (Exception e) {
            	throw new DocumentImportException("Could not create necessary class", e);
            }
			
		// Set created or updated time
		if(res.getDateCreated() == null)
			res.setDateCreated(encounterInfo.getDateCreated());
		else
			res.setDateChanged(encounterInfo.getDateCreated());

		
		// New?
		ActStatus currentStatus = this.calculateCurrentStatus(res);

		// What state are we entering? Valid transitions are
		// 	New -> *
		// Active -> Aborted
		// Active -> Completed
		// Active -> Suspended
		// Completed -> Active
		// Suspended -> *
		if(currentStatus == act.getStatusCode().getCode()) // no state transition
			;
		else if(currentStatus == ActStatus.Completed && act.getStatusCode().getCode() == ActStatus.Active)
			throw new IllegalStateException("Cannot re-activate a completed or aborted problem. Please create a new one"); 
		else if(currentStatus == ActStatus.Aborted)
			throw new IllegalStateException("Cannot update an aborted problem entry. Please create a new one");
		
		// Effective time?
		if(act.getEffectiveTime() != null)
		{
			// Can only update start date if currentStatus is New or Active
			if(act.getEffectiveTime().getLow() != null && !act.getEffectiveTime().getLow().isNull())
			{
				// Does this report it to be prior to the currently known start date?
				if(res.getStartDate() == null || act.getEffectiveTime().getLow().getDateValue().getTime().compareTo(res.getStartDate()) < 0)
				{
					res.setStartObs(obs);
					res.setStartDate(act.getEffectiveTime().getLow().getDateValue().getTime());
				}
			}
			if(act.getEffectiveTime().getHigh() != null && !act.getEffectiveTime().getHigh().isNull())
			{
				// Does this report it to be after the currently known end date?
				if(res.getEndDate() == null || act.getEffectiveTime().getHigh().getDateValue().getTime().compareTo(res.getEndDate()) > 0)
				{
					res.setStopObs(obs);
					res.setEndDate(act.getEffectiveTime().getHigh().getDateValue().getTime());
				}
			}
			if(res.getStartDate() == null && obs.getObsDatetime() != null)
				res.setStartDate(obs.getObsDatetime());
		}
		else if(act.getStatusCode().getCode() != ActStatus.Completed)
			throw new DocumentImportException("Missing effective time of the problem");

		// Void this?
		if(act.getStatusCode().getCode() == ActStatus.Aborted || 
				act.getStatusCode().getCode() == ActStatus.Suspended)
		{
			res.setVoided(true);
			res.setVoidReason(act.getStatusCode().getCode().getCode());
			res.setDateVoided(encounterInfo.getDateCreated());
		}
		
		// Copy attributes
		res.setPerson(encounterInfo.getPatient());
		
		return res;
    }
	
	
	
}
