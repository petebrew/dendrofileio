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
package org.tridas.io.formats.csv;

import org.tridas.io.defaults.AbstractMetadataFieldSet;

/**
 * Place to hold and change default fields for the TRIMS filetype
 * 
 * @see org.tridas.io.formats.tucson
 * @author peterbrewer
 * @deprecated use CSVMatrix instead
 */
public class TridasToCSVDefaults extends AbstractMetadataFieldSet {
	
	public enum CSVField {}
	
	/**
	 * @see org.tridas.io.defaults.AbstractMetadataFieldSet#initDefaultValues()
	 */
	@Override
	protected void initDefaultValues() {

	}
	
}
