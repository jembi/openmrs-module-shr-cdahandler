package org.openmrs.module.shr.cdahandler;


/**
 * A constant list of OIDs
 */
public final class CdaHandlerConstants {
	
	
	// Loinc
	public static final String CODE_SYSTEM_LOINC = "2.16.840.1.113883.6.1";
	public static final String CODE_SYSTEM_NAME_LOINC = "LOINC";
	public static final String CODE_SYSTEM_SNOMED = "2.16.840.1.113883.6.96";
	public static final String CODE_SYSTEM_ACT_CODE = "2.16.840.1.113883.5.4";
	public static final String CODE_SYSTEM_OBSERVATION_VALUE = "2.16.840.1.113883.5.1063";
	public static final String CODE_SYSTEM_FAMILY_MEMBER = "2.16.840.1.113883.5.111";
	public static final String CODE_SYSTEM_CIEL = "9.9.9.9.9.9.9"; // TODO: Get the actual OID for this
	
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

	public static final String SCT_TEMPLATE_CCD_3_12 = "2.16.840.1.113883.10.20.1.16";
	public static final String SCT_TEMPLATE_CODED_VITAL_SIGNS = "1.3.6.1.4.1.19376.1.5.3.1.1.5.3.2";
	public static final String SCT_TEMPLATE_VITAL_SIGNS = "1.3.6.1.4.1.19376.1.5.3.1.3.25";
	public static final String SCT_TEMPLATE_MEDICATIONS = "1.3.6.1.4.1.19376.1.5.3.1.4.7";
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
	
	
}
