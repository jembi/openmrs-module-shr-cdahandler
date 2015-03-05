package org.openmrs.module.shr.cdahandler.processor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to identify that a processor parses a particular type of template
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {
		ElementType.TYPE
})
public @interface ProcessTemplates {

	/**
	 * Identifies templates that this processor handles (OR) 
	 * @return
	 */
	String[] templateIds();
	
}
