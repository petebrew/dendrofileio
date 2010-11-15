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

import java.util.ArrayList;

import org.grlea.log.SimpleLogger;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.corina.CorinaToTridasDefaults.DefaultFields;
import org.tridas.io.util.SafeIntYear;
import org.tridas.schema.SeriesLink;
import org.tridas.schema.SeriesLinks;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.schema.SeriesLink.XLink;

public class CorinaReader extends AbstractDendroFileReader {

	private static final SimpleLogger log = new SimpleLogger(CorinaReader.class);
	private CorinaToTridasDefaults defaults = null;
	private int currentLineNumber = -1;
	
	private ArrayList<Integer> dataVals = new ArrayList<Integer>();
	private ArrayList<Integer> countVals = new ArrayList<Integer>();
	private ArrayList<String> parentSeries = new ArrayList<String>();
	private Boolean isDerivedSeries = false;
	
	
	public CorinaReader() {
		super(CorinaToTridasDefaults.class);
	}
	
	@Override
	public int getCurrentLineNumber() {
		return currentLineNumber;
	}

	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}


	/**
	 * @see org.tridas.io.IDendroFileReader#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("corina.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("corina.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("corina.about.shortName");
	}
	
	@Override
	public String[] getFileExtensions() {
		return new String[]{"raw", "rec", "cln", "sum"};
	}
	
	@Override
	protected void resetReader() {
		
		dataVals.clear();

	}


	/*@Override
	public TridasProject getProject() {
		
		TridasProject project = defaults.getProjectWithDefaults(true);
		
		TridasMeasurementSeries ms = project.getObjects().get(0).getElements().get(0).getSamples().get(0).getRadiuses().get(0).getMeasurementSeries().get(0);
		TridasValues valuesGroup = defaults.getTridasValuesWithDefaults();
		valuesGroup.setValues(getTridasValueList());
		
		ArrayList<TridasValues> valuesList = new ArrayList<TridasValues>();
		
		valuesList.add(valuesGroup);
		ms.setValues(valuesList);

		return project;
		
	}*/
	
	@Override
	public TridasProject getProject() {

		// Create entities
		TridasProject p = defaults.getProjectWithDefaults();
		TridasObject o = defaults.getObjectWithDefaults();
		TridasElement e = defaults.getElementWithDefaults();
		TridasSample s = defaults.getSampleWithDefaults();
		
		// Compile TridasValues array
		TridasValues valuesGroup = defaults.getTridasValuesWithDefaults();
		valuesGroup.setValues(getTridasValueList());
		ArrayList<TridasValues> vlist = new ArrayList<TridasValues>();
		vlist.add(valuesGroup);
		
		if(isDerivedSeries)
		{
			TridasDerivedSeries dseries = defaults.getDefaultTridasDerivedSeries();
			dseries.setValues(vlist);
			
			// Set Link series
			SeriesLinks slinks = new SeriesLinks();
			ArrayList<SeriesLink> seriesList = new ArrayList<SeriesLink>();
			for(String parent : parentSeries)
			{
				SeriesLink series = new SeriesLink();
				XLink linkvalue = new XLink();
				linkvalue.setHref(parent);
				series.setXLink(linkvalue);
				seriesList.add(series);
			}
			
			slinks.setSeries(seriesList);
			dseries.setLinkSeries(slinks);
			
			ArrayList<TridasDerivedSeries> dslist = new ArrayList<TridasDerivedSeries>();
			dslist.add(dseries);
			p.setDerivedSeries(dslist);
		}
		else
		{
			TridasRadius r = defaults.getRadiusWithDefaults(false);
					
			// Now build up our measurementSeries
			TridasMeasurementSeries series = defaults.getMeasurementSeriesWithDefaults();

			// Compile project
			series.setValues(vlist);
			
			ArrayList<TridasMeasurementSeries> seriesList = new ArrayList<TridasMeasurementSeries>();
			seriesList.add(series);
			r.setMeasurementSeries(seriesList);
		
			ArrayList<TridasRadius> rList = new ArrayList<TridasRadius>();
			rList.add(r);
			s.setRadiuses(rList);
			
			ArrayList<TridasSample> sList = new ArrayList<TridasSample>();
			sList.add(s);
			e.setSamples(sList);
			
			ArrayList<TridasElement> eList = new ArrayList<TridasElement>();
			eList.add(e);
			o.setElements(eList);
			
			ArrayList<TridasObject> oList = new ArrayList<TridasObject>();
			oList.add(o);		
			p.setObjects(oList);
		}
		
		
		
		return p;
		
	}

	
	private ArrayList<TridasValue> getTridasValueList()
	{
		ArrayList<TridasValue> tvs = new ArrayList<TridasValue>();
		
		for (int i=0; i<countVals.size(); i++)
		{
			if(countVals.get(i).compareTo(1)>0) isDerivedSeries=true;
		}
		
		if(countVals.size()!=dataVals.size())
		{
			log.warn("Count and data vals are not the same size: Count="+ countVals.size()+ " Data="+ dataVals.size());
			return null;
		}
		
		for (int i=0; i<dataVals.size(); i++)
		{
			TridasValue value = new TridasValue();
			value.setValue(dataVals.get(i).toString());
			if(isDerivedSeries)
			{
				value.setCount(countVals.get(i));
			}
			tvs.add(value);
		}
		
		return tvs;
	}

	@Override
	protected void parseFile(String[] argFileString,
			IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		
		log.debug("Parsing: " + argFileString);
		defaults = (CorinaToTridasDefaults) argDefaultFields;
				
		checkFileIsValid(argFileString);
		
		// Loop through lines
		for (int linenum=1; linenum<argFileString.length; linenum++)
		{
			String line = argFileString[linenum];
			currentLineNumber =linenum+1;
			
			// if line is blank, skip it
			if(line.equals("")) continue;
			
			// Last line is user name which is denoted by a ~
			if(line.startsWith("~"))
			{
				defaults.getStringDefaultValue(DefaultFields.USERNAME).setValue(line.substring(2).trim());
				return;
			}
			
			// Split the line into key-value pairs based on ';' delimiter
			if(!line.contains(";")) continue;
			
			for(String tagAndValue : line.split(";"))
			{	
				String key = null;
				String value = null;
				
				try{
				key = tagAndValue.substring(0, tagAndValue.indexOf(" "));
				value = tagAndValue.substring(tagAndValue.indexOf(" "));
				} catch (Exception e)
				{
					continue;
				}
				
				if(key.equalsIgnoreCase("DATA"))
				{
					readData(argFileString, linenum+1);
					linenum = this.getNextLineToRead(argFileString, linenum);
					
				}
				else if (key.equalsIgnoreCase("ELEMENTS"))
				{
					readElements(argFileString, linenum+1);
					linenum = this.getNextLineToRead(argFileString, linenum);
				}
				else if (key.equalsIgnoreCase("WEISERJAHRE"))
				{
					linenum = this.getNextLineToRead(argFileString, linenum);
				}
				else if (key.equalsIgnoreCase("ID"))
				{
					defaults.getStringDefaultValue(DefaultFields.ID).setValue(value.trim());
				}
				else if (key.equalsIgnoreCase("NAME"))
				{
					defaults.getStringDefaultValue(DefaultFields.NAME).setValue(value.trim());
				}
				else if (key.equalsIgnoreCase("DATING"))
				{
					defaults.getStringDefaultValue(DefaultFields.DATING).setValue(value.trim());
				}
				else if (key.equalsIgnoreCase("UNMEAS_PRE"))
				{
					try{
					defaults.getIntegerDefaultValue(DefaultFields.UNMEAS_PRE).setValue(Integer.parseInt(value.trim()));
					} catch (NumberFormatException e)
					{
						addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("corina.invalidUnmeasPre")));
					}
				}
				else if (key.equalsIgnoreCase("UNMEAS_POST"))
				{
					try{
					defaults.getIntegerDefaultValue(DefaultFields.UNMEAS_POST).setValue(Integer.parseInt(value.trim()));
					} catch (NumberFormatException e)
					{
						addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("corina.invalidUnmeasPost")));
					}
				}
				else if (key.equalsIgnoreCase("FILENAME"))
				{
					defaults.getStringDefaultValue(DefaultFields.FILENAME).setValue(value.trim());
				}
				else if (key.equalsIgnoreCase("TYPE"))
				{
					defaults.getStringDefaultValue(DefaultFields.TYPE).setValue(value.trim());
				}
				else if (key.equalsIgnoreCase("SPECIES"))
				{
					defaults.getStringDefaultValue(DefaultFields.SPECIES).setValue(value.trim());
				}
				else if (key.equalsIgnoreCase("FORMAT"))
				{
					defaults.getStringDefaultValue(DefaultFields.FORMAT).setValue(value.trim());
				}
				else if (key.equalsIgnoreCase("PITH"))
				{
					defaults.getStringDefaultValue(DefaultFields.PITH).setValue(value.trim());
				}
				else if (key.equalsIgnoreCase("TERMINAL"))
				{
					defaults.getStringDefaultValue(DefaultFields.TERMINAL).setValue(value.trim());
				}
				else if (key.equalsIgnoreCase("CONTINUOUS"))
				{
					defaults.getStringDefaultValue(DefaultFields.CONTINUOUS).setValue(value.trim());
				}
				else if (key.equalsIgnoreCase("QUALITY"))
				{
					defaults.getStringDefaultValue(DefaultFields.QUALITY).setValue(value.trim());
				}
				else if (key.equalsIgnoreCase("RECONCILED"))
				{
					defaults.getStringDefaultValue(DefaultFields.RECONCILED).setValue(value.trim());
				}
			}	
		}	
	}

	private Integer getNextLineToRead(String[] argFileString, Integer currentIndex)
	{
		for (int i=currentIndex; i<argFileString.length; i++)
		{
			if(argFileString[i].startsWith(";") || argFileString[i].startsWith("~"))
			{
				return i;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private void readData(String[] argFileString, Integer dataStartIndex) throws InvalidDendroFileException
	{
		defaults.getSafeIntYearDefaultValue(DefaultFields.START_YEAR).setValue(
				new SafeIntYear(argFileString[dataStartIndex].substring(0, 5).trim()));

		Boolean eolMarker = false;
		
		for (int i=dataStartIndex; i+1<=argFileString.length; i=i+2)
		{
			// Skip blank lines
			if(argFileString[i].equals("")) continue;
			
			// Grab data line (minus the year field)
			String dataLine = argFileString[i].substring(5);
			
			// Grab count line (minus starting spaces)
			String countLine = argFileString[i+1].substring(5);
			
			// Loop through values in dataLine
			for (int charpos=0; charpos+6<=dataLine.length(); charpos=charpos+6)
			{			
				String strval = dataLine.substring(charpos, charpos+6).trim();
				
				// Skip blank values
				if (strval.equals("")) continue;
				
				// Parse into integer
				try{
					Integer intval = Integer.parseInt(strval);
					
					// Check for stop marker
					if(intval.equals(9990)) {
						eolMarker = true;
						break;
					}
					
					// Add to array
					dataVals.add(intval);
				} catch(NumberFormatException e)
				{
					throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"), i);
				}
			}
			
			String[] countArr = countLine.split("\\[");
			
			for(String strcount : countArr)
			{
				if(strcount.trim().length()==0) continue;
				
				strcount = strcount.substring(0, strcount.indexOf("]"));
				
				// Parse into integer
				try{
					Integer intval = Integer.parseInt(strcount);
					countVals.add(intval);
				} catch(NumberFormatException e)
				{
					log.warn("invalid count number");
					throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"), i);
				}
			}
			
			// Reached end of data
			if(eolMarker) {
				// Remove the last count as it is just refers to the end marker
				countVals.remove(countVals.size()-1);
				return;
			}
			
		}
		
	}
	
	
	private void readElements(String[] argFileString, Integer dataStartIndex)
	{
		for (int i=dataStartIndex; i<argFileString.length; i++)
		{
			// Skip blank lines
			if(argFileString[i].equals("")) continue;
				
			String line = argFileString[i];
			
			// If line has a ; in it then return
			if (line.contains(";")) return;
			
			// Add to list
			parentSeries.add(line);
			
		}
		
	}
	
	private void readWJ(String[] argFileString, Integer dataStartIndex) throws InvalidDendroFileException
	{
		for (int i=dataStartIndex; i<argFileString.length; i++)
		{
			// Skip blank lines
			if(argFileString[i].equals("")) continue;
		
			// Grab data line (minus the year field)
			String wjLine = argFileString[i].substring(5);
		
			// Loop through values in wjLine
			for (int charpos=0; charpos+6<=wjLine.length(); charpos=charpos+6)
			{
				String strval = wjLine.substring(charpos, charpos+6).trim();
				
				// Skip blank values
				if (strval.equals("")) continue;
				
				// Parse into integer
				try{
					Integer intval = Integer.parseInt(strval);
					
					// Check for stop marker
					if(intval.equals(9990)) return;
					
					// Add to array
					dataVals.add(intval);
				} catch(NumberFormatException e)
				{
					throw new InvalidDendroFileException(I18n.getText("fileio.invalidDataValue"), i);
				}
			}
			
		}
		
		
	}
	
	
	/** 
	 * @param argFileString
	 * @throws InvalidDendroFileException
	 */
	private void checkFileIsValid(String[] argFileString) throws InvalidDendroFileException
	{
	
	}


}
