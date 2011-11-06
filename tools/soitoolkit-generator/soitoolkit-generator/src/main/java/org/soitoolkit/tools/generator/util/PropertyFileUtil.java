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
package org.soitoolkit.tools.generator.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class PropertyFileUtil {

	/**
     * Hidden constructor.
     */
    private PropertyFileUtil() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }

    public static PrintWriter openPropertyFileForAppend(String outputFolder, String propertyFile) throws IOException {
		String propFile = outputFolder + "/src/main/resources/" + propertyFile + ".properties";

		// TODO: Replace with sl4j!
		System.err.println("[INFO] Appending to property file: " + propFile);
		
	    return new PrintWriter(new BufferedWriter(new FileWriter(propFile, true)));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void updateMuleDeployPropertyFileWithNewService(String outputFolder, String serviceName) {
		
		String muleDeployPropertyFile = outputFolder + "/application/mule-deploy.properties";
		System.err.println("Open muleDeployPropertyFile: " + muleDeployPropertyFile);
		
		try {
			PropertiesConfiguration config = new PropertiesConfiguration(muleDeployPropertyFile);
			String key = "config.resources";
			List value = config.getList(key);
			value.add(serviceName + "-service.xml");
			System.err.println("Update muleDeployPropertyFile: " + key + " = " + value);
			config.setProperty(key, value);
			config.save();
			System.err.println("Saved muleDeployPropertyFile");
		
		} catch (ConfigurationException e1) {
			System.err.println("Error with muleDeployPropertyFile: " + e1.getMessage());
			throw new RuntimeException(e1);
		}
	}

}