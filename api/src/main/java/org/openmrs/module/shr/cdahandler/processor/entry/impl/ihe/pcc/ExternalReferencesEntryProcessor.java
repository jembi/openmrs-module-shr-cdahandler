package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import java.util.List;

import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.doc.StructDocNode;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.formatters.FormatterUtil;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Act;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Reference;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActRelationshipExternalReference;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.DocumentValidationException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.entry.impl.EntryProcessorImpl;

/**
 * Entry processor for external references
 */
@ProcessTemplates(templateIds = { CdaHandlerConstants.ENT_TEMPLATE_EXTERNAL_REFERENCES_ENTRY})
public class ExternalReferencesEntryProcessor extends EntryProcessorImpl {
	
	/**
	 * Get the name of the template
	 * @see org.openmrs.module.shr.cdahandler.processor.Processor#getTemplateName()
	 */
	@Override
	public String getTemplateName() {
		return "External Reference";
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
	 * Process the entry
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
		else if(!entry.isPOCD_MT000040UVAct())
			throw new DocumentImportException("Expected entry to be an Act");
		
		Act act = (Act)entry;
		
		// Create concept and datatype services
		Encounter encounterInfo = (Encounter)this.getEncounterContext().getParsedObject();
		Obs parentObs = (Obs)this.getContext().getParsedObject();
		Obs previousObs = super.voidOrThrowIfPreviousObsExists(act.getReference(), encounterInfo.getPatient(), act.getId());
				
		// Create the observation for the reference
		Obs res = new Obs();
		res.setPreviousVersion(previousObs);
		res.setObsGroup(parentObs);
		res.setPerson(encounterInfo.getPatient());
		res.setLocation(encounterInfo.getLocation());
		res.setDateCreated(encounterInfo.getDateCreated());
		res.setEncounter(encounterInfo);
		res.setConcept(this.m_conceptUtil.getOrCreateRMIMConcept(CdaHandlerConstants.RMIM_CONCEPT_UUID_REFERENCE, null));
		
		if(act.getId() != null && !act.getId().isNull())
			res.setAccessionNumber(this.m_datatypeUtil.formatIdentifier(act.getId().get(0)));
		
		// comment?
		if(act.getText() != null && act.getText().getReference() != null)
		{
			StructDocNode commentNode = this.getSection().getText().findNodeById(act.getText().getReference().getValue());
			if(commentNode != null)
				res.setComment(commentNode.toPlainString());
		}
		
		// Set the creator
		super.setCreator(res, act);

		// References as sub obs
		for(Reference ref : act.getReference())
		{
			// Create a more descriptive type code
			CE<x_ActRelationshipExternalReference> referenceType = new CE<x_ActRelationshipExternalReference>(ref.getTypeCode().getCode());
			if(ref.getTypeCode().getCode().equals(x_ActRelationshipExternalReference.REFR))
				referenceType.setDisplayName("Referenced Material");
			else if(ref.getTypeCode().getCode().equals(x_ActRelationshipExternalReference.SPRT))
				referenceType.setDisplayName("Supporting Material");
			
			II id = ref.getExternalActChoiceIfExternalDocument().getId().get(0);
			Concept obsConcept = this.m_conceptUtil.getTypeSpecificConcept(referenceType, id);
			if(obsConcept == null)
				obsConcept = this.m_conceptUtil.createConcept(referenceType, id);
			
			Obs refObs = this.m_dataUtil.addSubObservationValue(res, obsConcept, id);

			// Set original text as reference url
			if(ref.getExternalActChoiceIfExternalDocument().getText() != null &&
					ref.getExternalActChoiceIfExternalDocument().getText().getReference() != null)
				refObs.setComment(ref.getExternalActChoiceIfExternalDocument().getText().getReference().toString());
		}
		
		// Save the obs
		res = Context.getObsService().saveObs(res, null);
		
		return res;
	}

	/**
	 * 
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.EntryProcessorImpl#validate(org.marc.everest.interfaces.IGraphable)
	 */
	@Override
    public ValidationIssueCollection validate(IGraphable object) {
	    ValidationIssueCollection issues = super.validate(object);
	    if(issues.hasErrors())
	    	return issues;
	    
	    // Act?
	    ClinicalStatement statement = (ClinicalStatement)object;
	    if(!statement.isPOCD_MT000040UVAct())
	    	issues.error(this.getInvalidClinicalStatementErrorText(Act.class, statement.getClass()));
	    else
	    {
	    	Act act = (Act)statement;
	    	if(act.getId() == null || act.getId().isNull() || act.getId().isEmpty())
	    		issues.error("External reference must carry an identifier");
	    	if(act.getReference().size() == 0)
	    		issues.error("External reference must have at least one reference to an external document");
	    	else
	    		for(Reference ref : act.getReference())
	    		{
	    			if(ref.getTypeCode() == null || ref.getTypeCode().isNull() ||
	    				(!ref.getTypeCode().getCode().equals(x_ActRelationshipExternalReference.REFR) &&
	    					!ref.getTypeCode().getCode().equals(x_ActRelationshipExternalReference.SPRT)
	    					))
	    				issues.error("Reference must carry a type code of REFR or SPRT");
	    			if(ref.getExternalActChoiceIfExternalDocument() == null)
	    				issues.error("Reference must carry a reference to an externalDocument");
	    			else if(ref.getExternalActChoiceIfExternalDocument().getId() == null)
	    				issues.error("Referenced document must be associated via the id element");
	    		}
	    	
	    }
	    
	    return issues;
    }
	
	
	
}
