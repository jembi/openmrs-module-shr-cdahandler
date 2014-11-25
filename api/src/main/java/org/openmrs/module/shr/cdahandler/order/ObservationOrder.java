package org.openmrs.module.shr.cdahandler.order;

import org.openmrs.Concept;
import org.openmrs.Order;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsMetadataUtil;

/**
 * Represents a request to perform an observation
 */
public class ObservationOrder extends Order {

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
	
    // Openmrs meta-data util
	private final OpenmrsMetadataUtil s_metaDataUtil = OpenmrsMetadataUtil.getInstance();
	
	// Goal values
	private String goalText;
	private Double goalNumeric;
	private Concept goalCoded;
	
	// Method and target 
	private Concept method;
	private Concept targetSite;
	
	/**
	 * Observation order
	 */
	public ObservationOrder()
	{
		this.setOrderType(s_metaDataUtil.getOrCreateObservationOrderType());
	}

	/**
	 * Gets the textual description of the goal of this order
	 */
    public String getGoalText() {
    	return goalText;
    }

    /**
     * Sets a value indicating the goal as a form of text 
     */
    public void setGoalText(String goalText) {
    	this.goalText = goalText;
    }

	/**
	 * Gets a numeric value representing the goal of this order
	 */
    public Double getGoalNumeric() {
    	return goalNumeric;
    }

	/**
	 * Sets the numeric value representing the goal of the order
	 */
    public void setGoalNumeric(Double goalNumeric) {
    	this.goalNumeric = goalNumeric;
    }

    /**
     * Gets the codified value of the goal 
     */
    public Concept getGoalCoded() {
    	return goalCoded;
    }

    /**
     * Sets the codified value of the goal
     */
    public void setGoalCoded(Concept goalCoded) {
    	this.goalCoded = goalCoded;
    }

    /**
     * Gets a concept identifying the method of observation
     */
    public Concept getMethod() {
    	return method;
    }

    /**
     * Sets a concept identifying the method of observation
     */
    public void setMethod(Concept method) {
    	this.method = method;
    }

    /**
     * Gets a concept identifying the target site of the observation. Eg. Face, Neck, etc
     */
    public Concept getTargetSite() {
    	return targetSite;
    }

	/**
	 * Sets a concept identifying the target site of the observation
	 */
    public void setTargetSite(Concept targetSite) {
    	this.targetSite = targetSite;
    }

}
