package org.openmrs.module.shr.cdahandler.everest.sdtc;

import org.marc.everest.annotations.ConformanceType;
import org.marc.everest.annotations.Property;
import org.marc.everest.annotations.PropertyType;
import org.marc.everest.annotations.Structure;
import org.marc.everest.annotations.StructureType;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.generic.SET;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.SubjectPerson;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;

/**
 * Represents a subject person with the SDTC ID element
 */
@Structure(name="SubjectPerson", model = "POCD_MT000040", publisher = "HL7 International", structureType = StructureType.MESSAGETYPE)
public class SdtcSubjectPerson extends SubjectPerson {

	// The id of the playing entity.
	private SET<II> m_id;
	
	/**
	 * Gets the identifier of the entity
	 */
	@Property(name = "id", namespaceUri = CdaHandlerConstants.NS_SDTC, propertyType = PropertyType.NONSTRUCTURAL, conformance = ConformanceType.REQUIRED, minOccurs = 0, maxOccurs = -1)
	public SET<II> getId() { return this.m_id; }
	/**
	 * Sets the identifiers of the entity
	 */
	public void setId(SET<II> value) { this.m_id = value; }
		
}
