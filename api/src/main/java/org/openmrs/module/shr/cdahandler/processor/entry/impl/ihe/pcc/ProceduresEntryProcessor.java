package org.openmrs.module.shr.cdahandler.processor.entry.impl.ihe.pcc;

import java.util.Arrays;
import java.util.List;

import org.marc.everest.datatypes.TEL;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Procedure;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ActStatus;
import org.openmrs.module.shr.cdahandler.CdaHandlerConstants;
import org.openmrs.module.shr.cdahandler.exception.ValidationIssueCollection;
import org.openmrs.module.shr.cdahandler.processor.annotation.ProcessTemplates;
import org.openmrs.module.shr.cdahandler.processor.entry.impl.ProcedureEntryProcessor;

/**
 * Processor for IHE PCC Procedures Entry
 */
@ProcessTemplates(templateIds = { CdaHandlerConstants.ENT_TEMPLATE_PROCEDURE_ENTRY })
public class ProceduresEntryProcessor extends ProcedureEntryProcessor {

	// Allowed status codes
	private static final List<ActStatus> s_allowedStatusCodes = Arrays.asList(ActStatus.Active, ActStatus.Completed, ActStatus.Cancelled, ActStatus.Aborted);
	
	/**
	 * Get the template name
	 */
	@Override
	public String getTemplateName() {
		return "Procedures";
	}
	
	/**
	 * Get the expected entry relationships (none)
	 */
	@Override
	protected List<String> getExpectedEntryRelationships() {
		return null;
	}

	/**
	 * Additional validation rules
	 * @see org.openmrs.module.shr.cdahandler.processor.entry.impl.ProcedureEntryProcessor#validate(org.marc.everest.interfaces.IGraphable)
	 */
	@Override
    public ValidationIssueCollection validate(IGraphable object) {
		ValidationIssueCollection issues = super.validate(object);
		if(issues.hasErrors()) return issues;
		
		Procedure procedure = (Procedure)object;
		if(procedure.getText() == null || procedure.getText().isNull() || !TEL.isValidUrlFlavor(procedure.getText().getReference()))
			issues.error("Procedures entry shall contain a text element with a reference to the narritive text for the procedure");
		
		if(procedure.getStatusCode() == null || 
				procedure.getStatusCode().isNull() ||
				!s_allowedStatusCodes.contains(procedure.getStatusCode().getCode()))
			issues.error("Procedures entry shall contain a statusCode and shall be one of completed, active, aborted, or cancelled");
		
		return issues;
		
				
    }
	
	
	
}
