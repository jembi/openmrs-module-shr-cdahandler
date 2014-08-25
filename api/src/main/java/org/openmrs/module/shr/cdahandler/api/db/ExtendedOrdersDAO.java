package org.openmrs.module.shr.cdahandler.api.db;

import java.util.List;

import org.openmrs.Patient;
import org.openmrs.module.shr.cdahandler.order.ObservationOrder;
import org.openmrs.module.shr.cdahandler.order.ProcedureOrder;

/**
 * Represents a DAO for extended CDA properties
 */
public interface ExtendedOrdersDAO {

	/**
	 * Get procedure orders for a patient
	 */
	List<ProcedureOrder> getProcedureOrdersForPatient(Patient patient);

	/**
	 * Get observation orders for a patient
	 */
	List<ObservationOrder> getObservationOrdersForPatient(Patient patient);
	
}
