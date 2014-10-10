package org.openmrs.module.shr.cdahandler.processor.document.impl;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.PQ;
import org.marc.everest.formatters.FormatterUtil;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Author;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Component3;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Informant12;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.NonXMLBody;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Participant1;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Performer1;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.RelatedDocument;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ServiceEvent;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.StructuredBody;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActRelationshipDocument;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Relationship;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.activelist.Allergy;
import org.openmrs.activelist.Problem;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.api.CdaImportService;
import org.openmrs.module.shr.cdahandler.configuration.CdaHandlerConfiguration;
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
import org.openmrs.module.shr.cdahandler.processor.util.OpenmrsDataUtil;
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

	// Utilities
	protected final OpenmrsMetadataUtil m_openmrsMetadataUtil = OpenmrsMetadataUtil.getInstance();
	protected final OpenmrsConceptUtil m_openmrsConceptUtil = OpenmrsConceptUtil.getInstance();
	protected final DatatypeProcessorUtil m_datatypeProcessorUtil = DatatypeProcessorUtil.getInstance();
	protected final AssignedEntityProcessorUtil m_assignedEntityProcessorUtil = AssignedEntityProcessorUtil.getInstance();
	protected final PatientRoleProcessorUtil m_patientRoleProcessorUtil = PatientRoleProcessorUtil.getInstance();
	protected final PersonProcessorUtil m_personProcessorUtil = PersonProcessorUtil.getInstance();
	protected final LocationOrganizationProcessorUtil m_locationOrganizationProcessorUtil = LocationOrganizationProcessorUtil.getInstance();
	protected final CdaHandlerConfiguration m_configuration = CdaHandlerConfiguration.getInstance();
	protected final OpenmrsDataUtil m_openmrsDataUtil = OpenmrsDataUtil.getInstance();


	/**
	 * Gets the context within which this processor runs (if any)
	 */
	@Override
	public ProcessorContext getContext() {
		return this.m_context;
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
		
		// Validate
		if(this.m_configuration.getValidationEnabled())
		{
			ValidationIssueCollection issues = this.validate(doc);
			if(issues.hasErrors())
				throw new DocumentValidationException(doc, issues);
		}
		
		Visit visitInformation = this.processHeader(doc);
		
		// Encounters - This may be a level 1 document so we better check
		if(doc.getComponent().getBodyChoiceIfNonXMLBody() != null)
			visitInformation = this.processLevel1Content(doc, visitInformation);
		else // level 2 , just hand-off to a StructuredBodyDocumentProcessor
			visitInformation = this.processLevel2Content(doc, visitInformation);

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

		// TODO: How to add an attribute which points to the CDA from which the visit was constructed
		// Parse the header
		if(doc.getRecordTarget().size() != 1)
			throw new DocumentImportException("Can only handle documents with exactly one patient");
		Patient patient = this.m_patientRoleProcessorUtil.processPatient(doc.getRecordTarget().get(0).getPatientRole());
		
		// Look up the visit information
		Visit visitInformation = this.m_openmrsDataUtil.getVisitById(doc.getId(), patient); 
				
		if(visitInformation == null)
			visitInformation = new Visit();
		else if(this.m_configuration.getUpdateExisting())
		{
			visitInformation.setDateChanged(doc.getEffectiveTime().getDateValue().getTime());
		}
		else
			throw new DocumentImportException(String.format("Cannot persist a duplicate document %s!", doc.getId()));

		// Create an encounter for the of the document
		Encounter visitEncounter = new Encounter();

		// Set patient of the visit
		visitInformation.setPatient(patient);
		
		// Are we explicitly appending/replacing data?
		for(RelatedDocument dr : doc.getRelatedDocument())
		{
			if(dr.getParentDocument() == null || dr.getParentDocument().getNullFlavor() != null)
				continue;
			
			// Old visit found?
			Visit oldVisit = null;
			for(int i = 0; i < dr.getParentDocument().getId().size() && oldVisit == null; i++)
			{
				oldVisit = this.m_openmrsDataUtil.getVisitById(dr.getParentDocument().getId().get(i), visitInformation.getPatient());

				if(oldVisit == null)
					log.warn(String.format("Can't find the visit identified as %s to be associated", FormatterUtil.toWireFormat(dr.getParentDocument().getId())));
				else if(dr.getTypeCode().getCode().equals(x_ActRelationshipDocument.RPLC)) // Replacement of
				{
					this.voidVisitData(oldVisit, doc.getId());
				}
				else if(dr.getTypeCode().getCode().equals(x_ActRelationshipDocument.APND))
				{
					// Append, so that means we're updating the visit with a new encounter!
					visitInformation = oldVisit; // TODO: See if this is the correct way to do this
					visitInformation.setDateChanged(doc.getEffectiveTime().getDateValue().getTime());
				}
				else
					log.warn(String.format("Don't understand the relationship type %s", FormatterUtil.toWireFormat(dr.getTypeCode())));
			}
		}
				
		// Parse the authors of this document
		for(Author aut : doc.getAuthor())
		{
			// TODO: Figure out where to stuff aut.getTime() .
			// This element represents the time that the author started participating in the creation of the clinical document .. Is it important?
			Provider provider = this.m_assignedEntityProcessorUtil.processProvider(aut.getAssignedAuthor());
			EncounterRole role = this.m_openmrsMetadataUtil.getOrCreateEncounterRole(aut.getTypeCode());
			visitEncounter.addProvider(role, provider);
			
			// Assign this author as the creator or updater
			User createdOrUpdatedBy = this.m_openmrsDataUtil.getUser(provider);
			if(createdOrUpdatedBy != null)
			{
				visitEncounter.setCreator(createdOrUpdatedBy);
				if(visitInformation.getChangedBy() != null &&
						visitInformation.getDateChanged() != null)
					visitInformation.setChangedBy(createdOrUpdatedBy);
				else if(visitInformation.getCreator() != null)
					visitInformation.setCreator(createdOrUpdatedBy);
			}
			
		}

		// TODO: Authorization & Participants
		// These are kind of visit attributes 
		//	Authorization = The authority under which the operation/action was done
		//	Participants = Spouses, fathers, etc. related to the record target
		
		// TODO: Confidentiality (discussion about privacy enforcement)
		if(doc.getConfidentialityCode() != null && !doc.getConfidentialityCode().isNull())
		{
			VisitAttribute confidentiality = new VisitAttribute();
			confidentiality.setAttributeType(this.m_openmrsMetadataUtil.getOrCreateVisitConfidentialityCodeAttributeType());
			confidentiality.setValueReferenceInternal(this.m_datatypeProcessorUtil.formatSimpleCode(doc.getConfidentialityCode()));
			visitInformation.addAttribute(confidentiality);
		}
		
		// Custodian - Approximately the location where the event or original data is store
		// TODO: Provenance data, do we need to store this?
		if(doc.getCustodian() != null && doc.getCustodian().getNullFlavor() == null &&
				doc.getCustodian().getAssignedCustodian() != null && doc.getCustodian().getAssignedCustodian().getNullFlavor() == null)
		{
			visitInformation.setLocation(this.m_locationOrganizationProcessorUtil.processOrganization(doc.getCustodian().getAssignedCustodian().getRepresentedCustodianOrganization()));
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
				Provider provider = this.m_assignedEntityProcessorUtil.processProvider(prf.getAssignedEntity());
				EncounterRole role = this.m_openmrsMetadataUtil.getOrCreateEncounterRole(prf.getFunctionCode());
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
		if(visitInformation.getDateCreated() == null)
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
			Relationship rel = this.m_personProcessorUtil.processAssociatedEntity(ptcpt.getAssociatedEntity(), visitInformation.getPatient());
			
			// We want to get the time!
			Date startTime = null,
					endTime = null;
			
			// Explicit time provided
			if(ptcpt.getTime() != null)
			{
				if(ptcpt.getTime().getValue() != null)
					startTime = ptcpt.getTime().getValue().getDateValue().getTime();
				if(ptcpt.getTime().getLow() != null && !ptcpt.getTime().getLow().isNull())
					startTime = ptcpt.getTime().getLow().getDateValue().getTime();
				if(ptcpt.getTime().getHigh() != null && !ptcpt.getTime().getHigh().isNull())
					endTime = ptcpt.getTime().getHigh().getDateValue().getTime();
			}

			// Update the known time the relationship existed based on times provided
			// Start time was provided, and either the start date occurs before the existing start
			// date of the relationship on file (Extended the known time) or there is no start date
			// on file
			if(startTime != null && (rel.getStartDate() != null &&
					startTime.before(rel.getStartDate())  
					|| rel.getStartDate() == null))
				rel.setStartDate(startTime);
			// End time was provided and either the end date occurs after the current end date
			// of the relationship (extending the known time) or there is no end date on file
			if(endTime != null && (rel.getEndDate() != null &&
					endTime.after(rel.getEndDate()) || rel.getEndDate() != null))
				rel.setEndDate(endTime);
			
			Context.getPersonService().saveRelationship(rel);
		}

		// Parse informants
		for(Informant12 inf : doc.getInformant())
		{
			if(inf.getInformantChoice().isPOCD_MT000040UVRelatedEntity())
				throw new NotImplementedException("OpenSHR cannot store informants of type related persons .. yet");
			else
			{
				Provider provider = this.m_assignedEntityProcessorUtil.processProvider(inf.getInformantChoiceIfAssignedEntity());
				EncounterRole role = this.m_openmrsMetadataUtil.getOrCreateEncounterRole(inf.getTypeCode());
				visitEncounter.addProvider(role, provider);
			}
		}

		// TODO: Information recipient : Do we need to store this?
		
		// Parse the legal authenticator
		if(doc.getLegalAuthenticator() != null)
		{
			// TODO: Where to store signature code? Or is it enough that this data would be stored in the provenance with the document?
			Provider provider = this.m_assignedEntityProcessorUtil.processProvider(doc.getLegalAuthenticator().getAssignedEntity());
			EncounterRole role = this.m_openmrsMetadataUtil.getOrCreateEncounterRole(doc.getLegalAuthenticator().getTypeCode());
			visitEncounter.addProvider(role, provider);
		}

		// Parse dataEnterer - the person that entered the data
		if(doc.getDataEnterer() != null)
		{
			// TODO: Where to store the time the data was entered?
			Provider provider = this.m_assignedEntityProcessorUtil.processProvider(doc.getDataEnterer().getAssignedEntity());
			EncounterRole role = this.m_openmrsMetadataUtil.getOrCreateEncounterRole(doc.getDataEnterer().getTypeCode());
			visitEncounter.addProvider(role, provider);
		}

		// ID? Some basic provenance data
		if(doc.getId() != null && !doc.getId().isNull())
		{
			VisitAttribute provenance = new VisitAttribute();
			provenance.setAttributeType(this.m_openmrsMetadataUtil.getOrCreateVisitExternalIdAttributeType());
			provenance.setValueReferenceInternal(this.m_datatypeProcessorUtil.formatIdentifier(doc.getId()));
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
		visitInformation.setVisitType(this.m_openmrsMetadataUtil.getVisitType(visitTypeName));

		// Add attributes to the visit encounter
		visitEncounter.setPatient(visitInformation.getPatient());
		visitEncounter.setVisit(visitInformation);
		visitEncounter.setDateCreated(visitInformation.getDateCreated());
		visitEncounter.setLocation(visitInformation.getLocation());
		visitEncounter.setEncounterDatetime(visitInformation.getStartDatetime());
		visitEncounter.setDateCreated(visitInformation.getDateCreated());
		visitEncounter.setEncounterType(this.m_openmrsMetadataUtil.getOrCreateEncounterType(doc.getCode()));
		visitEncounter = Context.getEncounterService().saveEncounter(visitEncounter);
		
		// Add encounters
		Set<Encounter> encounters = new HashSet<Encounter>();
		encounters.add(visitEncounter);
		visitInformation.setEncounters(encounters);
		visitInformation = Context.getVisitService().saveVisit(visitInformation);
		
		return visitInformation;
	}

	/**
	 * Void the visit
	 * Auto generated method comment
	 * 
	 * @param oldVisit
	 */
	private void voidVisitData(Visit oldVisit, II newId) {
		// Void the old visit with reason of replaced by this one
		log.info(String.format("Voided %s", oldVisit));
		String voidReason = this.m_datatypeProcessorUtil.formatIdentifier(newId);
		Context.getVisitService().voidVisit(oldVisit, voidReason);

		// Void the encounter and all observations 
		for(Encounter enc : oldVisit.getEncounters())
		{
			log.info(String.format("Voided %s", enc));
			Context.getEncounterService().voidEncounter(enc, voidReason);
			for(Obs obs : enc.getObs())
			{
				log.info(String.format("Voided %s", obs));
				Context.getObsService().voidObs(obs, voidReason);
				if(obs.getAccessionNumber() != null)
				{
					// TODO: Validate this is the correct behavior?
					for(Problem p : Context.getService(CdaImportService.class).getActiveListItemByObs(obs, Problem.class))
						Context.getActiveListService().voidActiveListItem(p, voidReason);
					for(Allergy a : Context.getService(CdaImportService.class).getActiveListItemByObs(obs, Allergy.class))
						Context.getActiveListService().voidActiveListItem(a, voidReason);
					//					Context.getActiveListService().voidActiveListItem(p, voidReason);
				}
			}
			for(Order ord : enc.getOrders())
			{
				log.info(String.format("Voided %s", ord));
				Context.getOrderService().voidOrder(ord, voidReason);
			}
		}
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
		
		Encounter binaryContentEncounter = visitInformation.getEncounters().iterator().next();
		
		// Process contents
		Obs binaryContentObs = new Obs();
		binaryContentObs.setConcept(this.m_openmrsConceptUtil.getOrCreateRMIMConcept(CdaHandlerConstants.RMIM_CONCEPT_UUID_DOCUMENT_TEXT, bodyChoiceIfNonXMLBody.getText()));
		binaryContentObs.setAccessionNumber(this.m_datatypeProcessorUtil.formatIdentifier(doc.getId()));
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
	 * Sets the context within which this processor runs
	 */
	@Override
	public void setContext(ProcessorContext context) {
		this.m_context = context;
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
