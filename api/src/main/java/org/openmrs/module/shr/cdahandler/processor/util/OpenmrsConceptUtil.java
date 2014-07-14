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
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;

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
		} catch (DocumentParseException e) {
			Log.error(e.getMessage(), e);
		}
	}
	
	/**
	 * Create concept source if it doesn't already exist
	 * @throws DocumentParseException 
	 */
	private ConceptSource getOrCreateConceptSource(String name, String hl7,
			String description, Class<?> enumeratedVocabularySource) throws DocumentParseException {
	
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
			throw new DocumentParseException("Cannot create concept source");
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
	public List<Concept> getConcepts(CV<?> code) throws DocumentParseException
	{
		// First, attempt to get the ConceptSource from the CodeSystem
		ConceptSource conceptSource = this.getOrCreateConceptSource(code.getCodeSystemName(), code.getCodeSystem(), code.getCodeSystemName(), code.getCode().getClass());
		// Now attempt to get the concept by its mapping to the OpenMRS term
		List<Concept> concept = Context.getConceptService().getConceptsByMapping(code.getCode().toString(), conceptSource.getName());
		return concept;

	}
	/**
	 * Get a concept from a coded simple value and creates a reference term in the destination system if one doesn't already exist
	 * @param getConcept
	 * @return
	 * @throws DocumentParseException 
	 */
	public Concept getConcept(CV<?> code) throws DocumentParseException
	{

		List<Concept> concepts = this.getConcepts(code);
		if(concepts.size() > 1)
			throw new DocumentParseException("More than one potential concept exists. Unsure which one to select");
		
		if(concepts.size() > 0)
			return concepts.get(0);
		else 
			return null;
	}
	
	/**
	 * Gets or creates a concept matching the code 
	 * 
	 */
	public Concept getOrCreateConcept(CV<?> code) throws DocumentParseException
	{
		Concept concept = this.getConcept(code);
		// Was the concept found?
		if(concept == null && this.m_autoCreateConcepts)
			concept = this.createConcept(code);
		
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
	 * @throws DocumentParseException 
	 */
	public Concept createConcept(CV<?> code) throws DocumentParseException {
		return this.createConcept(code, new ANY());
    }

	/**
	 * Create a concept with appropriate datatype to store the specified type of data
	 * @param code The code representing the concept
	 * @param type The type of data
	 * @return The created concept
	 * @throws DocumentParseException
	 */
	public Concept createConcept(CV<?> code, ANY value) throws DocumentParseException {
		if(!this.m_autoCreateConcepts)
			throw new IllegalStateException("Cannot create concepts according to configuration policy");

		ConceptReferenceTerm referenceTerm = this.getOrCreateReferenceTerm(code);
		
		// Concept class for auto-created concepts
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
		ConceptMapType mapType = Context.getConceptService().getConceptMapTypeByName("SAME-AS");
		ConceptDatatype dataType = this.getConceptDatatype(value);
		// Is this a numeric type that has a value? If so, we need to ensure we create it
		if(value instanceof PQ)
		{
			fullName += " (" + ((PQ)value).getUnit() + ")";
			concept = new ConceptNumeric();
			((ConceptNumeric)concept).setUnits(((PQ)value).getUnit());
			mapType = Context.getConceptService().getConceptMapTypeByName("NARROWER-THAN");
		}
		else if(dataType.getUuid().equals(ConceptDatatype.COMPLEX_UUID))
		{
			concept = new ConceptComplex();
			((ConceptComplex)concept).setHandler("BinaryDataHandler");
		}

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
		Concept existingConcept = Context.getConceptService().getConceptByName(fullName);
		if(existingConcept != null)
		{
			ConceptName name = concept.getFullySpecifiedName(locale);
			name.setName(String.format("%s (%s)", name.getName(), code.getCode()));
		}

		// Now create a mapping to the term reference and the newly create code
		ConceptMap conceptMap = new ConceptMap(referenceTerm, mapType);
		conceptMap.setConcept(concept);
		concept.addConceptMapping(conceptMap);

		// Add a handler
		
		Context.getConceptService().saveConcept(concept);
		
		return concept;
	}
	
	/**
	 * Get or creste a reference term
	 * @throws DocumentParseException 
	 */
	private ConceptReferenceTerm getOrCreateReferenceTerm(CV<?> code) throws DocumentParseException {

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
			throw new DocumentParseException(String.format("Cannot find specified code %s in concept source %s", code.getCode(), code.getCodeSystem()));
		return referenceTerm;
    }

	/**
	 * Create a concept representing an RMIM (not really a code) value
	 * @throws DocumentParseException 
	 */
	public Concept getOrCreateRMIMConcept(String rmimName, ANY valueToStore) throws DocumentParseException
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
			throw new DocumentParseException(String.format("Cannot find conept %s in database", rmimName));
		
		return Context.getConceptService().getConcept(concept.getConceptId());
	}

	/**
	 * Get the root code or any one of its equivalents
	 * @param code The code or equivalent to retrieve
	 * @return The mapped concept
	 * @throws DocumentParseException 
	 */
	public Concept getConceptOrEquivalent(CE<?> code) throws DocumentParseException
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
	 * @throws DocumentParseException 
	 * 
	 */
	public Concept getOrCreateConceptAndEquivalents(CE<?> code) throws DocumentParseException 
	{
		Concept foundConcept = this.getConcept(code);
		
		if(foundConcept == null && this.m_autoCreateConcepts)
			foundConcept = this.createConcept(code);
		else if(foundConcept == null && !this.m_autoCreateConcepts)
			throw new DocumentParseException(String.format("Cannot find concept %s in source %s", code.getCode(), code.getCodeSystem()));
		
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
						conceptMap = map;

				if(conceptMap == null)
				{
					conceptMap = new ConceptMap(term, Context.getConceptService().getConceptMapTypeByName("SAME-AS"));
					conceptMap.setConcept(foundConcept);
					foundConcept.addConceptMapping(conceptMap);
					Context.getConceptService().saveConcept(foundConcept);
				}
			}
		}

		return foundConcept;
	}

	/**
	 * Gets a type specific concept
	 * @throws DocumentParseException 
	 */
	public Concept getTypeSpecificConcept(CE<String> code, ANY value) throws DocumentParseException {
		
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
			return null;
		}
		else if(candidateConcepts.size() > 0)
			return candidateConcepts.get(0);
		else 
			return null;

    }
	
}
