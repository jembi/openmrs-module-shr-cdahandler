package org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor;

/**
 * Processor that can handle coded history of infection
 * 
 * From PCC:
 * The History of Infection section shall contain a narrative description of any infections the patient may have contracted prior to the patient's current condition. It shall include entries for problems as described in the Entry Content Modules.
 * 
 * See: PCC TF-2 CDA Suppl:6.3.3.2.37
 */
@ProcessTemplates(
	process = {
			@TemplateId(root = CdaHandlerConstants.SCT_TEMPLATE_CODED_HISTORY_OF_INFECTION)
	})
public class CodedHistoryOfInfectionSectionProcessor extends GenericLevel3SectionProcessor {
	
	/**
	 * Coded history of infection expected entries
	 */
	@Override
	protected List<String> getExpectedEntries(Section section) {
		return Arrays.asList(CdaHandlerConstants.ENT_TEMPLATE_PROBLEM_CONCERN);
	}

	/**
	 * Get the expected section code
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#getExpectedSectionCode()
	 */
	@Override
    public CE<String> getExpectedSectionCode() {
		return new CE<String>("56838-6", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC,  null, "History of infectious disease", null);
    }
	
	
}
