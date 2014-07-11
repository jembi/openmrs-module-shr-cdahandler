/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.shr.cdahandler;

/**
 * An enumeration of CDA handler property names
 * @author Justin Fyfe
 *
 */
public class CdaHandlerGlobalPropertyNames {

	// Property name controlling auto-creation of entities
	public static final String AUTOCREATE_PROVIDERS = "shr.cdahandler.autocreate.providers";
	// Property name controlling auto-creation of encounter roles
	public static final String AUTOCREATE_METADATA = "shr.cdahandler.autocreate.metaData";
	// Property name controlling the auto-creation of patients
	public static final String AUTOCREATE_PATIENTS = "shr.cdahandler.autocreate.patients";
	// Property name controlling the auto-creation of patient id types
	public static final String AUTOCREATE_PATIENTIDTYPE = "shr.cdahandler.autocreate.patients.idtype";
	// Property name controlling the auto-creation of persons
	public static final String AUTOCREATE_PERSONS = "shr.cdahandler.autocreate.persons";
	// Automatically create concepts
	public static final String AUTOCREATE_CONCEPTS = "shr.cdahandler.autocreate.concepts";
	// Automatically create locations
	public static final String AUTOCREATE_LOCATIONS = "shr.cdahandler.autocreate.locations";
	// Property controlling the format of complex identifiers
	public static final String ID_FORMAT = "shr.cdahandler.idformat";
}
