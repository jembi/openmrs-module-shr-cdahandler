package org.openmrs.module.shr.cdahandler;

import java.util.UUID;

import org.marc.everest.datatypes.II;


/**
 * A constant list of OIDs
 */
public final class CdaHandlerConstants {
	
	// the sdtc namespace
	public static final String NS_SDTC = "urn:hl7-org:sdtc";
	
	public static final String FMT_CODE_PCC_MS = "urn:ihe:pcc:xds-ms:2007"; 
	public static final String FMT_CODE_PCC_HP = "urn:ihe:pcc:hp:2008";
	public static final String FMT_CODE_PCC_APHP = "urn:ihe:pcc:aphp:2008";
	public static final String FMT_CODE_PCC_APS = "urn:ihe:pcc:aps:2007";
	public static final String FMT_CODE_LAB = "urn:ihe:lab:xd-lab:2008"; 
	public static final String FMT_CODE_PCC_APL = "urn:ihe:pcc:apl:2008"; 
	public static final String FMT_CODE_PCC_APE = "urn:ihe:pcc:ape:2008"; 
	public static final String FMT_CODE_PCC_LDS = "urn:ihe:pcc:lds:2009";
	public static final String FMT_CODE_PCC_MDS = "urn:ihe:pcc:mds:2009";

	// Medication concept id
	public static final int CONCEPT_ID_MEDICATION_HISTORY = 160741;
	public static final int CONCEPT_ID_MEDICATION_DRUG = 1282;
	public static final int CONCEPT_ID_MEDICATION_STRENGTH = 1444;
	public static final int CONCEPT_ID_MEDICATION_QUANTITY = 160856;
	public static final int CONCEPT_ID_MEDICATION_FREQUENCY = 160855;
	public static final int CONCEPT_ID_MEDICATION_START_DATE = 1190;
	public static final int CONCEPT_ID_MEDICATION_STOP_DATE = 1191;
	public static final int CONCEPT_ID_MEDICATION_INDICATION = 160742;
	public static final int CONCEPT_ID_MEDICATION_TEXT = 160632;
	public static final int CONCEPT_ID_MEDICATION_DURATION = 159368;
	public static final int CONCEPT_ID_IMMUNIZATION_SEQUENCE = 1418;
	public static final int CONCEPT_ID_IMMUNIZATION_DATE = 1410;
	public static final int CONCEPT_ID_IMMUNIZATION_DRUG = 984;
	public static final int CONCEPT_ID_MEDICATION_FORM = 1519;
	public static final int CONCEPT_ID_IMMUNIZATION_HISTORY = 1421;
	public static final int CONCEPT_ID_SIGN_SYMPTOM_PRESENT = 1729;
	public static final int CONCEPT_ID_SUPPLY = 1442;
	public static final int CONCEPT_ID_SUPPLY_ORDERED_QTY = 1621;
	public static final int CONCEPT_ID_MEDICATION_TREATMENT_NUMBER = 1639;
	public static final int CONCEPT_ID_MEDICATION_DISPENSED = 1443;
	public static final int CONCEPT_ID_PROVIDER_NAME = 1473;
	public static final int CONCEPT_ID_DATE_OF_EVENT = 160753;
	public static final int CONCEPT_ID_PROCEDURE = 1651;
	public static final int CONCEPT_ID_UNSPECIFIED = 161548;
	public static final int CONCEPT_ID_PROCEDURE_HISTORY = 160714;
	public static final int CONCEPT_ID_PROCEDURE_DATE = 160715;
	public static final int CONCEPT_ID_PROCEDURE_TEXT = 160716;

