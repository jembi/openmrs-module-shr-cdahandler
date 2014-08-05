package org.openmrs.module.shr.cdahandler.processor.context;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.openmrs.EncounterRole;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.module.shr.cdahandler.processor.document.DocumentProcessor;

/**
 * Represents a specialized context from the DocumentProcessor
 * @author Justin
 *
 */
public class DocumentProcessorContext extends ProcessorContext {

	// Authors parsed at root of document
	// This is here because the authors "cascade" down RMIM relationships in the document
	private Map<EncounterRole, Set<Provider>> m_providers;
	// The data enterer
	private Provider m_dataEnterer;
	
	/**
	 * Constructs the document processor context
	 * @param rawObject The raw document being processed
	 * @param parsedObject The processed object
	 * @param parser The processor being used
	 */
	public DocumentProcessorContext(ClinicalDocument rawObject,
			Visit parsedObject, DocumentProcessor processor) {
		super(rawObject, parsedObject, processor);
		this.m_providers = new HashMap<EncounterRole, Set<Provider>>();
	}

	/**
	 * Add a provider to the visit context
	 * @param role
	 * @param provider
	 */
	public void addProvider(EncounterRole role, Provider provider) {
		Set<Provider> currentSet = this.m_providers.get(role);
		if(currentSet == null)
		{
			currentSet = new HashSet<Provider>();
			this.m_providers.put(role, currentSet);
		}
		currentSet.add(provider);
	}
	
	/**
	 * Get the clinical document
	 * @return
	 */
	public ClinicalDocument getDocument() {
		return (ClinicalDocument)this.getRawObject();
    }

	/**
	 * Gets the parsed object as a visit
	 */
	public Visit getParsedVisit() {
		return (Visit)this.getParsedObject();
	}

	/**
	 * Gets all providers parsed from the root context
	 */
	public Map<EncounterRole, Set<Provider>> getProviders() { return this.m_providers; }

}
