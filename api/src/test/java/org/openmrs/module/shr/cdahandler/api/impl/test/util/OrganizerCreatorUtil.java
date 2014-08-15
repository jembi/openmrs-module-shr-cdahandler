package org.openmrs.module.shr.cdahandler.api.impl.test.util;

import java.math.BigDecimal;

import org.marc.everest.datatypes.BL;
import org.marc.everest.datatypes.CO;
import org.marc.everest.datatypes.ED;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.PQ;
import org.marc.everest.datatypes.TS;
import org.marc.everest.datatypes.generic.CD;
import org.marc.everest.datatypes.generic.LIST;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Component4;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Organizer;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ActRelationshipHasComponent;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ActStatus;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActClassDocumentEntryOrganizer;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;

/**
 * Util for creating organizers
 */
public final class OrganizerCreatorUtil {

	/**
	 * Create an observation
	 */
	private static final Organizer createOrganizer(x_ActClassDocumentEntryOrganizer classCode, String code, String codeSystem, String... templateIds)
	{

		Organizer retVal = new Organizer(classCode);
		retVal.setCode(code, codeSystem);
		retVal.setTemplateId(new LIST<II>());
		for(String id : templateIds)
			retVal.getTemplateId().add(new II(id));
		retVal.setEffectiveTime(TS.now());
		retVal.setStatusCode(ActStatus.Completed);
		return retVal;
	}
	
	/**
	 * Create a visit flowsheet organizer
	 */
	public static Organizer createVisitFlowsheetOrganizer() {
		
		Organizer retVal = createOrganizer(x_ActClassDocumentEntryOrganizer.BATTERY, "57061-4", CdaHandlerConstants.CODE_SYSTEM_LOINC, "1.3.6.1.4.1.19376.1.5.3.1.1.11.2.3.2");
		
		// Add some simple observations
		
		// Fetal weight estimate
		retVal.getComponent().add(new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, ObservationCreatorUtil.createSimpleObservation("57067-1", CdaHandlerConstants.CODE_SYSTEM_LOINC, new PQ(BigDecimal.valueOf(900), "g"))));
		// Fetal Presentation
		retVal.getComponent().add(new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, ObservationCreatorUtil.createSimpleObservation("11876-0", CdaHandlerConstants.CODE_SYSTEM_LOINC, new CD<String>("21882006", CdaHandlerConstants.CODE_SYSTEM_SNOMED))));
		// Fetal heartrate
		retVal.getComponent().add(new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, ObservationCreatorUtil.createSimpleObservation("11948-7", CdaHandlerConstants.CODE_SYSTEM_LOINC, new PQ(BigDecimal.valueOf(80), "/min"))));
		// Fetal movement
		retVal.getComponent().add(new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, ObservationCreatorUtil.createSimpleObservation("57088-7", CdaHandlerConstants.CODE_SYSTEM_LOINC, new CO(new CD<String>("364755008", CdaHandlerConstants.CODE_SYSTEM_SNOMED)))));
		// Preterm Labour symptoms
		retVal.getComponent().add(new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, ObservationCreatorUtil.createSimpleObservation("57069-7", CdaHandlerConstants.CODE_SYSTEM_LOINC, BL.FALSE)));
		// Systolic 
		retVal.getComponent().add(new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, ObservationCreatorUtil.createSimpleObservation("8480-6", CdaHandlerConstants.CODE_SYSTEM_LOINC, new PQ(BigDecimal.valueOf(120), "mm[Hg]"))));
		// Diastolic 
		retVal.getComponent().add(new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, ObservationCreatorUtil.createSimpleObservation("8462-4", CdaHandlerConstants.CODE_SYSTEM_LOINC, new PQ(BigDecimal.valueOf(80), "mm[Hg]"))));
		// Glucose
		retVal.getComponent().add(new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, ObservationCreatorUtil.createSimpleObservation("1753-3", CdaHandlerConstants.CODE_SYSTEM_LOINC, new CO(new CD<String>("167275009", CdaHandlerConstants.CODE_SYSTEM_SNOMED)))));
		// Pain
		retVal.getComponent().add(new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, ObservationCreatorUtil.createSimpleObservation("38208-5", CdaHandlerConstants.CODE_SYSTEM_LOINC, new CO(BigDecimal.valueOf(2)))));

		
		return retVal;
    }

	/**
	 * Create an antenatal testing and surveillance battery entry
	 */
	public static Organizer createAntenatalTestingAndSurveillanceOrganizer() {
		Organizer retVal = createOrganizer(x_ActClassDocumentEntryOrganizer.BATTERY, "XX-ANTENATALTESTINGBATTERY", CdaHandlerConstants.CODE_SYSTEM_LOINC, "1.3.6.1.4.1.19376.1.5.3.1.1.21.3.10");
		
		// Add observations
		retVal.getComponent().add(new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, ObservationCreatorUtil.createSimpleObservation("11630-1", CdaHandlerConstants.CODE_SYSTEM_LOINC, new ED("This is a string"))));
		// Add observations
		retVal.getComponent().add(new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, ObservationCreatorUtil.createSimpleObservation("11631-9", CdaHandlerConstants.CODE_SYSTEM_LOINC, new ED("This is a string"))));
		// Add observations
		retVal.getComponent().add(new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, ObservationCreatorUtil.createSimpleObservation("35096-7", CdaHandlerConstants.CODE_SYSTEM_LOINC, new ED("This is a string"))));
		// Add observations
		retVal.getComponent().add(new Component4(ActRelationshipHasComponent.HasComponent, BL.TRUE, ObservationCreatorUtil.createSimpleObservation("57659-1", CdaHandlerConstants.CODE_SYSTEM_LOINC, new ED("This is a string"))));
		
		return retVal;
    }
	
	
	
}
