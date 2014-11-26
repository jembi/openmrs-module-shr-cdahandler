package org.openmrs.module.shr.cdahandler.configuration;

import org.jfree.util.Log;
import org.openmrs.api.context.Context;

/**
 * CdaHandlerConfiguration factory gets or sets the instance of the CDA
 * Handler Configuration
 * @author Justin
 *
 */
public final class CdaHandlerConfigurationFactory {
	
	// Instance of the constructed cda configuration
	private static CdaHandlerConfiguration s_instance;
	private final static Object s_lockObject = new Object();

	// Identifies the configuration class
	public final static String PROP_NAME_CONFIGURATION_CLASS = "cdahandler.configurationClass";
	// Configuration class
	public static String configurationClassName = CdaHandlerConfigurationImpl.class.getName();
	
	/**
	 * TODO: Find a better way
	 * @param configurationClassName the configurationClassName to set
	 */
	public static void setConfigurationClassName(String configurationClassName) {
		CdaHandlerConfigurationFactory.configurationClassName = configurationClassName;
	}

	/**
	 * Gets or creates the singleton configuration class as described in the property
	 * @return
	 */
	public static CdaHandlerConfiguration getInstance() {
		if(s_instance == null)
			synchronized (s_lockObject) 
			{
				if(s_instance == null) {
					
					String className = CdaHandlerConfigurationFactory.configurationClassName;
					if(className == null || className.isEmpty())
						s_instance = new CdaHandlerConfigurationImpl();
					else
					{
						try
						{
							Class<? extends CdaHandlerConfiguration> configurationClass = (Class<? extends CdaHandlerConfiguration>) Context.loadClass(className);
							s_instance = configurationClass.newInstance();
						}
						catch(Exception e)
						{
							Log.error(e);
							s_instance = new CdaHandlerConfigurationImpl();
						}
					}
				}
			}
		return s_instance;
	}
	
	
}