	// Loinc
	public static final String CODE_SYSTEM_LOINC = "2.16.840.1.113883.6.1";
	public static final String CODE_SYSTEM_NAME_LOINC = "LOINC";
	public static final String CODE_SYSTEM_SNOMED = "2.16.840.1.113883.6.96";
	public static final String CODE_SYSTEM_ACT_CODE = "2.16.840.1.113883.5.4";
	public static final String CODE_SYSTEM_OBSERVATION_VALUE = "2.16.840.1.113883.5.1063";
	public static final String CODE_SYSTEM_FAMILY_MEMBER = "2.16.840.1.113883.5.111";
	public static final String CODE_SYSTEM_CIEL = "9.9.9.9.9.9.9"; // TODO: Get the actual OID for this
	public static final String CODE_SYSTEM_UCUM = "2.16.840.1.113883.6.8";
	public static final String CODE_SYSTEM_ROUTE_OF_ADMINISTRATION = "2.16.840.1.113883.5.112";
	public static final String CODE_SYSTEM_MARITAL_STATUS = "2.16.840.1.113883.5.2";
	public static final String CODE_SYSTEM_CVX = "2.16.840.1.113883.6.59";
	public static final String CODE_SYSTEM_RXNORM = "2.16.840.1.113883.6.88";
	public static final String CODE_SYSTEM_IHE_ACT_CODE = "1.3.6.1.4.1.19376.1.5.3.2";
	public static final String CODE_SYSTEM_CPT4 = "2.16.840.1.113883.6.12";
	
	// Document Template Medical Documents
	public static final String DOC_TEMPLATE_MEDICAL_DOCUMENTS = "1.3.6.1.4.1.19376.1.5.3.1.1.1";
	public static final String DOC_TEMPLATE_MEDICAL_SUMMARY = "1.3.6.1.4.1.19376.1.5.3.1.1.2";
	public static final String DOC_TEMPLATE_HISTORY_PHYSICAL = "1.3.6.1.4.1.19376.1.5.3.1.1.16.1.4";
	public static final String DOC_TEMPLATE_ANTEPARTUM_HISTORY_AND_PHYSICAL = "1.3.6.1.4.1.19376.1.5.3.1.1.16.1.1";
	public static final String DOC_TEMPLATE_ANTEPARTUM_SUMMARY = "1.3.6.1.4.1.19376.1.5.3.1.1.11.2";
	public static final String DOC_TEMPLATE_SHARING_LAB_DOCS = "1.3.6.1.4.1.19376.1.3.3";
	public static final String DOC_TEMPLATE_ANTEPARTUM_LAB = "1.3.6.1.4.1.19376.1.5.3.1.1.16.1.2";
	public static final String DOC_TEMPLATE_ANTEPARTUM_EDUCATION = "1.3.6.1.4.1.19376.1.5.3.1.1.16.1.3";
	public static final String DOC_TEMPLATE_LABOR_AND_DELIVERY_SUMMARY = "1.3.6.1.4.1.19376.1.5.3.1.1.21.1.2";
	public static final String DOC_TEMPLATE_MATERNAL_DISCHARGE_SUMMARY = "1.3.6.1.4.1.19376.1.5.3.1.1.21.1.3";
	public static final String DOC_TEMPLATE_CCD = "2.16.840.1.113883.10.20.1";
	public static final String DOC_TEMPLATE_CDA4CDT = "2.16.840.1.113883.10.20.3";
	public static final String DOC_TEMPLATE_IMMUNIZATION_CONTENT = "1.3.6.1.4.1.19376.1.5.3.1.1.18.1.2";
	
