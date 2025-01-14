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

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.opengis.gml.schema.PointType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.I18n;
import org.tridas.io.TridasIO;
import org.tridas.io.TridasNamespacePrefixMapper;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.defaults.TridasMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarning.WarningType;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.transform.TridasVersionTransformer;
import org.tridas.io.transform.TridasVersionTransformer.TridasVersion;
import org.tridas.io.util.IOUtils;
import org.tridas.io.util.TridasUtils;
import org.tridas.schema.TridasObject;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasTridas;
import org.tridas.spatial.GMLPointSRSHandler;
import org.tridas.spatial.SpatialUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jhlabs.map.proj.ProjectionException;

/**
 * Reader for the TRiDaS file format. This is little more than a
 * wrapper around the JaXB unmarshaller
 * 
 * @see org.tridas.io.formats.tridas
 * @author peterbrewer
 */
public class TridasReader extends AbstractDendroFileReader {
	
	private static final Logger log = LoggerFactory.getLogger(TridasReader.class);
	
	private List<TridasProject> projects = null;
	private TridasMetadataFieldSet defaults = null;
	
	public TridasReader() {
		super(TridasMetadataFieldSet.class, new TridasFormat());
	}
	
	/**
	 * Special case for TridasReader.  This works like loadFile() but instead of 
	 * passing it the raw file strings, you pass a TridasContainer instead
	 * 
	 * @param container
	 */
	public void loadTridasContainer(TridasTridas container)throws InvalidDendroFileException
	{
		if(container==null) throw new InvalidDendroFileException("Null TRiDaS container");
		if(!container.isSetProjects()) throw new InvalidDendroFileException("TRiDaS container has no projects");

		List<TridasProject> ps = container.getProjects();
			
		
		projects = ps;
	}
	
