package org.tridas.io;

import java.util.ArrayList;

import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.warnings.ConversionWarningException;

/**
 * Interface for all Dendro File Formats.
 * 
 * @author peterbrewer
 *
 */
public abstract class DendroFile {
	
	private String extension;
	private ArrayList<ITridasSeries> seriesList = new ArrayList<ITridasSeries>();
	
	public DendroFile(){
		
	}
	
	public abstract String[] saveToString();
		
	/**
	 * Add a series to this file.  For file formats that only support
	 * a single file this should return an error.
	 * 
	 * @param series
	 * @throws ConversionWarningException 
	 */
	public abstract void addSeries(ITridasSeries series) throws ConversionWarningException;
	
	/**
	 * Set the series for this file.
	 * 
	 * @param series
	 */
	public abstract void setSeries(ITridasSeries series) throws ConversionWarningException;
	
	/**
	 * Get the series list.
	 * @return
	 */
	protected ArrayList<ITridasSeries> getSeriesList(){
		return seriesList;
	}
	
	/**
	 * Remove all series from file
	 */
	public void clearSeries(){
		seriesList.clear();
	}
	
	public void setExtension(String ext){
		// Strip leading dot if present
		if(ext.substring(0,1).equals("."))
			extension = ext.substring(1, ext.length());
		else
			extension = ext;
	}
	
	public String getExtension(){
		return extension;
	}
}