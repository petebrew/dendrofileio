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

import org.tridas.io.transform.TridasVersionTransformer.TridasVersion;

/**
 * Writer for the TRiDaS file format forcing creation of files in v1.2.3
 * 
 * @see org.tridas.io.formats.tridas
 * @author peterbrewer
 */
public class TridasWriter1_2_3 extends TridasWriter {

	public TridasWriter1_2_3()
	{
		super(TridasVersion.V_1_2_3);
	}
}
