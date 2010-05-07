/**
 * Created on Apr 22, 2010, 2:37:20 AM
 */
package org.tridas.io.naming;

import org.tridas.io.DendroFile;
import org.tridas.io.I18n;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;

/**
 * @author daniel
 *
 */
public class NumericalNamingConvention extends AbstractNamingConvention {

	private String baseFilename;
	
	public NumericalNamingConvention(){
		setBaseFilename("");
	}
	
	public NumericalNamingConvention(String argBaseName){
		setBaseFilename(argBaseName);
	}
	
	
	/**
	 * The base filename for all files
	 * @param baseFilename the base filename to set
	 */
	public void setBaseFilename(String baseFilename) {
		this.baseFilename = baseFilename;
	}

	/**
	 * The base filename for all files.
	 * @return the base filename
	 */
	public String getBaseFilename() {
		return baseFilename;
	}

	/**
	 * @see org.tridas.io.naming.AbstractNamingConvention#getDendroFilename(org.tridas.io.DendroFile, org.tridas.schema.TridasProject, org.tridas.schema.TridasObject, org.tridas.schema.TridasElement, org.tridas.schema.TridasSample, org.tridas.schema.TridasRadius, org.tridas.schema.TridasMeasurementSeries)
	 */
	@Override
	protected String getDendroFilename(DendroFile argFile, TridasProject argProject,
			TridasObject argObject, TridasElement argElement,
			TridasSample argSample, TridasRadius argRadius,
			TridasMeasurementSeries argSeries) {
		// since AbstractNamingConvension handles numerically naming
		// files with the same filename, we'll just return the same
		// filename every time
		return baseFilename;
	}

	
	@Override
	public String getDescription() {
		return I18n.getText("namingconvention.numerical.description");
	}
	
	@Override
	public String getName(){
		return I18n.getText("namingconvention.numerical");
	}
}