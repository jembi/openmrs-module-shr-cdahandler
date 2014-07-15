package org.openmrs.module.shr.cdahandler.processor.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jfree.util.Log;
import org.marc.everest.annotations.Structure;
import org.marc.everest.datatypes.ANY;
import org.marc.everest.datatypes.ED;
import org.marc.everest.datatypes.INT;
import org.marc.everest.datatypes.MO;
import org.marc.everest.datatypes.PQ;
import org.marc.everest.datatypes.SD;
import org.marc.everest.datatypes.ST;
import org.marc.everest.datatypes.TS;
import org.marc.everest.datatypes.generic.CD;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.datatypes.generic.CS;
import org.marc.everest.datatypes.generic.CV;
import org.marc.everest.datatypes.generic.RTO;
import org.marc.everest.datatypes.interfaces.ICodedSimple;
import org.marc.everest.datatypes.interfaces.ICodedValue;
import org.marc.everest.formatters.FormatterUtil;
import org.marc.everest.interfaces.IEnumeratedVocabulary;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptReferenceTermMap;
import org.openmrs.ConceptSource;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerGlobalPropertyNames;
import org.openmrs.module.shr.cdahandler.CdaHandlerOids;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.util.OpenmrsConstants;

/**
 * A class for interacting (creating/looking up) OpenMRS concepts
 * @author Justin
 *
 */
public final class OpenmrsConceptUtil extends OpenmrsMetadataUtil {

	// singleton instance
	private static OpenmrsConceptUtil s_instance;
	private static Object s_lockObject = new Object();

	// True if concepts can be auto-created
	private Boolean m_autoCreateConcepts = true;
	private ConceptMapType m_narrowerThan = null;
	private ConceptMapType m_sameAs = null;
	
	/**
	 * Private ctor
	 */
	private OpenmrsConceptUtil()
	{
		
	}
	
	/**
	 * Initialize instance
	 */
	private void initializeInstance()
	{
		String propertyValue = Context.getAdministrationService().getGlobalProperty(CdaHandlerGlobalPropertyNames.AUTOCREATE_CONCEPTS);
		if(propertyValue != null  && !propertyValue.isEmpty())
			this.m_autoCreateConcepts = Boolean.parseBoolean(propertyValue);
		else
			Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(CdaHandlerGlobalPropertyNames.AUTOCREATE_CONCEPTS, this.m_autoCreateConcepts.toString()));
		
