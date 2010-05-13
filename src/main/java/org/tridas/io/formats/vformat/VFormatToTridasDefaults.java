package org.tridas.io.formats.vformat;

import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet.TridasMandatoryField;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasSample;

public class VFormatToTridasDefaults extends TridasMetadataFieldSet {

	
	
	public TridasObject getObjectWithDefaults(String objectid)
	{
		TridasObject o = getObjectWithDefaults();
		TridasIdentifier id = new TridasIdentifier();
		id.setValue(objectid);
		id.setDomain(this.getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAN).getStringValue());
		o.setIdentifier(id);
		return o;
	}
	
	public TridasElement getElementWithDefaults(String elementid)
	{
		TridasElement e = getElementWithDefaults();
		TridasIdentifier id = new TridasIdentifier();
		id.setValue(elementid);
		id.setDomain(this.getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAN).getStringValue());
		e.setIdentifier(id);
		return e;
	}
	
	public TridasSample getSampleWithDefaults(String sampleid)
	{
		TridasSample s = getSampleWithDefaults();
		TridasIdentifier id = new TridasIdentifier();
		id.setValue(sampleid);
		id.setDomain(this.getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAN).getStringValue());
		s.setIdentifier(id);
		return s;
	}
}