	// Section Templates
	public static final String SCT_TEMPLATE_CCD_ADVNACE_DIRECTIVES = "2.16.840.1.113883.10.20.1.1";
	public static final String SCT_TEMPLATE_CCD_ALERTS = "2.16.840.1.113883.10.20.1.2";
	public static final String SCT_TEMPLATE_CCD_ENCOUNTERS = "2.16.840.1.113883.10.20.1.3";
	public static final String SCT_TEMPLATE_CCD_FAMILY_HISTORY = "2.16.840.1.113883.10.20.1.4";
	public static final String SCT_TEMPLATE_CCD_FUNCTIONAL_STATUS = "2.16.840.1.113883.10.20.1.5";
	public static final String SCT_TEMPLATE_CCD_IMMUNIZATIONS = "2.16.840.1.113883.10.20.1.6";
	public static final String SCT_TEMPLATE_CCD_MEDICAL_EQUIPMENT = "2.16.840.1.113883.10.20.1.7";
	public static final String SCT_TEMPLATE_CCD_MEDICATIONS = "2.16.840.1.113883.10.20.1.8";
	public static final String SCT_TEMPLATE_CCD_PAYERS = "2.16.840.1.113883.10.20.1.9";
	public static final String SCT_TEMPLATE_CCD_PLAN_OF_CARE = "2.16.840.1.113883.10.20.1.10";
	public static final String SCT_TEMPLATE_CCD_PROBLEM = "2.16.840.1.113883.10.20.1.11";
	public static final String SCT_TEMPLATE_CCD_PROCEDURES = "2.16.840.1.113883.10.20.1.12";
	public static final String SCT_TEMPLATE_CCD_PURPOSE = "2.16.840.1.113883.10.20.1.13";
	public static final String SCT_TEMPLATE_CCD_RESULTS = "2.16.840.1.113883.10.20.1.14";
	public static final String SCT_TEMPLATE_CCD_SOCIAL_HISTORY = "2.16.840.1.113883.10.20.1.15";
	public static final String SCT_TEMPLATE_CCD_VITAL_SIGNS = "2.16.840.1.113883.10.20.1.16";
	public static final String SCT_TEMPLATE_CODED_VITAL_SIGNS = "1.3.6.1.4.1.19376.1.5.3.1.1.5.3.2";
	public static final String SCT_TEMPLATE_VITAL_SIGNS = "1.3.6.1.4.1.19376.1.5.3.1.3.25";
	public static final String SCT_TEMPLATE_MEDICATIONS = "1.3.6.1.4.1.19376.1.5.3.1.3.19";
	public static final String SCT_TEMPLATE_CHIEF_COMPLAINT = "1.3.6.1.4.1.19376.1.5.3.1.1.13.2.1";
	public static final String SCT_TEMPLATE_ASSESSMENT_AND_PLAN = "1.3.6.1.4.1.19376.1.5.3.1.1.13.2.5";
	public static final String SCT_TEMPLATE_CODED_HISTORY_OF_INFECTION = "1.3.6.1.4.1.19376.1.5.3.1.1.16.2.1.1.1";
	public static final String SCT_TEMPLATE_PREGNANCY_HISTORY = "1.3.6.1.4.1.19376.1.5.3.1.1.5.3.4";
	public static final String SCT_TEMPLATE_DETAILED_PHYSICAL_EXAM = "1.3.6.1.4.1.19376.1.5.3.1.1.9.15";
	public static final String SCT_TEMPLATE_CODED_PHYISCAL_EXAM = "1.3.6.1.4.1.19376.1.5.3.1.1.9.15.1";
	public static final String SCT_TEMPLATE_ALLERGIES = "1.3.6.1.4.1.19376.1.5.3.1.3.13";
	public static final String SCT_TEMPLATE_FAMILY_HISTORY = "1.3.6.1.4.1.19376.1.5.3.1.3.14";
	public static final String SCT_TEMPLATE_CODED_FAMILY_MEDICAL_HISTORY = "1.3.6.1.4.1.19376.1.5.3.1.3.15";
	public static final String SCT_TEMPLATE_SOCIAL_HISTORY = "1.3.6.1.4.1.19376.1.5.3.1.3.16";
	public static final String SCT_TEMPLATE_CODED_SOCIAL_HISTORY = "1.3.6.1.4.1.19376.1.5.3.1.3.16.1";
	public static final String SCT_TEMPLATE_REVIEW_OF_SYSTEMS = "1.3.6.1.4.1.19376.1.5.3.1.3.18";
	public static final String SCT_TEMPLATE_CODED_RESULTS = "1.3.6.1.4.1.19376.1.5.3.1.3.28";
	public static final String SCT_TEMPLATE_HISTORY_OF_PRESENT_ILLNESS = "1.3.6.1.4.1.19376.1.5.3.1.3.4";
	public static final String SCT_TEMPLATE_HISTORY_OF_PAST_ILLNESS = "1.3.6.1.4.1.19376.1.5.3.1.3.8";
	public static final String SCT_TEMPLATE_HISTORY_OF_SURGICAL_PROCEDURES = "1.3.6.1.4.1.19376.1.5.3.1.1.16.2.2";
	public static final String SCT_TEMPLATE_ACTIVE_PROBLEMS = "1.3.6.1.4.1.19376.1.5.3.1.3.6";
	public static final String SCT_TEMPLATE_ESTIMATED_DELIVERY_DATES = "1.3.6.1.4.1.19376.1.5.3.1.1.11.2.2.1";
	public static final String SCT_TEMPLATE_ANTEPARTUM_TEMPLATE_VISIT_SUMMARY_FLOWSHEET = "1.3.6.1.4.1.19376.1.5.3.1.1.11.2.2.2";
	public static final String SCT_TEMPLATE_CODED_ANTENATAL_TESTING_AND_SURVEILLANCE = "1.3.6.1.4.1.19376.1.5.3.1.1.21.2.5.1";
	public static final String SCT_TEMPLATE_ANTENATAL_TESTING_AND_SURVEILLANCE = "1.3.6.1.4.1.19376.1.5.3.1.1.21.2.5";
	public static final String SCT_TEMPLATE_RESULTS = "1.3.6.1.4.1.19376.1.5.3.1.3.27";
	public static final String SCT_TEMPLATE_CARE_PLAN = "1.3.6.1.4.1.19376.1.5.3.1.3.31";
	public static final String SCT_TEMPLATE_ADVANCE_DIRECTIVES = "1.3.6.1.4.1.19376.1.5.3.1.3.34";	
	public static final String SCT_TEMPLATE_IMMUNIZATIONS = "1.3.6.1.4.1.19376.1.5.3.1.3.23";
	public static final String SCT_TEMPLATE_CODED_ADVANCE_DIRECTIVES = "1.3.6.1.4.1.19376.1.5.3.1.3.35";
	
