package org.openmrs.module.shr.cdahandler.processor.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jfree.util.Log;
import org.marc.everest.annotations.Structure;
import org.marc.everest.datatypes.ANY;
import org.marc.everest.datatypes.BL;
import org.marc.everest.datatypes.CO;
import org.marc.everest.datatypes.ED;
import org.marc.everest.datatypes.EN;
import org.marc.everest.datatypes.INT;
import org.marc.everest.datatypes.MO;
import org.marc.everest.datatypes.PQ;
import org.marc.everest.datatypes.SD;
import org.marc.everest.datatypes.ST;
import org.marc.everest.datatypes.TS;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.datatypes.generic.CS;
import org.marc.everest.datatypes.generic.CV;
import org.marc.everest.datatypes.generic.EIVL;
import org.marc.everest.datatypes.generic.PIVL;
import org.marc.everest.datatypes.generic.RTO;
import org.marc.everest.interfaces.IEnumeratedVocabulary;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.Drug;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.configuration.CdaHandlerConfiguration;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.util.OpenmrsConstants;

/**
 * A class for interacting (creating/looking up) OpenMRS concepts
 * @author Justin
 *
 */
public final class OpenmrsConceptUtil extends OpenmrsMetadataUtil {

	/**
	 * Get the singleton instance
	 */
	public static OpenmrsConceptUtil getInstance()
	{
		if(s_instance == null)
		{
			synchronized (s_lockObject) {
				if(s_instance == null) // Another thread might have created while we were waiting for a lock
				{
					s_instance = new OpenmrsConceptUtil();
					s_instance.initializeInstance();
				}
			}
		}
		return s_instance;
	}
	// singleton instance
	private static OpenmrsConceptUtil s_instance;
 
	private static Object s_lockObject = new Object();
	// Map types
	private ConceptMapType m_narrowerThan = null;
	
	private ConceptMapType m_sameAs = null;
	// Configuration and util instances
	private final CdaHandlerConfiguration m_configuration = CdaHandlerConfiguration.getInstance();

		
	// pq Unit maps
	private final Map<String, String> m_openMrsUcumUnitMaps = new HashMap<String,String>();
	
	/**
	 * Private ctor
	 */
	private OpenmrsConceptUtil()
	{
		
	}
	
	/**
	 * Add an answer to a concept
	 * @throws DocumentImportException 
	 */
	public void addAnswerToConcept(Concept questionConcept, Concept answerConcept) throws DocumentImportException {
		DatatypeProcessorUtil datatypeUtil = DatatypeProcessorUtil.getInstance();
		
		// Is the concept in the list of answers?
		ConceptAnswer answer = null;
		for(ConceptAnswer ans : questionConcept.getAnswers())
			if(ans.equals(answerConcept))
				answer = ans;
		if(answer == null && this.m_configuration.getAutoCreateConcepts())
		{
			answer = new ConceptAnswer();
			answer.setAnswerConcept(answerConcept);
			answer.setConcept(questionConcept);
			questionConcept.addAnswer(answer);
			Context.getConceptService().saveConcept(questionConcept);
		}
		else if(answer == null)
			throw new DocumentImportException(String.format("Cannot assign code %s to observation concept %s as it is not a valid value", answerConcept, questionConcept));
		// Set the value    
	}

	/**
	 * Add the concept to the set concept
	 */
	public void addConceptToSet(Concept setConcept, Concept concept) {

		Boolean needsSave = false; 
		// First is the setConcept already a set?
		if(!setConcept.isSet() && this.m_configuration.getAutoCreateConcepts())
		{
			setConcept.setSet(true);
			setConcept.setConceptClass(Context.getConceptService().getConceptClassByName("ConvSet"));
			needsSave = true;
		}
		else if(!setConcept.isSet())
			log.warn("Cannot convert Concept to a set!");
		
		// Now does the concept already exist in the set members?
		List<Concept> conceptSetMembers = Context.getConceptService().getConceptsByConceptSet(setConcept);
		if(!conceptSetMembers.contains(concept) && this.m_configuration.getAutoCreateConcepts()) // Add to the set
		{
			setConcept.addSetMember(concept);
			needsSave = true;
		}
		else if(!this.m_configuration.getAutoCreateConcepts())
			log.warn("Cannot add concept to the specified concept set according to configuration rules");

		if(needsSave)
		{
			
			setConcept = Context.getConceptService().saveConcept(setConcept);
		}		
			
		
    }

