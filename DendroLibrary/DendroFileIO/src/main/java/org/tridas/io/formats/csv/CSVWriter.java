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
package org.tridas.io.formats.csv;

import java.util.ArrayList;
import java.util.List;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.IncompleteTridasDataException;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.naming.UUIDNamingConvention;
import org.tridas.io.util.TridasUtils;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasTridas;

/**
 * Writer for Comma Separated Values spreadsheet format.
 * 
 * @author peterbrewer
 */
public class CSVWriter extends AbstractDendroCollectionWriter {
	
	IMetadataFieldSet defaults;
	INamingConvention naming = new UUIDNamingConvention();
	
	public CSVWriter() {
		super(TridasToCSVDefaults.class);
	}
	
	@Override
	protected void parseTridasProject(TridasProject argProject, IMetadataFieldSet argDefaults)
			throws IncompleteTridasDataException, ConversionWarningException {
		defaults = argDefaults;
		
		ArrayList<ITridasSeries> seriesList = new ArrayList<ITridasSeries>();
		
		// Grab all derivedSeries from project
		try {
			List<TridasDerivedSeries> lst = argProject.getDerivedSeries();
			for (TridasDerivedSeries ds : lst) {
				// Create a TrimsFile for each and add to file list
				CSVFile file = new CSVFile(defaults);
				naming.registerFile(file, argProject, ds);
				file.setSeries(ds);
				addToFileList(file);
			}
		} catch (NullPointerException e) {}
		
		// Loop through Objects
		List<TridasObject> obList;
		try {
			obList = argProject.getObjects();
		} catch (NullPointerException e) {
			throw new IncompleteTridasDataException(I18n.getText("fileio.objectMissing"));
		}
		for (TridasObject obj : obList) {
			
			// Loop through Elements
			ArrayList<TridasElement> elList;
			try {
				elList = TridasUtils.getElementList(obj);
			} catch (NullPointerException e) {
				throw new IncompleteTridasDataException(I18n.getText("fileio.elementMissing"));
			}
			
			for (TridasElement el : elList) {
				// Loop through Samples
				List<TridasSample> sList;
				try {
					sList = el.getSamples();
				} catch (NullPointerException e) {
					throw new IncompleteTridasDataException(I18n.getText("fileio.sampleMissing"));
				}
				
				for (TridasSample s : sList) {
					// Check this isn't a placeholder
					/*TridasRadiusPlaceholder rph = null;
					try {
						rph = s.getRadiusPlaceholder();
					} catch (NullPointerException e) {}
					
					if (rph != null) {
						continue;
					}*/
					
					// Loop through radii
					List<TridasRadius> rList;
					try {
						rList = s.getRadiuses();
					} catch (NullPointerException e) {
						throw new IncompleteTridasDataException(I18n.getText("fileio.radiusMissing"));
					}
					
					for (TridasRadius r : rList) {
						// Loop through series
						List<TridasMeasurementSeries> serList = null;
						try {
							serList = r.getMeasurementSeries();
						} catch (NullPointerException e) {}
						
						if (serList != null) {
							for (TridasMeasurementSeries ser : serList) {
								// Create a file for each and add to file list
								CSVFile file = new CSVFile(defaults);
								naming.registerFile(file, argProject, obj, el, s, r, ser);
								file.setSeries(ser);
								addToFileList(file);
							}
						}
					}
				}
			}
		}
		
		// No series found
		if (getFileList().size() == 0) {
			throw new IncompleteTridasDataException(I18n.getText("fileio.noData"));
		}
		
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("csv.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("csv.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("csv.about.shortName");
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getNamingConvention()
	 */
	@Override
	public INamingConvention getNamingConvention() {
		return naming;
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#setNamingConvention(org.tridas.io.naming.INamingConvention)
	 */
	@Override
	public void setNamingConvention(INamingConvention argConvension) {
		naming = argConvension;
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#parseTridasContainer()
	 */
	@Override
	protected void parseTridasContainer(TridasTridas argContainer,
			IMetadataFieldSet argDefaults)
			throws IncompleteTridasDataException, ConversionWarningException {
	
		for(TridasProject project : argContainer.getProjects())
		{
			parseTridasProject(project, argDefaults);
		}	
	}
	
}