	// Entry templates
	public static final String ENT_TEMPLATE_CCD_ADVANCE_DIRECTIVE_OBSERVATION = "2.16.840.1.113883.10.20.1.17";
	public static final String ENT_TEMPLATE_CCD_ALERT_OBSERVATION = "2.16.840.1.113883.10.20.1.18";
	public static final String ENT_TEMPLATE_CCD_ENCOUNTER_ACTIVITY = "2.16.840.1.113883.10.20.1.21";
	public static final String ENT_TEMPLATE_CCD_FAMILY_HISTORY_OBSERVATION = "2.16.840.1.113883.10.20.1.22";
	public static final String ENT_TEMPLATE_CCD_FAMILY_HISTORY_ORGANIZER = "2.16.840.1.113883.10.20.1.23";
	public static final String ENT_TEMPLATE_CCD_MEDICATION_ACTIVITY = "2.16.840.1.113883.10.20.1.24";
	public static final String ENT_TEMPLATE_CCD_PLAN_OF_CARE_ACTIVITY = "2.16.840.1.113883.10.20.1.25";
	public static final String ENT_TEMPLATE_CCD_PROBLEM_ACT = "2.16.840.1.113883.10.20.1.27";
	public static final String ENT_TEMPLATE_CCD_PROBLEM_OBSERVATION = "2.16.840.1.113883.10.20.1.28";
	public static final String ENT_TEMPLATE_CCD_PROCEDURE_ACTIVITY = "2.16.840.1.113883.10.20.1.29";
	public static final String ENT_TEMPLATE_CCD_PURPOSE_ACTIVITY = "2.16.840.1.113883.10.20.1.30";
	public static final String ENT_TEMPLATE_CCD_RESULT_OBSERVATION = "2.16.840.1.113883.10.20.1.31";
	public static final String ENT_TEMPLATE_CCD_RESULT_ORGANIZER = "2.16.840.1.113883.10.20.1.32";
	public static final String ENT_TEMPLATE_CCD_SOCIAL_HISTORY_OBSERVATION = "2.16.840.1.113883.10.20.1.33";
	public static final String ENT_TEMPLATE_CCD_SUPPLY_ACTIVITY = "2.16.840.1.113883.10.20.1.34";
	public static final String ENT_TEMPLATE_CCD_VITAL_SIGNS_ORGANIZER = "2.16.840.1.113883.10.20.1.35";
	public static final String ENT_TEMPLATE_CCD_AGE_OBSERVATION = "2.16.840.1.113883.10.20.1.38";
	public static final String ENT_TEMPLATE_CCD_DEATH_OBSERVATION = "2.16.840.1.113883.10.20.1.42";
	public static final String ENT_TEMPLATE_CCD_INSTRUCTIONS = "2.16.840.1.113883.10.20.1.49";
	public static final String ENT_TEMPLATE_CCD_REACTION_OBSERVATION = "2.16.840.1.113883.10.20.1.54";
	public static final String ENT_TEMPLATE_CCD_SEVERITY_OBSERVATION = "2.16.840.1.113883.10.20.1.55";
	public static final String ENT_TEMPLATE_CCD_ADVANCE_DIRECTIVE_STATUS = "2.16.840.1.113883.10.20.1.37";
	public static final String ENT_TEMPLATE_CCD_STATUS_OBSERVATION = "2.16.840.1.113883.10.20.1.57";
	public static final String ENT_TEMPLATE_SIMPLE_OBSERVATION = "1.3.6.1.4.1.19376.1.5.3.1.4.13";
	public static final String ENT_TEMPLATE_VITAL_SIGNS_ORGANIZER = "1.3.6.1.4.1.19376.1.5.3.1.4.13.1";
	public static final String ENT_TEMPLATE_VITAL_SIGNS_OBSERVATION = "1.3.6.1.4.1.19376.1.5.3.1.4.13.2";
	public static final String ENT_TEMPLATE_FAMILY_HISTORY_ORGANIZER = "1.3.6.1.4.1.19376.1.5.3.1.4.15";
	public static final String ENT_TEMPLATE_FAMILY_HISTORY_OBSERVATION = "1.3.6.1.4.1.19376.1.5.3.1.4.13.3";
	public static final String ENT_TEMPLATE_SOCIAL_HISTORY_OBSERVATION = "1.3.6.1.4.1.19376.1.5.3.1.4.13.4";
	public static final String ENT_TEMPLATE_PREGNANCY_OBSERVATION = "1.3.6.1.4.1.19376.1.5.3.1.4.13.5";
	public static final String ENT_TEMPLATE_PREGNANCY_HISTORY_ORGANIZER = "1.3.6.1.4.1.19376.1.5.3.1.4.13.5.1";
	public static final String ENT_TEMPLATE_BIRTH_EVENT_ORGANIZER = "1.3.6.1.4.1.19376.1.5.3.1.4.13.5.2";
	public static final String ENT_TEMPLATE_PROBLEM_CONCERN = "1.3.6.1.4.1.19376.1.5.3.1.4.5.2";
	public static final String ENT_TEMPLATE_CONCERN_ENTRY = "1.3.6.1.4.1.19376.1.5.3.1.4.5.1";
	public static final String ENT_TEMPLATE_PROBLEM_OBSERVATION = "1.3.6.1.4.1.19376.1.5.3.1.4.5";
	public static final String ENT_TEMPLATE_ALLERGIES_AND_INTOLERANCES_CONCERN = "1.3.6.1.4.1.19376.1.5.3.1.4.5.3";
	public static final String ENT_TEMPLATE_ALLERGY_AND_INTOLERANCE_OBSERVATION = "1.3.6.1.4.1.19376.1.5.3.1.4.6";
	public static final String ENT_TEMPLATE_SEVERITY_OBSERVATION = "1.3.6.1.4.1.19376.1.5.3.1.4.1";
	public static final String ENT_TEMPLATE_MEDICATIONS = "1.3.6.1.4.1.19376.1.5.3.1.4.7";
	public static final String ENT_TEMPLATE_MEDICATIONS_NORMAL_DOSING = "1.3.6.1.4.1.19376.1.5.3.1.4.7.1";
	public static final String ENT_TEMPLATE_MEDICATIONS_TAPERED_DOSING = "1.3.6.1.4.1.19376.1.5.3.1.4.8";
	public static final String ENT_TEMPLATE_MEDICATIONS_SPLIT_DOSING = "1.3.6.1.4.1.19376.1.5.3.1.4.9";
	public static final String ENT_TEMPLATE_MEDICATIONS_COMBINATION_DOSING = "1.3.6.1.4.1.19376.1.5.3.1.4.11";
	public static final String ENT_TEMPLATE_MEDICATIONS_CONDITIONAL_DOSING = "1.3.6.1.4.1.19376.1.5.3.1.4.10";
	public static final String ENT_TEMPLATE_INTERNAL_REFERENCE = "1.3.6.1.4.1.19376.1.5.3.1.4.4.1";
	public static final String ENT_TEMPLATE_SUPPLY = "1.3.6.1.4.1.19376.1.5.3.1.4.7.3";
	public static final String ENT_TEMPLATE_DELIVERY_DATE_OBSERVATION = "1.3.6.1.4.1.19376.1.5.3.1.1.11.2.3.1";
	public static final String ENT_TEMPLATE_ANTEPARTUM_FLOWSHEET_PANEL = "1.3.6.1.4.1.19376.1.5.3.1.1.11.2.3.2";
	public static final String ENT_TEMPLATE_MEDICATION_INSTRUCTIONS = "1.3.6.1.4.1.19376.1.5.3.1.4.3";
	public static final String ENT_TEMPLATE_ANTENATAL_TESTING_BATTERY = "1.3.6.1.4.1.19376.1.5.3.1.1.21.3.10";
	public static final String ENT_TEMPLATE_PROCEDURE_ENTRY = "1.3.6.1.4.1.19376.1.5.3.1.4.19";
	public static final String ENT_TEMPLATE_EXTERNAL_REFERENCES_ENTRY = "1.3.6.1.4.1.19376.1.5.3.1.4.4";
	public static final String ENT_TEMPLATE_IMMUNIZATIONS = "1.3.6.1.4.1.19376.1.5.3.1.4.12";
	public static final String ENT_TEMPLATE_IMMUNIZATION_SERIES = "2.16.840.1.113883.10.20.1.46";
	public static final String ENT_TEMPLATE_CCD_MEDICATION_PRODUCT = "2.16.840.1.113883.10.20.1.53";
	public static final String ENT_TEMPLATE_PRODUCT = "1.3.6.1.4.1.19376.1.5.3.1.4.7.2";
	public static final String ENT_TEMPLATE_MANIFESTATION_RELATION = "1.3.6.1.4.1.19376.1.5.3.1.4.6.1";
	public static final String ENT_TEMPLATE_ADVANCE_DIRECTIVE_OBSERVATION = "1.3.6.1.4.1.19376.1.5.3.1.4.13.7";

