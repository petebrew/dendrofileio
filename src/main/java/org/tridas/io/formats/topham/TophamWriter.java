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
package org.tridas.io.formats.topham;

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
import org.tridas.io.util.UnitUtils;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;

public class TophamWriter extends AbstractDendroCollectionWriter {

	private TridasToTophamDefaults defaults;
	private INamingConvention naming = new NumericalNamingConvention();
	
	public TophamWriter() {
		super(TridasToTophamDefaults.class, new TophamFormat());
	}
	
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
	

	@Override
	protected void parseTridasProject(TridasProject argProject,
			IMetadataFieldSet argDefaults)
			throws ImpossibleConversionException {
		
		defaults = (TridasToTophamDefaults) argDefaults;
		
		for (TridasObject o : TridasUtils.getObjectList(argProject)) {			
			for (TridasElement e : o.getElements()) {			
				for (TridasSample s : e.getSamples()) {					
					for (TridasRadius r : s.getRadiuses()) {											
						for (TridasMeasurementSeries ms : r.getMeasurementSeries()) {					
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
										case EARLYWOOD_WIDTH:
										case LATEWOOD_WIDTH:
											// All handled ok
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
								
								// Check there are all double values
								for (TridasValue v : tvsgroup.getValues()) {
									try {
										Double.parseDouble(v.getValue());
									} catch (NumberFormatException e2) {
										throw new ImpossibleConversionException(I18n.getText("general.ringValuesNotNumbers"));
									}
								}
																
								TophamFile file = new TophamFile(defaults);
								
							
								// Convert units and add data to file
								try {
									file.setDataValues(UnitUtils.convertTridasValues(NormalTridasUnit.MILLIMETRES, tvsgroup, false));
								} catch (NumberFormatException e1) {

								} catch (ConversionWarningException e1) {
									this.addWarning(e1.getWarning());
								}
								
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
						case EARLYWOOD_WIDTH:
						case LATEWOOD_WIDTH:
							// All handled ok
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
				
				// Check there are no non-number values
				for (TridasValue v : tvsgroup.getValues()) {
					try {
						Double.parseDouble(v.getValue());
					} catch (NumberFormatException e2) {
						throw new ImpossibleConversionException(
								"One or more data values are not numbers!  This is technically acceptable in TRiDaS but not supported in this library.");
					}
				}
								
				TophamFile file = new TophamFile(defaults);

				
				// Convert units and add data to file
				try {
					file.setDataValues(UnitUtils.convertTridasValues(NormalTridasUnit.MILLIMETRES, tvsgroup, false));
				} catch (NumberFormatException e) {
				} catch (ConversionWarningException e) {
					this.addWarning(e.getWarning());
				}
				
				// Set naming convention
				naming.registerFile(file, argProject, ds);
				
				// Add file to list
				addToFileList(file);
			}

			
			
		}
		

		if(this.getFiles().length==0)
		{
			this.clearWarnings();
			throw new ImpossibleConversionException("File conversion failed.  This output format is unable to represent the data stored in the input file.");
		}

	}

	/**
	 * @see org.tridas.io.IDendroCollectionWriter#setNamingConvention(org.tridas.io.naming.INamingConvention)
	 */
	@Override
	public void setNamingConvention(INamingConvention argConvension) {
		naming = argConvension;
	}
}
