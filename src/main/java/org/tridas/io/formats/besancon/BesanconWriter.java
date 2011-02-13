/*******************************************************************************
 * Copyright 2010 Peter Brewer and Daniel Murphy
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
package org.tridas.io.formats.besancon;

import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.IncompleteTridasDataException;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.naming.NumericalNamingConvention;
import org.tridas.io.util.TridasUtils;
import org.tridas.io.util.UnitUtils;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasTridas;
import org.tridas.schema.TridasValues;

public class BesanconWriter extends AbstractDendroCollectionWriter {

	private TridasToBesanconDefaults defaults;
	private INamingConvention naming = new NumericalNamingConvention();
	
	public BesanconWriter() {
		super(TridasToBesanconDefaults.class);
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
	public void setNamingConvention(INamingConvention argConvention) {
		naming = argConvention;
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getDefaults()
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
		return I18n.getText("besancon.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("besancon.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroCollectionWriter#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("besancon.about.shortName");
	}

	@Override
	protected void parseTridasProject(TridasProject argProject,
			IMetadataFieldSet argDefaults)
			throws IncompleteTridasDataException {
		defaults = (TridasToBesanconDefaults) argDefaults;
		defaults.populateFromTridasProject(argProject);
		
		for (TridasObject o : TridasUtils.getObjectList(argProject)) {
			TridasToBesanconDefaults objectDefaults = (TridasToBesanconDefaults) defaults.clone();
			objectDefaults.populateFromTridasObject(o);
			
			for (TridasElement e : o.getElements()) {
				TridasToBesanconDefaults elementDefaults = (TridasToBesanconDefaults) objectDefaults.clone();
				elementDefaults.populateFromTridasElement(e);
				
				for (TridasSample s : e.getSamples()) {
					TridasToBesanconDefaults sampleDefaults = (TridasToBesanconDefaults) elementDefaults.clone();
					sampleDefaults.populateFromTridasSample(s);
					
					for (TridasRadius r : s.getRadiuses()) {
						TridasToBesanconDefaults radiusDefaults = (TridasToBesanconDefaults) sampleDefaults.clone();
						radiusDefaults.populateFromTridasRadius(r);
												
						for (TridasMeasurementSeries ms : r.getMeasurementSeries()) {
							TridasToBesanconDefaults msDefaults = (TridasToBesanconDefaults) radiusDefaults
									.clone();
							msDefaults.populateFromWoodCompleteness(ms, r);
							
							for (int i = 0; i < ms.getValues().size(); i++) {
								boolean skipThisGroup = false;

								TridasValues tvsgroup = ms.getValues().get(i);
								
								// Check we can handle this variable
								if(tvsgroup.isSetVariable())
								{
									if (!tvsgroup.getVariable().isSetNormalTridas())
									{
										this.addWarning(new ConversionWarning(WarningType.AMBIGUOUS, I18n.getText("fileio.nonstandardVariable")));
									}
									else
									{
										switch(tvsgroup.getVariable().getNormalTridas())
										{
										case RING_WIDTH:
											// Able to handle this variable but need to convert units
											try {
												tvsgroup = UnitUtils.convertTridasValues(NormalTridasUnit.HUNDREDTH_MM, tvsgroup, true);
											} catch (NumberFormatException e1) {

											} catch (ConversionWarningException e1) {
												addWarning(e1.getWarning());
											}											
											
											break;
										default:
											// All other variables not representable
											this.addWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText("fileio.unsupportedVariable", tvsgroup.getVariable().getNormalTridas().value())));
											skipThisGroup = true;
										}
									}
								}
								
								// Dodgy variable so skip
								if(skipThisGroup) continue;
								
								TridasToBesanconDefaults tvDefaults = (TridasToBesanconDefaults) msDefaults.clone();
								tvDefaults.populateFromTridasValuesAndSeries(tvsgroup, ms);
	
								BesanconFile file = new BesanconFile();
								
								// Add series to file	
								file.addSeries(ms, tvDefaults);
								
								// Set naming convention
								naming.registerFile(file, argProject, o, e, s, r, ms);
								
								// Add file to list
								addToFileList(file);
							}
							
						}
					}
				}
			}
		}
		
		for (TridasDerivedSeries ds : argProject.getDerivedSeries()) {
			TridasToBesanconDefaults dsDefaults = (TridasToBesanconDefaults) defaults.clone();
						
			for (int i = 0; i < ds.getValues().size(); i++) {
				boolean skipThisGroup = false;

				TridasValues tvsgroup = ds.getValues().get(i);
				
				// Check we can handle this variable
				if(tvsgroup.isSetVariable())
				{
					if (!tvsgroup.getVariable().isSetNormalTridas())
					{
						this.addWarning(new ConversionWarning(WarningType.AMBIGUOUS, I18n.getText("fileio.nonstandardVariable")));
					}
					else
					{
						switch(tvsgroup.getVariable().getNormalTridas())
						{
						case RING_WIDTH:
							// Able to handle this variable but need to convert units
							try {
								tvsgroup = UnitUtils.convertTridasValues(NormalTridasUnit.HUNDREDTH_MM, tvsgroup, true);
							} catch (NumberFormatException e) {
							} catch (ConversionWarningException e) {
								this.addWarning(e.getWarning());
							}											
							
							break;
						default:
							// All other variables not representable
							this.addWarning(new ConversionWarning(WarningType.IGNORED, I18n.getText("fileio.unsupportedVariable", tvsgroup.getVariable().getNormalTridas().value())));
							skipThisGroup = true;
						}
					}
				}
				
				// Dodgy variable so skip
				if(skipThisGroup) continue;
				
				TridasToBesanconDefaults tvDefaults = (TridasToBesanconDefaults) dsDefaults.clone();
				tvDefaults.populateFromTridasValuesAndSeries(tvsgroup, ds);
				
				BesanconFile file = new BesanconFile();
				
				// Add series to file	
				file.addSeries(ds, tvDefaults);
								
				// Set naming convention
				naming.registerFile(file, argProject, ds);
				
				// Add file to list
				addToFileList(file);
			}
			
		}
		
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
