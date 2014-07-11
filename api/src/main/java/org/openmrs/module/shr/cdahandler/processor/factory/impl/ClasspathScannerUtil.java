package org.openmrs.module.shr.cdahandler.processor.factory.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.generic.LIST;
import org.openmrs.module.shr.cdahandler.processor.Processor;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.annotation.TemplateId;
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
				for(TemplateId id : processAnnotation.value())
					templateIds.add(new II(id.root()));
				
				// Add to the processor list
				this.m_processors.put((Class<Processor>)cls, templateIds);
			}
			catch (ClassNotFoundException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Create processor instance which can handle the template ids that are
	 * in the templateIds list
	 * @param templateIds The template identifiers
	 * @return A processor which can handle the template identifiers in the specified class
	 */
	public Processor createProcessor(LIST<II> templateIds)
	{

		LIST<II> processList = new LIST<II>(templateIds);

		// Try to exactly match the list of template identifiers
		for(Entry<Class<Processor>, LIST<II>> entry : this.m_processors.entrySet())
			if(processList.size() == entry.getValue().size() &&
					processList.containsAll(entry.getValue()))
				try {
					return entry.getKey().newInstance();
				} catch (InstantiationException e) {
					log.error(e.getMessage(), e);
				} catch (IllegalAccessException e) {
					log.error(e.getMessage(), e);
				}
		
		return null; // couldn't create a processor

	}
	
	/**
	 * Gets the current singleton instance
	 */
	public static ClasspathScannerUtil getInstance()
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
	
}
