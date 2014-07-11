package org.openmrs.module.shr.cdahandler.processor.document.impl;

import java.math.BigDecimal;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.marc.everest.datatypes.PQ;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Author;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Informant12;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Performer1;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ServiceEvent;
import org.openmrs.EncounterRole;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;
import org.openmrs.module.shr.cdahandler.processor.context.DocumentProcessorContext;
import org.openmrs.module.shr.cdahandler.processor.context.ProcessorContext;
import org.openmrs.module.shr.cdahandler.processor.document.DocumentProcessor;
import org.openmrs.module.shr.cdahandler.processor.util.AssignedEntityProcessorUtil;
import org.openmrs.module.shr.cdahandler.processor.util.DatatypeProcessorUtil;
import org.openmrs.module.shr.cdahandler.processor.util.LocationOrganizationProcessorUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsMetadataUtil;
import org.openmrs.module.shr.cdahandler.processor.util.PatientRoleProcessorUtil;

/**
 * Represents a basic implementation of a document processor
 * @author Justin Fyfe
 *
 */
public abstract class DocumentProcessorImpl implements DocumentProcessor {

	// Log
	protected final Log log = LogFactory.getLog(this.getClass());
	
	// The context within which this parser is operating
	protected ProcessorContext m_context;
	
	/**
	 * Gets the context within which this processor runs (if any)
	 */
	@Override
	public ProcessorContext getContext() {
		return this.m_context;
	}

	/**
	 * Sets the context within which this processor runs
	 */
	@Override
	public void setContext(ProcessorContext context) {
		this.m_context = context;
	}
	
	/**
	 * Get the template name that this processor handles
	 */
	public abstract String getTemplateName();

	/**
	 * Processes ClinicalDocument into an openMRS visit saving it into the 
	 * database.
	 * This implementation will parse the header elements into a visit
	 * This operation should be threadsafe
	 */
	@Override
	public abstract Visit process(ClinicalDocument doc) throws DocumentParseException;

