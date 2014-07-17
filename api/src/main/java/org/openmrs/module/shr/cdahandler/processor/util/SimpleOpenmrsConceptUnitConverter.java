package org.openmrs.module.shr.cdahandler.processor.util;

import java.math.BigDecimal;
import java.util.Arrays;

import org.marc.everest.datatypes.PQ;
import org.marc.everest.datatypes.interfaces.IUnitConverter;

/**
 * Represents a unit converter that can convert some common CDA units to OpenMRS
 */
public class SimpleOpenmrsConceptUnitConverter implements IUnitConverter {
	
	/**
	 * Determine the conversion algorithm
	 * @see org.marc.everest.datatypes.interfaces.IUnitConverter#canConvert(org.marc.everest.datatypes.PQ, java.lang.String)
	 */
	@Override
	public boolean canConvert(PQ value, String destinationUnit) {

		return value != null && !value.isNull() && value.getValue() != null && value.getUnit() != null &&
				(
				value.getValue().equals("mm[Hg]") && destinationUnit.equals("mmHg") ||
				value.getValue().equals("[degF]") && destinationUnit.equals("cel") ||
				value.getValue().equals("[lb_av]") && destinationUnit.equals("kg") ||
				value.getValue().equals("[oz_av]") && destinationUnit.equals("kg")
				);
		
	}

	/**
	 * Convert
	 * @see org.marc.everest.datatypes.interfaces.IUnitConverter#convert(org.marc.everest.datatypes.PQ, java.lang.String)
	 */
	@Override
	public PQ convert(PQ value, String destinationUnit) {
		if(value.getValue().equals("mm[Hg]") && destinationUnit.equals("mmHg"))
			return new PQ(value.getValue(), "mmHg"); // this is just a correction
		else if(value.getValue().equals("[degF]") && destinationUnit.equals("cel"))
			return new PQ(BigDecimal.valueOf((value.getValue().doubleValue() - 32) / 2.0f), "cel");
		else if(value.getValue().equals("[lb_av]") && destinationUnit.equals("kg"))
			return new PQ(BigDecimal.valueOf((value.getValue().doubleValue() / 2.2)), "kg");
		else if(value.getValue().equals("[oz_av]") && destinationUnit.equals("kg"))
			return new PQ(BigDecimal.valueOf((value.getValue().doubleValue() * 16 / 2.2)), "kg");
		else 
			throw new IllegalStateException("Can't convert units");
	}
	
	
	
}
