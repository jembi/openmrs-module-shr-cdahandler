package org.openmrs.module.shr.cdahandler.api.impl.test.util;

import java.math.BigDecimal;

import org.marc.everest.datatypes.BL;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.PQ;
import org.marc.everest.datatypes.SD;
import org.marc.everest.datatypes.generic.LIST;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Component5;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Entry;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ActRelationshipHasComponent;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActRelationshipEntry;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;


public final class SectionCreatorUtil {

	/**
	 * Create section
	 */
	private static final Section createSection(String code, String... templateIds)
	{
		
		Section retVal = new Section();
		retVal.setCode(code, CdaHandlerConstants.CODE_SYSTEM_LOINC);
		retVal.setTemplateId(new LIST<II>());
		for(String id : templateIds)
			retVal.getTemplateId().add(new II(id));
		
		retVal.setText(new SD());
		retVal.getText().getContent().add(SD.createText("Some text"));
		retVal.setTitle("Automatically Generated");

		return retVal;
		
	}
	
	/**
	 * Create an antepartum surveillance section
	 */
	public static Section createCodedAntenatalTestingAndSurveillanceSection() {
		Section retVal = createSection("57078-8", "1.3.6.1.4.1.19376.1.5.3.1.1.21.2.5.1");
		retVal.getEntry().add(new Entry(x_ActRelationshipEntry.HasComponent, BL.TRUE, OrganizerCreatorUtil.createAntenatalTestingAndSurveillanceOrganizer()));
		return retVal;
	}
	/**
	 * Create estimated delivery dates section
	 * @return
	 */
	public static Section createEstimatedDeliveryDatesSection() {
		Section retVal = createSection("57060-6", "1.3.6.1.4.1.19376.1.5.3.1.1.11.2.2.1");
		
		retVal.getEntry().add(new Entry(x_ActRelationshipEntry.HasComponent, BL.TRUE, ObservationCreatorUtil.createEstimatedDeliveryDateObservation()));
		
		return retVal;
    }

	/**
	 * Create visit summary flowsheet section
	 */
	public static Section createVisitSummaryFlowsheetSection() {
		Section retVal = createSection("57059-8", "1.3.6.1.4.1.19376.1.5.3.1.1.11.2.2.2");
		retVal.getEntry().add(new Entry(x_ActRelationshipEntry.HasComponent, BL.TRUE, ObservationCreatorUtil.createSimpleObservation("8348-5", CdaHandlerConstants.CODE_SYSTEM_LOINC, new PQ(BigDecimal.valueOf(86), "kg"))));
		retVal.getEntry().add(new Entry(x_ActRelationshipEntry.HasComponent, BL.TRUE, OrganizerCreatorUtil.createVisitFlowsheetOrganizer()));
		return retVal;
    }
	
}
