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
package org.tridas.io.formats.belfastapple;

import java.util.ArrayList;
import java.util.List;

import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.ImpossibleConversionException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.naming.NumericalNamingConvention;
import org.tridas.io.util.TridasUtils;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasIdentifier;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValues;

public class BelfastAppleWriter extends AbstractDendroCollectionWriter {
	
	IMetadataFieldSet defaults;
	INamingConvention naming = new NumericalNamingConvention();
	
	public BelfastAppleWriter() {
		super(TridasToBelfastAppleDefaults.class, new BelfastAppleFormat());
	}
		
	@Override
	protected void parseTridasProject(TridasProject argProject, IMetadataFieldSet argDefaults)
			throws ImpossibleConversionException, ConversionWarningException {
		defaults = argDefaults;
		String objecttitle = "";
		String sampletitle = "";
		
		// Grab all derivedSeries from project
		try {
			List<TridasDerivedSeries> lst = argProject.getDerivedSeries();
			for (TridasDerivedSeries ds : lst) {
				// Create a belfastappleFile for each and add to file list
				BelfastAppleFile file = new BelfastAppleFile(defaults, this);
				naming.registerFile(file, argProject, ds);
				
				
				for(TridasValues group : ds.getValues())
				{
					if(!group.isSetValues())
					{
						this.addWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText("fileio.noDataValues")));
						continue;
					}
					
					try{
					file.setValuesGroup(group);
					} catch (ConversionWarningException e)
					{
						this.addWarning(e.getWarning());
						continue;
					}
					
					// Try and grab object and sample titles from linked series
					if(ds.isSetLinkSeries())
					{
						// TODO what happens if there are links to multiple different entities?
						// For now just go with the first link
						//if(ds.getLinkSeries().getSeries().size()>1) break;
						TridasIdentifier id = ds.getLinkSeries().getSeries().get(0).getIdentifier();
						TridasObject parentObject = (TridasObject) TridasUtils.getEntityByIdentifier(argProject, id, TridasObject.class);
						if(parentObject!=null)
						{
							if(parentObject.isSetTitle())
							{
								file.setObjectTitle(parentObject.getTitle());
							}
						}
						TridasSample parentSample = (TridasSample) TridasUtils.getEntityByIdentifier(argProject, id, TridasSample.class);
						if(parentSample!=null)
						{
							if(parentSample.isSetTitle())
							{
								file.setSampleTitle(parentSample.getTitle());
							}
						}
					}
	
					addToFileList(file);
				}
			}
		} catch (NullPointerException e) {
			System.out.println("Null pointer exception");
		}
		
		// Loop through Objects
		List<TridasObject> obList;
		try {
			obList = argProject.getObjects();
		} catch (NullPointerException e) {
			throw new ImpossibleConversionException(I18n.getText("fileio.objectMissing"));
		}
		for (TridasObject obj : obList) {
			if (obj.getTitle() != null) {
				objecttitle = obj.getTitle();
			}
			
			// Loop through Elements
			ArrayList<TridasElement> elList;
			try {
				elList = TridasUtils.getElementList(obj);
			} catch (NullPointerException e) {
				continue;
				//throw new IncompleteTridasDataException(I18n.getText("fileio.elementMissing"));
			}
			
			for (TridasElement el : elList) {
				// Loop through Samples
				List<TridasSample> sList;
				try {
					sList = el.getSamples();
				} catch (NullPointerException e) {
					continue;
					//throw new IncompleteTridasDataException(I18n.getText("fileio.sampleMissing"));
				}
				
				for (TridasSample s : sList) {
					if (s.getTitle() != null) {
						sampletitle = s.getTitle();
					}
										
					// Loop through radii
					List<TridasRadius> rList;
					try {
						rList = s.getRadiuses();
					} catch (NullPointerException e) {
						continue;
						//throw new IncompleteTridasDataException(I18n.getText("fileio.radiusMissing"));
					}
					
					for (TridasRadius r : rList) {
						// Loop through series
						List<TridasMeasurementSeries> serList = null;
						try {
							serList = r.getMeasurementSeries();
						} catch (NullPointerException e) {
							continue;
						}
						
						if (serList != null) {
							for (TridasMeasurementSeries ser : serList) {								
								// Create a belfastappleFile for each values group and add to file list
								
								for(TridasValues group : ser.getValues())
								{
									if(!group.isSetValues())
									{
										this.addWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText("fileio.noDataValues")));
										continue;
									}
								
									BelfastAppleFile file = new BelfastAppleFile(defaults, this);
									naming.registerFile(file, argProject, obj, el, s, r, ser);
									try{
										file.setValuesGroup(group);
									} catch (ConversionWarningException e)
									{
										this.addWarning(e.getWarning());
										continue;
									}
									file.setObjectTitle(objecttitle);
									file.setSampleTitle(sampletitle);
									addToFileList(file);
								}
							}
						}
					}
				}
			}
		}
		
		if(this.getFiles().length==0)
		{
			this.clearWarnings();
			throw new ImpossibleConversionException("File conversion failed.  This output format is unable to represent the data stored in the input file.");
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


	
}
