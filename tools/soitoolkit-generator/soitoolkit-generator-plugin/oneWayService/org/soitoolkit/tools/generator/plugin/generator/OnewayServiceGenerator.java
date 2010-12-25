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
package org.soitoolkit.tools.generator.plugin.generator;

import static org.soitoolkit.tools.generator.plugin.util.PropertyFileUtil.openPropertyFileForAppend;
import static org.soitoolkit.tools.generator.plugin.util.XmlUtil.createDocument;
import static org.soitoolkit.tools.generator.plugin.util.XmlUtil.getXPathResult;
import static org.soitoolkit.tools.generator.plugin.util.XmlUtil.getXml;
import static org.soitoolkit.tools.generator.plugin.util.XmlUtil.appendXmlFragment;
import static org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum.JMS;
import static org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum.JDBC;
import static org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum.SFTP;
import static org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum.SERVLET;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum;
import org.soitoolkit.tools.generator.plugin.util.PreferencesUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OnewayServiceGenerator implements Generator {

	GeneratorUtil gu;
	
	public OnewayServiceGenerator(PrintStream ps, String groupId, String artifactId, String serviceName, TransportEnum inboundTransport, TransportEnum outboundTransport, String folderName) {
		gu = new GeneratorUtil(ps, groupId, artifactId, null, serviceName, null, inboundTransport, outboundTransport, "/oneWayService", folderName);
	}
		
    public void startGenerator() {

    	System.err.println("### A BRAND NEW ONE-WAY-SERVICE IS ON ITS WAY..., INB: " + gu.getModel().getInboundTransport() + ", OUTB: " + gu.getModel().getOutboundTransport());
		TransportEnum inboundTransport  = TransportEnum.valueOf(gu.getModel().getInboundTransport());
		TransportEnum outboundTransport = TransportEnum.valueOf(gu.getModel().getOutboundTransport());

    	gu.generateContentAndCreateFile("src/main/resources/services/__service__-service.xml.gt");
		gu.generateContentAndCreateFile("src/main/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__Transformer.java.gt");
		
		gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__-input.txt.gt");
		gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__-expected-result.txt.gt");
		gu.generateContentAndCreateFile("src/test/resources/teststub-services/__service__-teststub-service.xml.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__TransformerTest.java.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__IntegrationTest.java.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__TestReceiver.java.gt");
		
	    // Servlet test consumer (performs a mime multipart hppt post)
	    if (inboundTransport == SERVLET) {
			gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__TestConsumer.java.gt");
	    }

		updatePropertyFile(inboundTransport, outboundTransport);
		
	    if (inboundTransport == JDBC) {
	    	updateSqlDdlFilesAddExportTable();
	    	updateJdbcConnectorFileWithExportSql();
			gu.generateContentAndCreateFile("src/main/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__ExportFromDbTransformer.java.gt");
	    }
		
	    if (outboundTransport == JDBC) {
	    	updateSqlDdlFilesAddImportTable();
	    	updateJdbcConnectorFileWithImportSql();
			gu.generateContentAndCreateFile("src/main/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__ImportToDbTransformer.java.gt");
	    }
		
    }

	private void updatePropertyFile(TransportEnum inboundTransport, TransportEnum outboundTransport) {
		
		PrintWriter out = null;
		try {
			out = openPropertyFileForAppend(gu.getOutputFolder(), gu.getModel().getConfigPropertyFile());
			String service        = gu.getModel().getUppercaseService();
		    String serviceName    = gu.getModel().getLowercaseService();
			String sftpRootFolder = PreferencesUtil.getDefaultSftpRootFolder();
			
		    out.println("");
		    out.println("# Properties for service \"" + gu.getModel().getService() + "\"");
		    out.println("# TODO: Update to reflect your settings");

		    // JMS properties
		    if (inboundTransport == JMS) {
			    out.println(service + "_IN_QUEUE="  + gu.getModel().getJmsInQueue());
			    out.println(service + "_DL_QUEUE="  + gu.getModel().getJmsDLQueue());
		    }
		    if (outboundTransport == JMS) {
			    out.println(service + "_OUT_QUEUE=" + gu.getModel().getJmsOutQueue());
		    }

		    // Servlet properties
		    if (inboundTransport == SERVLET) {
			    out.println(service + "_INBOUND_SERVLET_URI=" + serviceName + "/inbound");
		    }
		    
		    // SFTP properties
		    if (inboundTransport == SFTP) {
				out.println(service + "_SENDER_SFTP_ADDRESS=" + sftpRootFolder + "/" + serviceName + "/sender");
			    out.println(service + "_SENDER_POLLING_MS=1000");
			    out.println(service + "_SENDER_SIZECHECK_MS=500");
		    }
		    if (outboundTransport == SFTP) {
			    out.println(service + "_RECEIVER_SFTP_ADDRESS=" + sftpRootFolder + "/" + serviceName + "/receiver");
			    out.println(service + "_ARCHIVE_RESEND_POLLING_MS=1000");
			    out.println(service + "_TESTSTUB_RECEIVER_POLLING_MS=1000");
			    out.println(service + "_TESTSTUB_RECEIVER_SIZECHECK_MS=500");

			    if (!gu.getModel().isInboundEndpointFilebased()) {
			    	// If we don't have a file based inbound endpoint (e.g. transport) we have to specify the name of the out-file ourself...
			    	out.println(service + "_RECEIVER_FILE=outfile.txt");
			    }
		    }

		    if (inboundTransport == SFTP || outboundTransport == SFTP) {
		    	out.println(service + "_ARCHIVE_FOLDER=/Users/magnuslarsson/archive/" + serviceName);
		    }
		    
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null) {out.close();}
		}
	}
	
	private void updateSqlDdlFilesAddExportTable() {
		String tbPrefix = gu.getModel().getUppercaseService() + "_EXPORT";
		updateSqlDdlFiles(tbPrefix);		
	}

	private void updateSqlDdlFilesAddImportTable() {
		String tbPrefix = gu.getModel().getUppercaseService() + "_IMPORT";
		updateSqlDdlFiles(tbPrefix);		
	}
	
	private void updateSqlDdlFiles(String tbPrefix) {
		String setupFolder = "/src/environment/setup/";
		String outFolder = gu.getOutputFolder() + setupFolder;

		// Ensure that the folder exists
		gu.generateFolder(setupFolder);
		
		updateCreateSqlDdlFiles(tbPrefix, outFolder);
		updateDropSqlDdlFiles(tbPrefix, outFolder);
		updateCreateTestdataFile(tbPrefix, outFolder);
	}
	
	private void updateCreateSqlDdlFiles(String tbPrefix, String outFolder) {
		PrintWriter out = null;
		try {
			out = openFileForAppend(outFolder + gu.getModel().getArtifactId() + "-db-create-tables.sql");
			out.println("CREATE TABLE " + tbPrefix + "_TB (ID VARCHAR(32), VALUE VARCHAR(128), CONSTRAINT " + tbPrefix + "_PK PRIMARY KEY (ID));");

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null) {out.close();}
		}
		
    }

	private void updateDropSqlDdlFiles(String tbPrefix, String outFolder) {
		PrintWriter out = null;
		try {
			out = openFileForAppend(outFolder + gu.getModel().getArtifactId() + "-db-drop-tables.sql");
			out.println("DROP TABLE " + tbPrefix + "_TB;");

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null) {out.close();}
		}
    }

	private void updateCreateTestdataFile(String tbPrefix, String outFolder) {
		PrintWriter out = null;
		try {
			// Just ensure that the file is created, don't insert any testdata for now...
			out = openFileForAppend(outFolder + gu.getModel().getArtifactId() + "-db-insert-testdata.sql");

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null) {out.close();}
		}
    }

	private void updateJdbcConnectorFileWithExportSql() {
		String outFolder = gu.getOutputFolder() + "/src/main/resources/";
		String key       = gu.getModel().getLowercaseJavaService() + "-export-query";
		String table     = gu.getModel().getUppercaseService() + "_EXPORT_TB";

		PrintWriter out = null;
		try {
			String file = outFolder + gu.getModel().getArtifactId() + "-jdbc-connector.xml";
			addQueryToMuleJdbcConnector(file, "<jdbc:query xmlns:jdbc=\"http://www.mulesource.org/schema/mule/jdbc/2.2\" key=\"" + key + "\" value=\"SELECT ID, VALUE FROM " + table + " ORDER BY ID\"/>");
			addQueryToMuleJdbcConnector(file, "<jdbc:query xmlns:jdbc=\"http://www.mulesource.org/schema/mule/jdbc/2.2\" key=\"" + key + ".ack\" value=\"DELETE FROM " + table + " WHERE ID = #[map-payload:ID]\"/>");

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null) {out.close();}
		}
    }

	private void updateJdbcConnectorFileWithImportSql() {
		String outFolder    = gu.getOutputFolder() + "/src/main/resources/";
		String key          = gu.getModel().getLowercaseJavaService() + "-import-query";
		String key_teststub = gu.getModel().getLowercaseJavaService() + "-teststub-export-query";
		String table        = gu.getModel().getUppercaseService() + "_IMPORT_TB";

		PrintWriter out = null;
		try {
			String file = outFolder + gu.getModel().getArtifactId() + "-jdbc-connector.xml";
			addQueryToMuleJdbcConnector(file, "<jdbc:query xmlns:jdbc=\"http://www.mulesource.org/schema/mule/jdbc/2.2\" key=\"" + key          + "\" value=\"INSERT INTO  " + table + "(ID, VALUE) VALUES (#[map-payload:ID], #[map-payload:VALUE])\"/>");
			addQueryToMuleJdbcConnector(file, "<jdbc:query xmlns:jdbc=\"http://www.mulesource.org/schema/mule/jdbc/2.2\" key=\"" + key_teststub + "\" value=\"SELECT ID, VALUE FROM " + table + " ORDER BY ID\"/>");
			addQueryToMuleJdbcConnector(file, "<jdbc:query xmlns:jdbc=\"http://www.mulesource.org/schema/mule/jdbc/2.2\" key=\"" + key_teststub + ".ack\" value=\"DELETE FROM " + table + " WHERE ID = #[map-payload:ID]\"/>");
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null) {out.close();}
		}
		
    }

	private PrintWriter openFileForAppend(String filename) throws IOException {

		// TODO: Replace with sl4j!
		System.err.println("Appending to file: " + filename);

	    return new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
	}

	private PrintWriter openFileForOverwrite(String filename) throws IOException {

		// TODO: Replace with sl4j!
		System.err.println("Overwrite file: " + filename);

	    return new PrintWriter(new BufferedWriter(new FileWriter(filename, false)));
	}

	private void addQueryToMuleJdbcConnector(String file, String xmlFragment) {
		InputStream content = null;
		String xml = null;
		try {
		    System.err.println("### ADD: " + xmlFragment + " to " + file);
			content = new FileInputStream(file);
			Document doc = createDocument(content);

			Map<String, String> namespaceMap = new HashMap<String, String>();
			namespaceMap.put("ns",   "http://www.mulesource.org/schema/mule/core/2.2");
			namespaceMap.put("jdbc", "http://www.mulesource.org/schema/mule/jdbc/2.2");

			NodeList rootList = getXPathResult(doc, namespaceMap, "/ns:mule/jdbc:connector");
			Node root = rootList.item(0);
		    System.err.println("### ROOT NODE: " + ((root == null) ? " NULL" : root.getLocalName()));		    
		    
		    appendXmlFragment(root, xmlFragment);
			
			xml = getXml(doc);
			System.err.println(xml);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (content != null) {try {content.close();} catch (IOException e) {}}
		}

		PrintWriter pw = null;
		try {
			pw = openFileForOverwrite(file);
			pw.print(xml);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (pw != null) {pw.close();}
		}
	
	    System.err.println("### UPDATED: " + file);
	
	}
	
}
