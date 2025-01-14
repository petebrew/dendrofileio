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
package org.tridas.io.formats.tucson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.formats.tucson.TridasToTucsonDefaults.TucsonField;
import org.tridas.io.util.AstronomicalYear;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.StringUtils;
import org.tridas.io.util.YearRange;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasDatingType;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

/**

 */
public class TucsonFile implements IDendroFile {
	private static final Logger log = LoggerFactory.getLogger(TucsonFile.class);

	/**
	 * Tucson only has space for 4 characters to represent years so
	 * is limited to -999 to 9999.  One method to work around this
	 * is to add 8000 to all years but this makes years difficult 
	 * to read.
	 */
	private Boolean useEightThousandYearOffsetBodge = false;	
	protected TridasToTucsonDefaults fileDefaults; 	 // Contains the defaults for the fields
	protected ArrayList<TucsonSeries> seriesList; // List of series represented by this file
	private YearRange allSeriesRange = null;              // Total range of years for data in this file
	
	public TucsonFile(IMetadataFieldSet argDefaults) {
		fileDefaults = (TridasToTucsonDefaults) argDefaults;
		seriesList = new ArrayList<TucsonSeries>();
	}
	
	/**
	 * @see org.tridas.io.IDendroFile#getExtension()
	 */
	@Override
	public String getExtension() {
		
		for (TucsonSeries series : seriesList)
		{
			if(series.isCRN) return "crn";
		}
		return "rwl";
	}
	
	/**
	 * @see org.tridas.io.IDendroFile#getSeries()
	 */
	@Override
	public ITridasSeries[] getSeries() {
		
		ArrayList<ITridasSeries> list = new ArrayList<ITridasSeries>();
		
		for(TucsonSeries s : seriesList)
		{
			list.add(s.series);
		}
		
		return list.toArray(new ITridasSeries[0]);
	}
	
