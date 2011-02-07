package org.soitoolkit.tools.generator.update_tool;

import static org.soitoolkit.tools.generator.plugin.util.XmlUtil.appendXmlFragment;
import static org.soitoolkit.tools.generator.plugin.util.XmlUtil.createDocument;
import static org.soitoolkit.tools.generator.plugin.util.XmlUtil.getXPathResult;
import static org.soitoolkit.tools.generator.plugin.util.XmlUtil.getXml;

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

public class UpdateTool {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String newVersion = "0.3.1";

		UpdateTool ut = new UpdateTool();
		ut.updateXmlTextNodeContent("../..", "commons/poms/default-parent/pom.xml",                            "ns", "http://maven.apache.org/POM/4.0.0", "/ns:project/ns:properties/ns:soitoolkit.version", newVersion);
		ut.updateXmlTextNodeContent("../..", "commons/poms/mule-dependencies/mule-2.2.5-dependencies/pom.xml", "ns", "http://maven.apache.org/POM/4.0.0", "/ns:project/ns:parent/ns:version", newVersion);
		ut.updateXmlTextNodeContent("../..", "commons/poms/mule-dependencies/mule-2.2.7-dependencies/pom.xml", "ns", "http://maven.apache.org/POM/4.0.0", "/ns:project/ns:parent/ns:version", newVersion);
		ut.updateXmlTextNodeContent("../..", "commons/poms/mule-dependencies/mule-3.0.0-dependencies/pom.xml", "ns", "http://maven.apache.org/POM/4.0.0", "/ns:project/ns:parent/ns:version", newVersion);
		ut.updateXmlTextNodeContent("../..", "commons/poms/mule-dependencies/mule-3.0.1-dependencies/pom.xml", "ns", "http://maven.apache.org/POM/4.0.0", "/ns:project/ns:parent/ns:version", newVersion);
		ut.updateXmlTextNodeContent("../..", "commons/poms/mule-dependencies/mule-3.1.0-dependencies/pom.xml", "ns", "http://maven.apache.org/POM/4.0.0", "/ns:project/ns:parent/ns:version", newVersion);
		ut.updateXmlTextNodeContent("../..", "tools/soitoolkit-generator/org.soitoolkit.generator.update/site.xml",     null, null, "/site/feature/@version", newVersion);
		ut.updateXmlTextNodeContent("../..", "tools/soitoolkit-generator/org.soitoolkit.generator.update/site.xml",     null, null, "/site/feature/@url",     "features/org.soitoolkit.generator.feature_" + newVersion + ".jar");
		ut.updateXmlTextNodeContent("../..", "tools/soitoolkit-generator/org.soitoolkit.generator.feature/feature.xml", null, null, "/feature/@version",      newVersion);
	}

	public void updateXmlTextNodeContent(String trunkFolder, String filename, String nsPrefix, String namespace, String xPath, String newContent) {
		File file = new File(trunkFolder + "/" + filename);

		InputStream content = null;
		String xml = null;
		try {


			content = new FileInputStream(file);
			Document doc = createDocument(content);

			Map<String, String> namespaceMap = new HashMap<String, String>();
			if (namespace != null) {
				namespaceMap.put(nsPrefix, namespace);
			}
			
			NodeList rootList = getXPathResult(doc, namespaceMap, xPath);
			Node root = rootList.item(0);
			System.err.println(filename + ": " + xPath + " = " + ((root == null) ? "NULL" : root.getTextContent()));		    
		    
//		    appendXmlFragment(root, xmlFragment);
			
			xml = getXml(doc);
//			System.err.println(xml);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (content != null) {try {content.close();} catch (IOException e) {}}
		}

		/*
		PrintWriter pw = null;
		try {
			pw = openFileForOverwrite(file);
			pw.print(xml);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (pw != null) {pw.close();}
		}
		*/
		
//	    System.err.println("### UPDATED: " + file);
	
	}

	private PrintWriter openFileForOverwrite(File file) throws IOException {

		// TODO: Replace with sl4j!
		System.err.println("Overwrite file: " + file);

	    return new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
	}

}
