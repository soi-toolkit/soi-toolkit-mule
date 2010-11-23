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
package org.soitoolkit.commons.mule.crossref;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Simplest possible implementation of the CrossRef interface using a property-file
 * 
 * @author magnus larsson
 *
 */
public class CrossReferencePropertyFileImpl implements CrossReference {

	private ResourceBundle rb;
	private String propertyFile;
	
	public void setPropertyFile(String propertyFile) {
		this.propertyFile = propertyFile;
		rb = ResourceBundle.getBundle(propertyFile);
	}

	public String getPropertyFile() {
		return propertyFile;
	}

	public String lookup(String key) {
		try {
			return rb.getString(key);
		} catch (MissingResourceException e) {
			throw new CrossReferenceException("Unknown key=" + key, e, key);
		} catch (Exception e) {
			throw new CrossReferenceException("Unknown error", e, key);
		}
	}

	public String lookup(String key, String defaultValue) {
		try {
			return rb.getString(key);
		} catch (MissingResourceException e) {
			return defaultValue;
		} catch (Exception e) {
			throw new CrossReferenceException("Unknown error", e, key);
		}
	}
}
