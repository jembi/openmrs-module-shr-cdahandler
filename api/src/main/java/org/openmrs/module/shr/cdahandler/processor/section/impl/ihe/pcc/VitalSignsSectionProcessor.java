package org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.formatters.FormatterUtil;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.module.shr.cdahandler.CdaHandlerOids;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor;

/**
 * Represents a vital signs section processor for coded or non coded vital signs
 */
@ProcessTemplates(
	understands = {
			@TemplateId(root = CdaHandlerOids.SCT_TEMPLATE_CODED_VITAL_SIGNS),
			@TemplateId(root = CdaHandlerOids.SCT_TEMPLATE_VITAL_SIGNS)
	})
public class VitalSignsSectionProcessor extends GenericLevel3SectionProcessor  {

	/**
	 * Get expected sections
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor#getExpectedEntries(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section)
	 */
	@Override
    protected List<String> getExpectedEntries(Section section) {
		if(section.getTemplateId().contains(new II(CdaHandlerOids.SCT_TEMPLATE_CODED_VITAL_SIGNS)))
			return Arrays.asList(CdaHandlerOids.ENT_TEMPLATE_VITAL_SIGNS_ORGANIZER);
		return null;
    }

	/**
	 * Get the expected code
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor#getExpectedSectionCode(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section)
	 */
	@Override
    protected CE<String> getExpectedSectionCode(Section section) {
		return new CE<String>("8716-3", CdaHandlerOids.CODE_SYSTEM_LOINC, "LOINC", null, "VITAL SIGNS", null);
    }
	
	
	
}
