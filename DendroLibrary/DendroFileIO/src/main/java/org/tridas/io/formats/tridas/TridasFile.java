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
package org.tridas.io.formats.tridas;

import java.io.StringWriter;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.interfaces.ITridasSeries;
import org.tridas.io.I18n;
import org.tridas.io.IDendroFile;
import org.tridas.io.TridasNamespacePrefixMapper;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.util.IOUtils;
import org.tridas.schema.TridasProject;
import org.xml.sax.SAXException;

/**
 * The TRiDaS file format is our standard format <h3>Reference</h3>
 * <p>
 * See the <a href="http://www.tridas.org">Tree Ring Data Standard</a> website for futher
 * information.
 * </p>
 * 
 * @author peterbrewer
 */
public class TridasFile implements IDendroFile {
	
	private final static Logger log = LoggerFactory.getLogger(TridasFile.class);
	
	TridasProject project;
	
	private IMetadataFieldSet defaults;
	
	public TridasFile(IMetadataFieldSet argDefaults) {
		defaults = argDefaults;
	}
	
	public void setProject(TridasProject p) {
		project = p;
	}
	
	@Override
	public String[] saveToString() {
		if (project == null) {
			return null;
		}
		Schema schema = null;
		
		// Validate output against schema first
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		URL file = IOUtils.getFileInJarURL("schemas/tridas.xsd");
		if(file == null){
			log.error("Could not find schema file");
		}else{
			try {
				schema = factory.newSchema(file);
			} catch (SAXException e) {
				log.error("Error getting TRiDaS schema for validation, not using.", e);
				defaults.addConversionWarning(new ConversionWarning(WarningType.DEFAULT,
						I18n.getText("fileio.errorGettingSchema")));
			}
		}
		
		StringWriter swriter = new StringWriter();
		// Marshaller code goes here...
		JAXBContext jc;
		try {
			jc = JAXBContext.newInstance("org.tridas.schema");
			Marshaller m = jc.createMarshaller();
			m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new TridasNamespacePrefixMapper());
			if (schema != null) {
				m.setSchema(schema);
			}
			m.marshal(project, swriter);

		} catch (Exception e) {
			log.error("Jaxb error", e);
			defaults.addConversionWarning(new ConversionWarning(WarningType.FILE_IGNORED, I18n.getText("fileio.jaxbError")));
			return null;
		}
		
		return swriter.getBuffer().toString().split("\n");
	}
	
	/**
	 * @see org.tridas.io.IDendroFile#getExtension()
	 */
	@Override
	public String getExtension() {
		return "xml";
	}
	
	/**
	 * @see org.tridas.io.IDendroFile#getSeries()
	 */
	@Override
	public ITridasSeries[] getSeries() {
		return null;
	}
	
	/**
	 * @see org.tridas.io.IDendroFile#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}
}
