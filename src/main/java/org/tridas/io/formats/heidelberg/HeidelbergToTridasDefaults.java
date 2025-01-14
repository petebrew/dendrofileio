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
package org.tridas.io.formats.heidelberg;

import java.util.ArrayList;
import java.util.UUID;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet.TridasExtraField;
import org.tridas.io.defaults.values.DateTimeDefaultValue;
import org.tridas.io.defaults.values.DoubleDefaultValue;
import org.tridas.io.defaults.values.GenericDefaultValue;
import org.tridas.io.defaults.values.IntegerDefaultValue;
import org.tridas.io.defaults.values.StringDefaultValue;
import org.tridas.io.util.DateUtils;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.ComplexPresenceAbsence;
import org.tridas.schema.ControlledVoc;
import org.tridas.schema.Date;
import org.tridas.schema.DateTime;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasDatingType;
import org.tridas.schema.ObjectFactory;
import org.tridas.schema.PresenceAbsence;
import org.tridas.schema.TridasAddress;
import org.tridas.schema.TridasBark;
import org.tridas.schema.TridasCoverage;
import org.tridas.schema.TridasDating;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasHeartwood;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasLocation;
import org.tridas.schema.TridasLocationGeometry;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasPith;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasSapwood;
import org.tridas.schema.TridasShape;
import org.tridas.schema.TridasSoil;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasUnitless;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;
import org.tridas.schema.TridasWoodCompleteness;
import org.tridas.spatial.SpatialUtils;

public class HeidelbergToTridasDefaults extends TridasMetadataFieldSet {
	private static final Logger log = LoggerFactory.getLogger(HeidelbergToTridasDefaults.class);

	public static enum DefaultFields {
		
		// Field marked with *** have been implemented
		
		ACCEPT_DATE,					
		//AGE,							//CALCULABLE
		//AUTOCORRELATION,				//USED FOR TUCSON
		BARK,							//***						
		BHD,
		//Bibliography[n]
		//BibliographyCount
		BUNDLE,
		//CardinalPoint
		//ChronologyType				//USED FOR SHEFFIELD
		CHRONO_MEMBER_COUNT,
		CHRONO_MEMBER_KEYCODES,
		CIRCUMFERENCE,
		CLIENT,
		CLIENT_NO,
		COLLECTOR,
		//Comment[n]
		COMMENTS,
		//CommentCount
		CONTINENT,				
		CORE_NUMBER,					//***
		COUNTRY,						//***
		//CREATION_DATE,				//DEPRECATED
		DATA_FORMAT,
		DATA_TYPE,						
		DATE_BEGIN,						//***
		DATED,							//***
		DATE_END,						//***
		//DATE_END_REL,					//DEPRECATED
		DATE_OF_SAMPLING,				//***
		//DateRelBegin[n]
		//DateRelEnd[n]
		//DateRelReferenceKey[n]
		//DateRelCount
		DELTA_MISSING_RINGS_AFTER,
		DELTA_MISSING_RINGS_BEFORE,
		DELTA_RINGS_FROM_SEED_TO_PITH,
		//DISK,							//USED FOR INRA
		DISTRICT,						//***
		//EDGE_INFORMATION,				//USED FOR SHEFFIELD
		//EffectiveAutoCorrelation      //USED FOR CATRAS
		//EffectiveMean                 //USED FOR CATRAS
		//EffectiveMeanSensitivity      //USED FOR CATRAS
		//EffectiveNORFAC               //USED FOR CATRAS
		//EffectiveNORFM                //USED FOR CATRAS
		//EffectiveStandardDeviation    //USED FOR CATRAS
		//Eigenvalue					//DEPRECATED
		ELEVATION,						//***
		ESTIMATED_TIME_PERIOD,			//***
		EXPOSITION,
		FIELD_NO,
		//FilmNo					  	//USED FOR BIREMSDORF
		FIRST_MEASUREMENT_DATE,			
		FIRST_MEASUREMENT_PERS_ID,
		//FromSeedToDateBegin			//DEPRECATED
		//GlobalMathComment[n]
		//GlobalMathCommentCount
		//GraphParam					//DEPRECATED
		//Group							//USED FOR SHEFFIELD
		HOUSE_NAME,						//***
		HOUSE_NUMBER,					//***
		//ImageCellRow					//DEPRECATED
		//ImageComment[n]
		//ImageFile[n]
		//ImageCount
		//ImageFile
		//Interpretation				//USED FOR SHEFFIELD
		INVALID_RINGS_AFTER,
		INVALID_RINGS_BEFORE,
		JUVENILE_WOOD,
		KEYCODE,						//***
		KEY_NUMBER,
		LAB_CODE,						
		LAST_REVISION_DATE,				
		LAST_REVISION_PERS_ID,			
		LATITUDE,						//***
		LEAVE_LOSS,
		LENGTH,							//***
		LOCATION,						
		LOCATION_CHARACTERISTICS,		//***
		LONGITUDE,						//***
		// MAJOR_DIMENSION,			    //USED FOR SHEFFIELD
		//MathComment
		//MathComment[n]
		//MathCommentCount
		//MeanSensitivity				//USED FOR TUCSON
		//MinorDimension				//USED FOR SHEFFIELD
		MISSING_RINGS_AFTER,			
		MISSING_RINGS_BEFORE,			
		//NumberOfSamplesInChrono		//DEPRECATED
		//NumberOfTreesInChrono			//DEPRECATED
		PERS_ID,
		PITH,							//***
		PROJECT,						//***
		//ProtectionCode				//USED FOR CATRAS
		PROVINCE,						//***
		QUALITY_CODE,
		//RADIUS,						//USED FOR INRA
		RADIUS_NUMBER,					//***
		RELATIVE_GROUND_WATER_LEVEL,	
		RINGS_FROM_SEED_TO_PITH,
		//SampleType					//USED FOR SHEFFIELD
		SAMPLING_HEIGHT,				//***
		SAMPLING_POINT,
		SAPWOOD_RINGS,					//***
		SEQUENCE,
		SERIES_END,						
		SERIES_START,					
		SERIES_TYPE,					//***
		SHAPE_OF_SAMPLE,				//***
		//SITE,							//USED FOR INRA
		SITE_CODE,						//***
		SOCIAL_STAND,
		SOIL_TYPE,
		SPECIES,						//***
		SPECIES_NAME,					//***
		//StandardDeviation				//USED FOR TUCSON
		STATE,							//***
		STEM_DISK_NUMBER,				//***
		STREET,							//***
		//TIMBER,						//USED FOR SHEFFIELD
		TIMBER_HEIGHT,					//***	
		TIMBER_TYPE,
		TIMBER_WIDTH,					//***
		//TotalAutoCorrelation			//USED FOR CATRAS
		//TotalMean						//USED FOR CATRAS
		//TotalMeanSensitivity			//USED FOR CATRAS
		//TotalNORFAC					//USED FOR CATRAS
		//TotalNORFM					//USED FOR CATRAS
		//TotalStandardDeviation		//USED FOR CATRAS
		TOWN,							//***
		TOWN_ZIP_CODE,					//***
		//Tree							//USED FOR INRA AND SHEFFIELD
		TREE_HEIGHT,					//***
		TREE_NUMBER,					//***
		UNIT,							//***
		//UNMEASURED_INNER_RINGS,		//USED FOR SHEFFIELD			
		//UNMEASURED_OUTER_RINGS,		//USED FOR SHEFFIELD
		WALDKANTE,						//***
		WOOD_MATERIAL_TYPE,
		WORK_TRACES,
		RING_COUNT,
		AV_RING_WIDTH
	}
	
