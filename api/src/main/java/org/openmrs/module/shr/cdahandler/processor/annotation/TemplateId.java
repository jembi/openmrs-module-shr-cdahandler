package org.openmrs.module.shr.cdahandler.processor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tags a single template id triggers the use of a processor
 * @author Justin
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {
		ElementType.TYPE
})
public @interface TemplateId {
	/**
	 * Identifies the template ID that triggers the use of the template
	 * @return
	 */
	String root();
}
