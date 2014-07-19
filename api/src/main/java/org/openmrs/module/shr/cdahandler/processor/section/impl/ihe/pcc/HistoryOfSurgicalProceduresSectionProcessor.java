package org.openmrs.module.shr.cdahandler.processor.section.impl.ihe.pcc;

import org.marc.everest.datatypes.generic.CE;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.section.impl.GenericLevel2SectionProcessor;

/**
 * A History of Surgical Procedures Section processor
 * 
 * From PCC:
 * The History of Surgical Procedures Section shall contain a narrative description of the surgical procedures performed on the patient.
 * 
 * See: PCC TF-2 CDA Suppl:6.3.3.2.44
 */
@ProcessTemplates(
	templateIds = {
			CdaHandlerConstants.SCT_TEMPLATE_HISTORY_OF_SURGICAL_PROCEDURES
	})
public class HistoryOfSurgicalProceduresSectionProcessor extends GenericLevel2SectionProcessor {

	/**
	 * Gets the name of the template
	 */
	@Override
    public String getTemplateName() {
		return "History of Surgical Procedures";
    }

	/**
	 * Gets the expected code for the section 
	 */
	@Override
    public CE<String> getExpectedSectionCode() {
		return new CE<String>("10167-5", CdaHandlerConstants.CODE_SYSTEM_LOINC, CdaHandlerConstants.CODE_SYSTEM_NAME_LOINC, null, "HISTORY OF SURGICAL PROCEDURES", null);
    }
	
	
}
