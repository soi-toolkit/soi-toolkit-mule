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
		// TODO Auto-generated method stub
		new UpdateTool().updateDefaultParentPom();

	}

	public void updateSourceFiles() throws IOException {
//		String srcFolder = projFolder + "/src/main/resources/";
//		String key       = gu.getModel().getLowercaseJavaService() + "-export-query";
//		String table     = gu.getModel().getUppercaseService() + "_EXPORT_TB";
//
//		PrintWriter out = null;
//		try {
//			String file = ""; // outFolder + gu.getModel().getArtifactId() + "-jdbc-connector.xml";
//			addQueryToMuleJdbcConnector(file, "<jdbc:query xmlns:jdbc=\"http://www.mulesource.org/schema/mule/jdbc/2.2\" key=\"" + key + "\" value=\"SELECT ID, VALUE FROM " + table + " ORDER BY ID\"/>");
//			addQueryToMuleJdbcConnector(file, "<jdbc:query xmlns:jdbc=\"http://www.mulesource.org/schema/mule/jdbc/2.2\" key=\"" + key + ".ack\" value=\"DELETE FROM " + table + " WHERE ID = #[map-payload:ID]\"/>");
//
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		} finally {
//			if (out != null) {out.close();}
//		}
    }

	private void updateDefaultParentPom() {
		String trunkFolder = "../../..";
		File file = new File(trunkFolder + "/commons/poms/default-parent/pom.xml");

		InputStream content = null;
		String xml = null;
		try {

			
			System.err.println(file.exists() + ": " + file.getCanonicalPath());

			content = new FileInputStream(file);
			Document doc = createDocument(content);

			Map<String, String> namespaceMap = new HashMap<String, String>();
			namespaceMap.put("ns",   "http://maven.apache.org/POM/4.0.0");

			NodeList rootList = getXPathResult(doc, namespaceMap, "/ns:project/ns:properties/ns:soitoolkit.version");
			Node root = rootList.item(0);
		    System.err.println("### ROOT NODE: " + ((root == null) ? " NULL" : root.getLocalName() + "=" + root.getTextContent()));		    
		    
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
		
	    System.err.println("### UPDATED: " + file);
	
	}

	private PrintWriter openFileForOverwrite(File file) throws IOException {

		// TODO: Replace with sl4j!
		System.err.println("Overwrite file: " + file);

	    return new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
	}

}
