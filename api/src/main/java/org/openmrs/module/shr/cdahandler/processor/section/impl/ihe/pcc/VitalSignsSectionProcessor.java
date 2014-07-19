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
 * Represents a vital signs section processor for coded or non coded vital signs
 * 
 * From PCC:
 * The vital signs section contains coded measurement results of a patient’s vital signs
 * 
 * See: PCC TF-2:6.3.3.4.5
 * See: PCC TF-2:6.3.3.4.4
 */
@ProcessTemplates(
	process = {
			@TemplateId(root = CdaHandlerConstants.SCT_TEMPLATE_CCD_3_12),
			@TemplateId(root = CdaHandlerConstants.SCT_TEMPLATE_CODED_VITAL_SIGNS),
			@TemplateId(root = CdaHandlerConstants.SCT_TEMPLATE_VITAL_SIGNS)
	})
public class VitalSignsSectionProcessor extends GenericLevel3SectionProcessor  {

	/**
	 * Get expected sections
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor#getExpectedEntries(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section)
	 */
	@Override
    protected List<String> getExpectedEntries(Section section) {
		if(section.getTemplateId().contains(new II(CdaHandlerConstants.SCT_TEMPLATE_CODED_VITAL_SIGNS)))
			return Arrays.asList(CdaHandlerConstants.ENT_TEMPLATE_VITAL_SIGNS_ORGANIZER);
		return null;
    }

	/**
	 * Get the expected code
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor#getExpectedSectionCode(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section)
	 */
	@Override
    public CE<String> getExpectedSectionCode() {
		return new CE<String>("8716-3", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "VITAL SIGNS", null);
    }

	/**
	 * Get the template name
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#getTemplateName()
	 */
	@Override
    public String getTemplateName() {
		return "Vital Signs";
    }
	
	
	
	
}
