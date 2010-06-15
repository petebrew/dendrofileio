package org.tridas.io.formats.tucson;

import java.util.List;

import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.I18n;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.naming.HierarchicalNamingConvention;
import org.tridas.io.naming.INamingConvention;
import org.tridas.io.util.TridasHierarchyHelper;
import org.tridas.io.util.UnitUtils;
import org.tridas.io.warningsandexceptions.IncompleteTridasDataException;
import org.tridas.io.warningsandexceptions.UnrepresentableTridasDataException;
import org.tridas.schema.NormalTridasUnit;
import org.tridas.schema.NormalTridasVariable;
import org.tridas.schema.TridasDerivedSeries;
import org.tridas.schema.TridasElement;
import org.tridas.schema.TridasMeasurementSeries;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasRadius;
import org.tridas.schema.TridasSample;
import org.tridas.schema.TridasUnit;
import org.tridas.schema.TridasValue;
import org.tridas.schema.TridasValues;
import org.tridas.schema.TridasVariable;

/**
 * Writer for the Tucson file format.
 * 
 * @see org.tridas.io.formats.tucson
 * @author peterbrewer
 */
public class TucsonWriter extends AbstractDendroCollectionWriter {
	
	TridasToTucsonDefaults defaults;
	INamingConvention naming = new HierarchicalNamingConvention();
	
	/**
	 * Standard constructor
	 */
	public TucsonWriter() {
		super(TridasToTucsonDefaults.class);
	}
	

	@Override
	public void parseTridasProject(TridasProject p, IMetadataFieldSet argDefaults) 
	throws IncompleteTridasDataException, UnrepresentableTridasDataException {
	
		// Base defaults for all the output files
		defaults = (TridasToTucsonDefaults) argDefaults;
		
		// Set project level fields
		defaults.populateFromTridasProject(p);
				
		// Extract any TridasDerivedSeries from project
		List<TridasDerivedSeries> dsList = null;
		try { dsList = p.getDerivedSeries();
		} catch (NullPointerException e) {}
				
		if (dsList.size()>0) 
		{
			/**
			 * CHRONOLOGY FILE
			 *
			 * There is a derived series in this project so we will be creating a .crn file.
			 * 
			 */
		
			for (TridasDerivedSeries ds : dsList) 
			{
				TridasValues tvs = ds.getValues().get(0);
				NormalTridasUnit inputunit = null;			
				if(tvs.getVariable().getNormalTridas()!=null)
				{
					switch (ds.getValues().get(0).getVariable().getNormalTridas())
					{
					case RING_WIDTH:
					case LATEWOOD_WIDTH:
					case EARLYWOOD_WIDTH:
						if(ds.getValues().get(0).getUnit().getNormalTridas()!=null)
						{
							inputunit =  ds.getValues().get(0).getUnit().getNormalTridas();
						}
						break;
					default: 
						break;
						
					}
				}
				
				ds.getValues().set(0, UnitUtils.convertTridasValues(getOutputUnits(tvs), ds.getValues().get(0), true));
				
				TucsonFile file = new TucsonFile(defaults);
				file.addSeries(ds);
				naming.registerFile(file, p, ds);
				addToFileList(file);
			}
			
		}
		
		if (TridasHierarchyHelper.getMeasurementSeriesFromTridasProject(p).size()>0)
		{
			/**
			 * RWL FILE(S)
			 * 
			 * The project contains one or more measurement series so we will save these
			 * to one or more RWL files.  RWL files should contain only one batch of metadata, 
			 * so we create a new file for each object.
			 */
			
			for (TridasObject o : p.getObjects()) {
				
				// Clone defaults and set fields specific to this object
				TridasToTucsonDefaults objectDefaults = (TridasToTucsonDefaults) defaults.clone();
				objectDefaults.populateFromTridasObject(o);
				
				for (TridasElement e : TridasHierarchyHelper.getElementList(o)) {
					TridasToTucsonDefaults elementDefaults = (TridasToTucsonDefaults) objectDefaults.clone();
					elementDefaults.populateFromTridasElement(e);
					
					for (TridasSample s : e.getSamples()) {
						
						for (TridasRadius r : s.getRadiuses()) {
							
							for (TridasMeasurementSeries ms : r.getMeasurementSeries()) {
								TridasToTucsonDefaults msDefaults = (TridasToTucsonDefaults) elementDefaults
										.clone();
								msDefaults.populateFromTridasMeasurementSeries(ms);
								
								for (int i = 0; i < ms.getValues().size(); i++) {
									TridasValues tvs = ms.getValues().get(i);
	
									ms.getValues().set(i, UnitUtils.convertTridasValues(getOutputUnits(tvs), ms.getValues().get(i), true));
																		
									TridasToTucsonDefaults tvDefaults = (TridasToTucsonDefaults) msDefaults.clone();
									tvDefaults.populateFromTridasValues(tvs);
									
									TucsonFile file = new TucsonFile(tvDefaults);
									file.addSeries(ms);
									naming.registerFile(file, p, o, e, s, r, ms);
									addToFileList(file);
								}
							}
						}
					}
				}
			}
		}
	}
	

	private NormalTridasUnit getOutputUnits(TridasValues tvs)
	{
		NormalTridasUnit inputunit = null;			
		if(tvs.getVariable().getNormalTridas()!=null)
		{
			switch (tvs.getVariable().getNormalTridas())
			{
			case RING_WIDTH:
			case LATEWOOD_WIDTH:
			case EARLYWOOD_WIDTH:
				if(tvs.getUnit().getNormalTridas()!=null)
				{
					if (tvs.getUnit().getNormalTridas().equals(NormalTridasUnit.HUNDREDTH_MM))
					{
						return NormalTridasUnit.HUNDREDTH_MM;
					}
					else
					{
						return NormalTridasUnit.MICROMETRES;
					}
				}
				break;
			default: 
				break;
				
			}
		}
		return null;
		
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
	 * @see org.tridas.io.IDendroFileReader#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getDescription()
	 */
	@Override
	public String getDescription() {
		return I18n.getText("tucson.about.description");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getFullName()
	 */
	@Override
	public String getFullName() {
		return I18n.getText("tucson.about.fullName");
	}
	
	/**
	 * @see org.tridas.io.IDendroFileReader#getShortName()
	 */
	@Override
	public String getShortName() {
		return I18n.getText("tucson.about.shortName");
	}
}
