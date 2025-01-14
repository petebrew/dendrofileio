/**
 * Copyright 2010 Peter Brewer and Daniel Murphy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tridas.io.formats.corina;

import java.io.File;
import java.util.UUID;

import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet.TridasMandatoryField;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.SafeIntYearDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.corina.TridasToCorinaDefaults.CorinaDatingType;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.ComplexPresenceAbsence;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasDatingType;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.PresenceAbsence;
import org.tridas.schema.TridasBark;
import org.tridas.schema.TridasDating;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasPith;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasSapwood;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;
import org.tridas.schema.TridasWoodCompleteness;

import com.google.common.io.Files;

public class CorinaToTridasDefaults extends TridasMetadataFieldSet implements
		IMetadataFieldSet {
	
	private static final Logger log = LoggerFactory.getLogger(CorinaToTridasDefaults.class);
	
	public static enum DefaultFields {
		
		ID,
		NAME,
		DATING,
		UNMEAS_PRE,
		UNMEAS_POST,
		COMMENTS,
		COMMENTS2,
		TYPE,
		SPECIES,
		SAPWOOD,
		PITH,
		TERMINAL,
		CONTINUOUS,
		QUALITY,
		FORMAT,
		INDEX_TYPE,
		FILENAME,
		RECONCILED,
		START_YEAR,
		USERNAME;

	}
	
	@Override
	public void initDefaultValues() {
		super.initDefaultValues();
		setDefaultValue(DefaultFields.ID, new StringDefaultValue());
		setDefaultValue(DefaultFields.NAME, new StringDefaultValue());
		setDefaultValue(DefaultFields.DATING, new StringDefaultValue());
		setDefaultValue(DefaultFields.UNMEAS_PRE, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.UNMEAS_POST, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.COMMENTS, new StringDefaultValue());
		setDefaultValue(DefaultFields.COMMENTS2, new StringDefaultValue());
		setDefaultValue(DefaultFields.TYPE, new StringDefaultValue());
		setDefaultValue(DefaultFields.SPECIES, new StringDefaultValue());
		setDefaultValue(DefaultFields.SAPWOOD, new StringDefaultValue());
		setDefaultValue(DefaultFields.PITH, new StringDefaultValue());
		setDefaultValue(DefaultFields.TERMINAL, new StringDefaultValue());
		setDefaultValue(DefaultFields.CONTINUOUS, new StringDefaultValue());
		setDefaultValue(DefaultFields.QUALITY, new StringDefaultValue());
		setDefaultValue(DefaultFields.FORMAT, new StringDefaultValue());
		setDefaultValue(DefaultFields.INDEX_TYPE, new StringDefaultValue());
		setDefaultValue(DefaultFields.FILENAME, new StringDefaultValue());
		setDefaultValue(DefaultFields.RECONCILED, new StringDefaultValue());
		setDefaultValue(DefaultFields.START_YEAR, new SafeIntYearDefaultValue(new SafeIntYear(1001)));
		setDefaultValue(DefaultFields.USERNAME, new StringDefaultValue());

	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasDerivedSeries()
	 */
	@Override
	protected TridasDerivedSeries getDefaultTridasDerivedSeries() {
		TridasDerivedSeries series = super.getDefaultTridasDerivedSeries();
		
		series.setTitle(getStringDefaultValue(DefaultFields.NAME).getValue());
		
		if(getStringDefaultValue(DefaultFields.USERNAME).getValue()!=null)
		{
			series.setAuthor(getStringDefaultValue(DefaultFields.USERNAME).getValue());
		}
		
		TridasInterpretation interp = new TridasInterpretation();
		interp.setFirstYear(getSafeIntYearDefaultValue(DefaultFields.START_YEAR).getValue().toTridasYear(DatingSuffix.AD));
		
		
		// ID > Generic field
		String keycode = null;
		
		if(getStringDefaultValue(DefaultFields.FILENAME).getValue()!=null)
		{		
			keycode = Files.getNameWithoutExtension(getStringDefaultValue(DefaultFields.FILENAME).getValue()).replace("-", "").replace("&", "+");
			if(keycode.length()>8) keycode=null;
		}
		if(keycode==null)
		{
			if(getStringDefaultValue(DefaultFields.ID).getValue()!=null)
			{
				keycode = getStringDefaultValue(DefaultFields.ID).getValue();
			}
			else
			{
				keycode = Files.getNameWithoutExtension(getStringDefaultValue(DefaultFields.FILENAME).getValue()).replace("-", "").replace("&", "+");
				if(keycode.length()>8) keycode = keycode.substring(0,8);
			}
		}
		
		if(keycode!=null)
		{
			TridasGenericField gf = new TridasGenericField();
			gf.setName("keycode");
			gf.setType("xs:string");
			gf.setValue(keycode);
			series.getGenericFields().add(gf);
		}
		
		
		// Dating type
		if(getDefaultValue(DefaultFields.DATING).getValue()!=null)
		{
			try{
				CorinaDatingType dating = CorinaDatingType.valueOf(getStringDefaultValue(DefaultFields.DATING).getValue());
				TridasDating td = new TridasDating();
				switch (dating)
				{
				case ABSOLUTE:
					td.setType(NormalTridasDatingType.ABSOLUTE);
					interp.setDating(td);
					break;
				case RELATIVE:
					td.setType(NormalTridasDatingType.RELATIVE);
					interp.setDating(td);
					break;
				default:
				}
			} catch (Exception e)
			{
				this.addConversionWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("corina.invalidDatingField")));
			}
			
		}
		
		
		series.setInterpretation(interp);
		return series;
	}
	
	@Override
	protected TridasWoodCompleteness getDefaultWoodCompleteness()
	{
		TridasWoodCompleteness wc = super.getDefaultWoodCompleteness();

		// Unmeasured Pre
		if(getIntegerDefaultValue(DefaultFields.UNMEAS_PRE).getValue()!=null)
		{
			wc.setNrOfUnmeasuredInnerRings(getIntegerDefaultValue(DefaultFields.UNMEAS_PRE).getValue());
		}
		
		// Unmeasured Post
		if(getIntegerDefaultValue(DefaultFields.UNMEAS_POST).getValue()!=null)
		{
			wc.setNrOfUnmeasuredOuterRings(getIntegerDefaultValue(DefaultFields.UNMEAS_POST).getValue());
		}
		
		// Pith
		if(getStringDefaultValue(DefaultFields.PITH).getValue()!=null)
		{
			TridasPith pith = wc.getPith();
			if(getStringDefaultValue(DefaultFields.PITH).getValue().equalsIgnoreCase("+"))
			{
				pith.setPresence(ComplexPresenceAbsence.COMPLETE);
			}
			else if(getStringDefaultValue(DefaultFields.PITH).getValue().equalsIgnoreCase("*"))
			{
				pith.setPresence(ComplexPresenceAbsence.INCOMPLETE);
			}
			else if(getStringDefaultValue(DefaultFields.PITH).getValue().equalsIgnoreCase("N"))
			{
				pith.setPresence(ComplexPresenceAbsence.ABSENT);
			}
		}
		
		// Terminal ring
		if(getStringDefaultValue(DefaultFields.TERMINAL).getValue()!=null)
		{
			TridasBark bark = wc.getBark();
			TridasSapwood sapwood = wc.getSapwood();
			if(getStringDefaultValue(DefaultFields.TERMINAL).getValue().equalsIgnoreCase("B"))
			{
				sapwood.setPresence(ComplexPresenceAbsence.COMPLETE);
				bark.setPresence(PresenceAbsence.PRESENT);
			}
			else if (getStringDefaultValue(DefaultFields.TERMINAL).getValue().equalsIgnoreCase("W"))
			{
				sapwood.setPresence(ComplexPresenceAbsence.COMPLETE);
				bark.setPresence(PresenceAbsence.ABSENT);
			}
			else if (getStringDefaultValue(DefaultFields.TERMINAL).getValue().equalsIgnoreCase("v"))
			{
				sapwood.setPresence(ComplexPresenceAbsence.INCOMPLETE);
				bark.setPresence(PresenceAbsence.ABSENT);
			}
			else
			{
				bark.setPresence(PresenceAbsence.ABSENT);
			}
		}
			
		return wc;
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasMeasurementSeries()
	 */
	@Override
	protected TridasMeasurementSeries getDefaultTridasMeasurementSeries() {
		TridasMeasurementSeries series = super.getDefaultTridasMeasurementSeries();
		
		TridasIdentifier id = new TridasIdentifier();
		id.setDomain(getStringDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getValue());
		id.setValue(UUID.randomUUID().toString());
		
		series.setIdentifier(id);
		
		series.setTitle(getStringDefaultValue(DefaultFields.NAME).getValue());
		
		if(getStringDefaultValue(DefaultFields.USERNAME).getValue()!=null)
		{
			series.setDendrochronologist(getStringDefaultValue(DefaultFields.USERNAME).getValue());
		}
		
		TridasInterpretation interp = new TridasInterpretation();
		TridasWoodCompleteness wc = getDefaultWoodCompleteness();
		
	
		// Dating type
		if(getDefaultValue(DefaultFields.DATING).getValue()!=null)
		{
			try{
				CorinaDatingType dating = CorinaDatingType.fromCode(getStringDefaultValue(DefaultFields.DATING).getValue());
				TridasDating td = new TridasDating();
				switch (dating)
				{
				case ABSOLUTE:
					td.setType(NormalTridasDatingType.ABSOLUTE);
					interp.setDating(td);
					break;
				case RELATIVE:
					td.setType(NormalTridasDatingType.RELATIVE);
					interp.setDating(td);
					break;
				default:
				}
			} catch (Exception e){
				e.printStackTrace();
				log.warn("Unknown dating type'"+getStringDefaultValue(DefaultFields.DATING).getValue()+"'");
			}		
		}
		
		
		// Comments
		if(getStringDefaultValue(DefaultFields.COMMENTS).getValue()!=null)
		{
			series.setComments(getStringDefaultValue(DefaultFields.COMMENTS).getValue());
		}
		
		// ID > Generic field
		String keycode = null;
		
		if(getStringDefaultValue(DefaultFields.FILENAME).getValue()!=null)
		{		
			keycode = Files.getNameWithoutExtension(getStringDefaultValue(DefaultFields.FILENAME).getValue()).replace("-", "").replace("&", "+");
			if(keycode.length()>8) keycode=null;
		}
		if(keycode==null)
		{
			if(getStringDefaultValue(DefaultFields.ID).getValue()!=null)
			{
				keycode = getStringDefaultValue(DefaultFields.ID).getValue();
			}
			else
			{
				keycode = Files.getNameWithoutExtension(getStringDefaultValue(DefaultFields.FILENAME).getValue()).replace("-", "").replace("&", "+");
				if(keycode.length()>8) keycode = keycode.substring(0,8);
			}
		}
		
		if(keycode!=null)
		{
			TridasGenericField gf = new TridasGenericField();
			gf.setName("keycode");
			gf.setType("xs:string");
			gf.setValue(keycode);
			series.getGenericFields().add(gf);
		}
		
		
		interp.setFirstYear(getSafeIntYearDefaultValue(DefaultFields.START_YEAR).getValue().toTridasYear(DatingSuffix.AD));
		
		
		series.setInterpretation(interp);
		series.setWoodCompleteness(wc);

		return series;
	}
	
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasElement()
	 */
	@Override
	protected TridasElement getDefaultTridasElement()
	{
		TridasElement e = super.getDefaultTridasElement();
		
		if(getStringDefaultValue(DefaultFields.SPECIES).getValue()!=null)
		{
			ControlledVoc taxon = new ControlledVoc();
			taxon.setValue(getStringDefaultValue(DefaultFields.SPECIES).getValue());
			e.setTaxon(taxon);
		}
		
		
		return e;
		
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasSample()
	 */
	@Override
	protected TridasSample getDefaultTridasSample()
	{
		TridasSample s = super.getDefaultTridasSample();
		ControlledVoc type = new ControlledVoc();
		
		if(getStringDefaultValue(DefaultFields.TYPE).getValue()!=null)
		{
			if(getStringDefaultValue(DefaultFields.TYPE).getValue().equalsIgnoreCase("C"))
			{
				type.setValue("Core");
			}
			else if(getStringDefaultValue(DefaultFields.TYPE).getValue().equalsIgnoreCase("S"))
			{
				type.setValue("Section");
			}
			else if(getStringDefaultValue(DefaultFields.TYPE).getValue().equalsIgnoreCase("H"))
			{
				type.setValue("Charcoal");
			}
			
			s.setType(type);
		}
		
		return s;
		
	}
	
	public TridasValues getTridasValuesWithDefaults() {
		TridasValues valuesGroup = new TridasValues();
		
		// Set units to 1/100th mm.
		TridasUnit units = new TridasUnit();
		units.setNormalTridas(NormalTridasUnit.HUNDREDTH_MM);
		valuesGroup.setUnit(units);
		
		// Set variable to ringwidth.  
		TridasVariable variable = new TridasVariable();
		variable.setNormalTridas(NormalTridasVariable.RING_WIDTH);
		valuesGroup.setVariable(variable);
		

		return valuesGroup;
	}
	
}
