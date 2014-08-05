package org.openmrs.module.shr.cdahandler.exception;

import org.marc.everest.interfaces.IGraphable;

/**
 * Represents a document import exception caused by an issue with validation
 */
public class DocumentValidationException extends DocumentImportException {

	// Serial identifier
    private static final long serialVersionUID = -4052731832891085407L;
    
    // List of issues
    private final ValidationIssueCollection m_issues;
    private final IGraphable m_target;
    
    /**
     * Constructs a new instance of the DocumentValidationException class
     */
    public DocumentValidationException(IGraphable target, ValidationIssueCollection issues) {
    	this.m_issues = issues;
    	this.m_target = target;
    }
    /**
     * Constructs a new instance of the DocumentValidationException class with
     * the specified message
     * 
     * @param msg The exception message
     */
	public DocumentValidationException(String msg, IGraphable target, ValidationIssueCollection issues) { 
		super(msg); 
    	this.m_issues = issues;
    	this.m_target = target;
	}
    /**
     * Constructs a new instance of the DocumentValidationException class with 
     * the specified message and cause
     * 
     * @param msg The exception message
     * @param ex The cause of the exception
     */
	public DocumentValidationException(String msg, Throwable ex, IGraphable target, ValidationIssueCollection issues) {
		super(msg, ex);
    	this.m_issues = issues;
    	this.m_target = target;
	}
    /**
     * Constructs a new instance of the DocumentValidationException class with
     * the specified cause.
     * 
     * @param ex The cause of this exception
     */
	public DocumentValidationException(Throwable ex, IGraphable target, ValidationIssueCollection issues) { 
		super(ex); 
    	this.m_issues = issues;
    	this.m_target = target;
	}

	/**
	 * Gets the target object that failed validation
	 */
	public IGraphable getTarget()
	{
		return this.m_target;
	}
	
	/**
	 * Gets the validation issues which caused this exception to be thrown
	 */
	public ValidationIssueCollection getValidationIssues()
	{
		return this.m_issues;
	}

}
