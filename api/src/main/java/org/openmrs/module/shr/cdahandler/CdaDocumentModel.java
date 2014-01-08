package org.openmrs.module.shr.cdahandler;

import java.io.InputStream;
import java.io.Serializable;

import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;


public class CdaDocumentModel extends BaseOpenmrsObject implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Integer id;

	private ClinicalDocument clinicalDocument;

	private String documentType;
	
	
	@Override
	public Integer getId() {
		return id;
	}
	
	@Override
	public void setId(Integer id) {
		this.id = id;
	}
	
	public CdaDocumentModel(ClinicalDocument clinicalDocument) throws DocumentParseException{
		this.clinicalDocument = clinicalDocument;
	}
	
    public ClinicalDocument getClinicalDocument() {
    	return clinicalDocument;
    }

    public void setClinicalDocument(ClinicalDocument clinicalDocument) {
    	this.clinicalDocument = clinicalDocument;
    }
    
    public String getDocumentType() {
    	return documentType;
    }

	
    public void setDocumentType(String documentType) {
    	this.documentType = documentType;
    }
    
}
