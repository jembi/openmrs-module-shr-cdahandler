package org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor;

/**
 * A processor for the pregnancy history section
 * 
 * From PCC:
 * The pregnancy history section contains coded entries describing the patient history of pregnancies. 
 * 
 * See: PCC TF-2:6.3.3.2.18 
 */
@ProcessTemplates(
	templateIds = {
			CdaHandlerConstants.SCT_TEMPLATE_PREGNANCY_HISTORY
	})
public class PregnancyHistorySectionProcessor extends GenericLevel3SectionProcessor {
	
	/**
	 * Get expected entries
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor#getExpectedEntries(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section)
	 */
	@Override
	protected List<String> getExpectedEntries(Section section) {
		return Arrays.asList(CdaHandlerConstants.ENT_TEMPLATE_PREGNANCY_OBSERVATION);
	}

	/**
	 * Get template name
	 */
	@Override
    public String getTemplateName() {
		return "Pregnancy History";
    }

	/**
	 * Get expected section code
	 */
	@Override
    public CE<String> getExpectedSectionCode() {
		return new CE<String>("10162-6", CdaHandlerConstants.CODE_SYSTEM_LOINC, "LOINC", null, "HISTORY OF PREGNANCIES", null);
    }
	
	
}