	/**
	 * @see org.tridas.io.IDendroFile#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return fileDefaults;
	}

	@Override
	public String[] saveToString() {
		
		StringBuilder string = new StringBuilder();
		
		writeFileHeader(string);
		writeSeriesData(string);
		return string.toString().split("\n");
	}
	
	
	
	/**
	 * Writes the data portion of this file
	 * TODO Implement support and conversion of units
	 * 
	 * @param string
	 * @throws IOException
	 */
	protected void writeSeriesData(StringBuilder string) {

		
		// Loop through each series in our list
		for (TucsonSeries s : seriesList) {
			
			ITridasSeries series = s.series;
			
			// Default the series identifier to the same as the site code
			String code = s.seriesDefaults.getStringDefaultValue(TucsonField.KEY_CODE).getStringValue();
			
			// Extract all values from series
			List<TridasValue> data = s.dataValues.getValues();
			
			// if it's summed, we print spaces instead of [1]'s later
			boolean isSummed = false; // s.isSummed();
			boolean isChronology = s.isCRN; // s.isIndexed() || s.isSummed();

			
			// Check if units are microns as we need to use a different EOF and missing ring markers 
			String eofMarker = "999";
			String missingRingMarker = "-999";
			try{
				if(series.getValues().get(0).getUnit().getNormalTridas().equals(NormalTridasUnit.MICROMETRES))
				{
					eofMarker = "-9999";
					missingRingMarker = "0";
				}
			} catch (Exception e){}
			
			// If its a chronology the EOF is different!
			if (isChronology)
			{
				eofMarker = "9990  0";
			}
			
			/*try {
				// Try and get the unique identifier
				code = StringUtils.rightPadWithTrim(series.getIdentifier().getValue().toString(), 8);
			} catch (NullPointerException e) {
				try {
					// That failed, so try and get title instead
					code = StringUtils.rightPadWithTrim(series.getTitle().toString(), 8);
				} catch (NullPointerException e2) {
					// That also failed so try site code
					if(isChronology) 
					{
						code = StringUtils.rightPadWithTrim(defaults.getStringDefaultValue(TucsonField.KEY_CODE).getStringValue(), 6);
					}
					else
					{
						code = StringUtils.rightPadWithTrim(defaults.getStringDefaultValue(TucsonField.KEY_CODE).getStringValue(), 8);
					}
				}
			}*/
			
			// Calculate start and end years
			AstronomicalYear start;
			AstronomicalYear end;
			try {
				start = (new SafeIntYear(series.getInterpretation().getFirstYear())).toAstronomicalYear();
			} catch (Exception e) {
				start = (new SafeIntYear()).toAstronomicalYear();
			}
			try {
				end = start.add(series.getValues().get(0).getValues().size());
			} catch (Exception e) {
				end = start.add(0);
			}
			

			
			// start year; processed files always start on the decade
			AstronomicalYear y = start;
			if (isChronology) {
				y = y.add(-start.column());
			}
			
			// Infinite loop until we reach end of data
			for (;;) {
				
				SafeIntYear safeintyear = y.toSafeIntYear();
				
				// Row header column
				log.debug("***** Y  = "+ y);
				System.out.println("Astronomical year = "+y);
				System.out.println("Calendar year     = "+safeintyear.formattedYear(NormalTridasDatingType.ABSOLUTE, DatingSuffix.AD));
				log.debug("y.column = "+y.column());
				log.debug("y.equals(new Astronomical Year(0)) : "+y.equals(new AstronomicalYear(0)));
				
				log.debug("y.equals(start): "+y.equals(start));
				
				
				if (  y.equals(new AstronomicalYear(0))       || 
					  y.column() == 0   					  || 
					 (y.equals(start) && !isChronology)) 
				{
					
					if(isChronology)
					{
						writeRowHeader(string, code, 6, y);
					}
					else
					{
						writeRowHeader(string, code, 8, y);
					}
				}
				
				// Reached end of data so print stop code
				if (y.compareTo(end) >= 0 || (isChronology && y.compareTo(start) < 0)) {
					if (!isChronology) {
						// "   999", and STOP
						string.append(StringUtils.leftPad(eofMarker, 6));
						break;
					}
					else {
						// "9990   " or "9990  0"
						string.append(isSummed ? StringUtils.rightPad(eofMarker, 6)+"0": StringUtils.rightPad(eofMarker, 7));
					}
				}
				else {
					// Extract data value			
					int index = y.diff(start);
					
					String thisDataValue = data.get(index).getValue().toString();
					System.out.println("Value = "+thisDataValue);
					System.out.println("Index = "+index);
					System.out.println("-----");
					
					// If this is a missing ring we should override
					if(thisDataValue.trim().equals("0"))
					{
						thisDataValue = missingRingMarker;
					}
					
					// Ring value is 999 which causes confusion in COFECHA etc
					if(thisDataValue.trim().equals("999"))
					{
						thisDataValue = "998";
					}
					
					// Print data value, either left padded to 4 or 6 digits for measurementSeries and derivedSeries
					// respectively
					string.append(StringUtils.leftPad
							(thisDataValue, 
							(isChronology ? 4 : 6)));
					
					// Include count if applicable: "%3d" (right-align)
					if (isChronology) {
						if(data.get(y.diff(start)).isSetCount())
						{
							string.append(StringUtils.leftPad(data.get(y.diff(start)).getCount().toString(), 3));
						}
						else
						{
							// This series is a derivedSeries but has no count info.  Set count to 1.
							string.append(StringUtils.leftPad("1", 3));
						}
					}

				}
				
				// chronologies end only after 9cols+eoln
 				if (isChronology && y.compareTo(end) > 0 && y.column() == 9) {
					break;
				}
				
				// eoln
 				 				
				if (y.column() == 9) {
					string.append("\n");
				}
				
				// increment year counter
				y = y.add(+1);
			}
			
			string.append("\n");
			
		}
	}
	
