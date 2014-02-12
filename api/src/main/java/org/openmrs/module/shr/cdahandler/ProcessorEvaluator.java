package org.openmrs.module.shr.cdahandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openmrs.module.shr.cdahandler.processor.Processor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;



public class ProcessorEvaluator {

	
	public static Processor identifyPreProcessor(CdaDocumentModel cdaDocumentModel) {	
		return identifyProsseor(cdaDocumentModel.getDocumentType(), null);
		
	}
	
	private static Processor identifyProsseor(String documentType, String namespace) {
		
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
			if (processor.getDocumentType().equals(documentType)) {
				return processor;
			}
		}
		return null;
	}
		
	public static CdaDocumentModel process(CdaDocumentModel cdaDocumentModel, Processor processor) {
		cdaDocumentModel = processor.process(cdaDocumentModel);
		return cdaDocumentModel;
	}
	

	
}
