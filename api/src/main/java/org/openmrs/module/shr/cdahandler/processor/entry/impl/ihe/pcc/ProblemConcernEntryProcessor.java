package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.II;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Act;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ActStatus;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.activelist.ActiveListItem;
import org.openmrs.activelist.Problem;
import org.openmrs.activelist.ProblemModifier;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.obs.ExtendedObs;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.entry.EntryProcessor;
import org.openmrs.module.shr.cdahandler.processor.factory.impl.EntryProcessorFactory;

/**
 * A concern entry processor
 * 
 * See: PCC TF-2: 6.3.4.12
 */
@ProcessTemplates(
	templateIds = {
			CdaHandlerConstants.ENT_TEMPLATE_PROBLEM_CONCERN
	})
public class ProblemConcernEntryProcessor extends ConcernEntryProcessor {

	/**
	 * Get expected entries which in this case are Problem Entries
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc.ConcernEntryProcessor#getExpectedEntries()
	 */
	@Override
    protected List<String> getExpectedEntryRelationships() {
		return Arrays.asList(CdaHandlerConstants.ENT_TEMPLATE_PROBLEM_OBSERVATION);
    }

	/**
	 * Get template name
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc.ConcernEntryProcessor#getTemplateName()
	 */
	@Override
    public String getTemplateName() {
		return "Problem Concern Entry";
    }

	/**
	 * Parse the contents of the Act to a Problem
	 * @throws DocumentImportException 
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc.ConcernEntryProcessor#parseActContents(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Act, org.openmrs.Obs)
	 */
	@Override
    protected ActiveListItem parseActContents(Act act, ClinicalStatement statement) throws DocumentImportException {
		EntryProcessor processor = EntryProcessorFactory.getInstance().createProcessor(statement);
		processor.setContext(this.getContext());

		
		BaseOpenmrsData processed = processor.process(statement);
		
		// Not a problem observation so don't create a problem
		
		if(!statement.getTemplateId().contains(new II(CdaHandlerConstants.ENT_TEMPLATE_PROBLEM_OBSERVATION)))
			return null;
		
		ExtendedObs obs = (ExtendedObs)processed;
		
		// Correct the act based on the effective time of the entry relationship?
		Problem res = super.createActiveListItem(act, statement, obs, Problem.class);
			
		// Problem
		if(obs.getValueCoded() == null)
			throw new DocumentImportException("Observation for this problem must be of type Coded");
		else if(res.getProblem() == null)
			res.setProblem(obs.getValueCoded());
		
		
		// Modifier
		if(act.getNegationInd() != null && act.getNegationInd().toBoolean())
			res.setModifier(ProblemModifier.RULE_OUT);
		else if(act.getStatusCode().getCode().equals(ActStatus.Completed))
			res.setModifier(ProblemModifier.HISTORY_OF);
		
		return res;
		
    }
	
	
}
