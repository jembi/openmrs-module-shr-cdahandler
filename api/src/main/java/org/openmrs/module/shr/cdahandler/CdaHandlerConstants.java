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
	public static final Object CODE_SYSTEM_OBSERVATION_VALUE = "2.16.840.1.113883.5.1063";
	
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
    
	
}