		// TODO: Check to see if there is a better way of seeding concepts and maps 
		// Sources for LOINC, DEEDS, and SNOMED (commonly used in CDA) 
		try {
			this.getOrCreateConceptSource("LOINC", CdaHandlerOids.CODE_SYSTEM_LOINC, "LOINC", null);
			this.getOrCreateConceptSource("SNOMED CT", CdaHandlerOids.CODE_SYSTEM_SNOMED, "SNOMED-CT", null);
			this.m_narrowerThan = Context.getConceptService().getConceptMapTypeByName("NARROWER-THAN");
			this.m_sameAs = Context.getConceptService().getConceptMapTypeByName("SAME-AS");
					
		} catch (DocumentImportException e) {
			Log.error(e.getMessage(), e);
		}
	}
	
	/**
	 * Create concept source if it doesn't already exist
	 * @throws DocumentImportException 
	 */
	private ConceptSource getOrCreateConceptSource(String name, String hl7,
			String description, Class<?> enumeratedVocabularySource) throws DocumentImportException {
	
		log.debug("Enter: getOrCreateConceptSource");
		if(name == null)
		{
			if(hl7.equals(CdaHandlerOids.CODE_SYSTEM_LOINC))
				name = "LOINC";
			else if(hl7.equals(CdaHandlerOids.CODE_SYSTEM_SNOMED))
				name = "SNOMED CT";
					
		}
		ConceptSource conceptSource = null;
		for(ConceptSource source : Context.getConceptService().getAllConceptSources())
			if(source.getHl7Code() != null && source.getHl7Code().equals(hl7) ||
					source.getName().equals(name))
				conceptSource = source;
		
		// Create a new concept source?
		if(conceptSource == null && this.m_autoCreateConcepts)
		{
			conceptSource = new ConceptSource();
			conceptSource.setName(name);
			conceptSource.setHl7Code(hl7);
			conceptSource.setDescription(description);
			conceptSource = Context.getConceptService().saveConceptSource(conceptSource);
			
			// Enumerated vocabulary source set? If so seed reference terms into it if applicable
			if(enumeratedVocabularySource != null &&
					IEnumeratedVocabulary.class.isAssignableFrom(enumeratedVocabularySource))
			{
				Structure struct = (Structure)enumeratedVocabularySource.getAnnotation(Structure.class);
				String structureOid = struct.codeSystem();
				if(structureOid.equals(hl7)) // it is the value set we're definig
				{
					for(Field f : enumeratedVocabularySource.getFields())
						if(Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers()))
						{
							try {
		                        IEnumeratedVocabulary value = (IEnumeratedVocabulary)f.get(null);
		                        if(value.getCodeSystem().equals(structureOid))
		                        {
		                        	ConceptReferenceTerm term = new ConceptReferenceTerm();
		                        	term.setCode(value.getCode());
		                        	term.setDescription(f.getName());
		                        	term.setConceptSource(conceptSource);
		                        	Context.getConceptService().saveConceptReferenceTerm(term);
		                        }
	                        }
	                        catch (Exception e) {
	                        }
							
						}
				}
			}
			
		}
		else if(conceptSource == null && !this.m_autoCreateConcepts)
			throw new DocumentImportException("Cannot create concept source");
		
		log.debug("Exit : getOrCreateConceptSource");
		
		return conceptSource;
	}

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
			throw new DocumentImportException("More than one potential concept exists. Unsure which one to select");
		
		log.debug("Exit: getConcept");
		
		if(concepts.size() > 0)
			return concepts.get(0);
		else 
			return null;
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
		if(concept == null && this.m_autoCreateConcepts)
			concept = this.createConcept(code);
		
		log.debug("Exit: getOrCreateConcept");
		
		return concept;
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
		else
			return Context.getConceptService().getConceptDatatypeByUuid(ConceptDatatype.N_A_UUID);
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
		if(!this.m_autoCreateConcepts)
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
		if(value instanceof PQ)
		{
			log.debug("Creating PQ based concept, set map to NARROWER-THAN");
			fullName += " (" + ((PQ)value).getUnit() + ")";
			concept = new ConceptNumeric();
			((ConceptNumeric)concept).setUnits(((PQ)value).getUnit());
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
		
		List<Concept> existingConcept = Context.getConceptService().getConceptsByName(fullName, locale, true);
		if(existingConcept.size() > 0)
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

		// Add a handler
		
		Context.getConceptService().saveConcept(concept);

		log.debug("Exit: createConcept");

		return concept;
	}
	
	/**
	 * Get or creste a reference term
	 * @throws DocumentImportException 
	 */
	private ConceptReferenceTerm getOrCreateReferenceTerm(CV<?> code) throws DocumentImportException {

		log.debug("Enter: getOrCreateReferenceTerm");

		// First, attempt to get the ConceptSource from the CodeSystem
		ConceptSource conceptSource = this.getOrCreateConceptSource(code.getCodeSystemName(), code.getCodeSystem(), code.getCodeSystemName(), code.getCode().getClass());
		
		// Reference term exists? We create a reference a term if not 
		ConceptReferenceTerm referenceTerm = Context.getConceptService().getConceptReferenceTermByCode(code.getCode().toString(), conceptSource);
		if(referenceTerm == null && this.m_autoCreateConcepts)
		{
			referenceTerm = new ConceptReferenceTerm();
			referenceTerm.setCode(code.getCode().toString());
			referenceTerm.setName(code.getDisplayName());
			referenceTerm.setDescription(code.getDisplayName());
			referenceTerm.setConceptSource(conceptSource);
			referenceTerm = Context.getConceptService().saveConceptReferenceTerm(referenceTerm);
		}
		else if (referenceTerm == null && !this.m_autoCreateConcepts)
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
		
		if(concept == null && this.m_autoCreateConcepts)
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
		else if(concept == null && !this.m_autoCreateConcepts)
			throw new DocumentImportException(String.format("Cannot find conept %s in database", rmimName));
		
		return Context.getConceptService().getConcept(concept.getConceptId());
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
		
		if(foundConcept == null && this.m_autoCreateConcepts)
			foundConcept = this.createConcept(code);
		else if(foundConcept == null && !this.m_autoCreateConcepts)
			throw new DocumentImportException(String.format("Cannot find concept %s in source %s", code.getCode(), code.getCodeSystem()));
		
		// Now create / map equivalents
		if(code.getTranslation() != null && this.m_autoCreateConcepts)
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
	 * Gets a type specific concept
	 * @throws DocumentImportException 
	 */
	public Concept getTypeSpecificConcept(CE<String> code, ANY value) throws DocumentImportException {
		
		log.debug("Enter: getTypeSpecificConcept");

		List<Concept> candidateConcepts = this.getConcepts(code);
		
		if(value instanceof PQ) // unit specific concept map
		{
			String unit = ((PQ)value).getUnit();
			for(Concept concept : candidateConcepts)
			{
				log.debug(String.format("Concept %s", concept.getName()));
				// Try to get as a numeric
				ConceptNumeric numericConcept = Context.getConceptService().getConceptNumericByUuid(concept.getUuid());
				if(numericConcept != null && numericConcept.getUnits().equals(unit))
						return concept;
			}
			log.debug("Exit: getTypeSpecificConcept");
			return null;
		}
		else if(candidateConcepts.size() > 0)
		{
			log.debug("Exit: getTypeSpecificConcept");
			return candidateConcepts.get(0);
		}
		else
		{
			log.debug("Exit: getTypeSpecificConcept");
			return null;
		}
    }
	
}
