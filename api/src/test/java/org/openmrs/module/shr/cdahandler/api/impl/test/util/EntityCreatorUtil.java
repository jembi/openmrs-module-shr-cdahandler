package org.openmrs.module.shr.cdahandler.api.impl.test.util;

import java.util.UUID;

import org.marc.everest.datatypes.generic.*;
import org.marc.everest.datatypes.*;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.AssignedAuthor;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.AssignedCustodian;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.AssociatedEntity;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Author;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Custodian;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.CustodianOrganization;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Organization;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Participant1;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Patient;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.PatientRole;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Person;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.RecordTarget;
import org.marc.everest.rmim.uv.cdar2.vocabulary.AdministrativeGender;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ContextControl;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ParticipationType;
import org.marc.everest.rmim.uv.cdar2.vocabulary.RoleClassAssociative;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;

/**
 * Utilities for creating Persons
 */
public class EntityCreatorUtil {

	/**
	 * Create a record target
	 * @return
	 */
	public final static RecordTarget createRecordTarget() {
		RecordTarget retVal = new RecordTarget(ContextControl.OverridingPropagating);
		PatientRole patientRole = new PatientRole();
		Patient patient = new Patient();
		patientRole.setId(SET.createSET(new II("1.3.6.1.4.1.12009.1.1.1", "3049")));
		patientRole.setAddr(SET.createSET(AD.fromSimpleAddress(PostalAddressUse.HomeAddress, "123 Main Street West", "Unit 20", "Hamilton", "ON", "CA", "L8K5N2")));
		patientRole.setTelecom(SET.createSET(new TEL("tel:+1-203-304-3045", TelecommunicationsAddressUse.Home)));
		patientRole.setProviderOrganization(createOrganization());
		patient.setName(SET.createSET(
			PN.fromFamilyGiven(EntityNameUse.Search, "Smith", "Elizabeth"),
			PN.fromFamilyGiven(EntityNameUse.Legal, "Taylor", "Elizabeth"),
			PN.fromFamilyGiven(EntityNameUse.Pseudonym, "Smith", "Liz")
			));
		patient.getName().get(0).getPart(0).setQualifier(SET.createSET(new CS<EntityNamePartQualifier>(EntityNamePartQualifier.Birth)));
		patient.getName().get(1).getPart(0).setQualifier(SET.createSET(new CS<EntityNamePartQualifier>(EntityNamePartQualifier.Spouse)));
		patient.setAdministrativeGenderCode(AdministrativeGender.Female);
		patient.setBirthTime(TS.valueOf("19930402"));
		patient.setMaritalStatusCode("M", "2.16.840.1.113883.5.2");
		
		retVal.setPatientRole(patientRole);
		patientRole.setPatient(patient);
		patientRole.setProviderOrganization(createOrganization());
		return retVal;
    }
	
	/**
	 * Create good health hospital
	 * Auto generated method comment
	 * 
	 * @return
	 */
	public static final Organization createOrganization()
	{
		Organization retVal = new Organization();
		retVal.setId(SET.createSET(new II("1.3.6.1.4.1.12009.1.2")));
		retVal.setName(SET.createSET(new ON()));
		retVal.getName().get(0).getParts().add(new ENXP("GOOD HEALTH CLINIC"));
		retVal.setTelecom(SET.createSET(new TEL("mailto:patadmin@ghhs.org")));
		retVal.setAddr(SET.createSET(AD.fromSimpleAddress(PostalAddressUse.PhysicalVisit, "203 Upper West Street", null, "Hamilton", "ON", "CA", "L8K5N2")));
		return retVal;
	}

	/**
	 * Create an author with the specified name
	 */
	public static final Author createAuthor(String id, String firstName, String familyName) {
		Author retVal = new Author(ContextControl.OverridingPropagating);
		AssignedAuthor assignedAuthor = new AssignedAuthor();
		retVal.setTime(TS.now());

		// Set ID 
		assignedAuthor.setId(SET.createSET(
				new II("2.16.840.1.113883.4.6", id),
				new II(String.format("1.3.6.1.4.1.12009.1.99.7.%s", id))
				));
		
		// Set assigned person
		assignedAuthor.setAssignedAuthorChoice(createPerson(firstName, familyName));
		
		// Address
		assignedAuthor.setAddr(SET.createSET(AD.fromSimpleAddress(PostalAddressUse.PhysicalVisit, "203 Upper West St.", null, "Hamilton", "ON", "CA", "L8K5N2")));
		assignedAuthor.setRepresentedOrganization(createOrganization());
		retVal.setAssignedAuthor(assignedAuthor);
		return retVal;
    }


