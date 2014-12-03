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
package org.tridas.io.formats.odfmatrix;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.TridasUtils;
import org.tridas.io.util.YearRange;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.TridasGenericField;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class ODFMatrixFile implements IDendroFile {
	
	private ArrayList<ITridasSeries> seriesList = new ArrayList<ITridasSeries>();
	private YearRange yrRange;
	private DatingSuffix calendar = DatingSuffix.AD;
	private IMetadataFieldSet defaults;
	
	public ODFMatrixFile(IMetadataFieldSet argDefaults) {
		defaults = argDefaults;
	}
	
	public void setSeriesList(ArrayList<ITridasSeries> lst) {
		
		
		// Switch the BP dating if any series are in BP
		for (ITridasSeries ser : lst) {
			
			if (calendar == DatingSuffix.BP) {
				break;
			}
			try {
				if (ser.getInterpretation().getFirstYear().getSuffix() == DatingSuffix.BP) {
					calendar = DatingSuffix.BP;
				}
			} catch (NullPointerException e) {}
		}
		

		
		
		// Calculate the range for these series
		for (ITridasSeries ser : lst) {
			Integer ringcount = ser.getValues().get(0).getValues().size();
			SafeIntYear startYear = null;
			try {
				
				// Make sure we're using years with the right calendar
				//Year yearsWithCalendar = new SafeIntYear(ser.getInterpretation().getFirstYear()).toTridasYear(calendar);
				startYear = new SafeIntYear(ser.getInterpretation().getFirstYear());
				
			} catch (NullPointerException e) {
				startYear = new SafeIntYear();
			}
			
			YearRange thisrange = new YearRange(startYear, ringcount);
			
			if (yrRange == null) {
				yrRange = thisrange;
			}
			
			yrRange = yrRange.union(thisrange);
		}
		
		// Set the list
		seriesList = lst;
	}
	
	@Override
	public String getExtension() {
		return "ods";
	}
	
	@Override
	public ITridasSeries[] getSeries() {
		return seriesList.toArray(new ITridasSeries[0]);

	}
	
	@Override
	public String[] saveToString() {
		
		throw new UnsupportedOperationException(I18n.getText("fileio.binaryAsStringUnsupported"));
		
	}
	
	/**
	 * An alternative to the normal saveToString() as this is a binary format
	 * 
	 * @param os
	 * @throws IOException
	 */
	public void saveToDisk(OutputStream os) throws IOException {
		OdfSpreadsheetDocument outputDocument;

		try {
			outputDocument = OdfSpreadsheetDocument.newSpreadsheetDocument();
			
			OdfTable table; 
			
			table = outputDocument.getTableByName("Sheet1");
			table.setTableName(I18n.getText("general.data"));
						


			writeYearHeaderCol(table);
			
			int col = 1;
			for (ITridasSeries series : seriesList) {
				
				for(TridasValues v : series.getValues())
				{
					writeRingWidthColumn(table, col, series, v);
					col++;
				}
			}
			
			outputDocument.save(os);
			
			
			
		} catch (Exception e) {

			throw new IOException(e.getLocalizedMessage());
		}
		
	}
	
	/**
	 * Write the range of years in the first column of the worksheet
	 * 
	 * @param table
	 */
	private void writeYearHeaderCol(OdfTable table) {
		if (yrRange == null) {
			return;
		}
		
		// Create year label
		String yearlabel = I18n.getText("general.years");
		if (calendar == DatingSuffix.BP) {
			yearlabel += " (" + I18n.getText("general.years.bp") + ")";
		}
		else if ((yrRange.getStart().compareTo(new SafeIntYear("-1")) < 0)
				&& (yrRange.getEnd().compareTo(new SafeIntYear("1")) > 0)) {
			yearlabel += " (" + I18n.getText("general.years.bc") + "/" + I18n.getText("general.years.ad") + ")";
		}
		else if ((yrRange.getStart().compareTo(new SafeIntYear("-1")) < 0)
				&& (yrRange.getEnd().compareTo(new SafeIntYear("1")) <= 0)) {
			yearlabel += " (" + I18n.getText("general.years.bc") + ")";
		}
		else if ((yrRange.getStart().compareTo(new SafeIntYear("-1")) >= 0)
				&& (yrRange.getEnd().compareTo(new SafeIntYear("1")) > 0)) {
			yearlabel += " (" + I18n.getText("general.years.ad") + ")";
		}
	
		table.getCellByPosition(0, 0).setStringValue(yearlabel);
		
		SafeIntYear yr = yrRange.getStart();
		Integer rowNumber = 1;
		Double yearval;
		while (yr.compareTo(yrRange.getEnd()) <= 0) {
			yearval = Double.parseDouble(yr.toTridasYear(calendar).getValue().toString());
			
			table.getCellByPosition(0, rowNumber).setDoubleValue(yearval);
			
			// Increment to next year and row number
			yr = yr.add(1);
			rowNumber++;
		}
		
	}
	

	/**
	 * Write the ring widths for the provided series in the specified column
	 * 
	 * @param table
	 * @param col
	 * @param series
	 */
	private void writeRingWidthColumn(OdfTable table, Integer col, ITridasSeries series, TridasValues values)  {
		
		// Creates year label
		String l;
		String keycode = null;
		if(series.isSetGenericFields())
		{
			for(TridasGenericField gf : series.getGenericFields())
			{
				if(gf.getName().toLowerCase().equals("keycode"))
				{
					keycode = gf.getValue();
				}
			}
		}
		if(keycode!=null)
		{
			l = keycode;
		}
		else
		{
		    l = series.getTitle();
		}
		
		// Add variable name to column title if necessary
		if(series.getValues().size()>1)
		{
			l = l+"-"+TridasUtils.controlledVocToString(values.getVariable());
		}
		
		table.getCellByPosition(col, 0).setStringValue(l);
		
		// Calculate which row to start on
		SafeIntYear thisStartYear = new SafeIntYear();
		try {
			thisStartYear = new SafeIntYear(series.getInterpretation().getFirstYear());
		} catch (NullPointerException e) {
			thisStartYear = new SafeIntYear(1);
			
		}
		
		Integer row = 1;
		
		for (SafeIntYear currYear = yrRange.getStart(); currYear.compareTo(thisStartYear) < 0; currYear = currYear
				.add(1)) {
			table.getCellByPosition(col, row).setStringValue("");
			row++;
		}
		
		// Loop through values and write to spreadsheet
		Double yearval;
		for (TridasValue value : values.getValues()) {
			if(value.getValue()!=null)
			{
			
				try{
					yearval = Double.parseDouble(value.getValue());
					
					if(yearval.equals(Double.NaN)) 
					{
						throw new Exception ("nan");
					}
					else
					{
						table.getCellByPosition(col, row).setDoubleValue(yearval);
					}
				} catch (Exception e)
				{
					// Unable to parse value as number so write as string instead
					String stryearval = value.getValue();
					table.getCellByPosition(col, row).setStringValue(stryearval);
				}
			}
			row++;
		}
		
	}
	
	/**
	 * @see org.tridas.io.IDendroFile#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}
}