	// Section Templates
	public static final String SCT_TEMPLATE_EXAM_GENERAL_APPEARANCE = "1.3.6.1.4.1.19376.1.5.3.1.1.9.16";
	public static final String SCT_TEMPLATE_EXAM_VISIBLE_IMPLANTED_DEVICES = "1.3.6.1.4.1.19376.1.5.3.1.1.9.48";
	public static final String SCT_TEMPLATE_EXAM_INTEGUMENTARY_SYSTEM = "1.3.6.1.4.1.19376.1.5.3.1.1.9.17";
	public static final String SCT_TEMPLATE_EXAM_HEAD = "1.3.6.1.4.1.19376.1.5.3.1.1.9.18";
	public static final String SCT_TEMPLATE_EXAM_EYES = "1.3.6.1.4.1.19376.1.5.3.1.1.9.19";
	public static final String SCT_TEMPLATE_EXAM_EARS_NOSE = "1.3.6.1.4.1.19376.1.5.3.1.1.9.20";
	public static final String SCT_TEMPLATE_EXAM_EARS = "1.3.6.1.4.1.19376.1.5.3.1.1.9.21";
	public static final String SCT_TEMPLATE_EXAM_NOSE = "1.3.6.1.4.1.19376.1.5.3.1.1.9.22";
	public static final String SCT_TEMPLATE_EXAM_MOUTH_THROAT = "1.3.6.1.4.1.19376.1.5.3.1.1.9.23";
	public static final String SCT_TEMPLATE_EXAM_NECK = "1.3.6.1.4.1.19376.1.5.3.1.1.9.24";
	public static final String SCT_TEMPLATE_EXAM_ENDOCRINE_SYSTEM = "1.3.6.1.4.1.19376.1.5.3.1.1.9.25";
	public static final String SCT_TEMPLATE_EXAM_THORAX_LUNGS = "1.3.6.1.4.1.19376.1.5.3.1.1.9.26";
	public static final String SCT_TEMPLATE_EXAM_CHEST_WALL = "1.3.6.1.4.1.19376.1.5.3.1.1.9.27";
	public static final String SCT_TEMPLATE_EXAM_BREASTS = "1.3.6.1.4.1.19376.1.5.3.1.1.9.28";
	public static final String SCT_TEMPLATE_EXAM_HEART = "1.3.6.1.4.1.19376.1.5.3.1.1.9.29";
	public static final String SCT_TEMPLATE_EXAM_RESPIRATORY_SYSTEM = "1.3.6.1.4.1.19376.1.5.3.1.1.9.30";
	public static final String SCT_TEMPLATE_EXAM_ABDOMEN = "1.3.6.1.4.1.19376.1.5.3.1.1.9.31";
	public static final String SCT_TEMPLATE_EXAM_LYMPHATIC = "1.3.6.1.4.1.19376.1.5.3.1.1.9.32";
	public static final String SCT_TEMPLATE_EXAM_VESSELS = "1.3.6.1.4.1.19376.1.5.3.1.1.9.33";
	public static final String SCT_TEMPLATE_EXAM_MUSCULOSKELETAL = "1.3.6.1.4.1.19376.1.5.3.1.1.9.34";
	public static final String SCT_TEMPLATE_EXAM_NEUROLOGIC= "1.3.6.1.4.1.19376.1.5.3.1.1.9.35";
	public static final String SCT_TEMPLATE_EXAM_GENITALIA = "1.3.6.1.4.1.19376.1.5.3.1.1.9.36";
	public static final String SCT_TEMPLATE_EXAM_RECTUM = "1.3.6.1.4.1.19376.1.5.3.1.1.9.37";
	public static final String SCT_TEMPLATE_EXAM_EXTREMITIES = "1.3.6.1.4.1.19376.1.5.3.1.1.16.2.1";
	public static final String SCT_TEMPLATE_EXAM_PELVIS = "1.3.6.1.4.1.19376.1.5.3.1.1.21.2.10";

