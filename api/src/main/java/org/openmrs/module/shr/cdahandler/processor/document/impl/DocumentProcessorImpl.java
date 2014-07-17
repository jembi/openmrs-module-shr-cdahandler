package org.openmrs.module.shr.cdahandler.processor.document.impl;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.PQ;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.datatypes.interfaces.IPredicate;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Author;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Component3;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Informant12;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.NonXMLBody;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Participant1;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Performer1;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ServiceEvent;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.StructuredBody;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Obs;
import org.openmrs.Provider;
import org.openmrs.Relationship;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.exception.DocumentValidationException;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.context.DocumentProcessorContext;
import org.openmrs.module.shr.cdahandler.processor.context.ProcessorContext;
import org.openmrs.module.shr.cdahandler.processor.document.DocumentProcessor;
import org.openmrs.module.shr.cdahandler.processor.factory.impl.SectionProcessorFactory;
import org.openmrs.module.shr.cdahandler.processor.section.SectionProcessor;
import org.openmrs.module.shr.cdahandler.processor.util.AssignedEntityProcessorUtil;
import org.openmrs.module.shr.cdahandler.processor.util.DatatypeProcessorUtil;
import org.openmrs.module.shr.cdahandler.processor.util.LocationOrganizationProcessorUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsConceptUtil;
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsMetadataUtil;
import org.openmrs.module.shr.cdahandler.processor.util.PatientRoleProcessorUtil;
import org.openmrs.module.shr.cdahandler.processor.util.PersonProcessorUtil;
import org.openmrs.obs.ComplexData;

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
	 */
	@Override
	public Visit process(ClinicalDocument doc) throws DocumentImportException
	{
		// Perform any additional validation over and above the regular validation
		ValidationIssueCollection validationIssues = this.validate(doc);
		if(validationIssues.hasErrors())
			throw new DocumentValidationException("This document is not valid according to its template", doc, validationIssues);
				
		Visit visitInformation = this.processHeader(doc);
		
		// Encounters - This may be a level 1 document so we better check
		if(doc.getComponent().getBodyChoiceIfNonXMLBody() != null)
			visitInformation = this.processLevel1Content(doc, visitInformation);
		else // level 2 , just hand-off to a StructuredBodyDocumentProcessor
			visitInformation = this.processLevel2Content(doc, visitInformation);

		return visitInformation;
	}

	/**
	 * Parse structured body content
	 * @throws DocumentImportException 
	 */
	private Visit processLevel2Content(ClinicalDocument doc, Visit visitInformation) throws DocumentImportException {

		StructuredBody structuredBody = doc.getComponent().getBodyChoiceIfStructuredBody();
		SectionProcessorFactory factory = SectionProcessorFactory.getInstance();
		Encounter visitEncounter = visitInformation.getEncounters().iterator().next();

		// Add visit to context
		DocumentProcessorContext rootContext = new DocumentProcessorContext(doc, visitInformation, this);
		// Add encounter to context
		ProcessorContext childContext = new ProcessorContext(structuredBody, visitEncounter, this, rootContext);
		
		// Iterate through sections saving them
		for(Component3 comp : structuredBody.getComponent())
		{
			// empty section?
			if(comp == null || comp.getNullFlavor() != null ||
					comp.getSection() == null || comp.getSection().getNullFlavor() != null)
			{
				log.warn("Component is missing section. Skipping");
				continue;
			}
			
			Section section = comp.getSection();
			
			// TODO: Now process section
			SectionProcessor processor = factory.createProcessor(section);
			processor.setContext(childContext);
			processor.process(section);
			
		}
		
		return visitInformation;
    }

	/**
	 * Process content when the body choice is non-xml content
	 * Auto generated method comment
	 * 
	 * @param bodyChoiceIfNonXMLBody
	 * @throws DocumentImportException 
	 */
	private Visit processLevel1Content(ClinicalDocument doc, Visit visitInformation) throws DocumentImportException {
		

		// Get the body
		NonXMLBody bodyChoiceIfNonXMLBody = doc.getComponent().getBodyChoiceIfNonXMLBody();
		
		OpenmrsMetadataUtil openmrsMetaData = OpenmrsMetadataUtil.getInstance();
		OpenmrsConceptUtil openmrsConceptUtil = OpenmrsConceptUtil.getInstance();
		DatatypeProcessorUtil datatypeProcessorUtil = DatatypeProcessorUtil.getInstance();

		Encounter binaryContentEncounter = visitInformation.getEncounters().iterator().next();
		
		// Process contents
		Obs binaryContentObs = new Obs();
		binaryContentObs.setConcept(openmrsConceptUtil.getOrCreateRMIMConcept(openmrsMetaData.getLocalizedString("obs.document.text"), bodyChoiceIfNonXMLBody.getText()));
		binaryContentObs.setAccessionNumber(datatypeProcessorUtil.formatIdentifier(doc.getId()));
		binaryContentObs.setDateCreated(binaryContentEncounter.getDateCreated());
		binaryContentObs.setObsDatetime(visitInformation.getStartDatetime());
		binaryContentObs.setLocation(visitInformation.getLocation());
		binaryContentObs.setVoided(false);
		binaryContentObs.setPerson(visitInformation.getPatient());
		binaryContentObs.setEncounter(binaryContentEncounter);
		binaryContentObs.setObsDatetime(binaryContentEncounter.getDateCreated());

		// Set the binary content
		ByteArrayInputStream textStream = new ByteArrayInputStream(bodyChoiceIfNonXMLBody.getText().getData());
		ComplexData complexData = new ComplexData(UUID.randomUUID().toString() + ".bin", textStream);
		binaryContentObs.setComplexData(complexData);
		binaryContentEncounter.addObs(binaryContentObs);
		
		// Update encounter
		Context.getEncounterService().saveEncounter(binaryContentEncounter);
		
		return visitInformation;
    }

	/**
	 * Parse the header of the CDA document
	 * @param doc The document to be parsed
	 * @return The parser context which resulted in the document being parsed.
	 * @throws DocumentException 
	 */
	protected Visit processHeader(ClinicalDocument doc) throws DocumentImportException
	{
		// Don't parse null elements
		if(doc == null || doc.getNullFlavor() != null)
			return null; // TODO: Should this be something more descriptive of what happened?

		
		Visit visitInformation = new Visit();
		
		// Processor instances (makes the code look cleaner)
		AssignedEntityProcessorUtil assignedEntityProcessorUtil = AssignedEntityProcessorUtil.getInstance();
		PatientRoleProcessorUtil patientRoleProcessorUtil = PatientRoleProcessorUtil.getInstance();
		PersonProcessorUtil personProcessorUtil = PersonProcessorUtil.getInstance();
		DatatypeProcessorUtil datatypeProcessorUtil = DatatypeProcessorUtil.getInstance();
		OpenmrsMetadataUtil openmrsMetadataUtil = OpenmrsMetadataUtil.getInstance();
		LocationOrganizationProcessorUtil locationOrganizationProcessorUtil = LocationOrganizationProcessorUtil.getInstance();
		
		// Create a context for this parse operation
		//DocumentProcessorContext parseContext = new DocumentProcessorContext(doc, visitInformation, this);

		// Create an encounter for the of the document
		Encounter visitEncounter = new Encounter();

		// TODO: How to add an attribute which points to the CDA from which the visit was constructed
		
		// Parse the header
		if(doc.getRecordTarget().size() != 1)
			throw new DocumentImportException("Can only handle documents with exactly one patient");
		visitInformation.setPatient(patientRoleProcessorUtil.processPatient(doc.getRecordTarget().get(0).getPatientRole()));

		// TODO: Document code
		
		// Parse the authors of this document
		for(Author aut : doc.getAuthor())
		{
			// TODO: Figure out where to stuff aut.getTime() .
			// This element represents the time that the author started participating in the creation of the clinical document .. Is it important?
			Provider provider = assignedEntityProcessorUtil.processProvider(aut.getAssignedAuthor());
			EncounterRole role = openmrsMetadataUtil.getOrCreateEncounterRole(aut.getTypeCode());
			visitEncounter.addProvider(role, provider);
		}

		// TODO: Authorization & Participants
		// These are kind of visit attributes 
		//	Authorization = The authority under which the operation/action was done
		//	Participants = Spouses, fathers, etc. related to the record target
		
		// TODO: Confidentiality (discussion about privacy enforcement)
		if(doc.getConfidentialityCode() != null && !doc.getConfidentialityCode().isNull())
		{
			VisitAttribute confidentiality = new VisitAttribute();
			confidentiality.setAttributeType(openmrsMetadataUtil.getOrCreateVisitConfidentialityCodeAttributeType());
			confidentiality.setValueReferenceInternal(datatypeProcessorUtil.formatSimpleCode(doc.getConfidentialityCode()));
			visitInformation.addAttribute(confidentiality);
		}
		
		// Custodian - Approximately the location where the event or original data is store
		// TODO: Provenance data, do we need to store this?
		if(doc.getCustodian() != null && doc.getCustodian().getNullFlavor() == null &&
				doc.getCustodian().getAssignedCustodian() != null && doc.getCustodian().getAssignedCustodian().getNullFlavor() == null)
		{
			visitInformation.setLocation(locationOrganizationProcessorUtil.processOrganization(doc.getCustodian().getAssignedCustodian().getRepresentedCustodianOrganization()));
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
				Provider provider = assignedEntityProcessorUtil.processProvider(prf.getAssignedEntity());
				EncounterRole role = openmrsMetadataUtil.getOrCreateEncounterRole(prf.getFunctionCode());
				visitEncounter.addProvider(role, provider);
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

		// Participants and their relationship
		for(Participant1 ptcpt : doc.getParticipant())
		{
			if(ptcpt == null || ptcpt.getNullFlavor() != null ||
					ptcpt.getAssociatedEntity() == null ||
					ptcpt.getAssociatedEntity().getNullFlavor() != null ||
					ptcpt.getAssociatedEntity().getAssociatedPerson() == null ||
					ptcpt.getAssociatedEntity().getAssociatedPerson().getNullFlavor() != null)
				continue;
			
			// Process the relationship
			Relationship rel = personProcessorUtil.processAssociatedEntity(ptcpt.getAssociatedEntity(), visitInformation.getPatient());
			
			// Update the known time the relationship existed based on event time
			if(rel.getStartDate() == null || visitInformation.getStartDatetime().compareTo(rel.getStartDate()) < 0)
				rel.setStartDate(visitInformation.getStartDatetime());
			if(rel.getEndDate() == null || visitInformation.getStopDatetime().compareTo(rel.getEndDate()) > 0)
				rel.setEndDate(visitInformation.getStopDatetime());
			
			Context.getPersonService().saveRelationship(rel);
		}

		// Parse informants
		for(Informant12 inf : doc.getInformant())
		{
			if(inf.getInformantChoice().isPOCD_MT000040UVRelatedEntity())
				throw new NotImplementedException("OpenSHR cannot store informants of type related persons .. yet");
			else
			{
				Provider provider = assignedEntityProcessorUtil.processProvider(inf.getInformantChoiceIfAssignedEntity());
				EncounterRole role = openmrsMetadataUtil.getOrCreateEncounterRole(inf.getTypeCode());
				visitEncounter.addProvider(role, provider);
			}
		}

		// TODO: Information recipient : Do we need to store this?
		
		// Parse the legal authenticator
		if(doc.getLegalAuthenticator() != null)
		{
			// TODO: Where to store signature code? Or is it enough that this data would be stored in the provenance with the document?
			Provider provider = assignedEntityProcessorUtil.processProvider(doc.getLegalAuthenticator().getAssignedEntity());
			EncounterRole role = openmrsMetadataUtil.getOrCreateEncounterRole(doc.getLegalAuthenticator().getTypeCode());
			visitEncounter.addProvider(role, provider);
		}

		// Parse dataEnterer - the person that entered the data
		if(doc.getDataEnterer() != null)
		{
			// TODO: Where to store the time the data was entered?
			Provider provider = assignedEntityProcessorUtil.processProvider(doc.getDataEnterer().getAssignedEntity());
			EncounterRole role = openmrsMetadataUtil.getOrCreateEncounterRole(doc.getDataEnterer().getTypeCode());
			visitEncounter.addProvider(role, provider);
		}

		// ID? Some basic provenance data
		if(doc.getId() != null && !doc.getId().isNull())
		{
			VisitAttribute provenance = new VisitAttribute();
			provenance.setAttributeType(openmrsMetadataUtil.getOrCreateVisitExternalIdAttributeType());
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
				visitTypeName = "UNKNOWN";
		}
		visitInformation.setVisitType(openmrsMetadataUtil.getVisitType(visitTypeName));

		// Add attributes to the visit encounter
		visitEncounter.setPatient(visitInformation.getPatient());
		visitEncounter.setVisit(visitInformation);
		visitEncounter.setDateCreated(visitInformation.getDateCreated());
		visitEncounter.setLocation(visitInformation.getLocation());
		visitEncounter.setEncounterDatetime(visitInformation.getStartDatetime());
		visitEncounter.setDateCreated(visitInformation.getDateCreated());
		visitEncounter.setEncounterType(openmrsMetadataUtil.getOrCreateEncounterType(doc.getCode()));
		visitEncounter = Context.getEncounterService().saveEncounter(visitEncounter);
		
		// Add encounters
		Set<Encounter> encounters = new HashSet<Encounter>();
		encounters.add(visitEncounter);
		visitInformation.setEncounters(encounters);
		visitInformation = Context.getVisitService().saveVisit(visitInformation);
		
		return visitInformation;
	}
	
	/**
	 * Validate a clinical document instance
	 * @param doc The clinical document to be validated
	 * @return True if the parsing of the clinical document should continue
	 */
	public ValidationIssueCollection validate(IGraphable object)
	{
		ValidationIssueCollection validationMessages = new ValidationIssueCollection();
		
		if(!(object instanceof ClinicalDocument))
			validationMessages.error(String.format("Expected ClinicalDocument, got %s", object.getClass()));
		return validationMessages;
	}
}
