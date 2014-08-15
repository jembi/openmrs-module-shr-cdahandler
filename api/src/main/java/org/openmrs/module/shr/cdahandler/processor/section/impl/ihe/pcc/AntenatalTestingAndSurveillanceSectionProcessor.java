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
 * Coded antenatal testing and surveillance processor
 * 
 * From PCC:
 * The Antenatal Testing and Surveillance section shall contain a narrative and coded description of reports and data from tests and surveillance performed during the pregnancy (e.g., Ultrasound, Biophysical Profile, Non-Stress Test, Contraction Stress Test). It shall contain an Antenatal Testing and 
 * 
 * See: PCC CDA Suppl. 6.3.3.5.7
 */
@ProcessTemplates(templateIds = {
		CdaHandlerConstants.SCT_TEMPLATE_CODED_ANTENATAL_TESTING_AND_SURVEILLANCE,
		CdaHandlerConstants.SCT_TEMPLATE_ANTENATAL_TESTING_AND_SURVEILLANCE
		})
public class AntenatalTestingAndSurveillanceSectionProcessor extends GenericLevel3SectionProcessor {

	/**
	 * Get the expected entries in the section
	 */
	@Override
	protected List<String> getExpectedEntries(Section section) {
		if(section.getTemplateId().contains(new II(CdaHandlerConstants.SCT_TEMPLATE_CODED_ANTENATAL_TESTING_AND_SURVEILLANCE)))
			return Arrays.asList(CdaHandlerConstants.ENT_TEMPLATE_ANTENATAL_TESTING_BATTERY);
		return null;
	}

	/**
	 * Get the expected section code 
	 */
	@Override
    public CE<String> getExpectedSectionCode() {
		return new CE<String>("57078-8", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "ANTENATAL TESTING AND SURVEILLANCE", null);
    }

	/**
	 * Gets the name of the template
	 * @see org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor#getTemplateName()
	 */
	@Override
    public String getTemplateName() {
		return "Coded Antenatal Testing & Surveillance";
    }
	
	
}