	// TODO: Turn these into Concept UUIDs
	public static final String RMIM_CONCEPT_UUID_REASON = "c79b75a0-9648-4133-b20c-7d244a5f691d";
//	public static final String RMIM_CONCEPT_UUID_STATUS = "2301ed92-8308-4082-99c4-b99f58a814dc";
	public static final String RMIM_CONCEPT_UUID_APPROACH_SITE = "e3a6097c-50f0-4e89-a3ac-a73dd9219296";
	public static final String RMIM_CONCEPT_UUID_TARGET_SITE = "fc1c315b-e1c7-47df-af7d-db6300684cec";
	public static final String RMIM_CONCEPT_UUID_REFERENCE = "185123e2-3e42-44fc-8a80-f1f58cbd4b22";
	public static final String RMIM_CONCEPT_UUID_ROUTE_OF_ADM = "d57c5d67-9d0a-4333-96b5-ef6ca7fe3900";
	public static final String RMIM_CONCEPT_UUID_MARITAL_STATUS = "e936260e-38db-495e-b024-55c843450528";
	public static final String RMIM_CONCEPT_UUID_DOCUMENT_TEXT = "caeed6f3-3625-451b-984e-d7ca3b7cafe3";
	public static final String RMIM_CONCEPT_UUID_METHOD = "335c3b3a-98d9-4956-9ef0-f2bdc12cd744";
	
