package org.openmrs.module.shr.cdahandler.api;

public class DocumentParseException extends Exception {

	private static final long serialVersionUID = -2337423245351560127L;

	public DocumentParseException() {}
	public DocumentParseException(String msg) { super(msg); }
	public DocumentParseException(Throwable ex) { super(ex); }
}
