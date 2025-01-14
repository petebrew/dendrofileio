package org.tridas.io.formats.cracow;

import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.ImpossibleConversionException;
import org.tridas.io.naming.INamingConvention;
import org.tridas.schema.TridasProject;

public class CracowWriter extends AbstractDendroCollectionWriter {

	public CracowWriter() {
		super(TridasToCracowDefaults.class, new CracowFormat());
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void parseTridasProject(TridasProject argProject,
			IMetadataFieldSet argDefaults)
			throws ImpossibleConversionException, ConversionWarningException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setNamingConvention(INamingConvention argConvension) {
		// TODO Auto-generated method stub

	}

	@Override
	public INamingConvention getNamingConvention() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getShortName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMetadataFieldSet getDefaults() {
		// TODO Auto-generated method stub
		return null;
	}

}
