/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.shr.cdahandler.api.impl;

import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.emf.common.util.Diagnostic;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.Component4;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.cda.util.ValidationResult;
import org.openhealthtools.mdht.uml.hl7.datatypes.ANY;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActMoodDocumentObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.x_DocumentActMood;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.shr.cdahandler.CdaDocumentModel;
import org.openmrs.module.shr.cdahandler.ProcessorEvaluator;
import org.openmrs.module.shr.cdahandler.api.DocumentParseException;
import org.openmrs.module.shr.cdahandler.api.cdaAntepartumService;
import org.openmrs.module.shr.cdahandler.api.db.cdaAntepartumDAO;
import org.openmrs.module.shr.cdahandler.processor.Processor;

/**
 * It is a default implementation of {@link cdaAntepartumService}.
 */
public class cdaAntepartumServiceImpl extends BaseOpenmrsService implements cdaAntepartumService {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private cdaAntepartumDAO dao;
	
	/**
	 * @param dao the dao to set
	 */
	public void setDao(cdaAntepartumDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @return the dao
	 */
	public cdaAntepartumDAO getDao() {
		return dao;
	}
	
	@Override
	public Encounter importAntepartumHistoryAndPhysical(InputStream doc) throws DocumentParseException {
		ClinicalDocument cd = null;
		ValidationResult result = new ValidationResult();
		Processor processor;
		
		try {
			
			cd = (ClinicalDocument) CDAUtil.load(doc);
			
			for (Diagnostic diagnostic : result.getWarningDiagnostics()) {
				log.error("Diagnostic Message : " + diagnostic.getMessage());
				throw new Exception("Exception");
			}
			
			// validation on clinical document object
			/*CDAUtil.validate(cd, new BasicValidationHandler() {
				public void handleError(Diagnostic diagnostic) {
					System.out.println("ERROR: " + diagnostic.getMessage());
				}
						
				public void handleWarning(Diagnostic diagnostic) {
					System.out.println("WARNING: " + diagnostic.getMessage());
				}
			});*/
			
		}
		catch (Exception ex) {
			throw new DocumentParseException(ex);
		}
				
		processor = ProcessorEvaluator.identifyPreProcessor(cd);
		if (processor == null) {
			new Exception("No preprocessor found !");
		}
		
		CdaDocumentModel cdaDocumentModel = new CdaDocumentModel(cd);
		processor.process(cdaDocumentModel);
		
		return processor.process(cdaDocumentModel);
	}
	
	//argh. Entry, EntryRelationship, ComponentX all share the same interfaces,
	//BUT a lot of their common methods aren't defined in the interfaces, but rather the classes themselves.
	//We'll just duplicate the code for now, but a better method should be found
	private Set<Obs> parseEntryRelationship(EntryRelationship entry) {
		Set<Obs> res = new HashSet<Obs>();
		
		if (entry.getAct() != null) {
			//we only handle events
			if (entry.getAct().getMoodCode().compareTo(x_DocumentActMood.EVN) == 0) {
				for (EntryRelationship er : entry.getAct().getEntryRelationships()) {
					res.addAll(parseEntryRelationship(er));
				}
			}
		} else if (entry.getObservation() != null) {
			//we only handle events
			if (entry.getObservation().getMoodCode().compareTo(x_ActMoodDocumentObservation.EVN) == 0) {
				
				if (entry.getObservation().getValues() != null && !entry.getObservation().getValues().isEmpty()) {
					Obs obs = new Obs();
					obs.setConcept(getConcept(entry.getObservation().getCode().getCodeSystem(), entry.getObservation()
					        .getCode().getCode()));
					//TODO multiple values
					processValue(obs, entry.getObservation().getValues().get(0));
					processEffectiveTime(obs, entry.getObservation().getEffectiveTime());
					
					res.add(obs);
				}
				
				for (EntryRelationship er : entry.getObservation().getEntryRelationships()) {
					res.addAll(parseEntryRelationship(er));
				}
			}
		} else if (entry.getOrganizer() != null) {
			for (Component4 c : entry.getOrganizer().getComponents()) {
				res.addAll(parseComponent4(c));
			}
		}
		
		return res;
	}
	
	//argh. Entry, EntryRelationship, ComponentX all share the same interfaces,
	//BUT a lot of their common methods aren't defined in the interfaces, but rather the classes themselves.
	//We'll just duplicate the code for now, but a better method should be found
	private Set<Obs> parseComponent4(Component4 entry) {
		Set<Obs> res = new HashSet<Obs>();
		
		if (entry.getAct() != null) {
			//we only handle events
			if (entry.getAct().getMoodCode().compareTo(x_DocumentActMood.EVN) == 0) {
				for (EntryRelationship er : entry.getAct().getEntryRelationships()) {
					res.addAll(parseEntryRelationship(er));
				}
			}
		} else if (entry.getObservation() != null) {
			//we only handle events
			if (entry.getObservation().getMoodCode().compareTo(x_ActMoodDocumentObservation.EVN) == 0) {
				
				if (entry.getObservation().getValues() != null && !entry.getObservation().getValues().isEmpty()) {
					Obs obs = new Obs();
					obs.setConcept(getConcept(entry.getObservation().getCode().getCodeSystem(), entry.getObservation()
					        .getCode().getCode()));
					//TODO multiple values
					processValue(obs, entry.getObservation().getValues().get(0));
					processEffectiveTime(obs, entry.getObservation().getEffectiveTime());
					
					res.add(obs);
				}
				
				for (EntryRelationship er : entry.getObservation().getEntryRelationships()) {
					res.addAll(parseEntryRelationship(er));
				}
			}
		} else if (entry.getOrganizer() != null) {
			for (Component4 c : entry.getOrganizer().getComponents()) {
				res.addAll(parseComponent4(c));
			}
		}
		
		return res;
	}
	
	private void processValue(Obs dst, ANY value) {
		if (value instanceof PQ) {
			dst.setValueNumeric(((PQ) value).getValue().doubleValue());
		} else if (value instanceof CD || value instanceof CE) {
			String code = ((CD) value).getCode();
			String namespace = ((CD) value).getCodeSystem();
			if (code != null && !code.isEmpty()) {
				dst.setValueCoded(getConcept(namespace, code));
			}
		}
	}
	
	private void processEffectiveTime(Obs dst, IVL_TS time) {
		//TODO
		dst.setObsDatetime(new Date());
	}
	
	private Concept getConcept(String namespace, String code) {
		if (namespace == null || namespace.isEmpty())
			namespace = "Default";
		
		ConceptService serv = Context.getConceptService();
		Concept c = serv.getConceptByMapping(code, namespace);
		
		if (c == null) {
			c = new Concept();
			
			ConceptReferenceTerm crt = new ConceptReferenceTerm();
			crt.setConceptSource(getConceptSource(namespace));
			crt.setCode(code);
			c.getConceptMappings().add(new ConceptMap(crt, serv.getConceptMapTypeByName("NARROWER-THAN")));
			
			//datatypes are an issue
			c.setDatatype(serv.getConceptDatatypeByName("N/A"));
			c.setConceptClass(serv.getConceptClassByName("Misc"));
			c.addName(new ConceptName(namespace + ":" + code, Context.getLocale()));
			serv.saveConcept(c);
		}
		
		return c;
	}
	
	private ConceptSource getConceptSource(String namespace) {
		ConceptSource cs = Context.getConceptService().getConceptSourceByName(namespace);
		
		if (cs == null) {
			cs = new ConceptSource();
			cs.setName(namespace);
			cs.setDescription(namespace);
			Context.getConceptService().saveConceptSource(cs);
		}
		
		return cs;
	}
}
