package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

import org.marc.everest.datatypes.BL;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.SetOperator;
import org.marc.everest.datatypes.TS;
import org.marc.everest.datatypes.generic.LIST;
import org.marc.everest.datatypes.generic.SXCM;
import org.marc.everest.datatypes.generic.SXPR;
import org.marc.everest.datatypes.interfaces.ISetComponent;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.EntryRelationship;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.SubstanceAdministration;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActRelationshipEntryRelationship;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.DocumentImportException;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;

/**
 * A template handler which processes tapered dosing instructions
 */
@ProcessTemplates(templateIds = { CdaHandlerConstants.ENT_TEMPLATE_MEDICATIONS_TAPERED_DOSING })
public class TaperedDosingMedicationsEntryProcessor extends MedicationsEntryProcessor {

	/**
	 * Process the dosing instructions
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.SubstanceAdministrationEntryProcessor#process(org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement)
	 */
	@Override
    public BaseOpenmrsData process(ClinicalStatement entry) throws DocumentImportException {
		
		// Start Description here:
		// Well, what can I say? This is a more complex scenario where
		// the frequency or dosing (or both) can change. When just the 
		// frequency changes it is represented as multiple repetitions
		// of effectiveTime in the one instance of SubstanceAdministration,
		// however when dosing changes it is represented as subordinate 
		// substance administrations. Well, it turns out tapered dosing 
		// is a little difficult to represent as an order and observations 
		// would be a little more difficult as well... So what we're going
		// to do is expand change of frequencies to be subordinate administrations
		// so that they appear as start/stop times.
		// For example:
		// 	For 25mg b.i.d. for 2 weeks then once daily for 2 weeks
		//  We'll create a subordinate substance administration so that there
		//  are two entries in the patient's file: 
		//		1. 25 mg bid from start - start + 2 wks
		//		2. 25 mg once daily from start + 2 wks - start + 2 wks + 2 wks
		
		if(!entry.isPOCD_MT000040UVSubstanceAdministration())
			throw new DocumentImportException("Expected entry to be a SubstanceAdministration");
		SubstanceAdministration administration = (SubstanceAdministration)entry;
		
		boolean hasSubordinateEntries = false;
		for(EntryRelationship er : administration.getEntryRelationship())
			hasSubordinateEntries |= er.getClinicalStatement().isPOCD_MT000040UVSubstanceAdministration();
		
		// Did just the frequency change?
		if(!hasSubordinateEntries && administration.getEffectiveTime().size() == 2 &&
				administration.getEffectiveTime().get(1) instanceof SXPR) // yes multiple frequencies!
		{
			Queue<SubstanceAdministration> administrationsQueue = new ArrayDeque<SubstanceAdministration>();
			SXPR<TS> taperedFrequency = (SXPR<TS>)administration.getEffectiveTime().get(1);
			
			SubstanceAdministration currentAdmin = null;
			// Break each component into sub-components and record as seperate administration
			for(ISetComponent<TS> comp : taperedFrequency)
			{
				SetOperator operator = SetOperator.Inclusive;
				
				if(comp instanceof SXCM)
					operator = ((SXCM<TS>)comp).getOperator();
				
				// Operator
				if(operator.equals(SetOperator.Inclusive) || operator == null)
				{
					currentAdmin = new SubstanceAdministration();
					this.m_datatypeUtil.cascade(administration, currentAdmin, "code", "text", "statusCode", "routeCode", "doseQuantity", "approachSiteCode", "administrationUnitCode", "rateQuantity", "consumable", "entryRelationship", "precondition");
					currentAdmin.setTemplateId(LIST.createLIST(new II(CdaHandlerConstants.ENT_TEMPLATE_MEDICATIONS)));
					administrationsQueue.add(currentAdmin);
				}
				
				// Add the effective time  
				if(comp instanceof SXPR) // the component itself is a set expression
				{
					SXPR<TS> compSxpr = (SXPR<TS>)comp;
					// Add the components of this SXPR to the administration
					currentAdmin.getEffectiveTime().addAll(new ArrayList<ISetComponent<TS>>(compSxpr));
				}
				else
					currentAdmin.getEffectiveTime().add(comp);
				
			}
			
			// First goes into the base processor with rest as sub-administrations represeting the tapering
			administration = administrationsQueue.poll();
			while(administrationsQueue.size() > 0)
				administration.getEntryRelationship().add(new EntryRelationship(x_ActRelationshipEntryRelationship.HasComponent, BL.TRUE, administrationsQueue.poll()));
		}
		return super.process(administration);
    }
	
	
	
	
}
