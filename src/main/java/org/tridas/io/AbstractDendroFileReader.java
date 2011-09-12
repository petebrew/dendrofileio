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
package org.tridas.io;

import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tridas.io.defaults.IMetadataFieldSet;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.IncorrectDefaultFieldsException;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.util.FileHelper;
import org.tridas.schema.TridasProject;
import org.tridas.schema.TridasTridas;


public abstract class AbstractDendroFileReader {
	private static final Logger log = LoggerFactory.getLogger(AbstractDendroFileReader.class);
	
	private ArrayList<ConversionWarning> warnings = new ArrayList<ConversionWarning>();
	private final Class<? extends IMetadataFieldSet> defaultFieldsClass;
	private String origFilename;

	
	/**
	 * @param argDefaultFieldsClass
	 *            the default field set class used for metadata.
	 * @throws RuntimeException
	 *             if argDefaultFieldsClass is null or doesn't have an empty constructor
	 */
	public AbstractDendroFileReader(Class<? extends IMetadataFieldSet> argDefaultFieldsClass) {
		if (argDefaultFieldsClass == null) {
			throw new RuntimeException(I18n.getText("fileio.defaultsnull"));
		}
		
		try {
			if (argDefaultFieldsClass.getConstructor(new Class<?>[]{}) == null) {
				log.error("Defaults class '" + argDefaultFieldsClass.getName() + "' does not have empty constructor.");
				throw new RuntimeException(I18n.getText("runtimeExceptions.emptyConstructor"));
			}
		} catch (SecurityException e) {
			throw new RuntimeException(I18n.getText("runtimeExceptions.emptyConstructor"));
		} catch (NoSuchMethodException e) {
			log.error(I18n.getText("runtimeExceptions.emptyConstructor"));
			throw new RuntimeException(I18n.getText("runtimeExceptions.emptyConstructor"));
		}
		
		defaultFieldsClass = argDefaultFieldsClass;
	}
	
	/**
	 * @return the class of the default fields
	 */
	public Class<? extends IMetadataFieldSet> getDefaultFieldsClass() {
		return defaultFieldsClass;
	}
	
	/**
	 * The warnings generated by loading a file
	 * 
	 * @return
	 */
	public ConversionWarning[] getWarnings() {
		return warnings.toArray(new ConversionWarning[0]);
	}
	
	/**
	 * Add a warning to the list.
	 * 
	 * @param warning
	 */
	protected void addWarning(ConversionWarning warning) {
		warnings.add(warning);
	}
	
	/**
	 * Clears the conversion warnings.
	 */
	protected void clearWarnings() {
		warnings.clear();
	}
	
	/**
	 * Resets the reader to load another file.
	 */
	public void reset() {
		warnings.clear();
		origFilename = null;
		resetReader();
	}
	
	/**
	 * Constructs a new metadata defaults.
	 * 
	 * @return
	 */
	public IMetadataFieldSet constructDefaultMetadata() {
		try {
			return defaultFieldsClass.newInstance();
		} catch (InstantiationException e) {
			log.error(I18n.getText("runtimeExceptions.emptyConstructor"));
			return null;
		} catch (IllegalAccessException e) {
			log.error(I18n.getText("fileio.defaults.cantConstruct"));
			return null;
		}
	}
	
	/**
	 * Load a file from the given filename or url, and the given defaults. Override this
	 * method
	 * and {@link #loadFile(String, String, IMetadataFieldSet)} to load files that aren't
	 * strings.
	 * 
	 * @param argFilename
	 * @param argDefaultFields
	 * @throws IOException
	 *             if the file could not be loaded
	 * @throws IncorrectDefaultFieldsException
	 * @throws InvalidDendroFileException
	 */
	public void loadFile(String argFilename, IMetadataFieldSet argDefaultFields) throws IOException,
			IncorrectDefaultFieldsException, InvalidDendroFileException {
		if(argDefaultFields == null){
			loadFile(argFilename);
			return;
		}
		FileHelper fileHelper = new FileHelper();
		log.debug("loading file: " + argFilename);
		origFilename = argFilename;
		String[] strings;
		if (TridasIO.getReadingCharset() != null) {
			strings = fileHelper.loadStrings(argFilename, TridasIO.getReadingCharset());
		}
		else {
			if (TridasIO.isCharsetDetection()) {
				strings = fileHelper.loadStringsFromDetectedCharset(argFilename);
			}
			else {
				strings = fileHelper.loadStrings(argFilename);
			}
		}
		
		if (strings == null) {
			throw new IOException(I18n.getText("fileio.loadfailed"));
		}
		loadFile(strings, argDefaultFields);
	}
	
	/**
	 * Loads a file from the given filename or url, using the default metadata.
	 * 
	 * @param argFilename
	 * @throws IOException
	 * @throws InvalidDendroFileException
	 */
	public void loadFile(String argFilename) throws IOException, InvalidDendroFileException {
		try {
			loadFile(argFilename, constructDefaultMetadata());
		} catch (IncorrectDefaultFieldsException e) {
			// not gonna happen
		}
	}
	
