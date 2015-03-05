/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.shr.cdahandler.exception;

/**
 * An exception that indicates that the document import operation failed
 */
public class DocumentImportException extends Exception {

	private static final long serialVersionUID = -2337423245351560127L;

    /**
     * Constructs a new instance of the DocumentImportException class
     */
    public DocumentImportException() {}
    /**
     * Constructs a new instance of the DocumentImportException class with
     * the specified message
     * 
     * @param msg The exception message
     */
	public DocumentImportException(String msg) { super(msg); }
    /**
     * Constructs a new instance of the DocumentImportException class with 
     * the specified message and cause
     * 
     * @param msg The exception message
     * @param ex The cause of the exception
     */
	public DocumentImportException(String msg, Throwable ex) {
		super(msg, ex);
	}
    /**
     * Constructs a new instance of the DocumentImportException class with
     * the specified cause.
     * 
     * @param ex The cause of this exception
     */
	public DocumentImportException(Throwable ex) { super(ex); }

}
