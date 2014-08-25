package org.openmrs.module.shr.cdahandler.everest;

import java.util.HashMap;
import java.util.Map;

import org.marc.everest.formatters.interfaces.IStructureFormatter;
import org.marc.everest.formatters.xml.datatypes.r1.DatatypeFormatter;
import org.marc.everest.formatters.xml.datatypes.r1.R1FormatterCompatibilityMode;
import org.marc.everest.formatters.xml.its1.XmlIts1Formatter;
import org.marc.everest.interfaces.IGraphable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.openmrs.module.shr.cdahandler.everest.sdtc.SdtcPlayingEntity;
import org.openmrs.module.shr.cdahandler.everest.sdtc.SdtcSubjectPerson;

/**
 * A utility for helping with Everest constructs 
 */
public class EverestUtil {
	
	
	// A map of custom extended types
	private static final Map<String, Class<? extends IGraphable>> s_extendedTypes = new HashMap<String, Class<? extends IGraphable>>()
			{{
				put("POCD_MT000040UV.PlayingEntity", SdtcPlayingEntity.class);
				put("POCD_MT000040UV.SubjectPerson", SdtcSubjectPerson.class);
			}};
	
	/**
	 * Creates a formatter with extended types registered  
	 */
	public static XmlIts1Formatter createFormatter()
	{
		XmlIts1Formatter formatter = new XmlIts1Formatter();
		formatter.getGraphAides().add(new DatatypeFormatter(R1FormatterCompatibilityMode.ClinicalDocumentArchitecture));
		formatter.addCachedClass(ClinicalDocument.class);
		for(Map.Entry<String, Class<? extends IGraphable>> entry : s_extendedTypes.entrySet())
			formatter.registerXSITypeName(entry.getKey(), entry.getValue());
		formatter.setValidateConformance(false); // Don't validate to RMIM conformance
		return formatter;
		
	}
			
	
}
