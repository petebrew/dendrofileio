/*******************************************************************************
 * Copyright 2011 Peter Brewer and Daniel Murphy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.tridas.io.formats.excelmatrix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.DendroFileFilter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.IncorrectDefaultFieldsException;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.exceptions.InvalidDendroFileException.PointerType;
import org.tridas.io.util.SafeIntYear;
import org.tridas.io.util.StringUtils;
import org.tridas.schema.DatingSuffix;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.TridasInterpretation;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasTridas;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;

public class ExcelMatrixReader extends AbstractDendroFileReader {

	private static final Logger log = LoggerFactory.getLogger(ExcelMatrixReader.class);
	private ExcelMatrixToTridasDefaults defaults;
	private Sheet sheet;
	private ArrayList<ExcelDendroSeries> series = new ArrayList<ExcelDendroSeries>();
	
	public ExcelMatrixReader()
	{
		super(ExcelMatrixToTridasDefaults.class, new ExcelMatrixFormat());
	}
	
	// *******************************
	// NOT SUPPORTED - BINARY FORMAT
	// *******************************
	
	@Override
	protected void parseFile(String[] argFileString, IMetadataFieldSet argDefaultFields) {
		throw new UnsupportedOperationException(I18n.getText("general.binaryNotText"));
	}
	
	@Override
	public void loadFile(String[] argFileStrings, String filename) throws InvalidDendroFileException {
		throw new UnsupportedOperationException(I18n.getText("general.binaryNotText"));
	}
		
	
	/**
	 * @throws IncorrectDefaultFieldsException
	 * @throws InvalidDendroFileException
	 * @see org.tridas.io.IDendroCollectionWriter#loadFile(java.lang.String)
	 */
	@Override
	public void loadFile(String argFilename, IMetadataFieldSet argDefaultFields) throws IOException,
			IncorrectDefaultFieldsException, InvalidDendroFileException {
		
		log.debug("loading file from: " + argFilename);
		defaults = (ExcelMatrixToTridasDefaults) argDefaultFields;
		File file = new File(argFilename);
		
		try {
			Workbook wb = Workbook.getWorkbook(file);
			parseFile(wb);
		} catch (BiffException e) {
			throw new InvalidDendroFileException(e.getMessage());
		}

	}
	
	@Override
	public void loadFile(String argPath, String argFilename, IMetadataFieldSet argDefaultFields) throws IOException,
			IncorrectDefaultFieldsException, InvalidDendroFileException {
		
		log.debug("loading file from: " + argPath + File.separatorChar + argFilename);
		defaults = (ExcelMatrixToTridasDefaults) argDefaultFields;
		File file = new File(argPath + File.separatorChar + argFilename);
		
		try {
			Workbook wb = Workbook.getWorkbook(file);
			parseFile(wb);
		} catch (BiffException e) {
			throw new InvalidDendroFileException(e.getMessage());
		}

	}
		
	/**
	 * Check this is a valid Excel file
	 * 
	 * @param argFileBytes
	 * @throws InvalidDendroFileException
	 */
	protected void parseFile(Workbook wb) throws InvalidDendroFileException{
	
		if(wb==null) throw new InvalidDendroFileException(I18n.getText("excelmatrix.workbookError"));
		
		if(wb.getSheets().length>1)
		{
			this.addWarning(new ConversionWarning(WarningType.IGNORED, 
					I18n.getText("excelmatrix.ignoringWorksheetsExcept",
							wb.getSheet(0).getName())));
		}
		
		sheet = wb.getSheet(0);

		// Check year column is valid
		Cell[] yearCol = sheet.getColumn(0);
		Integer lastval = null;
		Integer thisval = null;
		for (int i=1; i < yearCol.length; i++)
		{
			// Check cell is an integer
			try{
				thisval = Integer.parseInt(sheet.getCell(0, i).getContents());
				
				if(thisval.equals(0))
				{
					throw new InvalidDendroFileException(
							I18n.getText("excelmatrix.yearsNotGregorian"), 
							"A"+String.valueOf(i), PointerType.CELL);
				}
				
			} catch (NumberFormatException e)
			{
				throw new InvalidDendroFileException(
						I18n.getText("excelmatrix.yearNumberExpected"), 
						"A"+String.valueOf(i), PointerType.CELL);
			}
			

			
			if (lastval==null) 
			{
				// First year 
				lastval = thisval;
				continue;
			}
			
			SafeIntYear previousYear = new SafeIntYear(lastval);
			SafeIntYear thisYear = new SafeIntYear(thisval);
			
			if(previousYear.add(1).equals(thisYear))
			{
				// Next year in sequence - so ok
				lastval = thisval;
				continue;
			}
			else
			{
				throw new InvalidDendroFileException(
						I18n.getText("excelmatrix.invalidYearSequence"), 
						"A"+String.valueOf(i+1), PointerType.CELL);
			}
		}
		
		// Loop through data columns
		for(int i=1; i < sheet.getColumns(); i++)
		{
			Cell[] datacol = sheet.getColumn(i);
			ExcelDendroSeries edc = new ExcelDendroSeries();
			
			// Throw a wobbly if header is empty
			if(datacol[0].getContents().equals("")) 
			{
				throw new InvalidDendroFileException(
						I18n.getText("excelmatrix.emptyHeader"), 
						getColRef(i)+"1", PointerType.CELL);
			}
			
			// Warn if there is more data than years
			if(datacol.length>yearCol.length)
			{
				this.addWarning(new ConversionWarning(WarningType.IGNORED, 
						I18n.getText("excelmatrix.moreDataThanYears",
						getColRef(i))));
			}
					
			// Compile a list of the data values
			ArrayList<Double> dataVals = new ArrayList<Double>();
			Boolean atStartOfData =false;
			for(int j=1; j<datacol.length; j++)
			{
				if(atStartOfData== false && datacol[j].getContents().equals(""))
				{
					continue;
				}
				else if (atStartOfData == false)
				{
					atStartOfData = true;
					edc.startYear = this.getYearForRow(j);
				}
				else if(atStartOfData== true && datacol[j].getContents().equals(""))
				{
					break;
				}
				
				
				try{
					
					if(( datacol[j].getType() != CellType.NUMBER) && 
					   ( datacol[j].getType() != CellType.NUMBER_FORMULA) )
					{
						throw new InvalidDendroFileException(
								I18n.getText("excelmatrix.invalidDataValue"), 
								getColRef(i)+String.valueOf(j+1), 
								PointerType.CELL);
					}
					
					Double val = null;
					val = Double.valueOf(datacol[j].getContents());			
					dataVals.add(val);
					
					
				} catch (NumberFormatException e)
				{
					throw new InvalidDendroFileException(
							I18n.getText("excelmatrix.invalidDataValue"), 
							getColRef(i)+String.valueOf(j+1), 
							PointerType.CELL);
				}
			}
			
			edc.label = datacol[0].getContents();
			edc.defaults = defaults;
			edc.dataVals = dataVals;
			series.add(edc);
			
			
		}
		


	}
	
	private SafeIntYear getYearForRow(int row)
	{
		try{
			Cell cell = sheet.getCell(0, row);
			return new SafeIntYear(cell.getContents());
		} catch (NumberFormatException e)
		{
			return null;
		}
	}
	
	/**
	 * Get the Excel column reference for a column number 
	 * 
	 * @param col <= 676
	 * @return
	 */
	private String getColRef(int col)
	{
		String colcodes = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
				
		if(col<0) return null;
		
		if(col > 676)
		{
			log.error("getColRef called with number out of range");
			return "??";
		}
		else if (col < 26)
		{
			return String.valueOf(colcodes.charAt(col));
		}
		else
		{
			int quotient = col / 26;
			int remainder = col % 26;
			return String.valueOf(colcodes.charAt(quotient-1)) + String.valueOf(colcodes.charAt(remainder));
		}		
	}
	
	@Override
	protected void resetReader() {
		sheet = null;
		defaults = null;
	}
	
	@Override
	public int getCurrentLineNumber() {
		return 0;
	}

	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}

	private TridasProject getProject() {
		TridasProject project = defaults.getProjectWithDefaults();
		
		for (ExcelDendroSeries eds : series)
		{
			TridasObject o = eds.defaults.getObjectWithDefaults(true);
			TridasMeasurementSeries ms = o.getElements().get(0).getSamples().get(0).getRadiuses().get(0).getMeasurementSeries().get(0);
			
			ms.setTitle(eds.label);
			
			TridasInterpretation interp = new TridasInterpretation();
			interp.setFirstYear(eds.startYear.toTridasYear(DatingSuffix.AD));
			ms.setInterpretation(interp);
			
			ArrayList<TridasValue> valuesList = new ArrayList<TridasValue>();
			for(Double dbl : eds.dataVals)
			{
				TridasValue val = new TridasValue();
				if(StringUtils.isStringWholeInteger(dbl.toString()))
				{
					Integer intval = dbl.intValue();
					val.setValue(intval.toString());
				}
				else
				{
					val.setValue(dbl.toString());
				}
				
				valuesList.add(val);
			}
			
			TridasValues valuesGroup = new TridasValues();
			TridasVariable variable = new TridasVariable();
			variable.setNormalTridas(NormalTridasVariable.RING_WIDTH);
			TridasUnit units = new TridasUnit();
			units.setValue(I18n.getText("unknown"));
			
			valuesGroup.setVariable(variable);
			valuesGroup.setUnit(units);
			valuesGroup.setValues(valuesList);

			ms.getValues().add(valuesGroup);
			
			project.getObjects().add(o);
		}
		
		
		
		return project;
	}

	private static class ExcelDendroSeries {
		public ExcelMatrixToTridasDefaults defaults;
		public SafeIntYear startYear;
		public String label;
		public ArrayList<Double> dataVals = new ArrayList<Double>();
		
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getProjects()
	 */
	@Override
	public TridasProject[] getProjects() {
		TridasProject projects[] = new TridasProject[1];
		projects[0] = this.getProject();
		return projects;
	}

	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getTridasContainer()
	 */
	public TridasTridas getTridasContainer() {
		TridasTridas container = new TridasTridas();
		List<TridasProject> list = Arrays.asList(getProjects());
		container.setProjects(list);
		return container;
	}

}