	// UUIDs
	public static final String UUID_ORDER_TYPE_PROCEDURE = "9506c6fe-d517-4707-a0c8-e72da23ff16d";
	public static final String UUID_CONCEPT_CLS_DRUG_FORM = "de359f23-2bfc-4e8d-96d8-25b7526d6070";
	public static final String UUID_CONCEPT_CLS_AUTO = "4ccac411-eb8e-45d6-8ec1-f6e09602449f";
	public static final String UUID_CONCEPT_CLS_CDA = "895cf59a-3c14-43a1-8207-f13d20740c33";
	public static final String UUID_ORDER_TYPE_OBSERVATION = "7f14cf98-8452-42c0-acac-2ba96c8e66ce";

	// Attributes names
	public static final String ATTRIBUTE_NAME_EXTERNAL_ID = "ExternalId";
	public static final String ATTRIBUTE_NAME_TELECOM = "Telecom";
	public static final String ATTRIBUTE_NAME_CIVIL_STATUS = "Civil Status";
	public static final String ATTRIBUTE_NAME_ORGANIZATION = "Organization";
	public static final String ATTRIBUTE_NAME_CONFIDENTIALITY = "Confidentiality";
	public static final String ATTRIBUTE_NAME_ORIGINAL_COPY = "Original";
	public static final String ATTRIBUTE_NAME_OBS_GROUP = "orderObsGroup";

