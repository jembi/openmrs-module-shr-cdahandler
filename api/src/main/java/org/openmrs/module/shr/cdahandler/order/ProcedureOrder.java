package org.openmrs.module.shr.cdahandler.order;

import org.openmrs.Concept;
import org.openmrs.Order;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsMetadataUtil;

/**
 * Represents an order for a procedure
 */
public class ProcedureOrder extends Order {

	// Get the meta-data util
	private final OpenmrsMetadataUtil s_metaDataUtil = OpenmrsMetadataUtil.getInstance();
	
	/**
     * Procedure order
     */
    private static final long serialVersionUID = 1L;

    // Status
    private Concept status;
    // Target site
    private Concept targetSite; 
    // Approach site
    private Concept approachSite;
    
    /**
     * Creates a new instance of the ProcedureOrder
     */
    public ProcedureOrder()
    {
    	this.setOrderType(this.s_metaDataUtil.getOrCreateProcedureOrderType());
    }

	/**
	 * Get the status code
	 * @return
	 */
    public Concept getStatus() {
    	return status;
    }

    /**
     * Set the status code
     */
    public void setStatus(Concept status) {
    	this.status = status;
    }

    /**
     * Gets a value indicating the target of the procedure
     */
    public Concept getTargetSite() {
    	return targetSite;
    }

    /**
     * Sets a value indicating the target of the procedure
     */
    public void setTargetSite(Concept targetSite) {
    	this.targetSite = targetSite;
    }

    /**
     * Gets a value indicating how to approach of procedure 
     */
    public Concept getApproachSite() {
    	return approachSite;
    }

    /**
     * Set the approach site code
     */
    public void setApproachSite(Concept approachSite) {
    	this.approachSite = approachSite;
    }
    
}