	/**
	 * Loads a file from the given path, filename, and defaults. Override this method
	 * and {@link #loadFile(String, IMetadataFieldSet)} to load files that aren't strings.
	 * 
	 * @param argPath
	 * @param argFilename
	 * @param argDefaultFields
	 * @throws IOException
	 * @throws IncorrectDefaultFieldsException
	 * @throws InvalidDendroFileException
	 */
	public void loadFile(String argPath, String argFilename, IMetadataFieldSet argDefaultFields) throws IOException,
			IncorrectDefaultFieldsException, InvalidDendroFileException {
		if(argDefaultFields == null){
			loadFile(argPath, argFilename);
			return;
		}
		FileHelper fileHelper = new FileHelper(argPath);
		log.debug("loading file: " + argFilename);
		origFilename = argFilename;
		String[] strings;
		if (TridasIO.getReadingCharset() != null) {
			strings = fileHelper.loadStrings(argFilename, TridasIO.getReadingCharset());
		}
		else {
			if (TridasIO.isCharsetDetection()) {
				strings = fileHelper.loadStringsFromDetectedCharset(argFilename);
			}
			else {
				strings = fileHelper.loadStrings(argFilename);
			}
		}
		if (strings == null) {
			throw new IOException(I18n.getText("fileio.loadfailed"));
		}
		loadFile(strings, argDefaultFields);
	}
	
	/**
	 * Loads a file from the path and filename, using the default metadata.
	 * 
	 * @param argPath
	 * @param argFilename
	 * @throws IOException
	 * @throws InvalidDendroFileException
	 */
	public void loadFile(String argPath, String argFilename) throws IOException, InvalidDendroFileException {
		try {
			loadFile(argPath, argFilename, constructDefaultMetadata());
		} catch (IncorrectDefaultFieldsException e) {
			// not gonna happen
		}
	}
	
	/**
	 * Loads a file from the file strings
	 * 
	 * @param argFileStrings
	 * @param argDefaults
	 * @throws IncorrectDefaultFieldsException
	 * @throws InvalidDendroFileException
	 */
	public void loadFile(String[] argFileStrings, IMetadataFieldSet argDefaults)
			throws IncorrectDefaultFieldsException, InvalidDendroFileException {
		if(argDefaults == null){
			loadFile(argFileStrings);
			return;
		}
		if (!argDefaults.getClass().equals(defaultFieldsClass)) {
			throw new IncorrectDefaultFieldsException(defaultFieldsClass);
		}
		parseFile(argFileStrings, argDefaults);
	}
	
	/**
	 * Loads a file just from the strings, using the default metadata
	 * 
	 * @param argFileStrings
	 * @throws InvalidDendroFileException
	 */
	public void loadFile(String[] argFileStrings) throws InvalidDendroFileException {
		parseFile(argFileStrings, constructDefaultMetadata());
	}
	
	/**
	 * @param origFilename
	 *            the origFilename to set
	 */
	protected void setOriginalFilename(String origFilename) {
		this.origFilename = origFilename;
	}
	
	/**
	 * @return the original filename, or null if never specified
	 */
	public String getOriginalFilename() {
		return origFilename;
	}
	
	/**
	 * Parse the file from the given metadata and strings
	 * 
	 * @param argFileString
	 * @param argDefaultFields
	 * @throws InvalidDendroFileException
	 */
	protected abstract void parseFile(String[] argFileString, IMetadataFieldSet argDefaultFields)
			throws InvalidDendroFileException;
	
	/**
	 * Reset the reader
	 */
	protected abstract void resetReader();
	
	/**
	 * Gets the current line number of the reader. If errors occurred, this could
	 * help point towards the cause.
	 * 
	 * @return the current line number
	 */
	public abstract int getCurrentLineNumber();
	
	/**
	 * Returns a list of the file extensions for this file
	 * 
	 * @return
	 */
	public abstract String[] getFileExtensions();
	
	/**
	 * Get the short name of the format
	 * 
	 * @return
	 */
	public abstract String getShortName();
	
	/**
	 * Get the full name of the format
	 * 
	 * @return
	 */
	public abstract String getFullName();
	
	/**
	 * Get the description of the format
	 * 
	 * @return
	 */
	public abstract String getDescription();
	
	/**
	 * Get the default values for this reader that were given in
	 * loading the file. If the loadFile method was never called, then
	 * this returns null.
	 * 
	 * @return
	 */
	public abstract IMetadataFieldSet getDefaults();
	
	/**
	 * Gets the parsed projects after they are loaded.
	 */
	public abstract TridasProject[] getProjects();
		
	
	public abstract TridasTridas getTridasContainer();

	public abstract DendroFileFilter getDendroFileFilter();
}