	/**
	 * Create a concept in the database
	 * @param code
	 * @throws DocumentImportException 
	 */
	public Concept createConcept(CV<?> code) throws DocumentImportException {
		return this.createConcept(code, new ANY());
    }

	/**
	 * Create a concept with appropriate datatype to store the specified type of data
	 * @param code The code representing the concept
	 * @param type The type of data
	 * @return The created concept
	 * @throws DocumentImportException
	 */
	public Concept createConcept(CV<?> code, ANY value) throws DocumentImportException {
		if(!this.m_configuration.getAutoCreateConcepts())
			throw new IllegalStateException("Cannot create concepts according to configuration policy");

		log.debug("Enter: createConcept");
		
		ConceptReferenceTerm referenceTerm = this.getOrCreateReferenceTerm(code);
		
		// Concept class for auto-created concepts
		log.debug("Get Concept Class");
		ConceptClass conceptClass = Context.getConceptService().getConceptClassByName(value == null ? "ConvSet" : "Auto");
		if(conceptClass == null)
		{
			conceptClass = new ConceptClass();
			conceptClass.setName("Auto");
			conceptClass.setDescription(super.getLocalizedString("autocreated"));
			Context.getConceptService().saveConceptClass(conceptClass);
		}
		
		String fullName = code.getDisplayName();
		if(fullName == null)
			fullName = code.getCode().toString();

		
		// Create the concept and set properties
		Concept concept = new Concept();
		log.debug("Get concept mapping type to reference term");
		ConceptMapType mapType = this.m_sameAs;
		ConceptDatatype dataType = this.getConceptDatatype(value);
		// Is this a numeric type that has a value? If so, we need to ensure we create it
		if(dataType.getUuid().equals(ConceptDatatype.NUMERIC_UUID))
		{
			log.debug("Creating numeric based concept, set map to NARROWER-THAN");
			concept = new ConceptNumeric();
			
			if(value instanceof PQ)
			{
				fullName += " (" + ((PQ)value).getUnit() + ")";
				((ConceptNumeric)concept).setUnits(((PQ)value).getUnit());
				((ConceptNumeric)concept).setPrecise(true);
				
			}
			mapType = this.m_narrowerThan;
		}
		else if(dataType.getUuid().equals(ConceptDatatype.COMPLEX_UUID))
		{
			log.debug("Setting handler on complex Concept");
			concept = new ConceptComplex();
			((ConceptComplex)concept).setHandler("BinaryDataHandler");
		}

		log.debug("Set core attributes of concept");
		Locale locale = Context.getLocale();// new Locale("en");
		concept.setFullySpecifiedName(new ConceptName(fullName, locale));
		concept.addName(concept.getFullySpecifiedName(locale));
		concept.setPreferredName(concept.getFullySpecifiedName(locale));
		concept.setShortName(new ConceptName(code.getCode().toString(), Context.getLocale()));
		concept.setVersion(code.getCodeSystemVersion());
		concept.setConceptClass(conceptClass);
		concept.setSet(value == null);
		
		// Assign the datatype
		concept.setDatatype(dataType);

		// Does a concept exist with the exact same name?
		
		Concept existingConcept = Context.getConceptService().getConcept(fullName);
		if(existingConcept != null)
		{
			log.debug("Duplicate concept name found, renaming");
			ConceptName name = concept.getFullySpecifiedName(locale);
			name.setName(String.format("%s (%s)", name.getName(), code.getCode()));
		}

		// Now create a mapping to the term reference and the newly create code
		log.debug("Creating map between reference term and concept");
		ConceptMap conceptMap = new ConceptMap(referenceTerm, mapType);
		conceptMap.setConcept(concept);
		concept.addConceptMapping(conceptMap);

		// Save concept
		concept = Context.getConceptService().saveConcept(concept);
		
		log.debug("Exit: createConcept");

		return concept;
	}
	/**
	 * Create all the concepts in an enumerated vocabulary
	 */
	public List<Concept> createEnumeratedVocabularyConcepts(Class<? extends IEnumeratedVocabulary> enumeratedVocabularySource, String codeSystemOid, ANY valueToCarry) {

		if(!this.m_configuration.getAutoCreateConcepts())
			throw new IllegalStateException("Cannot create concepts according to configuration");
		
		List<Concept> retVal = new ArrayList<Concept>();
		
		// Enumerated vocabulary source set? If so seed reference terms into it if applicable
		Structure struct = (Structure)enumeratedVocabularySource.getAnnotation(Structure.class);
		String structureOid = struct.codeSystem();
		if(codeSystemOid == null || structureOid.equals(codeSystemOid)) // it is the value set we're definig
		{
			for(Field f : enumeratedVocabularySource.getFields())
				if(Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers()))
				{
					try {
						
                        IEnumeratedVocabulary value = (IEnumeratedVocabulary)f.get(null);
                        retVal.add(this.getOrCreateConcept(new CV<String>(value.getCode(), value.getCodeSystem(), null, null, f.getName(), null)));
                    }
                    catch (Exception e) {
                    }
				}
		}
		
