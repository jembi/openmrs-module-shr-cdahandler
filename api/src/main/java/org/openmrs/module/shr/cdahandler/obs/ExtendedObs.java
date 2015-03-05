package org.openmrs.module.shr.cdahandler.obs;

import java.util.Date;

import org.marc.everest.datatypes.TS;
import org.openmrs.Concept;
import org.openmrs.Obs;

/**
 * Represents an extended observation
 */
public class ExtendedObs extends Obs {
	
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
	// Represents the precision of the date of the observation
	private int obsDatePrecision = TS.FULL;
	// Represents the lowest point in time the observation took place
	private Date obsStartDate;
	// Represents when the observation ended
	private Date obsEndDate;
	// Interpretation
	private Concept obsInterpretation;
	// Indicates the mood for the obs
	private Concept obsMood;
	// Indicates the repeat number of the obs
	private Integer obsRepeatNumber;
	// Status of the obs
	private Concept obsStatus;
    
    
    /**
     * @return the obsStatus
     */
    public Concept getObsStatus() {
    	return obsStatus;
    }



	
    /**
     * @param obsStatus the obsStatus to set
     */
    public void setObsStatus(Concept obsStatus) {
    	this.obsStatus = obsStatus;
    }



	/**
     * @return the obsRepeatNumber
     */
    public Integer getObsRepeatNumber() {
    	return obsRepeatNumber;
    }


	
    /**
     * @param obsRepeatNumber the obsRepeatNumber to set
     */
    public void setObsRepeatNumber(Integer obsRepeatNumber) {
    	this.obsRepeatNumber = obsRepeatNumber;
    }


	/**
     * @return the obsMood
     */
    public Concept getObsMood() {
    	return obsMood;
    }

	
    /**
     * @param obsMood the obsMood to set
     */
    public void setObsMood(Concept obsMood) {
    	this.obsMood = obsMood;
    }

	/**
     * @return the obsDatePrecision
     */
    public int getObsDatePrecision() {
    	return obsDatePrecision;
    }
	
    /**
     * @param obsDatePrecision the obsDatePrecision to set
     */
    public void setObsDatePrecision(int obsDatePrecision) {
    	this.obsDatePrecision = obsDatePrecision;
    }
	
    /**
     * @return the obsStartDate
     */
    public Date getObsStartDate() {
    	return obsStartDate;
    }
	
    /**
     * @param obsStartDate the obsStartDate to set
     */
    public void setObsStartDate(Date obsStartDate) {
    	this.obsStartDate = obsStartDate;
    }
	
    /**
     * @return the obsEndDate
     */
    public Date getObsEndDate() {
    	return obsEndDate;
    }
	
    /**
     * @param obsEndDate the obsEndDate to set
     */
    public void setObsEndDate(Date obsEndDate) {
    	this.obsEndDate = obsEndDate;
    }
	
    /**
     * @return the obsInterpretation
     */
    public Concept getObsInterpretation() {
    	return obsInterpretation;
    }
	
    /**
     * @param obsInterpretation the obsInterpretation to set
     */
    public void setObsInterpretation(Concept obsInterpretation) {
    	this.obsInterpretation = obsInterpretation;
    }

	
	
}
