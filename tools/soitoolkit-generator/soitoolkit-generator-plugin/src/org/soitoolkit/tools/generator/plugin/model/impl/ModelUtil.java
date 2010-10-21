/* 
 * Licensed to the soi-toolkit project under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The soi-toolkit project licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.soitoolkit.tools.generator.plugin.model.impl;

import java.util.StringTokenizer;

public class ModelUtil {

	/**
     * Hidden constructor.
     */
    private ModelUtil() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }

    static public String capitalize(String name) {
		return name.substring(0,1).toUpperCase() + name.substring(1);
	}

	static public String makeJavaName(String name) {
		
		if (name == null) {
			return null;
		}
		
		// Remove all '-' and capitalize word folloing after...
	    StringBuffer javaName = new StringBuffer();
	    StringTokenizer st = new StringTokenizer(name, "-");

	    while(st.hasMoreTokens()) {
	    	javaName.append(capitalize(st.nextToken()));
	    }

	    return javaName.toString(); 
	}


}
