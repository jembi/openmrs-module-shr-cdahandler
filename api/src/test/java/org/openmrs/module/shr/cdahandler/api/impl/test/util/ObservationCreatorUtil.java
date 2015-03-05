package org.openmrs.module.shr.cdahandler.api.impl.test.util;

import java.math.BigDecimal;
import java.util.UUID;

import groovy.xml.Entity;

import org.marc.everest.datatypes.*;
import org.marc.everest.datatypes.generic.*;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.EntryRelationship;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Observation;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ActStatus;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ObservationInterpretation;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActMoodDocumentObservation;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActRelationshipEntryRelationship;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;

/**
 * Creates observations
 */
public class ObservationCreatorUtil {

	/**
	 * Create an observation
	 */
	private static final Observation createObservation(x_ActMoodDocumentObservation mood, String code, String codeSystem, String... templateIds)
	{

		Observation retVal = new Observation(mood);
		retVal.setCode(code, codeSystem);
		retVal.setTemplateId(new LIST<II>());
		for(String id : templateIds)
			retVal.getTemplateId().add(new II(id));
		retVal.setEffectiveTime(TS.now());
		retVal.setId(SET.createSET(new II(UUID.randomUUID())));
		return retVal;
	}
	
	/**
	 * Observation creator utility
	 */
	public static final Observation createEstimatedDeliveryDateObservation() {
		
		Observation retVal = createObservation(x_ActMoodDocumentObservation.Eventoccurrence, "11778-8", CdaHandlerConstants.CODE_SYSTEM_LOINC, "1.3.6.1.4.1.19376.1.5.3.1.4.13", "1.3.6.1.4.1.19376.1.5.3.1.1.11.2.3.1");
		TS estimatedDeliveryDate = TS.now();
		estimatedDeliveryDate.add(new PQ(BigDecimal.valueOf(5), "mo"));
		TS lmp = TS.now();
		lmp.subtract(new PQ(BigDecimal.valueOf(5), "mo"));
		
		retVal.setStatusCode(ActStatus.Completed);
		retVal.setEffectiveTime(TS.now());
		retVal.getAuthor().add(EntityCreatorUtil.createAuthorLimited("1"));
		retVal.setId(SET.createSET(new II(UUID.randomUUID())));
		retVal.setValue(estimatedDeliveryDate);
		
		
		// Entry relationship for support
		Observation support = createSimpleObservation("11779-6", CdaHandlerConstants.CODE_SYSTEM_LOINC, lmp);
		EntryRelationship erSupport = new EntryRelationship(x_ActRelationshipEntryRelationship.SPRT, BL.TRUE);
		support.setInterpretationCode(null);
		erSupport.setClinicalStatement(support);
		
		// Support 2 
		EntryRelationship erSupport2 = new EntryRelationship(x_ActRelationshipEntryRelationship.SPRT, BL.TRUE);
		Observation support2 = createSimpleObservation("11888-5", CdaHandlerConstants.CODE_SYSTEM_LOINC, new PQ(BigDecimal.valueOf(20), "wk"));
		support2.setInterpretationCode(null);
		erSupport2.setClinicalStatement(support2);
		
		// Link
		support.getEntryRelationship().add(erSupport2);
		retVal.getEntryRelationship().add(erSupport);
		
		return retVal;
    }

	/**
	 * Create simple observation
	 */
	public static Observation createSimpleObservation(String code, String codeSystem, ANY value) {
		Observation retVal = createObservation(x_ActMoodDocumentObservation.Eventoccurrence, code, codeSystem, "1.3.6.1.4.1.19376.1.5.3.1.4.13");
		retVal.setStatusCode(ActStatus.Completed);
		retVal.setEffectiveTime(TS.now());
		retVal.setInterpretationCode(SET.createSET(new CE<ObservationInterpretation>(ObservationInterpretation.Normal)));
		retVal.setValue(value);
		return retVal;
    }
	
}