	/**
	 * Parse the header of the CDA document
	 * @param doc The document to be parsed
	 * @return The parser context which resulted in the document being parsed.
	 * @throws DocumentException 
	 */
	protected DocumentProcessorContext parseHeader(ClinicalDocument doc) throws DocumentParseException
	{
		// Don't parse null elements
		if(doc == null || doc.getNullFlavor() != null)
			return null; // TODO: Should this be something more descriptive of what happened?

		// Perform any additional validation over and above the regular validation
		if(!this.validate(doc))
			throw new DocumentParseException("This document is not valid according to its template");
		
		Visit visitInformation = new Visit();
		
		// Processor instances (makes the code look cleaner)
		AssignedEntityProcessorUtil assignedEntityProcessorUtil = AssignedEntityProcessorUtil.getInstance();
		PatientRoleProcessorUtil patientRoleProcessorUtil = PatientRoleProcessorUtil.getInstance();
		DatatypeProcessorUtil datatypeProcessorUtil = DatatypeProcessorUtil.getInstance();
		OpenmrsMetadataUtil openmrsMetadataUtil = OpenmrsMetadataUtil.getInstance();
		LocationOrganizationProcessorUtil locationOrganizationProcessorUtil = LocationOrganizationProcessorUtil.getInstance();
		
		// Create a context for this parse operation
		DocumentProcessorContext parseContext = new DocumentProcessorContext(doc, visitInformation, this);
		
		// TODO: How to add an attribute which points to the CDA from which the visit was constructed
		
		// Parse the header
		if(doc.getRecordTarget().size() != 1)
			throw new DocumentParseException("Can only handle documents with exactly one patient");
		visitInformation.setPatient(patientRoleProcessorUtil.parsePatient(doc.getRecordTarget().get(0).getPatientRole()));

		// TODO: Document code
		
		// Parse the authors of this document
		for(Author aut : doc.getAuthor())
		{
			// TODO: Figure out where to stuff aut.getTime() .
			// This element represents the time that the author started participating in the creation of the clinical document .. Is it important?
			Provider provider = assignedEntityProcessorUtil.parseProvider(aut.getAssignedAuthor());
			EncounterRole role = openmrsMetadataUtil.getEncounterRole(aut.getTypeCode());
			parseContext.addProvider(role, provider);
		}

		// TODO: Authorization
		
		// TODO: Confidentiality (discussion about privacy enforcement)
		if(doc.getConfidentialityCode() != null && !doc.getConfidentialityCode().isNull())
		{
			VisitAttribute confidentiality = new VisitAttribute();
			confidentiality.setAttributeType(openmrsMetadataUtil.getVisitConfidentialityCodeAttributeType());
			confidentiality.setValueReferenceInternal(datatypeProcessorUtil.formatSimpleCode(doc.getConfidentialityCode()));
			visitInformation.addAttribute(confidentiality);
		}
		
	
		// Custodian - Approximately the location where the event or original data is store
		// TODO: Provenance data, do we need to store this?
		if(doc.getCustodian() != null && doc.getCustodian().getNullFlavor() == null &&
				doc.getCustodian().getAssignedCustodian() != null && doc.getCustodian().getAssignedCustodian().getNullFlavor() == null)
		{
			visitInformation.setLocation(locationOrganizationProcessorUtil.parseOrganization(doc.getCustodian().getAssignedCustodian().getRepresentedCustodianOrganization()));
		}
		
		// TODO: DocumentationOf (perhaps as an encounter related to the Visit with an EncounterType of CDA Service Information)
		// DocumentationOf identifies the visit or service event that occurred 
		// Let's only process one service event, mapping it to the Visit with entries 
		if(doc.getDocumentationOf().size() == 1)
		{
			ServiceEvent serviceEvent = doc.getDocumentationOf().get(0).getServiceEvent();
			// Effective time should represent the start/stop time of the visit
			if(serviceEvent.getEffectiveTime() != null && !serviceEvent.getEffectiveTime().isNull())
			{ 
				// FROM
				if(serviceEvent.getEffectiveTime().getLow() != null && !serviceEvent.getEffectiveTime().getLow().isNull())
					visitInformation.setStartDatetime(serviceEvent.getEffectiveTime().getLow().getDateValue().getTime());
				// TO
				if(serviceEvent.getEffectiveTime().getHigh() != null && !serviceEvent.getEffectiveTime().getHigh().isNull())
					visitInformation.setStopDatetime(serviceEvent.getEffectiveTime().getHigh().getDateValue().getTime());
			}
			
			// Add performers with their function this is used when the provider is referenced elsewhere in the document
			for(Performer1 prf : serviceEvent.getPerformer())
			{
				Provider provider = assignedEntityProcessorUtil.parseProvider(prf.getAssignedEntity());
				EncounterRole role = openmrsMetadataUtil.getEncounterRole(prf.getFunctionCode());
				parseContext.addProvider(role, provider);
			}
		}
		else if(doc.getDocumentationOf().size() > 1)
			throw new NotImplementedException("OpenSHR cannot store more than one serviceEvent.. yet");
		
		// TODO: If no service is specified we have to specify that the date is the exact instant the visit started
		if(visitInformation.getStartDatetime() == null)
			visitInformation.setStartDatetime(doc.getEffectiveTime().getDateValue().getTime());
		if(visitInformation.getStopDatetime() == null)
			visitInformation.setStopDatetime(doc.getEffectiveTime().add(new PQ(BigDecimal.ONE, "s")).getDateValue().getTime());
		
		// Effective time
		visitInformation.setDateCreated(doc.getEffectiveTime().getDateValue().getTime());

		// Parse informants
		for(Informant12 inf : doc.getInformant())
		{
			if(inf.getInformantChoice().isPOCD_MT000040UVRelatedEntity())
				throw new NotImplementedException("OpenSHR cannot store informants of type related persons .. yet");
			else
			{
				Provider provider = assignedEntityProcessorUtil.parseProvider(inf.getInformantChoiceIfAssignedEntity());
				EncounterRole role = openmrsMetadataUtil.getEncounterRole(inf.getTypeCode());
				parseContext.addProvider(role, provider);
			}
		}

		// TODO: Information recipient : Do we need to store this?
		
		// Parse the legal authenticator
		if(doc.getLegalAuthenticator() != null)
		{
			// TODO: Where to store signature code? Or is it enough that this data would be stored in the provenance with the document?
			Provider provider = assignedEntityProcessorUtil.parseProvider(doc.getLegalAuthenticator().getAssignedEntity());
			EncounterRole role = openmrsMetadataUtil.getEncounterRole(doc.getLegalAuthenticator().getTypeCode());
			parseContext.addProvider(role, provider);
		}
		
		// Parse dataEnterer - the person that entered the data
		if(doc.getDataEnterer() != null)
		{
			// TODO: Where to store the time the data was entered?
			Provider provider = assignedEntityProcessorUtil.parseProvider(doc.getDataEnterer().getAssignedEntity());
			EncounterRole role = openmrsMetadataUtil.getEncounterRole(doc.getDataEnterer().getTypeCode());
			parseContext.addProvider(role, provider);
		}

		// ID? Some basic provenance data
		if(doc.getId() != null && !doc.getId().isNull())
		{
			VisitAttribute provenance = new VisitAttribute();
			provenance.setAttributeType(openmrsMetadataUtil.getVisitExternalIdAttributeType());
			provenance.setValueReferenceInternal(datatypeProcessorUtil.formatIdentifier(doc.getId()));
			visitInformation.addAttribute(provenance);
		}
		
		// Type of visit
		String visitTypeName = this.getTemplateName();
		if(visitTypeName == null)
		{
			if(doc.getCode() != null && !doc.getCode().isNull())
			{
				visitTypeName = doc.getCode().getDisplayName();
				if(visitTypeName == null)
					visitTypeName = doc.getCode().getCode();
			}
			else
				visitTypeName = doc.getTitle().toString();
		}
		visitInformation.setVisitType(openmrsMetadataUtil.getVisitType(visitTypeName));
		
		return parseContext;
	}
	
	/**
	 * Validate a clinical document instance
	 * @param doc The clinical document to be validated
	 * @return True if the parsing of the clinical document should continue
	 */
	public Boolean validate(IGraphable object)
	{
		return object instanceof ClinicalDocument;
	}
	
}
