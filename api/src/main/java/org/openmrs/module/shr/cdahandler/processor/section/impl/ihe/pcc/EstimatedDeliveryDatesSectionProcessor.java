package org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor;

/**
 * Estimated Delivery Dates Section Processor
 * 
 * From PCC Suppl:
 * This section contains the physician’s best estimate of the patients due date. This is generally done both on an initial evaluation, and later confirmed at 18-20 weeks. The date is supported by evidence such as the patient’s history of last menstrual period, a physical examination, or ultrasound measurements. 
 * 
 * See: PCC CDA Suppl: 6.3.3.2.28
 */
@ProcessTemplates(templateIds = { CdaHandlerConstants.SCT_TEMPLATE_ESTIMATED_DELIVERY_DATES})
public class EstimatedDeliveryDatesSectionProcessor extends GenericLevel3SectionProcessor {
	
	
	/**
	 * Get the expected entries
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel3SectionProcessor#getExpectedEntries(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section)
	 */
	@Override
	protected List<String> getExpectedEntries(Section section) {
		return Arrays.asList(CdaHandlerConstants.ENT_TEMPLATE_DELIVERY_DATE_OBSERVATION);
	}

	/**
	 * Get the expected section code
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#getExpectedSectionCode()
	 */
	@Override
    public CE<String> getExpectedSectionCode() {
		return new CE<String>("57060-6", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "Estimated date of delivery", null);
    }

	/**
	 * Get template name
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#getTemplateName()
	 */
	@Override
    public String getTemplateName() {
		return "Estimated Delivery Date";
    }
	
}
