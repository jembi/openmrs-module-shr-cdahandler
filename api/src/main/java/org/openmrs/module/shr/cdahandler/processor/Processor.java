package org.openmrs.module.shr.cdahandler.processor;

import org.openmrs.module.shr.cdahandler.CdaDocumentModel;



public abstract class Processor {
	
	public abstract String getDocumentType();
	public  abstract CdaDocumentModel process(CdaDocumentModel cdaDocumentModel);
	
}