		return retVal;
    }
	
	/**
	 * Get a concept from a coded simple value and creates a reference term in the destination system if one doesn't already exist
	 * @param getConcept
	 * @return
	 * @throws DocumentImportException 
	 */
	public Concept getConcept(CV<?> code) throws DocumentImportException
	{

		log.debug("Enter: getConcept");
		
		List<Concept> concepts = this.getConcepts(code);
		if(concepts.size() > 1)
			throw new DocumentImportException(String.format("More than one potential concept exists for %s. Unsure which one to select", code));
		
		log.debug("Exit: getConcept");
		
		if(concepts.size() > 0)
			return concepts.get(0);
		else 
			return null;
	}
	
	/**
	 * Get the concept datatype for the specified concept
	 */
	public ConceptDatatype getConceptDatatype(ANY value)
	{
		
		if(value instanceof PQ)
			return Context.getConceptService().getConceptDatatypeByUuid(ConceptDatatype.NUMERIC_UUID);
		else if(value instanceof RTO ||
				value instanceof MO)
		{
			return Context.getConceptService().getConceptDatatypeByUuid(ConceptDatatype.TEXT_UUID);
		}
		else if(value instanceof INT)
			return Context.getConceptService().getConceptDatatypeByUuid(ConceptDatatype.NUMERIC_UUID);
		else if(value instanceof ST)
			return Context.getConceptService().getConceptDatatypeByUuid(ConceptDatatype.TEXT_UUID);
		else if(value instanceof ED || value instanceof SD)
			return Context.getConceptService().getConceptDatatypeByUuid(ConceptDatatype.COMPLEX_UUID);
		else if(value instanceof TS)
			return Context.getConceptService().getConceptDatatypeByUuid(ConceptDatatype.DATE_UUID);
		else if(value instanceof CS)
			return Context.getConceptService().getConceptDatatypeByUuid(ConceptDatatype.CODED_UUID);
		else if(value instanceof BL)
			return Context.getConceptService().getConceptDatatypeByUuid(ConceptDatatype.BOOLEAN_UUID);
		else if (value instanceof CO)
		{
			if(((CO)value).getValue() != null)
				return Context.getConceptService().getConceptDatatypeByUuid(ConceptDatatype.NUMERIC_UUID);
			else
				return Context.getConceptService().getConceptDatatypeByUuid(ConceptDatatype.CODED_UUID);
		}
		else
			return Context.getConceptService().getConceptDatatypeByUuid(ConceptDatatype.N_A_UUID);
	}
	
	/**
	 * Get the root code or any one of its equivalents
	 * @param code The code or equivalent to retrieve
	 * @return The mapped concept
	 * @throws DocumentImportException 
	 */
	public Concept getConceptOrEquivalent(CE<?> code) throws DocumentImportException
	{
		Concept concept = this.getConcept(code);
		if(concept == null && code.getTranslation() != null)
			for(CE<?> translation : code.getTranslation())
			{
				concept = this.getConcept(translation);
				if(concept != null) break;
			}
		return concept;
	}

	/**
	 * Get all concepts mapped to the source term
	 */
	public List<Concept> getConcepts(CV<?> code) throws DocumentImportException
	{
		log.debug("Enter: getConcepts");
		
		// First, attempt to get the ConceptSource from the CodeSystem
		ConceptSource conceptSource = this.getOrCreateConceptSource(code.getCodeSystemName(), code.getCodeSystem(), code.getCodeSystemName(), code.getCode().getClass());
		// Now attempt to get the concept by its mapping to the OpenMRS term
		List<Concept> concept = Context.getConceptService().getConceptsByMapping(code.getCode().toString(), conceptSource.getName());
		
		log.debug("Exit : getConcepts");
		
		return concept;

	}
	
	/**
	 * Gets or creates a concept matching the code 
	 * 
	 */
	public Concept getOrCreateConcept(CV<?> code) throws DocumentImportException
	{
		log.debug("Enter: getOrCreateConcept");
		
		Concept concept = this.getConcept(code);
		// Was the concept found?
		if(concept == null && this.m_configuration.getAutoCreateConcepts())
			concept = this.createConcept(code);
		
		log.debug("Exit: getOrCreateConcept");
		
		return concept;
	}

	/**
	 * 
	 * Gets or creates the specified concept and its translations. Also creates translations
	 * which do not exist and maps terms to the root
	 * @throws DocumentImportException 
	 * 
	 */
	public Concept getOrCreateConceptAndEquivalents(CE<?> code) throws DocumentImportException 
	{
		log.debug("Enter: getOrCreateConceptAndEquivalents");

		Concept foundConcept = this.getConcept(code);
		
		if(foundConcept == null && this.m_configuration.getAutoCreateConcepts())
			foundConcept = this.createConcept(code);
		else if(foundConcept == null && !this.m_configuration.getAutoCreateConcepts())
			throw new DocumentImportException(String.format("Cannot find concept %s in source %s", code.getCode(), code.getCodeSystem()));
		
		// Now create / map equivalents
		if(code.getTranslation() != null && this.m_configuration.getAutoCreateConcepts())
		{
			for(CE<?> translation : code.getTranslation())
			{
				ConceptReferenceTerm term = this.getOrCreateReferenceTerm(translation);

				// Map already exists?
				ConceptMap conceptMap = null; 
				for(ConceptMap map : foundConcept.getConceptMappings())
					if(map.getConceptReferenceTerm().equals(term))
					{
						conceptMap = map;
						break;
					}

				if(conceptMap == null)
				{
					conceptMap = new ConceptMap(term, Context.getConceptService().getConceptMapTypeByName("SAME-AS"));
					conceptMap.setConcept(foundConcept);
					foundConcept.addConceptMapping(conceptMap);
					foundConcept = Context.getConceptService().saveConcept(foundConcept);
				}
			}
		}

		log.debug("Exit: getOrCreateConceptAndEquivalents");

		return foundConcept;
	}

	/**
	 * Create concept source if it doesn't already exist
	 * @throws DocumentImportException 
	 */
	@SuppressWarnings("unchecked")
    private ConceptSource getOrCreateConceptSource(String name, String hl7,
			String description, Class<?> enumeratedVocabularySource) throws DocumentImportException {
	
		log.debug("Enter: getOrCreateConceptSource");
		if(hl7 != null && hl7.equals(CdaHandlerConstants.CODE_SYSTEM_LOINC))
			name = "LOINC";
		else if(hl7 != null && hl7.equals(CdaHandlerConstants.CODE_SYSTEM_SNOMED))
			name = "SNOMED CT";
		else if(hl7 != null && hl7.equals(CdaHandlerConstants.CODE_SYSTEM_CIEL))
			name = "CIEL";

		// HL7 name
		if(name == null)
			name = hl7;
		
		ConceptSource conceptSource = null;
		for(ConceptSource source : Context.getConceptService().getAllConceptSources())
			if(source.getHl7Code() != null && source.getHl7Code().equals(hl7) ||
					source.getName().equals(name))
				conceptSource = source;
		
		// Create a new concept source?
		if(conceptSource == null && this.m_configuration.getAutoCreateConcepts())
		{
			conceptSource = new ConceptSource();
			conceptSource.setName(name);
			conceptSource.setHl7Code(hl7);
			if(description == null)
				description = "Automatically Created by OpenSHR";
			conceptSource.setDescription(description);
			conceptSource = Context.getConceptService().saveConceptSource(conceptSource);
			if(enumeratedVocabularySource != null && IEnumeratedVocabulary.class.isAssignableFrom(enumeratedVocabularySource))
				this.createEnumeratedVocabularyConcepts((Class<? extends IEnumeratedVocabulary>)enumeratedVocabularySource, hl7, null);
			
		}
		else if(conceptSource == null && !this.m_configuration.getAutoCreateConcepts())
			throw new DocumentImportException("Cannot create concept source");
		
		log.debug("Exit : getOrCreateConceptSource");
		
		return conceptSource;
	}
	
	/**
	 * Get a drug code from a concept
	 * @throws DocumentImportException 
	 */
	public Drug getOrCreateDrugFromConcept(CE<?> drugCode, EN name, CE<String> administrationUnitCode) throws DocumentImportException {


		// First, is there a concept matching the type?
		Concept drugConcept = this.getOrCreateDrugConcept(drugCode);
		
		// The the form (table, puffer, etc)
		Concept formCode = this.getOrCreateDrugAdministrationFormConcept(administrationUnitCode);

		// CAndidate drugs with the concept of the product
		List<Drug> candidateDrugs = Context.getConceptService().getDrugsByConcept(drugConcept);
		Drug candidateDrugWithMatchingForm = null;
		for(Drug candidate : candidateDrugs)
			if(formCode != null && formCode.equals(candidate.getDosageForm()))
				candidateDrugWithMatchingForm = candidate;
			else if(formCode == null && candidate.getDosageForm() == null)
				candidateDrugWithMatchingForm = candidate;
		
		// Process the search results
		if(candidateDrugWithMatchingForm != null)// found one
			return candidateDrugWithMatchingForm;
		else if(candidateDrugWithMatchingForm == null && this.m_configuration.getAutoCreateConcepts()) // found none and can create
		{
			Drug retVal = new Drug();
			
			// Set name
			if(name != null && !name.isNull())
				retVal.setName(name.toString());
			else
				retVal.setName(drugCode.getDisplayName());

			// SEt concept for the material
			retVal.setConcept(drugConcept);
			
			// Set admin form
			if(formCode != null)
				retVal.setDosageForm(formCode);
			
			if(!this.m_configuration.getAutoCreateConcepts())
				throw new IllegalStateException("Cannot create concepts according to configuration policy");
			
			retVal = Context.getConceptService().saveDrug(retVal);
			
			return retVal;
		}
		else
			throw new DocumentImportException("Could not reliably determine the drug to associate with this administration");
    }


	/**
	 * Get or creste a reference term
	 * @throws DocumentImportException 
	 */
	public ConceptReferenceTerm getOrCreateReferenceTerm(CV<?> code) throws DocumentImportException {

		log.debug("Enter: getOrCreateReferenceTerm");

		// First, attempt to get the ConceptSource from the CodeSystem
		ConceptSource conceptSource = this.getOrCreateConceptSource(code.getCodeSystemName(), code.getCodeSystem(), code.getCodeSystemName(), code.getCode().getClass());
		
		// Reference term exists? We create a reference a term if not 
		ConceptReferenceTerm referenceTerm = Context.getConceptService().getConceptReferenceTermByCode(code.getCode().toString(), conceptSource);
		if(referenceTerm == null && this.m_configuration.getAutoCreateConcepts())
		{
			referenceTerm = new ConceptReferenceTerm();
			referenceTerm.setCode(code.getCode().toString());
			referenceTerm.setName(code.getDisplayName());
			referenceTerm.setDescription(code.getDisplayName());
			referenceTerm.setConceptSource(conceptSource);
			referenceTerm = Context.getConceptService().saveConceptReferenceTerm(referenceTerm);
		}
		else if (referenceTerm == null && !this.m_configuration.getAutoCreateConcepts())
			throw new DocumentImportException(String.format("Cannot find specified code %s in concept source %s", code.getCode(), code.getCodeSystem()));

		log.debug("Exit: getOrCreateReferenceTerm");

		return referenceTerm;
    }

	/**
	 * Create a concept representing an RMIM (not really a code) value
	 * @throws DocumentImportException 
	 */
	public Concept getOrCreateRMIMConcept(String rmimName, ANY valueToStore) throws DocumentImportException
	{
		
		Concept concept = Context.getConceptService().getConceptByName(rmimName);
		
		if(concept == null && this.m_configuration.getAutoCreateConcepts())
		{
			Log.warn(String.format("Creating CDA RMIM concept %s", rmimName));
			ConceptClass conceptClass = Context.getConceptService().getConceptClassByName("Misc");
			
			ConceptDatatype datatype = this.getConceptDatatype(valueToStore);
					
			if(datatype.getUuid().equals(ConceptDatatype.COMPLEX_UUID))
			{
				concept = new ConceptComplex();
				((ConceptComplex)concept).setHandler("BinaryDataHandler");
			}
			else
				concept = new Concept();
			
			concept.setFullySpecifiedName(new ConceptName(rmimName, Context.getLocale()));
			concept.setVersion("CDAr2");
			concept.setConceptClass(conceptClass);
			concept.setDatatype(datatype);
			concept = Context.getConceptService().saveConcept(concept);
		}
		else if(concept == null && !this.m_configuration.getAutoCreateConcepts())
			throw new DocumentImportException(String.format("Cannot find conept %s in database", rmimName));
		
		return Context.getConceptService().getConcept(concept.getConceptId());
	}
	
	/**
	 * Get or creates a concept for a UCUM code
	 * @throws DocumentImportException 
	 */
	public Concept getOrCreateUcumConcept(String unit) throws DocumentImportException {

		// Try to get the concept source based on reference term
		CV<String> properCode = new CV<String>(unit, CdaHandlerConstants.CODE_SYSTEM_UCUM, "UCUM", null, unit, null);
		Concept concept = this.getOrCreateConcept(properCode);
		
		
		// For validation
		String doseConceptUuid = Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GP_DRUG_DOSING_UNITS_CONCEPT_UUID);
		Concept drugDoses = Context.getConceptService().getConceptByUuid(doseConceptUuid);
		if(drugDoses != null && !drugDoses.getSetMembers().contains(concept))
		{
			drugDoses.addSetMember(concept);
			drugDoses = Context.getConceptService().saveConcept(drugDoses);
		}
		
		return concept;
	}

	/**
	 * Gets a type specific concept
	 * @throws DocumentImportException 
	 */
	public Concept getTypeSpecificConcept(CE<String> code, ANY value) throws DocumentImportException {
		
		log.debug("Enter: getTypeSpecificConcept");

		List<Concept> candidateConcepts = this.getConcepts(code);
		
		
		if(value instanceof PQ) // unit specific concept map
		{
			for(Concept concept : candidateConcepts)
			{
				
				// Try to get as a numeric
				ConceptNumeric numericConcept = Context.getConceptService().getConceptNumericByUuid(concept.getUuid());
				// Does this unit need to be mapped to ucum so PQ can compare ?
				String mappedUnit = this.getUcumUnitCode(numericConcept);
				if(numericConcept != null && ((PQ)value).isUnitComparable(mappedUnit))
					return concept;
			}
			log.debug("Exit: getTypeSpecificConcept");
			return null;
		}
		else{
			ConceptDatatype conceptDatatype = this.getConceptDatatype(value);
			for(Concept c : candidateConcepts)
				if(c.getDatatype().equals(conceptDatatype))
					return c;
			
			// Final check for indicator values that can be used
			if(value instanceof CV || value instanceof BL)
				for(Concept c : candidateConcepts)
					if(c.getDatatype().getUuid().equals(ConceptDatatype.N_A_UUID))
						return c;
			return null;
		}
    }

	/**
	 * Get the ucum code from the openMRS numeric concept's units
	 */
	public String getUcumUnitCode(ConceptNumeric numericConcept) {
		// Does this unit need to be mapped to ucum so PQ can compare ?
		String mappedUnit = numericConcept.getUnits();
		if(this.m_openMrsUcumUnitMaps.containsKey(mappedUnit))
			mappedUnit = this.m_openMrsUcumUnitMaps.get(mappedUnit);
		return mappedUnit;
	}

	/**
	 * Initialize instance
	 */
	private void initializeInstance()
	{
		
		// TODO: Check to see if there is a better way of seeding concepts and maps 
		// Sources for LOINC, DEEDS, and SNOMED (commonly used in CDA) 
		try {
			this.getOrCreateConceptSource("LOINC", CdaHandlerConstants.CODE_SYSTEM_LOINC, "LOINC", null);
			this.getOrCreateConceptSource("SNOMED CT", CdaHandlerConstants.CODE_SYSTEM_SNOMED, "SNOMED-CT", null);
			this.getOrCreateConceptSource("UCUM", CdaHandlerConstants.CODE_SYSTEM_UCUM, "Universal Code for Units of Measure", null);
			this.m_narrowerThan = Context.getConceptService().getConceptMapTypeByName("NARROWER-THAN");
			this.m_sameAs = Context.getConceptService().getConceptMapTypeByName("SAME-AS");
			
			// UCUM unit maps from OPENMRS concept types commonly found
			this.m_openMrsUcumUnitMaps.put("wks", "wk");
			this.m_openMrsUcumUnitMaps.put("mmHg", "mm[Hg]");
			this.m_openMrsUcumUnitMaps.put("months", "mon");
					
		} catch (DocumentImportException e) {
			Log.error(e.getMessage(), e);
		}
	}

	/**
	 * Get or create a frequency concept 
	 * @throws DocumentImportException 
	 */
	public Concept getOrCreateFrequencyConcept(ANY frequency) throws DocumentImportException {
		if(frequency instanceof TS)
			return Context.getConceptService().getConcept(162135);
		else if(frequency instanceof PIVL)
		{
			PIVL<TS> pivlValue = (PIVL<TS>)frequency;
			
			if(pivlValue.getPhase() != null)
				throw new DocumentImportException("Cannot represent periodic intervals with phase as FrequencyConcept");
			else if(pivlValue.getPeriod() != null)
			{
				PQ dailyFreq = pivlValue.getPeriod().convert("h");
				int cid = 0;
				switch(dailyFreq.getValue().intValue())
				{
					case 24:
						cid = 162252;
						break;
					case 12:
						cid = 162251;
						break;
					case 8:
						cid = 162250;
						break;
					case 36:
						cid = 162254;
						break;
					case 48:
						cid = 162253;
						break;
					case 72:
						cid = 162255;
						break;
					default:
						int dailyFreqInt = dailyFreq.getValue().intValue();
						if(dailyFreqInt >= 1 && dailyFreqInt <= 6)
							cid = 162243 + dailyFreqInt;
						else if(pivlValue.getPeriod().convert("min").getValue().intValue() == 30) // 30 mins
							cid = 162243;
						else
							cid = this.createFrequencyConcept(dailyFreq).getId();
				} // switch
				
				Concept concept = Context.getConceptService().getConcept(cid);
				if(concept == null)
					concept = this.createFrequencyConcept(dailyFreq);
				return concept;
			}
			else
				throw new DocumentImportException("Cannot represent this period interval");
		}
		else if(frequency instanceof EIVL)
		{
			EIVL<TS> event = (EIVL<TS>)frequency;
			PQ dailyFreq = null;
			if(event.getOffset() != null && event.getOffset().getValue() != null)
				dailyFreq = event.getOffset().getValue().convert("h");
			
			int cid = 0;
			switch(event.getEvent().getCode())
			{
				case HourOfSleep:
					cid = 160863;
					break;
				case AfterMeal:
					if(dailyFreq == null)
						cid = this.getOrCreateConcept(new CV<String>(event.getEvent().getCode().getCode(), event.getEvent().getCode().getCodeSystem(), null, null, "After Meals", null)).getId();
					else if(dailyFreq.getValue().intValue() == 12)
						cid = 160860;
					else if(dailyFreq.getValue().intValue() == 8)
						cid = 160867;
					else if(dailyFreq.getValue().intValue() == 6)
						cid = 160871;
					else
						throw new DocumentImportException("Can't determine how to represent frequency");
					break;
				case BeforeMeal:
					if(dailyFreq == null)
						cid = this.getOrCreateConcept(new CV<String>(event.getEvent().getCode().getCode(), event.getEvent().getCode().getCodeSystem(), null, null, "Before Meals", null)).getId();
					else if(dailyFreq.getValue().intValue() == 12)
						cid = 160859;
					else if(dailyFreq.getValue().intValue() == 8)
						cid = 160868;
					else if(dailyFreq.getValue().intValue() == 6)
						cid = 160872;
					else
						throw new DocumentImportException("Can't determine how to represent frequency");
					break;
				case BeforeBreakfast:
				case AfterBreakfast:
				case BetweenBreakfastAndLunch:
					cid = 160865;
					break;
				case AfterDinner:
				case BeforeDinner:
				case BetweenDinnerAndSleep:
				case BetweenLunchAndDinner:
					cid = 160864;
					break;
				default:
					cid = this.getOrCreateConcept(new CV<String>(event.getEvent().getCode().getCode(), event.getEvent().getCode().getCodeSystem(), null, null, event.getEvent().getCode().getCode(), null)).getId();
			}
			
			Concept concept = Context.getConceptService().getConcept(cid);
			if(concept == null)
				concept = this.getOrCreateConcept(new CV<String>(event.getEvent().getCode().getCode(), event.getEvent().getCode().getCodeSystem(), null, null, event.getEvent().getCode().getCode(), null));
			return concept;
		}
		else
			throw new DocumentImportException(String.format("Cannot represent the frequency %s", frequency));
    }

	/**
	 * Create a PQ frequency concept
	 */
	private Concept createFrequencyConcept(PQ dailyFreq) {
		String conceptName = String.format("Every %s %s", dailyFreq.getValue(), dailyFreq.getUnit());
		Concept concept = Context.getConceptService().getConceptByName(conceptName);
		if(concept == null)
		{
			ConceptClass conceptClass = Context.getConceptService().getConceptClassByName("Frequency");
			if(conceptClass == null)
			{
				conceptClass = new ConceptClass();
				conceptClass.setName("Frequency");
				conceptClass.setDescription(super.getLocalizedString("autocreated"));
				conceptClass = Context.getConceptService().saveConceptClass(conceptClass);
			}
			concept = new Concept();
			concept.addName(new ConceptName(conceptName, Context.getLocale()));
			concept.setPreferredName(concept.getName());
			concept.setConceptClass(conceptClass);
			concept = Context.getConceptService().saveConcept(concept);
		}
		return concept;
    }

	/**
	 * Get or create route concept
	 * @throws DocumentImportException 
	 */
	public Concept getOrCreateRouteConcept(CE<String> routeCode) throws DocumentImportException {
		// Try to get the concept source based on reference term
		Concept concept = this.getOrCreateConcept(routeCode);
		
		// For validation
		String routeConceptUuid = Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GP_DRUG_ROUTES_CONCEPT_UUID);
		Concept routeCodes = Context.getConceptService().getConceptByUuid(routeConceptUuid);
		if(routeCodes != null && !routeCodes.getSetMembers().contains(concept))
		{
			routeCodes.addSetMember(concept);
			routeCodes = Context.getConceptService().saveConcept(routeCodes);
		}
		
		return concept;
	}

	/**
	 * Get or create a drug form code
	 * @throws DocumentImportException 
	 */
	public Concept getOrCreateDrugAdministrationFormConcept(CE<String> administrationUnitCode) throws DocumentImportException {
		// Try to get the concept source based on reference term
		Concept concept = this.getConceptOrEquivalent(administrationUnitCode);

		if(concept == null)
		{
			concept = this.createConcept(administrationUnitCode);
			ConceptClass conceptClass = Context.getConceptService().getConceptClassByName("Drug Form");
			if(conceptClass == null)
			{
				conceptClass = new ConceptClass();
				conceptClass.setName("Drug Form");
				conceptClass.setDescription(super.getLocalizedString("autocreated"));
				conceptClass = Context.getConceptService().saveConceptClass(conceptClass);
			}
			concept = Context.getConceptService().saveConcept(concept);
		}
		
		return concept;
    }

	/**
	 * Get or create a drug concept
	 * @throws DocumentImportException 
	 */
	public Concept getOrCreateDrugConcept(CE<?> drugCode) throws DocumentImportException {
		Concept concept = this.getConceptOrEquivalent(drugCode);

		if(concept == null)
		{
			concept = this.createConcept(drugCode);
			ConceptClass conceptClass = Context.getConceptService().getConceptClassByName("Drug");
			if(conceptClass == null)
			{
				conceptClass = new ConceptClass();
				conceptClass.setName("Drug");
				conceptClass.setDescription(super.getLocalizedString("autocreated"));
				conceptClass = Context.getConceptService().saveConceptClass(conceptClass);
			}
			concept = Context.getConceptService().saveConcept(concept);
		}
		
		return concept;
    }

	/**
	 * Get or creates the drug route question concept
	 * @return
	 * @throws DocumentImportException 
	 */
	public Concept getOrCreateDrugRouteConcept() throws DocumentImportException {
		Concept rmimConcept = this.getOrCreateRMIMConcept("Drug Route of Administration", new CV<String>());
		
		// Now, add as a set member to History of Medications
		Concept historyOfMedications = Context.getConceptService().getConcept(160741),
				medicationOrders = Context.getConceptService().getConcept(1282);
		if(historyOfMedications != null && !historyOfMedications.getSetMembers().contains(rmimConcept))
		{
			historyOfMedications.addSetMember(rmimConcept);
			historyOfMedications = Context.getConceptService().saveConcept(historyOfMedications);
		}
		if(medicationOrders != null && !historyOfMedications.getSetMembers().contains(rmimConcept))
		{
			medicationOrders.addSetMember(rmimConcept);
			medicationOrders = Context.getConceptService().saveConcept(historyOfMedications);
		
		}
		
		return rmimConcept;
		

	}

}
