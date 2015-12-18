package org.openmrs.module.shr.cdahandler.processor.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.util.Log;
import org.marc.everest.datatypes.ANY;
import org.marc.everest.datatypes.BL;
import org.marc.everest.datatypes.CO;
import org.marc.everest.datatypes.ED;
import org.marc.everest.datatypes.EN;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.INT;
import org.marc.everest.datatypes.MO;
import org.marc.everest.datatypes.PQ;
import org.marc.everest.datatypes.SD;
import org.marc.everest.datatypes.ST;
import org.marc.everest.datatypes.TEL;
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
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.api.CdaImportService;
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

	// Maps between oids and code systems which may not have an hl7Code
	private static final Map<String, String> s_conceptSourceNameMap = new HashMap<String, String>() {{
		put("LOINC", CdaHandlerConstants.CODE_SYSTEM_LOINC);
		put("SNOMED CT", CdaHandlerConstants.CODE_SYSTEM_SNOMED);
		put("CIEL", CdaHandlerConstants.CODE_SYSTEM_CIEL);
		put("HL-7 CVX", CdaHandlerConstants.CODE_SYSTEM_CVX);
		put("RxNORM", CdaHandlerConstants.CODE_SYSTEM_RXNORM);
		put("ICD-10-WHO", CdaHandlerConstants.CODE_SYSTEM_ICD_10);
	}};
	
	// Map types
	private ConceptMapType m_narrowerThan = null;
	private ConceptMapType m_sameAs = null;
	
	// Configuration and util instances
	private final CdaHandlerConfiguration m_configuration = CdaHandlerConfiguration.getInstance();
		
	// pq Unit maps
	private final Map<String, String> m_openMrsUcumUnitMaps = new HashMap<String,String>();

	// OpenMRS Services
	private final ConceptService m_conceptService = Context.getConceptService();
	private final CdaImportService m_importService =  Context.getService(CdaImportService.class);
	
	// Concept datatypes
	private final ConceptDatatype N_A_DATATYPE = this.m_conceptService.getConceptDatatypeByUuid(ConceptDatatype.N_A_UUID);
	private final ConceptDatatype NUMERIC_DATATYPE = this.m_conceptService.getConceptDatatypeByUuid(ConceptDatatype.NUMERIC_UUID);
	private final ConceptDatatype CODED_DATATYPE = this.m_conceptService.getConceptDatatypeByUuid(ConceptDatatype.CODED_UUID);
	private final ConceptDatatype COMPLEX_DATATYPE = this.m_conceptService.getConceptDatatypeByUuid(ConceptDatatype.COMPLEX_UUID);
	private final ConceptDatatype DATE_DATATYPE = this.m_conceptService.getConceptDatatypeByUuid(ConceptDatatype.DATE_UUID);
	private final ConceptDatatype TEXT_DATATYPE = this.m_conceptService.getConceptDatatypeByUuid(ConceptDatatype.TEXT_UUID);
	private final ConceptDatatype BOOLEAN_DATATYPE = this.m_conceptService.getConceptDatatypeByUuid(ConceptDatatype.BOOLEAN_UUID);
	
	
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
		
		if(this.m_configuration.getValidateConceptStructure())
		{
			// Is the concept in the list of answers?
			ConceptAnswer answer = null;
			questionConcept = Context.getConceptService().getConcept(questionConcept.getConceptId());
			for(ConceptAnswer ans : questionConcept.getAnswers())
			{
				log.debug(String.format("Existing Answer: %s for %s", ans.getAnswerConcept().getId(), questionConcept));
				if(ans.getAnswerConcept().getId().equals(answerConcept.getId()))
				{
					answer = ans;
					break;
				}
			}
			if(answer == null && this.m_configuration.getAutoCreateConcepts())
			{
				answer = new ConceptAnswer();
				answer.setAnswerConcept(answerConcept);
				answer.setConcept(questionConcept);
				questionConcept.addAnswer(answer);
				synchronized (s_lockObject) {
					log.debug(String.format("Added answer %s to set %s", answerConcept, questionConcept));
	
					questionConcept = this.m_importService.saveConcept(questionConcept);
	            }
			}
			else if(answer == null)
				throw new DocumentImportException(String.format("Cannot assign code %s to observation concept %s as it is not a valid value", answerConcept, questionConcept));
			// Set the value
		}
	}

	/**
	 * Add the concept to the set concept
	 */
	public void addConceptToSet(Concept setConcept, Concept concept) {

		if(this.m_configuration.getValidateConceptStructure())
		{
			Boolean needsSave = false, 
					hasMember = false; 
			
			// First is the setConcept already a set?
			if(!setConcept.isSet() && this.m_configuration.getAutoCreateConcepts())
			{
				setConcept.setSet(true);
				setConcept.setConceptClass(this.m_conceptService.getConceptClassByUuid(ConceptClass.CONVSET_UUID));
				needsSave = true;
			}
			else if(!setConcept.isSet())
				log.warn("Cannot convert Concept to a set!");
			
			// Now does the concept already exist in the set members?
			for(Concept setMember : setConcept.getSetMembers())
				if(setMember != null && setMember.getId().equals(concept.getId()))
				{
					hasMember = true;
					break;
				}
			
			if(!hasMember && this.m_configuration.getAutoCreateConcepts()) // Add to the set
			{
				setConcept.addSetMember(concept);
				needsSave = true;
			}
			else if(!this.m_configuration.getAutoCreateConcepts())
				log.warn("Cannot add concept to the specified concept set according to configuration rules");
	
			if(needsSave)
				synchronized (s_lockObject) {
					log.debug(String.format("Adding concept %s to set %s", concept, setConcept));
	
					setConcept = this.m_importService.saveConcept(setConcept);
	            }
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
		if(referenceTerm == null)
			return null;
		// Concept class for auto-created concepts
		log.debug("Get Concept Class");
		ConceptClass conceptClass = this.m_conceptService.getConceptClassByUuid(value == null ? ConceptClass.CONVSET_UUID : CdaHandlerConstants.UUID_CONCEPT_CLS_AUTO);
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
		//if(code.getDisplayName() != null)
		//	concept.addName(new ConceptName(code.getDisplayName(), locale));
		concept.setPreferredName(concept.getFullySpecifiedName(locale));
		concept.setShortName(new ConceptName(code.getCode().toString(), Context.getLocale()));
		concept.setVersion(code.getCodeSystemVersion());
		concept.setConceptClass(conceptClass);
		concept.setSet(value == null);
		
		// Assign the datatype
		concept.setDatatype(dataType);
		// Does a concept exist with the exact same name?
		Concept existingConcept = this.m_conceptService.getConcept(fullName);
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
		synchronized (s_lockObject) {
			log.debug(String.format("Creating Concept %s, reference term %s, concept source %s", concept, referenceTerm, referenceTerm.getConceptSource()));
			concept = this.m_importService.saveConcept(concept);
        }
		
		log.debug("Exit: createConcept");

		return concept;
	}
	/**
	 * Create all the concepts in an enumerated vocabulary
	 */
	public List<Concept> createEnumeratedVocabularyConcepts(Class<? extends IEnumeratedVocabulary> enumeratedVocabularySource, String codeSystemOid, ANY valueToCarry) {

		/*
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
*/		
		return new ArrayList<Concept>();
    }
	
	/**
	 * Get a concept from a coded simple value and creates a reference term in the destination system if one doesn't already exist
	 * @param getConcept
	 * @return
	 * @throws DocumentImportException 
	 */
	public Concept getConcept(CV<?> code, ANY valueToStore) throws DocumentImportException
	{

		log.debug("Enter: getConcept");
		
		List<Concept> concepts = this.getConcepts(code);
		if(concepts.size() > 1)
		{
			ConceptDatatype cdt = this.getConceptDatatype(valueToStore);
			for(Concept c : concepts)
			{
				if(cdt.getId().equals(c.getDatatype().getId()))
					return c;
			}
			throw new DocumentImportException(String.format("More than one potential concept exists for %s. Unsure which one to select", code));
		}
		
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
			return this.NUMERIC_DATATYPE;
		else if(value instanceof RTO ||
				value instanceof MO )
		{
			return this.TEXT_DATATYPE;
		}
		else if(value instanceof INT)
			return this.NUMERIC_DATATYPE;
		else if(value instanceof ST || value instanceof II || value instanceof TEL)
			return this.TEXT_DATATYPE;
		else if(value instanceof ED || value instanceof SD )
			return this.COMPLEX_DATATYPE;
		else if(value instanceof TS)
			return this.DATE_DATATYPE;
		else if(value instanceof CS)
			return this.CODED_DATATYPE;
		else if(value instanceof BL)
			return this.BOOLEAN_DATATYPE;
		else if(value instanceof CO) // will change based on data (can't be cached)
		{
			if(((CO)value).getValue() != null)
				return this.NUMERIC_DATATYPE;
			else
				return this.CODED_DATATYPE;
		}
		else
			return this.N_A_DATATYPE;
	}
	
	/**
	 * Get the root code or any one of its equivalents
	 * @param code The code or equivalent to retrieve
	 * @return The mapped concept
	 * @throws DocumentImportException 
	 */
	public Concept getConceptOrEquivalent(CE<?> code) throws DocumentImportException
	{
		
		Concept concept = null;
		if(!code.isNull())
			concept = this.getConcept(code, null);
		if(concept == null && code.getTranslation() != null)
			for(CE<?> translation : code.getTranslation())
			{
				concept = this.getConcept(translation, null);
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

		List<Concept> concept = this.m_importService.getConceptsByMapping(this.getOrCreateReferenceTerm(code), "SAME-AS");
		if(concept == null || concept.size() == 0)
			concept = this.m_importService.getConceptsByMapping(this.getOrCreateReferenceTerm(code), "NARROWER-THAN");
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
		
		Concept concept = this.getConcept(code, null);
		// Was the concept found?
		if(concept == null && this.m_configuration.getAutoCreateConcepts())
			concept = this.createConcept(code, null);
		
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

		Concept foundConcept = this.getConceptOrEquivalent(code);

		if(!code.isNull())
		{
			if(foundConcept == null && this.m_configuration.getAutoCreateConcepts())
				foundConcept = this.createConcept(code);
			else if(foundConcept == null && !this.m_configuration.getAutoCreateConcepts())
				throw new DocumentImportException(String.format("MISSING CONCEPT: Cannot find concept %s in source %s", code.getCode(), code.getCodeSystem()));
		}
		
		// Was there a concept found? If no, then find one!
		if(foundConcept == null && code.getTranslation() != null)
			for(CE<?> translation : code.getTranslation())
			{
				foundConcept = this.getConcept(translation, null);
				if(foundConcept != null) break;
			}
		
		// Now create / map equivalents
		if(code.getTranslation() != null && this.m_configuration.getAutoCreateConcepts())
		{
			for(CE<?> translation : code.getTranslation())
			{
				ConceptReferenceTerm term = this.getOrCreateReferenceTerm(translation);

				// Map already exists?
				ConceptMap conceptMap = null; 

				// A found concept at all?
				if(foundConcept == null)
				{
					if(this.m_configuration.getAutoCreateConcepts())
						foundConcept = this.createConcept(translation);
					else
						throw new DocumentImportException(String.format("MISSING CONCEPT: Cannot find concept %s in source %s", code.getCode(), code.getCodeSystem()));
				}
					
				// Map
				for(ConceptMap map : foundConcept.getConceptMappings())
					if(map.getConceptReferenceTerm().equals(term))
					{
						conceptMap = map;
						break;
					}

				// Create a map if not exists
				if(conceptMap == null)
				{
					conceptMap = new ConceptMap(term, this.m_conceptService.getConceptMapTypeByName("SAME-AS"));
					conceptMap.setConcept(foundConcept);
					foundConcept.addConceptMapping(conceptMap);
					synchronized (s_lockObject) {
						foundConcept = this.m_importService.saveConcept(foundConcept);
                    }
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
		name = this.mapOidToConceptSourceName(hl7);
		if (name==null || name.trim().isEmpty()) {
			name = "Unknown";
		}
		
		
		ConceptSource conceptSource = this.m_conceptService.getConceptSourceByName(name);
		if(conceptSource == null)
			conceptSource = this.m_importService.getConceptSourceByHl7(hl7);
		
		// Create a new concept source?
		if(conceptSource == null && this.m_configuration.getAutoCreateConcepts())
		{
			conceptSource = new ConceptSource();
			conceptSource.setName(name);
			conceptSource.setHl7Code(hl7);
			if(description == null)
				description = "Automatically Created by OpenSHR";
			conceptSource.setDescription(description);
			
			synchronized (s_lockObject) {
				log.debug(String.format("Saving Concept Source %s", conceptSource));
				conceptSource = this.m_conceptService.saveConceptSource(conceptSource);
            }

			if(enumeratedVocabularySource != null && IEnumeratedVocabulary.class.isAssignableFrom(enumeratedVocabularySource))
				this.createEnumeratedVocabularyConcepts((Class<? extends IEnumeratedVocabulary>)enumeratedVocabularySource, hl7, null);
			
		}
		else if(conceptSource == null && !this.m_configuration.getAutoCreateConcepts())
			throw new DocumentImportException(String.format("MISSING SOURCE: Cannot create concept source %s", hl7));
		
		log.debug("Exit : getOrCreateConceptSource");
		
		return conceptSource;
	}
	
	/**
	 * Map a concept source name (SNOMED CT) to an OID 
	 */
	public String mapConceptSourceNameToOid(String name) {
		String oid = s_conceptSourceNameMap.get(name);
		if(oid != null)
			return oid;
		return name;
    }
	
	/**
	 * Map a OID to a concept source name 
	 */
	public String mapOidToConceptSourceName(String oid) {
		for(Entry<String,String> ent : s_conceptSourceNameMap.entrySet())
			if(ent.getValue().equals(oid))
				return ent.getKey();
		return oid;
    }
	/**
	 * Get a drug code from a concept
	 * @throws DocumentImportException 
	 */
	public Drug getOrCreateDrugFromConcept(CE<?> drugCode, EN name, CE<String> administrationUnitCode) throws DocumentImportException {


		// First, is there a concept matching the type?
		Concept drugConcept = null;
		drugConcept = this.getOrCreateDrugConcept(drugCode);
		
		if(drugConcept == null && name != null) // Find by a name? Maybe?
			drugConcept = this.m_conceptService.getConcept(name.toString());
		
		// HACK: Fallback
		if(this.m_configuration.getAutoCreateConcepts() && drugConcept == null)
		{
			// Create the concept and set properties
			drugConcept = new Concept();
			Locale locale = Context.getLocale();// new Locale("en");
			if(name != null)
				drugConcept.setFullySpecifiedName(new ConceptName(name.toString(), locale));
			else
				drugConcept.setFullySpecifiedName(new ConceptName(drugCode.getDisplayName(), locale));
			drugConcept.addName(drugConcept.getFullySpecifiedName(locale));
			drugConcept.setPreferredName(drugConcept.getFullySpecifiedName(locale));
			drugConcept.setConceptClass(this.m_conceptService.getConceptClassByUuid(ConceptClass.DRUG_UUID));
			// Assign the datatype
			drugConcept.setDatatype(this.m_conceptService.getConceptDatatypeByUuid(ConceptDatatype.N_A_UUID));
			drugConcept = this.m_importService.saveConcept(drugConcept);
		}
			
		// The the form (table, puffer, etc)
		Concept formCode = null;
		if(administrationUnitCode != null)
			formCode = this.getOrCreateDrugAdministrationFormConcept(administrationUnitCode);

		// CAndidate drugs with the concept of the product
		List<Drug> candidateDrugs = this.m_conceptService.getDrugsByConcept(drugConcept);
		Drug candidateDrugWithMatchingForm = null;
		for(Drug candidate : candidateDrugs)
			// Name and form match
			if(name != null && name.toString().equals(candidate.getName()) &&
					(formCode != null && formCode.equals(candidate.getDosageForm()) || formCode == candidate.getDosageForm()))
				return candidate; // Found a drug with the same name!
			else if(formCode != null && formCode.equals(candidate.getDosageForm()))
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
			
			synchronized (s_lockObject) {
				log.debug(String.format("Saving drug %s", retVal));

				retVal = this.m_conceptService.saveDrug(retVal);
            }
			
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
		if(code == null || code.getCode() == null)
			return null;
		
		ConceptSource conceptSource = this.getOrCreateConceptSource(code.getCodeSystemName(), code.getCodeSystem(), code.getCodeSystemName(), code.getCode().getClass());
		
		// Reference term exists? We create a reference a term if not 
		ConceptReferenceTerm referenceTerm = this.m_conceptService.getConceptReferenceTermByCode(code.getCode().toString(), conceptSource);
		if(referenceTerm == null && this.m_configuration.getAutoCreateConcepts())
		{
			referenceTerm = new ConceptReferenceTerm();
			referenceTerm.setCode(code.getCode().toString());
			referenceTerm.setName(code.getDisplayName());
			referenceTerm.setDescription(code.getDisplayName());
			referenceTerm.setConceptSource(conceptSource);
			synchronized (s_lockObject) {
				log.debug(String.format("Saving Reference term %s", referenceTerm));
				referenceTerm = this.m_importService.saveConceptReferenceTerm(referenceTerm);
            }
		}
		else if (referenceTerm == null && !this.m_configuration.getAutoCreateConcepts())
			throw new DocumentImportException(String.format("MISSING CONCEPT: Cannot find specified code %s in concept source %s", code.getCode(), code.getCodeSystem()));

		log.debug("Exit: getOrCreateReferenceTerm");

		return referenceTerm;
    }

	/**
	 * Create a concept representing an RMIM (not really a code) value
	 * @throws DocumentImportException 
	 */
	public Concept getOrCreateRMIMConcept(String rmimUuid, ANY valueToStore) throws DocumentImportException
	{
		
		Concept concept = this.m_conceptService.getConceptByUuid(rmimUuid);
		
		if(concept == null && this.m_configuration.getAutoCreateConcepts())
		{
			Log.warn(String.format("Creating CDA RMIM concept %s", rmimUuid));
			ConceptClass conceptClass = this.m_conceptService.getConceptClassByUuid(ConceptClass.MISC_UUID);
			
			ConceptDatatype datatype = this.getConceptDatatype(valueToStore);
					
			if(datatype.getUuid().equals(ConceptDatatype.COMPLEX_UUID))
			{
				concept = new ConceptComplex();
				((ConceptComplex)concept).setHandler("BinaryDataHandler");
			}
			else
				concept = new Concept();
			
			concept.setFullySpecifiedName(new ConceptName(rmimUuid, Context.getLocale()));
			concept.setUuid(rmimUuid);
			concept.setVersion("CDAr2");
			concept.setConceptClass(conceptClass);
			concept.setDatatype(datatype);
			synchronized (s_lockObject) {
				log.debug(String.format("Saving RMIM Concept %s", concept));
				concept = this.m_conceptService.saveConcept(concept);
            }
		}
		else if(concept == null && !this.m_configuration.getAutoCreateConcepts())
			throw new DocumentImportException(String.format("MISSING CONCEPT: Cannot find conept %s in database", rmimUuid));
		else if(concept.getNames().size() == 0)
		{
			concept.setFullySpecifiedName(new ConceptName(rmimUuid, Context.getLocale()));
			synchronized (s_lockObject) {
				log.debug(String.format("Saving RMIM Concept %s", concept));
				concept = this.m_conceptService.saveConcept(concept);
            }
		}
		return this.m_conceptService.getConcept(concept.getConceptId());
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
		Concept drugDoses = this.m_conceptService.getConceptByUuid(doseConceptUuid);
		if(drugDoses != null && !drugDoses.getSetMembers().contains(concept))
		{
			drugDoses.addSetMember(concept);
			synchronized (s_lockObject) {
				drugDoses = this.m_importService.saveConcept(drugDoses);
            }
		}
		
		return concept;
	}

	/**
	 * Gets a type specific concept
	 * @throws DocumentImportException 
	 */
	public Concept getTypeSpecificConcept(CE<?> code, ANY value) throws DocumentImportException {
		
		log.debug("Enter: getTypeSpecificConcept");

		List<Concept> candidateConcepts = this.getConcepts(code);
		
		
		if(value instanceof PQ) // unit specific concept map
		{
			for(Concept concept : candidateConcepts)
			{
				
				// Try to get as a numeric
				ConceptNumeric numericConcept = this.m_conceptService.getConceptNumericByUuid(concept.getUuid());
				if(numericConcept == null)
					continue;
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
			this.m_narrowerThan = this.m_conceptService.getConceptMapTypeByName("NARROWER-THAN");
			this.m_sameAs = this.m_conceptService.getConceptMapTypeByName("SAME-AS");
			
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
		if(frequency == null || frequency.isNull())
		{
			return this.m_conceptService.getConcept(CdaHandlerConstants.CONCEPT_ID_UNSPECIFIED);
		}
		else if(frequency instanceof TS)
			return this.m_conceptService.getConcept(CdaHandlerConstants.MEDICATION_FREQUENCY_ONCE);
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
						if(pivlValue.getPeriod().getUnit().equals("d")) // originally once per day
							cid = CdaHandlerConstants.MEDICATION_FREQUENCY_ONCE_DAILY;
						else
							cid = CdaHandlerConstants.MEDICATION_FREQUENCY_24_HOURS;
						break;
					case 12:
						cid = CdaHandlerConstants.MEDICATION_FREQUENCY_12_HOURS;
						break;
					case 8:
						cid = CdaHandlerConstants.MEDICATION_FREQUENCY_8_HOURS;
						break;
					case 36:
						cid = CdaHandlerConstants.MEDICATION_FREQUENCY_36_HOURS;
						break;
					case 48:
						cid = CdaHandlerConstants.MEDICATION_FREQUENCY_48_HOURS;
						break;
					case 72:
						cid = CdaHandlerConstants.MEDICATION_FREQUENCY_72_HOURS;
						break;
					default:
						int dailyFreqInt = dailyFreq.getValue().intValue();
						if(dailyFreqInt >= 1 && dailyFreqInt <= 6)
							cid = CdaHandlerConstants.MEDICATION_FREQUENCY_30_MINS + dailyFreqInt;
						else if(pivlValue.getPeriod().convert("min").getValue().intValue() == 30) // 30 mins
							cid = CdaHandlerConstants.MEDICATION_FREQUENCY_30_MINS;
						else
							cid = this.createFrequencyConcept(dailyFreq).getId();
				} // switch
				
				Concept concept = this.m_conceptService.getConcept(cid);
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
					cid = CdaHandlerConstants.MEDICATION_FREQUENCY_AT_BEDTIME;
					break;
				case AfterMeal:
					if(dailyFreq == null)
						cid = CdaHandlerConstants.MEDICATION_FREQUENCY_THRICE_DAILY_AFTER_MEALS;
					else if(dailyFreq.getValue().intValue() == 12)
						cid = CdaHandlerConstants.MEDICATION_FREQUENCY_TWICE_DAILY_AFTER_MEALS;
					else if(dailyFreq.getValue().intValue() == 8)
						cid = CdaHandlerConstants.MEDICATION_FREQUENCY_THRICE_DAILY_AFTER_MEALS;
					else if(dailyFreq.getValue().intValue() == 6)
						cid = CdaHandlerConstants.MEDICATION_FREQUENCY_FOUR_TIMES_DAILY_AFTER_MEALS;
					else
						throw new DocumentImportException("Can't determine how to represent frequency");
					break;
				case BeforeMeal:
					if(dailyFreq == null)
						cid = CdaHandlerConstants.MEDICATION_FREQUENCY_THRICE_DAILY_BEFORE_MEALS;
					else if(dailyFreq.getValue().intValue() == 12)
						cid = CdaHandlerConstants.MEDICATION_FREQUENCY_TWICE_DAILY_BEFORE_MEALS;
					else if(dailyFreq.getValue().intValue() == 8)
						cid = CdaHandlerConstants.MEDICATION_FREQUENCY_THRICE_DAILY_BEFORE_MEALS;
					else if(dailyFreq.getValue().intValue() == 6)
						cid = CdaHandlerConstants.MEDICATION_FREQUENCY_FOUR_TIMES_DAILY_BEFORE_MEALS;
					else
						throw new DocumentImportException("Can't determine how to represent frequency");
					break;
				case BeforeBreakfast:
					cid = CdaHandlerConstants.MEDICATION_FREQUENCY_ONCE_DAILY_MORNING;;
					break;
				case BetweenDinnerAndSleep:
					cid = CdaHandlerConstants.MEDICATION_FREQUENCY_ONCE_DAILY_EVENING;
					break;
				default:
					cid = this.getOrCreateConcept(new CV<String>(event.getEvent().getCode().getCode(), event.getEvent().getCode().getCodeSystem(), null, null, event.getEvent().getCode().getCode(), null)).getId();
			}
			
			Concept concept = this.m_conceptService.getConcept(cid);
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
		Concept concept = this.m_conceptService.getConceptByName(conceptName);
		if(concept == null)
		{
			ConceptClass conceptClass = this.m_conceptService.getConceptClassByUuid(ConceptClass.FREQUENCY_UUID);
			if(conceptClass == null)
			{
				conceptClass = new ConceptClass();
				conceptClass.setName("Frequency");
				conceptClass.setUuid(ConceptClass.FREQUENCY_UUID);
				conceptClass.setDescription("Automatically created");
				synchronized (s_lockObject) {
					conceptClass = this.m_conceptService.saveConceptClass(conceptClass);
                }
			}
			concept = new Concept();
			concept.addName(new ConceptName(conceptName, Context.getLocale()));
			concept.setPreferredName(concept.getName());
			concept.setConceptClass(conceptClass);
			synchronized (s_lockObject) {
				concept = this.m_importService.saveConcept(concept);
            }
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
		Concept routeCodes = this.m_conceptService.getConceptByUuid(routeConceptUuid);
		if(routeCodes != null && !routeCodes.getSetMembers().contains(concept))
		{
			routeCodes.addSetMember(concept);
			synchronized (s_lockObject) {
				routeCodes = this.m_importService.saveConcept(routeCodes);
            }
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
			ConceptClass conceptClass = this.m_conceptService.getConceptClassByUuid(CdaHandlerConstants.UUID_CONCEPT_CLS_DRUG_FORM);
			if(conceptClass == null)
			{
				conceptClass = new ConceptClass();
				conceptClass.setName("Drug Form");
				conceptClass.setDescription("Automatically created");
				synchronized (s_lockObject) {
					conceptClass = this.m_conceptService.saveConceptClass(conceptClass);
                }
			}
			synchronized (s_lockObject) {
				concept = this.m_importService.saveConcept(concept);
            }
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
			concept = this.getOrCreateConceptAndEquivalents(drugCode);
			ConceptClass conceptClass = this.m_conceptService.getConceptClassByUuid(ConceptClass.DRUG_UUID);
			if(conceptClass == null)
			{
				conceptClass = new ConceptClass();
				conceptClass.setName("Drug");
				conceptClass.setUuid(ConceptClass.DRUG_UUID);
				conceptClass.setDescription("Automatically created");
				synchronized (s_lockObject) {
					conceptClass = this.m_conceptService.saveConceptClass(conceptClass);
                }
			}
			// Make this a drug
			if(concept != null && !concept.getConceptClass().equals(conceptClass))
			{
				concept.setConceptClass(conceptClass);
				concept = this.m_importService.saveConcept(concept);
			}
		}
		
		return concept;
    }

	/**
	 * Get or creates the drug route question concept
	 * @return
	 * @throws DocumentImportException 
	 */
	public Concept getOrCreateDrugRouteConcept() throws DocumentImportException {
		Concept rmimConcept = this.getOrCreateRMIMConcept(CdaHandlerConstants.RMIM_CONCEPT_UUID_ROUTE_OF_ADM, new CV<String>());
		
		// Now, add as a set member to History of Medications
		Concept historyOfMedications = this.m_conceptService.getConcept(CdaHandlerConstants.CONCEPT_ID_MEDICATION_HISTORY),
				historyOfIz = this.m_conceptService.getConcept(CdaHandlerConstants.CONCEPT_ID_IMMUNIZATION_HISTORY);
		
		if(historyOfMedications != null)
			this.addConceptToSet(historyOfMedications, rmimConcept);
		if(historyOfIz != null)
			this.addConceptToSet(historyOfIz, rmimConcept);
		
		return rmimConcept;

	}

}