	/**
	 * Writes the row header
	 * 
	 * @param string
	 *            BufferedWriter to write to
	 * @param code
	 *            Series code
	 * @param colWidth
	 *            How many characters should the code be?
	 * @param y
	 *            The year we're at
	 * @throws IOException
	 */
	private void writeRowHeader(StringBuilder string, String code, int colWidth, AstronomicalYear y) {
		String yearMarker; // don't print the decade for the first one
		if (y.compareTo(allSeriesRange.getStart()) <= 0) {
			yearMarker = allSeriesRange.getStart().toAstronomicalYear().toString();
		}
		else {
			yearMarker = y.toString();
		}

		yearMarker = StringUtils.leftPad(yearMarker, 4);
		code = StringUtils.rightPad(code, colWidth).substring(0,8);
		string.append(code + yearMarker);
	}
	
	
	/**
	 * Writes the header for this file
	 * 
	 * @param string
	 */
	private void writeFileHeader(StringBuilder string) {
		// Write header info
		String siteCode = fileDefaults.getStringDefaultValue(TucsonField.SITE_CODE).getStringValue();
		String siteName = fileDefaults.getStringDefaultValue(TucsonField.SITE_NAME).getStringValue();
		String speciesCode = fileDefaults.getStringDefaultValue(TucsonField.SPECIES_CODE).getStringValue();
		String stateCountry = fileDefaults.getStringDefaultValue(TucsonField.STATE_COUNTRY).getStringValue();
		String speciesName = fileDefaults.getStringDefaultValue(TucsonField.SPECIES_NAME).getStringValue();
		String elevation = fileDefaults.getDoubleDefaultValue(TucsonField.ELEVATION).getStringValue();
		if(fileDefaults.getDoubleDefaultValue(TucsonField.ELEVATION).getValue()!=null)
		{
			elevation+= "m";
		}
		else
		{
			elevation+= " ";
		}
		String latlong = fileDefaults.getStringDefaultValue(TucsonField.LATLONG).getStringValue();
		String investigator = fileDefaults.getStringDefaultValue(TucsonField.INVESTIGATOR).getStringValue();
		String compDate = fileDefaults.getStringDefaultValue(TucsonField.COMP_DATE).getStringValue();
		
		string.append(siteCode + "1" +StringUtils.getSpaces(1) + siteName + speciesCode + StringUtils.getSpaces(15) + "\n");
		string.append(siteCode + "2" +StringUtils.getSpaces(1) + stateCountry + speciesName  
				+ elevation + latlong + StringUtils.getSpaces(10) + getRangeAsString() + StringUtils.getSpaces(4)+"\n");
		string.append(siteCode + "3" +StringUtils.getSpaces(1) + investigator + compDate + "\n");
	}
	
	
	/**
	 * Get the range as a string 'XXXX XXXX'. The Tucson format
	 * requires this field to be 9 chars long but this function
	 * *may* return more. I've tried (using +8000 fix) to handle
	 * this, but what can you do if years are before 8000BC?
	 * 
	 * @return
	 */
	private String getRangeAsString() {
		
		// Range is null so just return spaces
		if (allSeriesRange == null) {
			return StringUtils.getSpaces(9);
		}
		
		// If years are BC (negative) then add 8000
		// if 'bodge' flag is turned on
		if ((Integer.parseInt(allSeriesRange.getStart().toString()) < 0 || Integer.parseInt(allSeriesRange.getEnd().toString()) < 0)
				&& useEightThousandYearOffsetBodge) {
			return String.valueOf((Integer.parseInt(allSeriesRange.getStart().toAstronomicalYear().toString()) + 8000)) + " "
					+ String.valueOf((Integer.parseInt(allSeriesRange.getEnd().toAstronomicalYear().toString()) + 8000));
			
		}
		
		// return range
		return allSeriesRange.getStart().toAstronomicalYear() + " " + allSeriesRange.getEnd().add(-1).toAstronomicalYear();
	}
	
	/**
	 * Add a Tridas series to the list of series to be written to this file
	 * 
	 * @param series
	 */
	public void addSeries(ITridasSeries series, TridasValues values, TridasToTucsonDefaults dfts){
			
		YearRange thisSeriesRange = new YearRange(series);
		
		if (thisSeriesRange != null) {
			
			// Add this series range to the files total range
			if (allSeriesRange == null) {
				allSeriesRange = thisSeriesRange;
			}
			else {
				YearRange newrange = allSeriesRange.union(thisSeriesRange);
				allSeriesRange = newrange;
			}
						
			// Warn if any data is BC
			if (SafeIntYear.min(thisSeriesRange.getStart(), new SafeIntYear(1)) == thisSeriesRange.getStart()) {
				fileDefaults.addConversionWarning(new ConversionWarning(WarningType.AMBIGUOUS, 
						I18n.getText("tucson.before1AD")));
			}
			
			// Warn if any data = 999
			boolean has999Data = false;
			for (TridasValue val : values.getValues())
			{
				if(val.getValue().equals("999"))
				{
					has999Data = true;
				}
			}
			
			if(has999Data)
			{
				fileDefaults.addConversionWarning(new ConversionWarning(WarningType.UNREPRESENTABLE, 
						I18n.getText("tucson.has999data")));
			}
			
			// Add this series to our list
			seriesList.add(new TucsonSeries(series, values, dfts));
			
		}
		
	}
	
	
	
	public class TucsonSeries
	{
		public final ITridasSeries series;
		public final TridasValues dataValues;
		public final TridasToTucsonDefaults seriesDefaults; 
		public Boolean isCRN;
		
		public TucsonSeries(ITridasSeries series, TridasValues dataValues, TridasToTucsonDefaults defaults)
		{
			this.series = series;
			this.dataValues = dataValues;
			this.seriesDefaults = defaults;
			isCRN = false;
			
			for(TridasValue value : series.getValues().get(0).getValues())
			{
				if(value.isSetCount() && value.getCount()>1)
				{
					isCRN = true;
					break;
				}
			}
			
		}
		
	}
	
}