	public static final int MEDICATION_FREQUENCY_24_HOURS = 162252;
	public static final int MEDICATION_FREQUENCY_12_HOURS = 162251;
	public static final int MEDICATION_FREQUENCY_8_HOURS = 162250;
	public static final int MEDICATION_FREQUENCY_36_HOURS = 162254;
	public static final int MEDICATION_FREQUENCY_48_HOURS = 162253;
	public static final int MEDICATION_FREQUENCY_72_HOURS = 162255;
	public static final int MEDICATION_FREQUENCY_30_MINS = 162243;
	public static final int MEDICATION_FREQUENCY_AT_BEDTIME = 160863;
	public static final int MEDICATION_FREQUENCY_TWICE_DAILY_AFTER_MEALS = 160860;
	public static final int MEDICATION_FREQUENCY_THRICE_DAILY_AFTER_MEALS = 160867;
	public static final int MEDICATION_FREQUENCY_FOUR_TIMES_DAILY_AFTER_MEALS = 160871;
	public static final int MEDICATION_FREQUENCY_THRICE_DAILY_BEFORE_MEALS = 160868;
	public static final int MEDICATION_FREQUENCY_TWICE_DAILY_BEFORE_MEALS = 160859;
	public static final int MEDICATION_FREQUENCY_FOUR_TIMES_DAILY_BEFORE_MEALS = 160872;
	public static final int MEDICATION_FREQUENCY_ONCE_DAILY_MORNING = 160865;
	public static final int MEDICATION_FREQUENCY_ONCE_DAILY_EVENING = 160864;
	public static final int MEDICATION_FREQUENCY_ONCE_DAILY = 160862;
	public static final int MEDICATION_FREQUENCY_ONCE = 162135;
	


}
