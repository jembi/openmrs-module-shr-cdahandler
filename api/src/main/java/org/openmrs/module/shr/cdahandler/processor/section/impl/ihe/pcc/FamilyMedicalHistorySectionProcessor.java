package org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor;


/**
 * A processor that parses the family medical history templates (Level 2 and Level 3)
 */
@ProcessTemplates(
	understands = {
		@TemplateId(root = CdaHandlerConstants.SCT_TEMPLATE_FAMILY_HISTORY),
		@TemplateId(root = CdaHandlerConstants.SCT_TEMPLATE_CODED_FAMILY_MEDICAL_HISTORY)
	})
public class FamilyMedicalHistorySectionProcessor extends GenericLevel3SectionProcessor {
	
	/**
	 * Get the expected entries if this is level 3
	 */
	@Override
	protected List<String> getExpectedEntries(Section section) {
		if(section.getTemplateId().contains(new II(CdaHandlerConstants.SCT_TEMPLATE_CODED_FAMILY_MEDICAL_HISTORY)))
			return Arrays.asList(CdaHandlerConstants.ENT_TEMPLATE_FAMILY_HISTORY_ORGANIZER);
		return null;
	}

	/**
	 * Get the template name
	 */
	@Override
    public String getTemplateName() {
		return "Family History";
    }

	/**
	 * Get the expected code
	 */
	@Override
    protected CE<String> getExpectedSectionCode(Section section) {
		return new CE<String>("10157-6", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "HISTORY OF FAMILY MEMBER DISEASES", null);
    }
	
	
}
