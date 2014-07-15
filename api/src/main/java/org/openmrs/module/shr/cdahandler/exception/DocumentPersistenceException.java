package org.openmrs.module.shr.cdahandler.exception;

/**
 * Represents a document import exception caused by a problem with the persistence
 */
public class DocumentPersistenceException extends DocumentImportException {

	/**
     * 
     */
    private static final long serialVersionUID = 2400768521213675724L;
	
    /**
     * Constructs a new instance of the DocumentValidationException class
     */
    public DocumentPersistenceException() {}
    /**
     * Constructs a new instance of the DocumentPersistenceException class with
     * the specified message
     * 
     * @param msg The exception message
     */
	public DocumentPersistenceException(String msg) { super(msg); }
    /**
     * Constructs a new instance of the DocumentPersistenceException class with
     * the specified cause.
     * 
     * @param ex The cause of this exception
     */
	public DocumentPersistenceException(Throwable ex) { super(ex); }
    /**
     * Constructs a new instance of the DocumentPersistenceException class with 
     * the specified message and cause
     * 
     * @param msg The exception message
     * @param ex The cause of the exception
     */
	public DocumentPersistenceException(String msg, Throwable ex) {
		super(msg, ex);
	}
	
	
	
}
