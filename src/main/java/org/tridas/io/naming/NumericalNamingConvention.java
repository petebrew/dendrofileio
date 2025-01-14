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
package org.tridas.io.naming;

import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.io.util.FileHelper;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;

/**
 * @author daniel
 */
public class NumericalNamingConvention extends AbstractNamingConvention {
	
	private String baseFilename;
	
	public NumericalNamingConvention() {
		setBaseFilename(null);
	}
	
	public NumericalNamingConvention(String argBaseName) {
		setBaseFilename(argBaseName);
	}
	
	/**
	 * The base filename for all files
	 * 
	 * @param baseFilename
	 *            the base filename to set
	 */
	public void setBaseFilename(String baseFilename) {
		
		this.baseFilename = FileHelper.sanitiseFilename(baseFilename);
	}
	
	/**
	 * The base filename for all files.
	 * 
	 * @return the base filename
	 */
	public String getBaseFilename() {
		return baseFilename;
	}
	
	/**
	 * @see org.tridas.io.naming.AbstractNamingConvention#getDendroFilename(org.tridas.io.IDendroFile,
	 *      org.tridas.schema.TridasProject, org.tridas.schema.TridasObject,
	 *      org.tridas.schema.TridasElement, org.tridas.schema.TridasSample,
	 *      org.tridas.schema.TridasRadius, org.tridas.schema.TridasMeasurementSeries)
	 */
	@Override
	protected String getDendroFilename(IDendroFile argFile, TridasProject argProject, TridasObject argObject,
			TridasElement argElement, TridasSample argSample, TridasRadius argRadius, TridasMeasurementSeries argSeries) {
		// since AbstractNamingConvension handles numerically naming
		// files with the same filename, we'll just return the same
		// filename every time
		if (baseFilename != null) {
			return baseFilename;
		}
		else if(argFile!=null) {
			return argFile.getClass().getSimpleName();
		}
		else
		{
			return "TRiCYCLE";
		}
	}
	
	@Override
	protected String getDendroFilename(IDendroFile argFile, TridasProject argProject, TridasDerivedSeries argSeries) {
		// since AbstractNamingConvension handles numerically naming
		// files with the same filename, we'll just return the same
		// filename every time
		if (baseFilename != null) {
			return baseFilename;
		}
		else if(argFile!=null) {
			return argFile.getClass().getSimpleName();
		}
		else
		{
			return "TRiCYCLE";
		}
	}
	
	@Override
	public String getDescription() {
		return I18n.getText("namingconvention.numerical.description");
	}
	
	@Override
	public String getName() {
		return I18n.getText("namingconvention.numerical");
	}
	
}
