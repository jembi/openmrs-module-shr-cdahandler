package org.openmrs.module.shr.cdahandler;


/**
 * A constant list of OIDs
 */
public final class CdaHandlerConstants {
	
	public static final String FMT_CODE_PCC_MS = "urn:ihe:pcc:xds-ms:2007"; 
	public static final String FMT_CODE_PCC_HP = "urn:ihe:pcc:hp:2008";
	public static final String FMT_CODE_PCC_APHP = "urn:ihe:pcc:aphp:2008";
	public static final String FMT_CODE_PCC_APS = "urn:ihe:pcc:aps:2007";
	public static final String FMT_CODE_LAB = "urn:ihe:lab:xd-lab:2008"; 
	public static final String FMT_CODE_PCC_APL = "urn:ihe:pcc:apl:2008"; 
	public static final String FMT_CODE_PCC_APE = "urn:ihe:pcc:ape:2008"; 
	public static final String FMT_CODE_PCC_LDS = "urn:ihe:pcc:lds:2009";
	public static final String FMT_CODE_PCC_MDS = "urn:ihe:pcc:mds:2009";

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

	// Section Templates
	public static final String SCT_TEMPLATE_CCD_3_12 = "2.16.840.1.113883.10.20.1.16";
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
	
	// Entry templates
	public static final String ENT_TEMPLATE_SIMPLE_OBSERVATION = "1.3.6.1.4.1.19376.1.5.3.1.4.13";
	public static final String ENT_TEMPLATE_VITAL_SIGNS_ORGANIZER = "1.3.6.1.4.1.19376.1.5.3.1.4.13.1";
	public static final String ENT_TEMPLATE_VITAL_SIGNS_OBSERVATION = "1.3.6.1.4.1.19376.1.5.3.1.4.13.2";
	public static final String ENT_TEMPLATE_FAMILY_HISTORY_ORGANIZER = "1.3.6.1.4.1.19376.1.5.3.1.4.15";
	public static final String ENT_TEMPLATE_FAMILY_HISTORY_OBSERVATION = "1.3.6.1.4.19376.1.5.3.1.4.13.3";
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
	public static final String RMIM_CONCEPT_NAME_REASON = "Reason";
	public static final String RMIM_CONCEPT_NAME_STATUS = "Status";
	public static final String RMIM_CONCEPT_NAME_APPROACH_SITE = "Approach Site";
	public static final String RMIM_CONCEPT_NAME_TARGET_SITE = "Target Site";
	public static final String RMIM_CONCEPT_NAME_REFERENCE = "Reference";
	public static final String RMIM_CONCEPT_NAME_ROUTE_OF_ADM = "Route of Administration";
	public static final String RMIM_CONCEPT_NAME_MARITAL_STATUS = "Civil Status";
	public static final String RMIM_CONCEPT_NAME_DOCUMENT_TEXT = "Text";
	public static final String RMIM_CONCEPT_NAME_REPEAT = "Repeat";
	public static final String RMIM_CONCEPT_NAME_METHOD = "Method";
	public static final String RMIM_CONCEPT_NAME_INTERPRETATION = "Interpretation";
	public static final String RMIM_CONCEPT_NAME_MOOD = "Mood";
	
	
	// UUIDs
	public static final String UUID_ORDER_TYPE_PROCEDURE = "9506c6fe-d517-4707-a0c8-e72da23ff16d";
	public static final String UUID_CONCEPT_CLS_DRUG_FORM = "de359f23-2bfc-4e8d-96d8-25b7526d6070";
	public static final String UUID_CONCEPT_CLS_AUTO = "4ccac411-eb8e-45d6-8ec1-f6e09602449f";
	public static final String UUID_ORDER_TYPE_OBSERVATION = "7f14cf98-8452-42c0-acac-2ba96c8e66ce";
}