	/**
	 * Create an author with the specified name
	 */
	public static final Author createAuthorLimited(String id) {
		Author retVal = new Author(ContextControl.OverridingPropagating);
		AssignedAuthor assignedAuthor = new AssignedAuthor();
		retVal.setTime(TS.now());

		// Set ID 
		assignedAuthor.setId(SET.createSET(
				new II("2.16.840.1.113883.4.6", id),
				new II(String.format("1.3.6.1.4.1.12009.1.99.7.%s", id))
				));
		retVal.setAssignedAuthor(assignedAuthor);
		return retVal;
    }
	
	/**
	 * Create a person
	 */
	public static final Person createPerson(String firstName, String familyName)
	{
		Person retVal = new Person();
		retVal.setName(SET.createSET(PN.fromFamilyGiven(EntityNameUse.Legal, familyName, firstName)));
		return retVal;
	}

	/**
	 * Create a custodian organization
	 */
	public static final Custodian createCustodian() {
		Custodian retVal = new Custodian();
		AssignedCustodian assignedCustodian = new AssignedCustodian();
		CustodianOrganization organization = new CustodianOrganization();
		Organization copyOrganization = createOrganization();
		
		organization.setId(copyOrganization.getId());
		organization.setTelecom(copyOrganization.getTelecom().get(0));
		organization.setName(copyOrganization.getName().get(0));
		
		assignedCustodian.setRepresentedCustodianOrganization(organization);
		retVal.setAssignedCustodian(assignedCustodian);
		return retVal;
    }

	/**
	 * Creates a participant as a father
	 */
	public static Participant1 createFatherParticipant() {
		Participant1 retVal = new Participant1();
		retVal.setTypeCode(ParticipationType.IND);
		AssociatedEntity associatedEntity = new AssociatedEntity();
		
		retVal.setTime(TS.now());
		associatedEntity.setId(SET.createSET(new II("1.3.6.1.4.1.12009.1.99.7", "3012")));
		associatedEntity.setClassCode(RoleClassAssociative.NextOfKin);
		associatedEntity.setCode("FTH", CdaHandlerConstants.CODE_SYSTEM_FAMILY_MEMBER);
		associatedEntity.setAssociatedPerson(createPerson("Andrew", "Smith"));
		retVal.setAssociatedEntity(associatedEntity);
		return retVal;
    }
	

	/**
	 * Creates a participant as a spouse
	 */
	public static Participant1 createSpouseParticipant() {
		Participant1 retVal = new Participant1();
		retVal.setTypeCode(ParticipationType.IND);
		
		AssociatedEntity associatedEntity = new AssociatedEntity();
		retVal.setTemplateId(LIST.createLIST(
			new II("1.3.6.1.4.1.19376.1.5.3.1.2.4.1")
				));
		associatedEntity.setClassCode(RoleClassAssociative.NextOfKin);
		retVal.setTime(TS.now());
		associatedEntity.setId(SET.createSET(new II("1.3.6.1.4.1.12009.1.99.7", "3014")));
		associatedEntity.setCode("127848009", CdaHandlerConstants.CODE_SYSTEM_SNOMED);
		associatedEntity.setAssociatedPerson(createPerson("Jason", "Taylor"));
		retVal.setAssociatedEntity(associatedEntity);
		return retVal;
    }

	/**
	 * Creates a participant as a father of baby
	 */
	public static Participant1 createFatherOfBabyParticipant() {
		Participant1 retVal = new Participant1();
		retVal.setTypeCode(ParticipationType.IND);
		
		AssociatedEntity associatedEntity = new AssociatedEntity();
		retVal.setTemplateId(LIST.createLIST(
			new II("1.3.6.1.4.1.19376.1.5.3.1.2.4.2")
				));
		associatedEntity.setClassCode(RoleClassAssociative.PersonalRelationship);
		retVal.setTime(TS.now());
		associatedEntity.setId(SET.createSET(new II("1.3.6.1.4.1.12009.1.99.7", "3014")));
		associatedEntity.setCode("xx-fatherofbaby", CdaHandlerConstants.CODE_SYSTEM_SNOMED);
		associatedEntity.setAssociatedPerson(createPerson("Jason", "Taylor"));
		retVal.setAssociatedEntity(associatedEntity);
		return retVal;
    }

}
