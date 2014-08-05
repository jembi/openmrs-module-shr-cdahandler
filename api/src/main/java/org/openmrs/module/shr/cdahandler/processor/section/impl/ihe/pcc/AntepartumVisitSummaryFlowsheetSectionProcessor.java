package org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.PQ;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Entry;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Observation;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor;

/**
 * Visit summary flowsheet section processor
 * 
 * From PCC Suppl:
 * This section is a running history of the most important elements noted for a pregnant woman. 
 * 
 * See: PCC TF Suppl: 6.3.3.9.3
 */
@ProcessTemplates(templateIds = {CdaHandlerConstants.SCT_ANTEPARTUM_TEMPLATE_VISIT_SUMMARY_FLOWSHEET})
public class AntepartumVisitSummaryFlowsheetSectionProcessor extends GenericLevel3SectionProcessor {
	
	/**
	 * Get the expected entries
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor#getExpectedEntries(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section)
	 */
	@Override
	protected List<String> getExpectedEntries(Section section) {
		return Arrays.asList(
			CdaHandlerConstants.ENT_TEMPLATE_SIMPLE_OBSERVATION,
			CdaHandlerConstants.ENT_TEMPLATE_ANTEPARTUM_FLOWSHEET_PANEL
			);
	}

	/**
	 * Validate the section
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor#validate(org.marc.everest.interfaces.IGraphable)
	 */
	@Override
    public ValidationIssueCollection validate(IGraphable object) {
	    ValidationIssueCollection issueCollection = super.validate(object);
	    if(issueCollection.hasErrors()) return issueCollection;
	    
	    // Must have a simple observation
	    Section section = (Section)object;
	    for(Entry ent : super.getEntry(section, CdaHandlerConstants.ENT_TEMPLATE_SIMPLE_OBSERVATION))
	    {
	    	if(ent.getClinicalStatementIfObservation() == null)
	    		continue; // not an obs? that is odd
	    	
	    	// Now validate the code
	    	Observation obs = ent.getClinicalStatementIfObservation();
	    	if(obs.getCode() == null || obs.getCode().isNull() ||
	    			!obs.getCode().getCode().equals("8348-5") ||
	    			!obs.getCode().getCodeSystem().equals(CdaHandlerConstants.CODE_SYSTEM_LOINC)
	    		)
	    		issueCollection.error("Simple observation in Antepartum Visit Summary Flowsheet must carry pregnancy weight with code 8348-5");
	    	if(obs.getValue() == null || obs.getValue().isNull() || !(obs.getValue() instanceof PQ))
	    		issueCollection.error("Simple observation value must be a PQ");
	    	
	    }
	    
	    return issueCollection;
    }

	/**
	 * Get the name of the template
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#getTemplateName()
	 */
	@Override
    public String getTemplateName() {
		return "Antepartum Visit Summary Flowsheet";
    }

	/**
	 * Get expected section code
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#getExpectedSectionCode()
	 */
	@Override
    public CE<String> getExpectedSectionCode() {
		return new CE<String>("57059-8", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "Pregnancy Visit Summary", null);
    }
	
	
}
