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

import static org.soitoolkit.commons.xml.XPathUtil.appendXmlFragment;
import static org.soitoolkit.commons.xml.XPathUtil.createDocument;
import static org.soitoolkit.commons.xml.XPathUtil.getXPathResult;
import static org.soitoolkit.commons.xml.XPathUtil.getXml;
import static org.soitoolkit.tools.generator.util.FileUtil.openFileForOverwrite;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlFileUtil {

	private static final String NAMESPACE_CORE   = "http://www.mulesoft.org/schema/mule/core";
	private static final String NAMESPACE_SPRING = "http://www.springframework.org/schema/beans";

	/**
     * Hidden constructor.
     */
    private XmlFileUtil() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }

	public static void updateConfigXmlFileWithNewService(String outputFolder, String icName, String serviceName) {
		String filename  = outputFolder + "/src/main/app/" + icName + "-config.xml";
		updateXmlConfigFileWithNewService(filename, serviceName);
    }

	public static void updateTeststubsAndServicesConfigXmlFileWithNewService(String outputFolder, String icName, String serviceName) {
		String filename  = outputFolder + "/src/test/resources/" + icName + "-teststubs-and-services-config.xml";
		updateXmlConfigFileWithNewService(filename, serviceName);
    }

	private static void updateXmlConfigFileWithNewService(String filename, String serviceName) {
		String xmlFragment = "<spring:import xmlns:spring=\"" + NAMESPACE_SPRING + "\" resource=\"classpath:" + serviceName + "-service.xml\"/>";
		
		InputStream content = null;
		String xml = null;
		try {
			System.err.println("Add: " + xmlFragment + " to " + filename);
			content = new FileInputStream(filename);
			Document doc = createDocument(content);

			Map<String, String> namespaceMap = new HashMap<String, String>();
			namespaceMap.put("ns",     NAMESPACE_CORE);
			namespaceMap.put("spring", NAMESPACE_SPRING);

			NodeList rootList = getXPathResult(doc, namespaceMap, "/ns:mule/spring:beans");
			Node root = rootList.item(0);
			System.err.println("Root node: " + ((root == null) ? " NULL" : root.getLocalName()));		    
		    
		    appendXmlFragment(root, xmlFragment);
			
			xml = getXml(doc);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (content != null) {try {content.close();} catch (IOException e) {}}
		}

		PrintWriter pw = null;
		try {
			System.err.println("Overwrite file: " + filename);
			pw = openFileForOverwrite(filename);
			pw.print(xml);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (pw != null) {pw.close();}
		}
		System.err.println("Updated: " + filename);
	}    
}