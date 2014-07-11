package org.openmrs.module.shr.cdahandler.processor.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jfree.util.Log;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.datatypes.generic.CV;
import org.marc.everest.datatypes.interfaces.ICodedSimple;
import org.marc.everest.datatypes.interfaces.ICodedValue;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.shr.cdahandler.CdaHandlerGlobalPropertyNames;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;

/**
 * A class for interacting (creating/looking up) OpenMRS concepts
 * @author Justin
 *
 */
public final class OpenmrsConceptUtil {

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
			this.getOrCreateConceptSource("2.16.840.1.113883.6.1", "LN", "LOINC");
			this.getOrCreateConceptSource("2.16.840.1.113883.6.96", "SNOMED", "SNOMED-CT");
		} catch (DocumentParseException e) {
			Log.error(e.getMessage(), e);
		}
	}
	
	/**
	 * Create concept source if it doesn't already exist
	 * @throws DocumentParseException 
	 */
	private ConceptSource getOrCreateConceptSource(String name, String hl7,
			String description) throws DocumentParseException {
		
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
	 * Get a concept from a coded simple value and creates a reference term in the destination system if one doesn't already exist
	 * @param getConcept
	 * @return
	 * @throws DocumentParseException 
	 */
	public Concept getConcept(CV<?> code) throws DocumentParseException
	{
		// First, attempt to get the ConceptSource from the CodeSystem
		ConceptSource conceptSource = this.getOrCreateConceptSource(code.getCodeSystemName(), code.getCodeSystem(), code.getCodeSystemName());

		// Reference term exists?
		ConceptReferenceTerm referenceTerm = Context.getConceptService().getConceptReferenceTermByCode(code.getCode().toString(), conceptSource);
		if(referenceTerm == null && this.m_autoCreateConcepts)
		{
			referenceTerm = new ConceptReferenceTerm();
			referenceTerm.setCode(code.getCode().toString());
			referenceTerm.setDescription(code.getDisplayName());
			referenceTerm.setConceptSource(conceptSource);
			referenceTerm = Context.getConceptService().saveConceptReferenceTerm(referenceTerm);
		}
		else if(referenceTerm == null && !this.m_autoCreateConcepts)
			throw new DocumentParseException(String.format("Cannot find reference term %s in %s", code.getCode(), code.getCodeSystem()));
		
		// Now attempt to get the concept by its mapping to the OpenMRS term
		Concept concept = Context.getConceptService().getConceptByMapping(code.getCode().toString(), conceptSource.getName());

		return concept;
		
	}
	
	/**
	 * Create a concept representing an RMIM (not really a code) value
	 * @throws DocumentParseException 
	 */
	public Concept getRMIMConcept(String rmimName) throws DocumentParseException
	{
		
		Concept concept = Context.getConceptService().getConceptByName(rmimName);
		
		if(concept == null && this.m_autoCreateConcepts)
		{
			Log.warn(String.format("Creating CDA RMIM concept %s", rmimName));
			ConceptClass conceptClass = Context.getConceptService().getConceptClassByName("Misc");
			concept = new Concept();
			concept.setFullySpecifiedName(new ConceptName(rmimName, Locale.getDefault()));
			concept.setVersion("CDAr2");
			concept.setConceptClass(conceptClass);
			concept.setDatatype(Context.getConceptService().getConceptDatatypeByName("Document"));
			concept = Context.getConceptService().saveConcept(concept);
		}
		else if(concept == null && !this.m_autoCreateConcepts)
			throw new DocumentParseException(String.format("Cannot find conept %s in database", rmimName));
		return concept;
	}

	/**
	 * Get the root code or any one of its equivalents
	 * @param code The code or equivalent to retrieve
	 * @return The mapped concept
	 * @throws DocumentParseException 
	 */
	public Concept getConceptOrEquivalent(CE<?> code) throws DocumentParseException
	{
		return this.getConcept(code);
	}
	
}