	@Override
	public void initDefaultValues() {
		super.initDefaultValues();
		setDefaultValue(DefaultFields.BARK, new GenericDefaultValue<FHBarkType>());
		setDefaultValue(DefaultFields.CORE_NUMBER, new StringDefaultValue());
		setDefaultValue(DefaultFields.COMMENTS, new StringDefaultValue());
		setDefaultValue(DefaultFields.COUNTRY, new StringDefaultValue());
		setDefaultValue(DefaultFields.CLIENT, new StringDefaultValue());
		setDefaultValue(DefaultFields.DATA_FORMAT, new GenericDefaultValue<FHDataFormat>());
		setDefaultValue(DefaultFields.DATA_TYPE, new GenericDefaultValue<FHDataType>());
		setDefaultValue(DefaultFields.DATE_BEGIN, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.DATED, new GenericDefaultValue<FHDated>());
		setDefaultValue(DefaultFields.DATE_END, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.DATE_OF_SAMPLING, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.DISTRICT, new StringDefaultValue());
		setDefaultValue(DefaultFields.ELEVATION, new DoubleDefaultValue(null, -418.0, 8850.0)); // Heights of Dead Sea and Everest! ;-)
		setDefaultValue(DefaultFields.ESTIMATED_TIME_PERIOD, new StringDefaultValue());
		setDefaultValue(DefaultFields.FIRST_MEASUREMENT_DATE, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.HOUSE_NAME, new StringDefaultValue());
		setDefaultValue(DefaultFields.HOUSE_NUMBER, new StringDefaultValue());	
		setDefaultValue(DefaultFields.KEYCODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.LAB_CODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.LAST_REVISION_DATE, new DateTimeDefaultValue());
		setDefaultValue(DefaultFields.LAST_REVISION_PERS_ID, new StringDefaultValue());
		setDefaultValue(DefaultFields.LATITUDE, new DoubleDefaultValue(null, -90.0, 90.0));
		setDefaultValue(DefaultFields.LENGTH, new StringDefaultValue());
		setDefaultValue(DefaultFields.LOCATION, new StringDefaultValue());
		setDefaultValue(DefaultFields.LOCATION_CHARACTERISTICS, new StringDefaultValue());
		setDefaultValue(DefaultFields.LONGITUDE, new DoubleDefaultValue(null, -180.0, 180.0));
		setDefaultValue(DefaultFields.MISSING_RINGS_AFTER, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.MISSING_RINGS_BEFORE, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.PERS_ID, new StringDefaultValue());
		setDefaultValue(DefaultFields.PITH, new GenericDefaultValue<FHPith>());
		setDefaultValue(DefaultFields.PROJECT, new StringDefaultValue());
		setDefaultValue(DefaultFields.PROVINCE, new StringDefaultValue());
		setDefaultValue(DefaultFields.RADIUS_NUMBER, new StringDefaultValue());
		setDefaultValue(DefaultFields.SAMPLING_HEIGHT, new StringDefaultValue());
		setDefaultValue(DefaultFields.SAPWOOD_RINGS, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.SERIES_END, new GenericDefaultValue<FHStartsOrEndsWith>());
		setDefaultValue(DefaultFields.SERIES_START, new GenericDefaultValue<FHStartsOrEndsWith>());
		setDefaultValue(DefaultFields.SERIES_TYPE, new GenericDefaultValue<FHSeriesType>());
		setDefaultValue(DefaultFields.SHAPE_OF_SAMPLE, new StringDefaultValue());
		setDefaultValue(DefaultFields.SITE_CODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.SOIL_TYPE, new StringDefaultValue());
		setDefaultValue(DefaultFields.SPECIES, new GenericDefaultValue<ControlledVoc>());
		setDefaultValue(DefaultFields.SPECIES_NAME, new StringDefaultValue());
		setDefaultValue(DefaultFields.STATE, new StringDefaultValue());
		setDefaultValue(DefaultFields.STEM_DISK_NUMBER, new StringDefaultValue());
		setDefaultValue(DefaultFields.STREET, new StringDefaultValue());
		setDefaultValue(DefaultFields.TIMBER_HEIGHT, new StringDefaultValue());
		setDefaultValue(DefaultFields.TIMBER_WIDTH, new StringDefaultValue());
		setDefaultValue(DefaultFields.TOWN, new StringDefaultValue());
		setDefaultValue(DefaultFields.TOWN_ZIP_CODE, new StringDefaultValue());
		setDefaultValue(DefaultFields.TREE_HEIGHT, new StringDefaultValue());
		setDefaultValue(DefaultFields.TREE_NUMBER, new StringDefaultValue());
		setDefaultValue(DefaultFields.UNIT, new GenericDefaultValue<TridasUnit>());	
		setDefaultValue(DefaultFields.WALDKANTE, new GenericDefaultValue<FHWaldKante>());
		setDefaultValue(DefaultFields.RING_COUNT, new IntegerDefaultValue());
		setDefaultValue(DefaultFields.AV_RING_WIDTH, new DoubleDefaultValue());

	}
		
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasDerivedSeries()
	 */
	@Override
	protected TridasDerivedSeries getDefaultTridasDerivedSeries() {
		TridasDerivedSeries series = super.getDefaultTridasDerivedSeries();
	
		series = (TridasDerivedSeries) populateSeriesFields(series);
		
		series.setStandardizingMethod(getDefaultValue(DefaultFields.SERIES_TYPE).getStringValue());
		
		// AUTHOR
		if(getStringDefaultValue(DefaultFields.PERS_ID).getStringValue()!=null)
		{
			series.setAuthor(getStringDefaultValue(DefaultFields.PERS_ID).getStringValue());
		}
		
		ArrayList<TridasGenericField> gflist = new ArrayList<TridasGenericField>();

		
		if(getStringDefaultValue(DefaultFields.KEYCODE).getStringValue()!=null)
		{
			TridasGenericField gf = new TridasGenericField();
			gf.setName("keycode");
			gf.setValue(getStringDefaultValue(DefaultFields.KEYCODE).getStringValue());
			gf.setType("xs:string");
			gflist.add(gf);
			
		}
		
		if (getDefaultValue(TridasExtraField.ORIGINAL_FILENAME).getValue() != null) {
			TridasGenericField gf = new ObjectFactory().createTridasGenericField();
			gf.setName("original.treeringdatafile");
			gf.setType("xs:string");
			gf.setValue(getDefaultValue(TridasExtraField.ORIGINAL_FILENAME).getValue().toString());
			gflist.add(gf);
		}
		
		series.setGenericFields(gflist);

		return series;
	}
		
	
	@SuppressWarnings("unchecked")
	private ITridasSeries populateSeriesFields(ITridasSeries series)
	{
	
		// TITLE
		if((getStringDefaultValue(DefaultFields.KEYCODE).getStringValue()!=null) &&
				(getStringDefaultValue(DefaultFields.KEYCODE).getStringValue()!=""))
		{
			series.setTitle(getStringDefaultValue(DefaultFields.KEYCODE).getStringValue());
		}
		
		// IDENTIFIER
		TridasIdentifier id = new TridasIdentifier();
		id.setValue(getStringDefaultValue(DefaultFields.KEYCODE).getStringValue());
		id.setDomain(getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getStringValue());
		series.setIdentifier(id);
		
		//INTERPRETATION			
		GenericDefaultValue<FHDated> dated = (GenericDefaultValue<FHDated>) getDefaultValue(DefaultFields.DATED);
		if(dated!=null && dated.getValue()!=null)
		{
			TridasInterpretation interp = new TridasInterpretation();
			TridasDating dating = new TridasDating();
			
			if (dated.getValue().equals(FHDated.Dated) || dated.getValue() == null) {
				if (getIntegerDefaultValue(DefaultFields.DATE_BEGIN).getValue() != null) {
					SafeIntYear startYear = new SafeIntYear(getIntegerDefaultValue(DefaultFields.DATE_BEGIN).getValue());
					interp.setFirstYear(startYear.toTridasYear(DatingSuffix.AD));
				}
				if (getIntegerDefaultValue(DefaultFields.DATE_END).getValue() != null) {
					SafeIntYear endYear = new SafeIntYear(getIntegerDefaultValue(DefaultFields.DATE_END).getValue());
					interp.setLastYear(endYear.toTridasYear(DatingSuffix.AD));
				}
				dating.setType(NormalTridasDatingType.ABSOLUTE);
				interp.setDating(dating);
				series.setInterpretation(interp);
			}
			else if (dated.getValue().equals(FHDated.RelDated))
			{
				if (getIntegerDefaultValue(DefaultFields.DATE_BEGIN).getValue() != null) {
					SafeIntYear startYear = new SafeIntYear(getIntegerDefaultValue(DefaultFields.DATE_BEGIN).getValue().toString(), true);
					interp.setFirstYear(startYear.toTridasYear(DatingSuffix.RELATIVE));
				}
				if (getIntegerDefaultValue(DefaultFields.DATE_END).getValue() != null) {
					SafeIntYear endYear = new SafeIntYear(getIntegerDefaultValue(DefaultFields.DATE_END).getValue().toString(), true);
					interp.setLastYear(endYear.toTridasYear(DatingSuffix.RELATIVE));
				}
				dating.setType(NormalTridasDatingType.RELATIVE);
				interp.setDating(dating);
				series.setInterpretation(interp);
			}
			else if (dated.getValue().equals(FHDated.Undated))
			{
				// Don't include if series is undated
			}
		}
	
		
		// Created
		if(getDateTimeDefaultValue(DefaultFields.FIRST_MEASUREMENT_DATE).getValue()!=null)
		{
			DateTime date = getDateTimeDefaultValue(DefaultFields.FIRST_MEASUREMENT_DATE).getValue();
			if(series instanceof TridasMeasurementSeries)
			{
				Date date2 = DateUtils.dateTimeToDate(date);
				((TridasMeasurementSeries)series).setMeasuringDate(date2);
			}						
		}
		
		series.setCreatedTimestamp(DateUtils.getTodaysDateTime());
		

		
		// Last modified
		if(getDateTimeDefaultValue(DefaultFields.LAST_REVISION_DATE).getValue()!=null)
		{
			//log.debug("Last modified timestamp value: "+getDateTimeDefaultValue(DefaultFields.LAST_REVISION_DATE).getValue());

			series.setLastModifiedTimestamp(getDateTimeDefaultValue(DefaultFields.LAST_REVISION_DATE).getValue());
		}

		// Comments
		if(getStringDefaultValue(DefaultFields.COMMENTS).getStringValue()!=null)
		{
			series.setComments(getStringDefaultValue(DefaultFields.COMMENTS).getStringValue());
		}
		
		return series;
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasMeasurementSeries()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected TridasMeasurementSeries getDefaultTridasMeasurementSeries() {
		TridasMeasurementSeries series = super.getDefaultTridasMeasurementSeries();
		
		series = (TridasMeasurementSeries) populateSeriesFields(series);
		
		

		if(getStringDefaultValue(DefaultFields.PERS_ID).getStringValue()!=null)
		{
			series.setAnalyst(getStringDefaultValue(DefaultFields.PERS_ID).getStringValue());
		}
		
		ArrayList<TridasGenericField> gflist = new ArrayList<TridasGenericField>();

		
		if(getStringDefaultValue(DefaultFields.KEYCODE).getStringValue()!=null)
		{
			TridasGenericField gf = new TridasGenericField();
			gf.setName("keycode");
			gf.setValue(getStringDefaultValue(DefaultFields.KEYCODE).getStringValue());
			gf.setType("xs:string");
			gflist.add(gf);
			
		}
		
		if (getDefaultValue(TridasExtraField.ORIGINAL_FILENAME).getValue() != null) {
			TridasGenericField gf = new ObjectFactory().createTridasGenericField();
			gf.setName("original.treeringdatafile");
			gf.setType("xs:string");
			gf.setValue(getDefaultValue(TridasExtraField.ORIGINAL_FILENAME).getValue().toString());
			gflist.add(gf);
		}
		
		series.setGenericFields(gflist);

		

		
		// Woodcompleteness
		
		TridasWoodCompleteness wc = new TridasWoodCompleteness();
		TridasPith pith = new TridasPith();
		TridasHeartwood hw = new TridasHeartwood();
		TridasSapwood sw = new TridasSapwood();
		TridasBark bark = new TridasBark();
		
		pith.setPresence(ComplexPresenceAbsence.UNKNOWN);
		hw.setPresence(ComplexPresenceAbsence.UNKNOWN);
		sw.setPresence(ComplexPresenceAbsence.UNKNOWN);
		bark.setPresence(PresenceAbsence.UNKNOWN);
		
		wc.setPith(pith);
		wc.setHeartwood(hw);
		wc.setSapwood(sw);
		wc.setBark(bark);
		
		if(getIntegerDefaultValue(DefaultFields.RING_COUNT).getValue()!=null)
		{
			wc.setRingCount(getIntegerDefaultValue(DefaultFields.RING_COUNT).getValue());
		}
		
		if(getDoubleDefaultValue(DefaultFields.AV_RING_WIDTH).getValue()!=null)
		{
			wc.setAverageRingWidth(getDoubleDefaultValue(DefaultFields.AV_RING_WIDTH).getValue());
		}
		
		
		GenericDefaultValue<FHPith> pithField = (GenericDefaultValue<FHPith>) getDefaultValue(DefaultFields.PITH);
		if(pithField!=null && pithField.getValue()!=null)
		{
			if(pithField.getValue().equals(FHPith.PRESENT))
			{
				pith.setPresence(ComplexPresenceAbsence.COMPLETE);
			}
			else if(pithField.getValue().equals(FHPith.ABSENT))
			{
				pith.setPresence(ComplexPresenceAbsence.ABSENT);
			}
		}
		
		if (getIntegerDefaultValue(DefaultFields.SAPWOOD_RINGS).getValue() != null) {
			sw.setNrOfSapwoodRings(getIntegerDefaultValue(DefaultFields.SAPWOOD_RINGS).getValue());
			if(getIntegerDefaultValue(DefaultFields.SAPWOOD_RINGS).getValue()==0)
			{
				sw.setPresence(ComplexPresenceAbsence.ABSENT);
			}
		}

		try{
			if ((getIntegerDefaultValue(DefaultFields.SAPWOOD_RINGS).getValue() != null) && 
				pithField.getValue().equals(FHPith.PRESENT))
			{
				if(getIntegerDefaultValue(DefaultFields.SAPWOOD_RINGS).getValue()>0)
				{
					hw.setPresence(ComplexPresenceAbsence.COMPLETE);
				}
			}
			else if((pithField.getValue().equals(FHPith.ABSENT)) && 
					(getIntegerDefaultValue(DefaultFields.SAPWOOD_RINGS).getValue() == null))
			{
				hw.setPresence(ComplexPresenceAbsence.INCOMPLETE);
			}
			else if((pithField.getValue().equals(FHPith.ABSENT)) && 
					(getIntegerDefaultValue(DefaultFields.SAPWOOD_RINGS).getValue() != null))
			{
				if(getIntegerDefaultValue(DefaultFields.SAPWOOD_RINGS).getValue()==0)
				{
					hw.setPresence(ComplexPresenceAbsence.INCOMPLETE);
				}
			}
		} catch (NullPointerException ex){ }
		
		
		GenericDefaultValue<FHBarkType> barkField = (GenericDefaultValue<FHBarkType>) getDefaultValue(DefaultFields.BARK);
		if(barkField!=null && barkField.getValue()!=null)
		{
			if(barkField.getValue().equals(FHBarkType.AVAILABLE))
			{
				bark.setPresence(PresenceAbsence.PRESENT);
			}
			else if(barkField.getValue().equals(FHBarkType.UNAVAILABLE))
			{
				bark.setPresence(PresenceAbsence.ABSENT);
			}
		}
		
		series.setWoodCompleteness(wc);
		
		return series;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public TridasValues getTridasValuesWithDefaults() {
		TridasValues valuesGroup = new TridasValues();
		
		GenericDefaultValue<TridasUnit> units = (GenericDefaultValue<TridasUnit>) getDefaultValue(DefaultFields.UNIT);
		if (units.getValue() == null) {
			valuesGroup.setUnitless(new TridasUnitless());
		}
		else {
			valuesGroup.setUnit(units.getValue());
		}
		
		
		GenericDefaultValue<TridasVariable> variable = (GenericDefaultValue<TridasVariable>) getDefaultValue(TridasMandatoryField.MEASUREMENTSERIES_VARIABLE);
		valuesGroup.setVariable(variable.getValue());
	
		return valuesGroup;
	}
	
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasRadius()
	 */
	@Override
	protected TridasRadius getDefaultTridasRadius() {
		TridasRadius r = super.getDefaultTridasRadius();
		
		// Identifier
		if((getStringDefaultValue(DefaultFields.RADIUS_NUMBER).getStringValue()!=null) &&
				(getStringDefaultValue(DefaultFields.RADIUS_NUMBER).getStringValue()!=""))
		{
			r.setTitle(getStringDefaultValue(DefaultFields.RADIUS_NUMBER).getStringValue());
			TridasIdentifier id = new ObjectFactory().createTridasIdentifier();
			id.setDomain(super.getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getStringValue());
			id.setValue(getStringDefaultValue(DefaultFields.RADIUS_NUMBER).getStringValue());
			r.setIdentifier(id);
		}
		
		

		return r;
	}
	
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasSample()
	 */
	@Override
	protected TridasSample getDefaultTridasSample() {
		TridasSample s = super.getDefaultTridasSample();
		TridasIdentifier id = new ObjectFactory().createTridasIdentifier();;
		
		// Identifier
		if((getStringDefaultValue(DefaultFields.CORE_NUMBER).getStringValue()!=null) &&
			(getStringDefaultValue(DefaultFields.CORE_NUMBER).getStringValue()!=""))
		{
			s.setTitle(getStringDefaultValue(DefaultFields.CORE_NUMBER).getStringValue());
			id.setDomain(super.getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getStringValue());
			id.setValue(getStringDefaultValue(DefaultFields.CORE_NUMBER).getStringValue());
		}
		else if((getStringDefaultValue(DefaultFields.STEM_DISK_NUMBER).getStringValue()!=null) &&
			(getStringDefaultValue(DefaultFields.STEM_DISK_NUMBER).getStringValue()!=""))
		{
			s.setTitle(getStringDefaultValue(DefaultFields.STEM_DISK_NUMBER).getStringValue());
			id.setDomain(super.getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getStringValue());
			id.setValue(getStringDefaultValue(DefaultFields.STEM_DISK_NUMBER).getStringValue());
		}
		else
		{
			// Default to random UUID
			id.setDomain(I18n.getText("domain.value"));
			id.setValue(UUID.randomUUID().toString());
		}
		s.setIdentifier(id);
	
		
		// Sampling height
		if(getStringDefaultValue(DefaultFields.SAMPLING_HEIGHT).getStringValue()!=null)
		{
			s.setPosition(getStringDefaultValue(DefaultFields.SAMPLING_HEIGHT).getStringValue());
		}
		
		// Sampling date
		if(getDateTimeDefaultValue(DefaultFields.DATE_OF_SAMPLING).getValue()!=null)
		{
			s.setSamplingDate(DateUtils.dateTimeToDate(
					getDateTimeDefaultValue(DefaultFields.DATE_OF_SAMPLING).getValue()));
		}
		
		

		return s;
	}
	
	
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasElement()
	 */
	@Override
	protected TridasElement getDefaultTridasElement() {
		TridasElement e = super.getDefaultTridasElement();
					
		// Identifier
		if((getStringDefaultValue(DefaultFields.TREE_NUMBER).getStringValue()!=null) &&
		   (getStringDefaultValue(DefaultFields.TREE_NUMBER).getStringValue())!="")
		{
			e.setTitle(getStringDefaultValue(DefaultFields.TREE_NUMBER).getStringValue());
			TridasIdentifier id = new ObjectFactory().createTridasIdentifier();
			id.setDomain(super.getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getStringValue());
			id.setValue(getStringDefaultValue(DefaultFields.TREE_NUMBER).getStringValue());
			e.setIdentifier(id);
		}
		
		// Hopefully we can set from ITRDB controlled voc
		ControlledVoc v = (ControlledVoc) getDefaultValue(DefaultFields.SPECIES).getValue();
		if(v!=null){
			if(v.isSetNormalId())
			{
				// Code was absent or invalid for ITRDB controlled voc so try the plain
				// species name instead
				if (getDefaultValue(DefaultFields.SPECIES_NAME).getValue()!=null)
				{
					v.setValue(getDefaultValue(DefaultFields.SPECIES_NAME).getValue().toString());
				}
			}
		}
		e.setTaxon(v);
		
		// Location
		if((getDefaultValue(DefaultFields.LATITUDE).getValue()!=null &&
		   getDefaultValue(DefaultFields.LONGITUDE).getValue()!=null)
		   || getStringDefaultValue(DefaultFields.COUNTRY).getValue()!=null
		   || getStringDefaultValue(DefaultFields.LOCATION).getValue()!=null
		   || getStringDefaultValue(DefaultFields.TOWN).getValue()!=null
		   || getStringDefaultValue(DefaultFields.TOWN_ZIP_CODE).getValue()!=null
		   || getStringDefaultValue(DefaultFields.STATE).getValue()!=null
		   || getStringDefaultValue(DefaultFields.PROVINCE).getValue()!=null)
		{
			TridasLocation location = new ObjectFactory().createTridasLocation();
			TridasLocationGeometry geometry;
			TridasAddress address = null;
			
			// Geometry
			if(getDefaultValue(DefaultFields.LATITUDE).getValue()!=null &&
					   getDefaultValue(DefaultFields.LONGITUDE).getValue()!=null)
			{	   
				geometry = SpatialUtils.getWGS84LocationGeometry(getDoubleDefaultValue(DefaultFields.LATITUDE).getValue(), 
						getDoubleDefaultValue(DefaultFields.LONGITUDE).getValue());
				location.setLocationGeometry(geometry);
			}
		
			// Address
			if(getStringDefaultValue(DefaultFields.COUNTRY).getValue()!=null
		   || getStringDefaultValue(DefaultFields.TOWN).getValue()!=null
		   || getStringDefaultValue(DefaultFields.TOWN_ZIP_CODE).getValue()!=null
		   || getStringDefaultValue(DefaultFields.STATE).getValue()!=null
		   || getStringDefaultValue(DefaultFields.PROVINCE).getValue()!=null)
		   {
				address = new ObjectFactory().createTridasAddress();
				if(getStringDefaultValue(DefaultFields.COUNTRY).getValue()!=null)
				{
					address.setCountry(getStringDefaultValue(DefaultFields.COUNTRY).getValue());
				}
				if(getStringDefaultValue(DefaultFields.TOWN).getValue()!=null)
				{
					address.setCityOrTown(getStringDefaultValue(DefaultFields.TOWN).getValue());
				}
				if(getStringDefaultValue(DefaultFields.TOWN_ZIP_CODE).getValue()!=null)
				{
					address.setPostalCode(getStringDefaultValue(DefaultFields.TOWN_ZIP_CODE).getValue());
				}
				if(getStringDefaultValue(DefaultFields.STATE).getValue()!=null)
				{
					address.setStateProvinceRegion(getStringDefaultValue(DefaultFields.STATE).getValue());
				}
				else if(getStringDefaultValue(DefaultFields.PROVINCE).getValue()!=null)
				{
					address.setStateProvinceRegion(getStringDefaultValue(DefaultFields.PROVINCE).getValue());
				}
	
				String addressline1 = "";
				if(getStringDefaultValue(DefaultFields.HOUSE_NAME).getValue()!=null)
				{
					addressline1 += getStringDefaultValue(DefaultFields.HOUSE_NAME).getValue()+" ";
				}
				if(getStringDefaultValue(DefaultFields.HOUSE_NUMBER).getValue()!=null)
				{
					addressline1 += getStringDefaultValue(DefaultFields.HOUSE_NUMBER).getValue()+" ";
				}
				address.setAddressLine1(addressline1.trim());	
				
				String addressline2 = "";
				if(getStringDefaultValue(DefaultFields.STREET).getValue()!=null)
				{
					addressline2 += getStringDefaultValue(DefaultFields.STREET).getValue()+ " ";
				}
				if(getStringDefaultValue(DefaultFields.DISTRICT).getValue()!=null)
				{
					addressline2 += getStringDefaultValue(DefaultFields.DISTRICT).getValue()+ " ";
				}
				address.setAddressLine2(addressline2.trim());	
					
				location.setAddress(address);
		   }
			
			
			String locComment = "";
			if(getStringDefaultValue(DefaultFields.LOCATION).getValue()!=null )
			{
				locComment = getStringDefaultValue(DefaultFields.LOCATION).getValue();
			}
			if(getStringDefaultValue(DefaultFields.LOCATION_CHARACTERISTICS).getValue()!=null )
			{
				locComment = locComment + " " +getStringDefaultValue(DefaultFields.LOCATION_CHARACTERISTICS).getValue();
			}
			if (locComment!="")
			{
				location.setLocationComment(locComment.trim());
			}
			
			e.setLocation(location);
		}
		
		// Soil
		if(getStringDefaultValue(DefaultFields.SOIL_TYPE).getValue()!=null)
		{
			TridasSoil soil = new TridasSoil();
			soil.setDescription(getStringDefaultValue(DefaultFields.SOIL_TYPE).getValue());
			e.setSoil(soil);
		}
		
		// Shape
		if(getStringDefaultValue(DefaultFields.SHAPE_OF_SAMPLE).getValue()!=null)
		{
			TridasShape shape = new TridasShape();
			shape.setValue(getStringDefaultValue(DefaultFields.SHAPE_OF_SAMPLE).getValue());
			e.setShape(shape);
		}
		
		// Dimensions
		if(getStringDefaultValue(DefaultFields.TIMBER_HEIGHT).getValue()!=null && 
		   getStringDefaultValue(DefaultFields.TIMBER_WIDTH).getValue()!=null )
		{

		}
		
		// Elevation
		if(getDoubleDefaultValue(DefaultFields.ELEVATION).getValue()!=null)
		{
			e.setAltitude(getDoubleDefaultValue(DefaultFields.ELEVATION).getValue());
		}
		
		return e;
	}
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasObject()
	 */
	@Override
	protected TridasObject getDefaultTridasObject() {
		TridasObject o = super.getDefaultTridasObject();
		
		if((getStringDefaultValue(DefaultFields.SITE_CODE).getStringValue()!=null) &&
				(getStringDefaultValue(DefaultFields.SITE_CODE).getStringValue())!="")
		{
			o.setTitle(getStringDefaultValue(DefaultFields.SITE_CODE).getStringValue());
			TridasIdentifier id = new ObjectFactory().createTridasIdentifier();
			id.setDomain(super.getDefaultValue(TridasMandatoryField.IDENTIFIER_DOMAIN).getStringValue());
			id.setValue(getStringDefaultValue(DefaultFields.SITE_CODE).getStringValue());
			o.setIdentifier(id);
		}
		
		
		// Temporal coverage
		if((getStringDefaultValue(DefaultFields.ESTIMATED_TIME_PERIOD).getStringValue()!=null) && 
				(getStringDefaultValue(DefaultFields.ESTIMATED_TIME_PERIOD).getStringValue()!=""))
		{
			TridasCoverage coverage = new TridasCoverage();
			coverage.setCoverageTemporal(getStringDefaultValue(DefaultFields.ESTIMATED_TIME_PERIOD).getStringValue());
			coverage.setCoverageTemporalFoundation(I18n.getText("unknown"));
			o.setCoverage(coverage);
		}
		
		return o;
	}
	
	
	/**
	 * @see org.tridas.io.defaults.TridasMetadataFieldSet#getDefaultTridasProject()
	 */
	@Override
	protected TridasProject getDefaultTridasProject() {
		TridasProject p = super.getDefaultTridasProject();
		
		if((getStringDefaultValue(DefaultFields.PROJECT).getStringValue()!=null) &&
		   (getStringDefaultValue(DefaultFields.PROJECT).getStringValue())!="")
		{
			p.setTitle(getStringDefaultValue(DefaultFields.PROJECT).getStringValue());
		}
		
		if((getStringDefaultValue(DefaultFields.CLIENT).getValue()!=null) &&
				   (getStringDefaultValue(DefaultFields.CLIENT).getStringValue())!="")
		{
			p.setCommissioner(getStringDefaultValue(DefaultFields.CLIENT).getStringValue());
		}
		
		return p;
	}
	
	
	public enum FHBarkType {
		AVAILABLE("B"), UNAVAILABLE("-");
		
		private String code;
		
		FHBarkType(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return WordUtils.capitalize(name().toLowerCase().replace("_", " "));
		}
		
		public final String toCode() {
			return code;
		}
		
		public static FHBarkType fromCode(String code) {
			for (FHBarkType val : FHBarkType.values()) {
				if (val.toCode().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	public enum FHDataFormat {
		Double, Single, Chrono, HalfChrono, Quadro, Tree, Table, Unknown;
		// formatted this way to match the string
	}
	
	public enum FHDataType {
		RING_WIDTH("Ringwidth"),
		EARLY_WOOD("Earlywood"),
		LATE_WOOD("Latewood"),
		EARLY_LATE_WOOD("EarlyLateWood"),
		MIN_DENSITY("Min density"),
		MAX_DENSITY("Max density"),
		EARLY_WOOD_DENSITY("Earlywood density"),
		LATE_WOOD_DENSITY("Latewood density"),
		PITH_AGE("Pith age"),
		WEIGHT_OF_RING("Weight of ring");
		
		private String code;
		
		FHDataType(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return code;
		}
				
		public static FHDataType fromCode(String code) {
			for (FHDataType val : FHDataType.values()) {
				if (val.toString().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	public enum FHDated {
		Undated, Dated, RelDated;
	}
	
	public enum FHPith {
		PRESENT("P"), ABSENT("-");
		
		private String code;
		
		FHPith(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return WordUtils.capitalize(name().toLowerCase().replace("_", " "));
		}
		
		public final String toCode() {
			return code;
		}
		
		public static FHPith fromCode(String code) {
			for (FHPith val : FHPith.values()) {
				if (val.toCode().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	public enum FHStartsOrEndsWith {
		RING_WIDTH("Ring width"), EARLYWOOD("Earlywood"), LATEWOOD("Latewood");
		
		private String code;
		
		FHStartsOrEndsWith(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return WordUtils.capitalize(name().toLowerCase().replace("_", " "));
		}
		
		public final String toCode() {
			return code;
		}
		
		public static FHStartsOrEndsWith fromCode(String code) {
			for (FHStartsOrEndsWith val : FHStartsOrEndsWith.values()) {
				if (val.toCode().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	public enum FHSeriesType {
		SINGLE_CURVE("Single curve"), 
		MEAN_CURVE("Mean curve"), 
		RADIUS("Radius"),
		CHRONOLOGY("Chronology"),
		AUTOCORRELATION("Autocorrelation");
		
		private String code;
		
		FHSeriesType(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return WordUtils.capitalize(name().toLowerCase().replace("_", " "));
		}
		
		public final String toCode() {
			return code;
		}
		
		public static FHSeriesType fromCode(String code) {
			for (FHSeriesType val : FHSeriesType.values()) {
				if (val.toCode().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
	public enum FHWaldKante {
		EARLYWOOD("WKE"),
		LATEWOOD("WKL"),
		UNKNOWN("WKX"),
		INDISTINCT("WK?"),
		NONE("---");
		
		private String code;
		
		FHWaldKante(String c) {
			code = c;
		}
		
		@Override
		public final String toString() {
			return WordUtils.capitalize(name().toLowerCase().replace("_", " "));
		}
		
		public final String toCode() {
			return code;
		}
		
		public static FHWaldKante fromCode(String code) {
			for (FHWaldKante val : FHWaldKante.values()) {
				if (val.toCode().equalsIgnoreCase(code)) {
					return val;
				}
			}
			return null;
		}
	}
	
}
