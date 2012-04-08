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
package org.soitoolkit.tools.generator.update_tool;

import static org.soitoolkit.commons.xml.XPathUtil.createDocument;
import static org.soitoolkit.commons.xml.XPathUtil.getXPathResult;
import static org.soitoolkit.commons.xml.XPathUtil.getXml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Updates soi-toolkit version number in xml-files that mavens release plugin does not update automatically.
 * 
 * @author magnus larsson
 *
 */
public class UpdateTool {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String newVersion = "0.5.1"; // specified as "n.n.n"
		boolean isSnapshot = true; // true or false

		UpdateTool ut = new UpdateTool();
		ut.updateXmlTextNodeContent("../..", "commons/poms/default-parent/pom.xml",                            "ns", "http://maven.apache.org/POM/4.0.0", "/ns:project/ns:properties/ns:soitoolkit.version", ut.getNewVersion(newVersion, isSnapshot));		
//		ut.updateXmlTextNodeContent("../..", "commons/poms/mule-dependencies/mule-2.2.5-dependencies/pom.xml", "ns", "http://maven.apache.org/POM/4.0.0", "/ns:project/ns:parent/ns:version", ut.getNewVersion(newVersion, isSnapshot));
//		ut.updateXmlTextNodeContent("../..", "commons/poms/mule-dependencies/mule-2.2.7-dependencies/pom.xml", "ns", "http://maven.apache.org/POM/4.0.0", "/ns:project/ns:parent/ns:version", ut.getNewVersion(newVersion, isSnapshot));
//		ut.updateXmlTextNodeContent("../..", "commons/poms/mule-dependencies/mule-3.0.0-dependencies/pom.xml", "ns", "http://maven.apache.org/POM/4.0.0", "/ns:project/ns:parent/ns:version", ut.getNewVersion(newVersion, isSnapshot));
//		ut.updateXmlTextNodeContent("../..", "commons/poms/mule-dependencies/mule-3.0.1-dependencies/pom.xml", "ns", "http://maven.apache.org/POM/4.0.0", "/ns:project/ns:parent/ns:version", ut.getNewVersion(newVersion, isSnapshot));
//		ut.updateXmlTextNodeContent("../..", "commons/poms/mule-dependencies/mule-3.1.0-dependencies/pom.xml", "ns", "http://maven.apache.org/POM/4.0.0", "/ns:project/ns:parent/ns:version", ut.getNewVersion(newVersion, isSnapshot));
		ut.updateXmlTextNodeContent("../..", "commons/poms/mule-dependencies/mule-3.2.0-dependencies/pom.xml", "ns", "http://maven.apache.org/POM/4.0.0", "/ns:project/ns:parent/ns:version", ut.getNewVersion(newVersion, isSnapshot));
		ut.updateXmlTextNodeContent("../..", "tools/soitoolkit-generator/org.soitoolkit.generator.update/site.xml",     null, null, "/site/feature/@version", newVersion); // site.xml file requires strict "n.n.n" versions so no snapshot info can be added
		ut.updateXmlTextNodeContent("../..", "tools/soitoolkit-generator/org.soitoolkit.generator.update/site.xml",     null, null, "/site/feature/@url",     "features/org.soitoolkit.generator.feature_" + newVersion + ".jar");
		ut.updateXmlTextNodeContent("../..", "tools/soitoolkit-generator/org.soitoolkit.generator.feature/feature.xml", null, null, "/feature/@version",      newVersion); // feature.xml file requires strict "n.n.n" versions so no snapshot info can be added
		ut.updateXmlTextNodeContent("../..", "tools/soitoolkit-populate-local-maven-repo/pom.xml",             "ns", "http://maven.apache.org/POM/4.0.0", "/ns:project/ns:version", ut.getNewVersion(newVersion, isSnapshot));		
	}

	public String getNewVersion(String newVersion, boolean isSnapshot) {
		return newVersion + (isSnapshot ? "-SNAPSHOT" : "");
	}

	public void updateXmlTextNodeContent(String trunkFolder, String filename, String nsPrefix, String namespace, String xPath, String newContent) {
		File file = new File(trunkFolder + "/" + filename);

		InputStream content = null;
		String xml = null;
		try {

			// Get the current content from the xml-file
			content = new FileInputStream(file);
			Document doc = createDocument(content);

			// Setup namespace if specified
			Map<String, String> namespaceMap = new HashMap<String, String>();
			if (namespace != null) {
				namespaceMap.put(nsPrefix, namespace);
			}

			// Lookup the text-node and print its current value
			NodeList rootList = getXPathResult(doc, namespaceMap, xPath);
			Node root = rootList.item(0);
			
			if (root == null) {
				throw new RuntimeException("Can't find " + xPath + " in file " + file.getCanonicalFile());
			}
			System.err.println(filename + ": " + xPath + ": " + root.getTextContent() + " --> " + newContent);		    

			// Update it value
			root.setTextContent(newContent);

			// Get the new total xml content as a string
			xml = getXml(doc);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (content != null) {try {content.close();} catch (IOException e) {}}
		}

		// Update the file with the new content
		PrintWriter pw = null;
		try {
			pw = openFileForOverwrite(file);
			pw.print(xml);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (pw != null) {pw.close();}
		}
	}

	private PrintWriter openFileForOverwrite(File file) throws IOException {
	    return new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
	}
}
