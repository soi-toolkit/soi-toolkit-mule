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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.soitoolkit.tools.generator.GeneratorUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlFileUtil {

	private static final String NAMESPACE_CORE   = "http://www.mulesoft.org/schema/mule/core";
	private static final String NAMESPACE_SPRING = "http://www.springframework.org/schema/beans";

	/**
     * Hidden constructor.
     */
    private XmlFileUtil() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }

	public static void x_updateConfigXmlFileWithNewService(String outputFolder, String icName, String serviceName) {
		String filename  = outputFolder + "/src/main/app/" + icName + "-config.xml";
		x_updateXmlConfigFileWithNewService(filename, serviceName);
    }

	public static void x_updateTeststubsAndServicesConfigXmlFileWithNewService(String outputFolder, String icName, String serviceName) {
		String filename  = outputFolder + "/src/test/resources/" + icName + "-teststubs-and-services-config.xml";
		x_updateXmlConfigFileWithNewService(filename, serviceName);
    }

	private static void x_updateXmlConfigFileWithNewService(String filename, String serviceName) {
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

	public static boolean updateCommonFileWithSpringImport(GeneratorUtil gu, String comment, String xmlFragment) {
		return updateCommonFileWithSpringImport(gu, comment, xmlFragment, null);
	}    

	public static boolean updateCommonFileWithSpringImport(GeneratorUtil gu, String comment, String xmlFragment, String springBeanProfile) {
		String xmlFile = gu.getOutputFolder() + "/src/main/app/" + gu.getModel().getArtifactId() + "-common.xml";
		return updateSpringImportInXmlFile(gu, xmlFile, comment, xmlFragment, springBeanProfile);
	}

	public static void x_updateConfigFileWithSpringImport(GeneratorUtil gu, String comment, String xmlFragment, String springBeanProfile) {
		String xmlFile = gu.getOutputFolder() + "/src/main/app/" + gu.getModel().getArtifactId() + "-config.xml";
		updateSpringImportInXmlFile(gu, xmlFile, comment, xmlFragment, springBeanProfile);
	}

	static boolean updateSpringImportInXmlFile(GeneratorUtil gu, String xmlFile, String comment, String xmlFragment, String springBeanProfile) {

		InputStream content = null;
		String xml = null;
		try {
			
			gu.logDebug("Add: " + xmlFragment + " to " + xmlFile);
			content = new FileInputStream(xmlFile);
			
			xml = updateSpringImportInXmlInputStream(gu, content, comment, xmlFragment, springBeanProfile);
			
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} finally {
			if (content != null) {try {content.close();} catch (IOException e) {}}
		}

		PrintWriter pw = null;
		try {
			gu.logDebug("Writing back:\n" + xml);
			gu.logDebug("Overwrite file: " + xmlFile);
			pw = openFileForOverwrite(xmlFile);
			pw.print(xml);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (pw != null) {pw.close();}
		}
		return true;
	}

	static String updateSpringImportInXmlInputStream(GeneratorUtil gu, InputStream content, String comment, String xmlFragment, String springBeanProfile) {

		try {
			String xmlFragmentId = "classpath:" + xmlFragment;

			Document doc = createDocument(content);

			Map<String, String> namespaceMap = new HashMap<String, String>();
			namespaceMap.put("mule", "http://www.mulesoft.org/schema/mule/core");
			namespaceMap.put("spring", "http://www.springframework.org/schema/beans");

			// First verify that the dependency does not exist already
			NodeList testList = getXPathResult(doc, namespaceMap, "/mule:mule/spring:beans/spring:import/@resource[.='" + xmlFragmentId + "']");
			gu.logDebug("Look for: " + xmlFragmentId + ", resulted in " + testList.getLength() + " elements");

			if (testList.getLength() == 0) {
				gu.logDebug("Fragment does not exist, let's add it!!!");
				
				// Look up the beans-element, with or without profile-attribute
				String xpathExpression = "/mule:mule/spring:beans";
				if (springBeanProfile == null) {
					// With no profile we look for the first <beans> - element and set it as root
					xpathExpression += "[not(@profile)]";
				} else {
					// With a profile we look for the <beans> - profile attribute that match the specified profile and set its parent (i.e. the its <beans> - element as root
					xpathExpression += "[@profile='" + springBeanProfile + "']";
				}
				NodeList rootList = getXPathResult(doc, namespaceMap, xpathExpression);
				Node root = rootList.item(0);
			    
				xmlFragment = 
					// TODO: Comment not added to the document, simply skippen when the xml ragment is parsed...
					"<!-- " + comment + " -->\n" + 
					"    <spring:import xmlns:spring=\"http://www.springframework.org/schema/beans\" resource=\"" + xmlFragmentId + "\"/>";

				// If the spring:beans - element was not found then add it as well
				if (root == null) {
					String profileAttribute = "";
					if (springBeanProfile != null) {
						profileAttribute = "profile=\"" + springBeanProfile + "\"";
					}
					
					xmlFragment = 
						"    <spring:beans " + profileAttribute + " xmlns:spring=\"http://www.springframework.org/schema/beans\">\n" +
						"    " + xmlFragment + "\n" +
						"    </spring:beans>";
					rootList = getXPathResult(doc, namespaceMap, "/mule:mule");
					root = rootList.item(0);
				}
				
				appendXmlFragment(root, xmlFragment);
			}
			
			String xml = getXml(doc);
			xml = SourceFormatterUtil.formatSource(xml);
			
			return xml;

		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void updateJaxbContextInConfigFile(GeneratorUtil gu, String xmlFile, String javaPackage) {
		
		PrintWriter pw = null;
		InputStream content = null;
		try {

			content = new FileInputStream(xmlFile);
			String xml = updateJaxbContextInConfigInputStream(content, javaPackage);
			
			gu.logDebug("Writing back:\n" + xml);
			gu.logDebug("Overwrite file: " + xmlFile);
			
			pw = openFileForOverwrite(xmlFile);
			pw.print(xml);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (content != null) {try {content.close();} catch (IOException e) {}}
			if (pw != null) {pw.close();}
		}
	}

	static String updateJaxbContextInConfigInputStream(InputStream content, String javaPackage) {

		String xml = null;
		
		try {

			Document configDoc = createDocument(content);

			Map<String, String> namespaceMap = new HashMap<String, String>();
			namespaceMap.put("mule", "http://www.mulesoft.org/schema/mule/core");
			namespaceMap.put("mulexml", "http://www.mulesoft.org/schema/mule/xml");
			
			// Lookup the fragment...
			NodeList testList = getXPathResult(configDoc, namespaceMap, "/mule:mule/mulexml:jaxb-context");

			// If not found then add one with the supplied package name
			if (testList.getLength() == 0) {
				// Not found, ok try to add it...
				NodeList rootList = getXPathResult(configDoc, namespaceMap, "/mule:mule");
				Node root = rootList.item(0);
			    
				String xmlFragment = 
					"    <mulexml:jaxb-context xmlns:mulexml=\"http://www.mulesoft.org/schema/mule/xml\" name=\"jaxbContext\" packageNames=\"" + javaPackage + "\"/>";
				
				appendXmlFragment(root, xmlFragment);

			// Add the supplied package name to the existing element if not already there
			} else if (testList.getLength() == 1) {

				// Lookup the mandatory packageNames - attribute
				NodeList testList2 = getXPathResult(configDoc, namespaceMap, "/mule:mule/mulexml:jaxb-context/@packageNames");
				Node packageNamesAttr = testList2.item(0);
				String packageNames = packageNamesAttr.getNodeValue();

				// Add package name if it doesn't already exist in the jaxb-context
				if (!existsPackageName(packageNames, javaPackage)) {
				
					// Add it
					if (packageNames.length() > 0) {
						packageNames += ":";
					}
					packageNames += javaPackage;
	
					// Update the attribute
					packageNamesAttr.setNodeValue(packageNames);
				}
			}

			xml = getXml(configDoc);

		} catch (RuntimeException e) {
			throw e;
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return xml;
	}

	static boolean existsPackageName(String packageNames, String newJavaPackage) {
		
		if (packageNames == null) return false;
		
		String[] packArr = packageNames.split(":");
		
		for (String javaPackage : packArr) {
			// Got it, return true
			if (javaPackage.equals(newJavaPackage)) return true;
		}
		
		// Noop, not there, return false.
		return false;
	}

	static int packageCount(String packageNames, String newJavaPackage) {

		int count = 0;
		String[] packArr = packageNames.split(":");
		
		for (String javaPackage : packArr) {
			// Got it, increate counter
			if (javaPackage.equals(newJavaPackage)) count++;
		}
		return count;
	}

}