	@Override
	protected void parseFile(String[] argFileString, IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException {
		defaults = (TridasMetadataFieldSet) argDefaultFields;
		StringBuilder fileString = new StringBuilder();
		StringReader reader;
		
		// Detect what version of TRiDaS this XML file adheres to...
		TridasVersion xmlFileVersion = TridasVersionTransformer.getTridasVersionFromXMLStrings(argFileString);
		if(xmlFileVersion==null)
		{
			// version number parsing failed.  We'll try to continue anyway.  The schema validation will 
			// pick up if it's not a valid recent TRiDaS file
			//throw new InvalidDendroFileException(I18n.getText("tridas.schemaParseVersionError"));
			throw new InvalidDendroFileException("This is not a valid TRiDaS file.  The file is missing a namespace declaration, or the declaration is invalid");

		}
		else if(xmlFileVersion.equals(TridasVersion.V_FUTURE))
		{
			throw new InvalidDendroFileException(I18n.getText("tridas.versionTooNew"));
		}
		else
		{
			// Version parsed successfully so we can now transform to the version of TRiDaS that we're using internally.
			// If it's already that version then the file will be untouched.
			try {
				argFileString = TridasVersionTransformer.transformTridas(argFileString, TridasIO.tridasVersionUsedInternally);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	
		
		// Validate the file against the TRiDaS schema
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		URL file = IOUtils.getFileInJarURL("tridas.xsd");
		Schema schema = null;
		if(file==null)
		{
			log.error(I18n.getText("tridas.schemaMissing"));
			addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("tridas.schemaMissing")));
		}
		else
		{
			// Next try to load the schema to validate
			try {
				schema = factory.newSchema(file);
			} catch (Exception e) {
				// if we can't find the schema it's ok, doesn't mean it's not an invalid
				// dendro file
				log.error(I18n.getText("tridas.schemaMissing", e.getLocalizedMessage()));
				addWarning(new ConversionWarning(WarningType.INVALID, I18n.getText("tridas.schemaMissing", e
						.getLocalizedMessage())));
			}
			
			
			// Build the string array into a FileReader
			Boolean firstLine = true;
			for (String s : argFileString) {
				if(firstLine)
				{
					fileString.append(s.replaceFirst("^[^<]*", "")+"\n");
					firstLine = false;
				}
				else
				{
					fileString.append(s + "\n");
				}
			}
			reader = new StringReader(fileString.toString());
			
			// Do the validation
			Validator validator = schema.newValidator();
			StreamSource source = new StreamSource();
			source.setReader(reader);
			try {
				validator.validate(source);
			} catch (SAXException ex) 
			{
				throw new InvalidDendroFileException(I18n.getText("tridas.schemaException", ex.getLocalizedMessage()));
			} catch (IOException e) {
				throw new InvalidDendroFileException(I18n.getText("tridas.schemaIOError"));
			}
		}

		
		// All should be ok so now unmarshall to Java classes
		JAXBContext jc;
		reader = new StringReader(fileString.toString());
		try {
			jc = JAXBContext.newInstance("org.tridas.schema");
			Unmarshaller u = jc.createUnmarshaller();
			// Read the file into the project
			
			Object root = u.unmarshal(reader);
			
			if(root instanceof TridasProject)
			{
				projects = new ArrayList<TridasProject>();
				projects.add((TridasProject) root);
			}
			else if (root instanceof TridasTridas)
			{
				projects = ((TridasTridas) root).getProjects();
			}
			else
			{
				addWarning(new ConversionWarning(WarningType.DEFAULT, I18n.getText("fileio.loadfailed")));
			}
			
		} catch (JAXBException e2) {
			addWarning(new ConversionWarning(WarningType.DEFAULT, I18n.getText("fileio.loadfailed")));
		}
		
		// Now check to see if there are any coordinates in the file and
		// convert to WGS84 if possible
		for(TridasProject p : projects)
		{
			for(TridasObject o: TridasUtils.getObjectList(p))
			{
				if(o.isSetLocation())
				{
					if(o.getLocation().isSetLocationGeometry())
					{
						if(o.getLocation().getLocationGeometry().isSetPoint())
						{
							PointType point = o.getLocation().getLocationGeometry().getPoint();
							if(!point.isSetSrsName())
							{
								o.getLocation().getLocationGeometry().getPoint().setSrsName(SpatialUtils.WGS84);
								addWarning(new ConversionWarning(WarningType.ASSUMPTION, 
										I18n.getText("srsname.noneSpecifiedAssumingWGS84")));
								continue;
							}
							else
							{
								try{
									GMLPointSRSHandler tph = new GMLPointSRSHandler(point);
									o.getLocation().getLocationGeometry().setPoint(tph.getAsWGS84PointType());
									if(tph.hasSpecificProjection())
									{
										addWarning(new ConversionWarning(WarningType.INFORMATION, 
												I18n.getText("srsname.projectedToWGS84")));
									}
								}
								catch (ProjectionException ex)
								{
									o.getLocation().getLocationGeometry().getPoint().setSrsName(SpatialUtils.WGS84);
									addWarning(new ConversionWarning(WarningType.ASSUMPTION, 
											I18n.getText("srsname.notSupportedAssumingWGS84")));
								}
							}
						}
					}
				}				
			}
		}
		
		
	}
	
	/**
	 * 
	 * 
	 * @param v1
	 * @param v2
	 * @return
	 */
	@Deprecated
	private Integer compareTridasVersions(String v1, String v2)
	{
		if(v1==null || v2==null) return null;
		
		String ver1 = null;
		String ver2 = null;
		try{
			ver1 = v1.substring(v1.lastIndexOf("/")+1);
			ver2 = v2.substring(v2.lastIndexOf("/")+1);
		} catch (Exception e)
		{
			return null;
		}
		
		if(ver1==null || ver2==null || ver1.length()==0 || ver2.length()==0) return null;
		
		String[] version1 = ver1.split("\\.");
		String[] version2 = ver2.split("\\.");
		
		return compareStringArrayContents(version1, version2, 0);
	
	}
	
	private Integer compareStringArrayContents(String[] v1, String[] v2, int depth)
	{
		if(v1==null || v1.length==0 || v2==null || v2.length==0) return null;
		
		if(v1.length-1<depth || v2.length-1<depth) return 0;
		
		try
		{
			Integer ver1 = Integer.parseInt(v1[depth]);
			Integer ver2 = Integer.parseInt(v2[depth]);
			
			if(ver1.equals(ver2))
			{
				return compareStringArrayContents(v1, v2, depth+1);
			}
			else
			{
				return ver1.compareTo(ver2);
			}
			
		}
		catch (Exception e)
		{
			return null;
		}
		
	}

	private String getCurrentTridasVersionURI()
	{
		return TridasNamespacePrefixMapper.getTridasNamespaceURI();
	}
	
	private String getCurrentTridasVersionNumber()
	{
		try{
			return getCurrentTridasVersionURI().substring(getCurrentTridasVersionURI().lastIndexOf("/")+1);
		} catch (Exception e)
		{
			
		}
		
		return null;
	}
	
	private String getTridasVersionFromFile(String xmlfile)
	{
		
		String regex = null;
		Pattern p1;
		Matcher m1;
		
		regex = "http://www.tridas.org/[\\d.]*";
		p1 = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		m1 = p1.matcher(xmlfile);
		if (m1.find()) 
		{
			return xmlfile.substring(m1.start(), m1.end());
		}
		else
		{
			return "";
		}
	}
	
	/**
	 * Check that the current Tridas namespace is mentioned in the
	 * provided XML file
	 * 
	 * @param xmlfile
	 * @return
	 */
	@SuppressWarnings("unused")
	private Boolean isTridasNamespaceCorrect(String xmlfile)
	{
		try{
	    DocumentBuilderFactory domFactory = 
	        DocumentBuilderFactory.newInstance();
	              domFactory.setNamespaceAware(true); 
	        DocumentBuilder builder = domFactory.newDocumentBuilder();
	        Document doc = builder.parse(xmlfile);
	        XPath xpath = XPathFactory.newInstance().newXPath();
	        XPathExpression expr = xpath.compile("//*[namespace-uri()='"+
	        		TridasNamespacePrefixMapper.getTridasNamespaceURI()+"']");

	        Object result = expr.evaluate(doc, XPathConstants.NODESET);
	        NodeList nodes = (NodeList) result;
	        
	        if(nodes.getLength()>0)
	        {
	        	return true;
	        }
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return false;
	}
		
	/**
	 * @see org.tridas.io.IDendroFileReader#getDefaults()
	 */
	@Override
	public IMetadataFieldSet getDefaults() {
		return defaults;
	}
	
	@Override
	public int getCurrentLineNumber() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#resetReader()
	 */
	@Override
	protected void resetReader() {
		projects = null;
		defaults = null;
	}
	
	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getProjects()
	 */
	@Override
	public TridasProject[] getProjects() {
		return projects.toArray(new TridasProject[0]);
	}

	/**
	 * @see org.tridas.io.AbstractDendroFileReader#getTridasContainer()
	 */
	public TridasTridas getTridasContainer() {
		TridasTridas container = new TridasTridas();
		List<TridasProject> list = Arrays.asList(getProjects());
		container.setProjects(list);
		return container;
	}
}
