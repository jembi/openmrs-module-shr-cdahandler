package org.openmrs.module.shr.cdahandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openmrs.Encounter;
import org.openmrs.module.shr.cdahandler.processor.Processor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;



public class ProcessorEvaluator {

	
	public static Processor identifyPreProcessor(ClinicalDocument cd) {	
		return identifyProsseor(cd.getCode().getCode(), cd.getCode().getCodeSystem());
		
	}
	
	private static Processor identifyProsseor(String code, String codeSystem) {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true);
		provider.addIncludeFilter(new AssignableTypeFilter(Processor.class));
		
		List<Object> processors = new ArrayList<Object>();
		
		// scan in org.openmrs.module.RegenstriefHl7Adapter.preprocessorHandler package
		Set<BeanDefinition> components = provider.findCandidateComponents("org.openmrs.module.shr.cdahandler.processor");
		for (BeanDefinition component : components) {
			try {
				Class cls = Class.forName(component.getBeanClassName());
				Processor p = (Processor) cls.newInstance();
				processors.add(p);
			}
			catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			catch (InstantiationException e) {
				e.printStackTrace();
			}
			catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		for (Object object : processors) {
			Processor processor = (Processor) object;
			
			//this needs to be fixed so that appopriate app/fac's will be identified
			if (processor.getCode().equals(code) && (processor.getCodeSystem().equals(codeSystem))) {
				return processor;
			}
		}
		return null;
	}
		
	public static Encounter process(CdaDocumentModel cdaDocumentModel, Processor processor) {
		Encounter encounter = processor.process(cdaDocumentModel);
		return encounter;
	}
	

	
}
