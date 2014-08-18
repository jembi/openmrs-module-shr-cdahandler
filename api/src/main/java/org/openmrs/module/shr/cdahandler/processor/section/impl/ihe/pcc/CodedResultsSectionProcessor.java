package org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor;

/**
 * A section processor which can interpret the Coded Results section
 * 
 * From PCC TF:
 * The results section shall contain a narrative description of the relevant diagnostic procedures the patient received in the past. It shall include entries for procedures and references to procedure reports when known as described in the Entry Content Modules. 
 * 
 * See: PCC TF-2: 6.3.3.5.2
 */
@ProcessTemplates(templateIds = {
		CdaHandlerConstants.SCT_TEMPLATE_RESULTS,
		CdaHandlerConstants.SCT_TEMPLATE_CODED_RESULTS
})
public class CodedResultsSectionProcessor extends GenericLevel3SectionProcessor {
	
	/**
	 * Get the expected entries
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor#getExpectedEntries(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section)
	 */
	@Override
	protected List<String> getExpectedEntries(Section section) {
		if(section.getTemplateId().contains(new II(CdaHandlerConstants.SCT_TEMPLATE_CODED_RESULTS)))
			return Arrays.asList(
				CdaHandlerConstants.ENT_TEMPLATE_PROCEDURE_ENTRY,
				CdaHandlerConstants.ENT_TEMPLATE_EXTERNAL_REFERENCES_ENTRY,
				CdaHandlerConstants.ENT_TEMPLATE_SIMPLE_OBSERVATION
				);
		return null;
	}

	/**
	 * Get the expected section code
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#getExpectedSectionCode()
	 */
	@Override
    public CE<String> getExpectedSectionCode() {
		return new CE<String>("30954-2", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "Relevant diagnostic tests/laboratory data", null);
    }

	/**
	 * Get template name
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#getTemplateName()
	 */
	@Override
    public String getTemplateName() {
		return "Results";
    }
	
	
	
}
