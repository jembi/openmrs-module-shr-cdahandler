package org.openmrs.module.shr.cdahandler.processor.factory.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.generic.LIST;
import org.marc.everest.datatypes.interfaces.IPredicate;
import org.openmrs.module.shr.cdahandler.processor.Processor;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

/**
 * A utility class that handles the scanning of classpath
 * @author Justin
 *
 */
public final class ClasspathScannerUtil {

	
	/**
	 * Predicate used to find best match
	 */
	private class TemplateMatcher implements IPredicate<II>
	{

		private LIST<II> m_sourceTemplateIdList;
		
		/**
		 * Constructs the template matcher
		 */
		public TemplateMatcher(LIST<II> sourceTemplateIds)
		{
			this.m_sourceTemplateIdList = sourceTemplateIds;
		}
		
		/**
		 * Perform match
		 */
		@Override
        public boolean match(II arg0) {
			for(II id : this.m_sourceTemplateIdList)
				if(id.semanticEquals(arg0).toBoolean())
					return true; 
			return false;
        }
		
	}
	
	/**
	 * Gets the current singleton instance
	 */
	public final static ClasspathScannerUtil getInstance()
	{
		if(s_instance == null)
		{
			synchronized (s_lockObject) {
				if(s_instance == null) // double check as someone else may have created this
				{
					s_instance = new ClasspathScannerUtil();
					s_instance.initializeProcessorList();
				}
			}
		}
		return s_instance;
	}

	// Log
	private final Log log = LogFactory.getLog(this.getClass());
	// Classpath scanning utility
	private static ClasspathScannerUtil s_instance;
	
	private static Object s_lockObject = new Object();
	
	// The procesors and the templates they handle
	private Map<Class<Processor>, LIST<II>> m_processors;
	
	/**
	 * Private ctor for classpathscanner utility
	 */
	private ClasspathScannerUtil() {
		
	}

	/**
	 * Get all processor types registered
	 */
	public Set<Class<Processor>> getProcessors()
	{
		return this.m_processors.keySet();
	}
	
	
	/**
	 * Create processor instance which can handle the template ids that are
	 * in the templateIds list
	 * @param templateIds The template identifiers
	 * @return A processor which can handle the template identifiers in the specified class
	 */
	public final Processor createProcessor(LIST<II> templateIds)
	{

		TemplateMatcher matcher = new TemplateMatcher(templateIds);

		// Find the best match that is the candidate with the most matching template ids
		Class<Processor> bestMatch = null;
				
		// Try to exactly match the list of template identifiers
		for(Entry<Class<Processor>, LIST<II>> entry : this.m_processors.entrySet())
		{
			int noTemplatesHandled = entry.getValue().findAll(matcher).size();
			
			if(noTemplatesHandled > 0 && (bestMatch == null || bestMatch.isAssignableFrom(entry.getKey()))) 
			{
				// Is the current proposed processor better (a subclass) of the current?
				bestMatch = entry.getKey();
			}
		}
			
		// Construct a processor	
		if(bestMatch != null)
			try {
				return bestMatch.newInstance();
			} catch (InstantiationException e) {
				log.error(e.getMessage(), e);
			} catch (IllegalAccessException e) {
				log.error(e.getMessage(), e);
			}
		return null; // couldn't create a processor

	}
	
	/**
	 * Initialize document processor list
	 */
	@SuppressWarnings("unchecked")
	private void initializeProcessorList()
	{
		ClassPathScanningCandidateComponentProvider classPathScanner = new ClassPathScanningCandidateComponentProvider(true);
		classPathScanner.addIncludeFilter(new AssignableTypeFilter(Processor.class));
		classPathScanner.addIncludeFilter(new AnnotationTypeFilter(ProcessTemplates.class));
	
		this.m_processors = new HashMap<Class<Processor>, LIST<II>>();
		
		// scan in org.openmrs.module.RegenstriefHl7Adapter.preprocessorHandler package
		Set<BeanDefinition> components = classPathScanner.findCandidateComponents("org.openmrs.module.shr.cdahandler.processor");
		for (BeanDefinition component : components) {
			try {
				Class<?> cls = Class.forName(component.getBeanClassName());
				// Appears not to be a processor
				if(!Processor.class.isAssignableFrom(cls))
					continue;
				
				// Get the annotation for the process templates
				ProcessTemplates processAnnotation = cls.getAnnotation(ProcessTemplates.class);
				if(processAnnotation == null) // skip if no templates registered
					continue;
				
				// Get the templates and add them to the  
				LIST<II> templateIds = new LIST<II>();
				for(String id : processAnnotation.templateIds())
					templateIds.add(new II(id));
				
				// Add to the processor list
				this.m_processors.put((Class<Processor>)cls, templateIds);
			}
			catch (ClassNotFoundException e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
}
