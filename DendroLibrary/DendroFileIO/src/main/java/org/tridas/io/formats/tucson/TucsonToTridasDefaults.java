package org.tridas.io.formats.tucson;

import java.util.ArrayList;
import java.util.UUID;

import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.values.DoubleDefaultValue;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.util.ITRDBTaxonConverter;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasMeasurementSeriesPlaceholder;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasUnitless;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;

/**
 * here for the library user to create and pass in the loadFile() arguments
 * 
 * @author Daniel
 */
public class TucsonToTridasDefaults extends TridasMetadataFieldSet implements IMetadataFieldSet {
	
	public enum TucsonDefaultField {
		SITE_CODE, SITE_NAME, SPECIES_CODE, SPECIES_NAME, INVESTIGATOR, ELEVATION, LATLONG, 
		STATE_COUNTRY, COMP_DATE, UNITS, VARIABLE, SERIES_CODE;
	}
	
	/**
	 * @see org.tridas.io.defaults.AbstractMetadataFieldSet#initDefaultValues()
	 */
	@Override
	protected void initDefaultValues() {
		super.initDefaultValues();
		setDefaultValue(TucsonDefaultField.SITE_CODE, new StringDefaultValue(UUID.randomUUID().toString()));
		setDefaultValue(TucsonDefaultField.SITE_NAME, new StringDefaultValue(I18n.getText("unnamed.object"), 50, 50));
		setDefaultValue(TucsonDefaultField.SPECIES_CODE, new StringDefaultValue());
		setDefaultValue(TucsonDefaultField.SPECIES_NAME, new StringDefaultValue());
		setDefaultValue(TucsonDefaultField.INVESTIGATOR, new StringDefaultValue());
		setDefaultValue(TucsonDefaultField.ELEVATION, new DoubleDefaultValue());
		setDefaultValue(TucsonDefaultField.LATLONG, new StringDefaultValue());
		setDefaultValue(TucsonDefaultField.STATE_COUNTRY, new StringDefaultValue());
		setDefaultValue(TucsonDefaultField.COMP_DATE, new StringDefaultValue());
		setDefaultValue(TucsonDefaultField.UNITS, new GenericDefaultValue<NormalTridasUnit>());
		setDefaultValue(TucsonDefaultField.VARIABLE, new GenericDefaultValue<NormalTridasVariable>(NormalTridasVariable.RING_WIDTH));
		setDefaultValue(TucsonDefaultField.SERIES_CODE, new StringDefaultValue(UUID.randomUUID().toString()));

	}

	
	@Override
	public TridasProject getDefaultTridasProject()
	{
		TridasProject p = super.getDefaultTridasProject();
		
		// Investigator
		if(getStringDefaultValue(TucsonDefaultField.INVESTIGATOR).getValue()!=null)
		{
			p.setInvestigator(getStringDefaultValue(TucsonDefaultField.INVESTIGATOR).getValue().trim());
		}
		
		return p;
		
	}
	
	
	@Override
	public TridasObject getDefaultTridasObject()
	{
		TridasObject o = super.getDefaultTridasObject();
		
		// Object code and title
		o.setTitle(getStringDefaultValue(TucsonDefaultField.SITE_NAME).getValue());
		TridasIdentifier id = new TridasIdentifier();
		id.setDomain(getStringDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getValue());
		id.setValue(getStringDefaultValue(TucsonDefaultField.SITE_CODE).getValue());
		o.setIdentifier(id);
		
		// Lat Long
		ArrayList<TridasGenericField> genericFields = new ArrayList<TridasGenericField>();
		
		if (getStringDefaultValue(TucsonDefaultField.LATLONG).getValue() != null) {
			TridasGenericField coords = new TridasGenericField();
			coords.setName("tucson.Coords");
			coords.setType("xs:string");
			coords.setValue(getStringDefaultValue(TucsonDefaultField.LATLONG).getValue());
			genericFields.add(coords);
		}
		
		// State / Country
		if (getStringDefaultValue(TucsonDefaultField.STATE_COUNTRY).getValue() != null) {
			TridasGenericField country = new TridasGenericField();
			country.setName("tucson.StateOrCountry");
			country.setType("xs:string");
			country.setValue(getStringDefaultValue(TucsonDefaultField.STATE_COUNTRY).getValue());
			genericFields.add(country);
		}
	
		o.setGenericFields(genericFields);
		
		
		return o;
		
	}
	
	
	@Override
	public TridasElement getDefaultTridasElement()
	{
		TridasElement e = super.getDefaultTridasElement();
		
		// Taxon
		if (getStringDefaultValue(TucsonDefaultField.SPECIES_CODE).getValue() != null) {
			ControlledVoc taxon = ITRDBTaxonConverter.getControlledVocFromCode(getStringDefaultValue(
					TucsonDefaultField.SPECIES_CODE).getValue());
			e.setTaxon(taxon);
		}
		else 
		{
			ControlledVoc taxon = ITRDBTaxonConverter.getControlledVocFromCode(getStringDefaultValue(
					TucsonDefaultField.SPECIES_NAME).getValue());
			e.setTaxon(taxon);
		}

		
		// Elevation
		if(getDoubleDefaultValue(TucsonDefaultField.ELEVATION).getValue()!=null)
		{
			e.setAltitude(getDoubleDefaultValue(TucsonDefaultField.ELEVATION).getValue());
		}
		

		
		return e;
		
	}
	
	@Override
	public TridasSample getDefaultTridasSample()
	{
		TridasSample s = super.getDefaultTridasSample();
		
		return s;
		
	}
	
	@Override
	public TridasRadius getDefaultTridasRadius()
	{
		TridasRadius r = super.getDefaultTridasRadius();
		
		return r;
		
	}
	
	@Override
	public TridasMeasurementSeries getDefaultTridasMeasurementSeries()
	{
		TridasMeasurementSeries ms = super.getDefaultTridasMeasurementSeries();
		TridasIdentifier id = new TridasIdentifier();
		id.setDomain(getStringDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getValue());
		id.setValue(getStringDefaultValue(TucsonDefaultField.SERIES_CODE).getValue());
		ms.setIdentifier(id);
		ms.setTitle(getStringDefaultValue(TucsonDefaultField.SERIES_CODE).getValue());

		return ms;
		
	}
	
	@Override
	public TridasDerivedSeries getDefaultTridasDerivedSeries()
	{
		TridasDerivedSeries ds = super.getDefaultTridasDerivedSeries();
		TridasIdentifier id = new TridasIdentifier();
		id.setDomain(getStringDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getValue());
		id.setValue(getStringDefaultValue(TucsonDefaultField.SERIES_CODE).getValue());
		ds.setIdentifier(id);
		return ds;
		
	}
	
	public TridasMeasurementSeriesPlaceholder getDefaultTridasMeasurementSeriesPlaceholder()
	{
		TridasMeasurementSeriesPlaceholder msph = new TridasMeasurementSeriesPlaceholder();				
		msph.setId(UUID.randomUUID().toString());
		return msph;
	}
	
	public TridasValues getDefaultTridasValues()
	{		
		TridasValues values = new TridasValues();
		TridasVariable variable = new TridasVariable();
		
		// Variable
		variable.setNormalTridas((NormalTridasVariable) getDefaultValue(TucsonDefaultField.VARIABLE).getValue());
		values.setVariable(variable);
		
		// Units
		TridasUnit unit = new TridasUnit();
		if (getDefaultValue(TucsonDefaultField.UNITS).getValue()!=null)
		{
			unit.setNormalTridas((NormalTridasUnit) getDefaultValue(TucsonDefaultField.UNITS).getValue());
			values.setUnit(unit);
		}
		else
		{		
			values.setUnitless(new TridasUnitless());
		}

		return values;
		
	}
	
}
