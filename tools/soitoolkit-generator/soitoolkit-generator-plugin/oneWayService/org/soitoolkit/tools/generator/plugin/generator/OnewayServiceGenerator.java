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
import static org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum.*;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.soitoolkit.tools.generator.plugin.model.IModel;
import org.soitoolkit.tools.generator.plugin.model.enums.TransformerEnum;
import org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum;
import org.soitoolkit.tools.generator.plugin.util.PreferencesUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OnewayServiceGenerator implements Generator {

	private static final String NAMESPACE_CORE = "http://www.mulesoft.org/schema/mule/core";
	private static final String NAMESPACE_JDBC = "http://www.mulesoft.org/schema/mule/jdbc";
	
	GeneratorUtil gu;
	IModel m;
	
	public OnewayServiceGenerator(PrintStream ps, String groupId, String artifactId, String serviceName, TransportEnum inboundTransport, TransportEnum outboundTransport, TransformerEnum transformerType, String folderName) {
		gu = new GeneratorUtil(ps, groupId, artifactId, null, serviceName, null, inboundTransport, outboundTransport, transformerType, "/oneWayService", folderName);
		m = gu.getModel();
	}
		
    public void startGenerator() {

    	System.err.println("### A BRAND NEW ONE-WAY-SERVICE IS ON ITS WAY..., INB: " + m.getInboundTransport() + ", OUTB: " + m.getOutboundTransport());
		TransportEnum inboundTransport  = TransportEnum.valueOf(m.getInboundTransport());
		TransportEnum outboundTransport = TransportEnum.valueOf(m.getOutboundTransport());

    	gu.generateContentAndCreateFile("src/main/resources/services/__service__-service.xml.gt");
		gu.generateContentAndCreateFile("src/main/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__Transformer.java.gt");
		
		gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__-input.txt.gt");
		gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__-expected-result.txt.gt");
		gu.generateContentAndCreateFile("src/test/resources/teststub-services/__service__-teststub-service.xml.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__TransformerTest.java.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__IntegrationTest.java.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__TestReceiver.java.gt");
		
	    // Servlet test sender (performs a mime multipart http post)
	    if (inboundTransport == SERVLET || inboundTransport == HTTP) {
			gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__TestSender.java.gt");
	    }

//		TODO: Wait with attachments... 	    
//	    if (inboundTransport == POP3 || inboundTransport == IMAP) {
//			gu.copyContentAndCreateFile("src/test/resources/testfiles/__service__-input-attachment.pdf.gt");
//			gu.copyContentAndCreateFile("src/test/resources/testfiles/__service__-input-attachment.png.gt");
//	    }

		updatePropertyFiles(inboundTransport, outboundTransport);
		
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

	private void updatePropertyFiles(TransportEnum inboundTransport, TransportEnum outboundTransport) {
		
		PrintWriter cfg = null;
		PrintWriter sec = null;
		try {
			cfg = openPropertyFileForAppend(gu.getOutputFolder(), m.getConfigPropertyFile());
			sec = openPropertyFileForAppend(gu.getOutputFolder(), m.getSecurityPropertyFile());

			String artifactId     = m.getArtifactId();
			String service        = m.getUppercaseService();
		    String serviceName    = m.getLowercaseService();
			String fileRootFolder = PreferencesUtil.getDefaultFileRootFolder();
			String ftpRootFolder  = PreferencesUtil.getDefaultFtpRootFolder();
			String sftpRootFolder = PreferencesUtil.getDefaultSftpRootFolder();
			String archiveFolder  = PreferencesUtil.getDefaultArchiveFolder(); 

			// Print header for this service's properties
		    cfg.println("");
		    cfg.println("# Properties for service \"" + m.getService() + "\"");
		    cfg.println("# TODO: Update to reflect your settings");

		    if (inboundTransport == POP3 || inboundTransport == IMAP) {
				// Print header for this service's properties if any security related properties are required
			    sec.println("");
			    sec.println("# Security related properties for service \"" + m.getService() + "\"");
			    sec.println("# TODO: Update to reflect your settings");
		    }
		    
		    // VM properties
		    if (inboundTransport == VM) {
			    cfg.println(service + "_IN_VM_QUEUE="  + m.getJmsInQueue());
		    }
		    if (outboundTransport == VM) {
			    cfg.println(service + "_OUT_VM_QUEUE=" + m.getJmsOutQueue());
		    }

		    // JMS properties
		    if (inboundTransport == JMS) {
			    cfg.println(service + "_IN_QUEUE="  + m.getJmsInQueue());
			    cfg.println(service + "_DL_QUEUE="  + m.getJmsDLQueue());
		    }
		    if (outboundTransport == JMS) {
			    cfg.println(service + "_OUT_QUEUE=" + m.getJmsOutQueue());
		    }
		    		    
		    // Http properties
		    if (inboundTransport == HTTP) {
			    cfg.println(service + "_INBOUND_URL=http://localhost:" + m.getHttpPort() + "/" + artifactId + "/services/" + serviceName + "/inbound");
		    }

		    // Servlet properties
		    if (inboundTransport == SERVLET) {
			    cfg.println(service + "_INBOUND_URL=servlet://" + serviceName + "/inbound");
		    }

		    // File properties
		    if (inboundTransport == FILE) {
				cfg.println(service + "_INBOUND_FOLDER=" + fileRootFolder + "/" + serviceName + "/inbound");
			    cfg.println(service + "_INBOUND_POLLING_MS=1000");
			    cfg.println(service + "_INBOUND_FILE_AGE_MS=500");
		    }
		    if (outboundTransport == FILE) {
				cfg.println(service + "_OUTBOUND_FOLDER=" + fileRootFolder + "/" + serviceName + "/outbound");
			    cfg.println(service + "_TESTSTUB_INBOUND_POLLING_MS=1000");
			    cfg.println(service + "_TESTSTUB_INBOUND_FILE_AGE_MS=500");
		    }

		    // FTP properties
		    if (inboundTransport == FTP) {
				cfg.println(service + "_INBOUND_FOLDER=" + ftpRootFolder + "/" + serviceName + "/inbound");
			    cfg.println(service + "_INBOUND_POLLING_MS=1000");
		    }
		    if (outboundTransport == FTP) {
				cfg.println(service + "_OUTBOUND_FOLDER=" + ftpRootFolder + "/" + serviceName + "/outbound");
			    cfg.println(service + "_TESTSTUB_INBOUND_POLLING_MS=1000");
		    }		    
		    
		    // SFTP properties
		    if (inboundTransport == SFTP) {
				cfg.println(service + "_INBOUND_SFTP_FOLDER=" + sftpRootFolder + "/" + serviceName + "/inbound");
			    cfg.println(service + "_INBOUND_SFTP_POLLING_MS=1000");
			    cfg.println(service + "_INBOUND_SFTP_SIZECHECK_MS=500");
		    }
		    if (outboundTransport == SFTP) {
			    cfg.println(service + "_OUTBOUND_SFTP_FOLDER=${" + service + "_TESTSTUB_INBOUND_SFTP_FOLDER}");
			    cfg.println(service + "_TESTSTUB_INBOUND_SFTP_FOLDER=" + sftpRootFolder + "/" + serviceName + "/outbound");
			    cfg.println(service + "_TESTSTUB_INBOUND_SFTP_POLLING_MS=1000");
			    cfg.println(service + "_TESTSTUB_INBOUND_SFTP_SIZECHECK_MS=500");
		    }

		    // Properties common to all filebased transports
		    if (m.isInboundEndpointFilebased() || m.isOutboundEndpointFilebased()) {
		    	cfg.println(service + "_ARCHIVE_FOLDER=" + archiveFolder + "/" + serviceName);
		    }
		    if (m.isOutboundEndpointFilebased()) {
			    cfg.println(service + "_ARCHIVE_RESEND_POLLING_MS=1000");
			    
		    	// If we don't have a file based inbound endpoint (e.g. transport) we have to specify the name of the out-file ourself...
				if (!m.isInboundEndpointFilebased()) {
			    	cfg.println(service + "_OUTBOUND_FILE=outfile.txt");
			    }
		    }

		    // Properties common to POP3, IMAP and SMTP (Inb POP3 and IMAP needs SMTP to send testmessages in junit-testcode)
		    if (inboundTransport == POP3 || inboundTransport == IMAP || outboundTransport == SMTP) {
				cfg.println(service + "_SMTP_HOST=smtp.bredband.net");
			    cfg.println(service + "_SMTP_PORT=25");
		    }

		    // Properties common to IMAP and SMTP (SMTP teststub reads mails using IMAP...)
		    if (inboundTransport == IMAP || outboundTransport == SMTP) {
				cfg.println(service + "_IMAP_HOST=imap.n.mail.yahoo.com");
			    cfg.println(service + "_IMAP_PORT=143");
		    }
		    
		    // Properties common to POP3 and IMAP (Mail-proeprties use to send testmails from juin-testcode)
		    if (inboundTransport == POP3 || inboundTransport == IMAP) {
				cfg.println(service + "_INBOUND_EMAIL_TEST_FROM=soitoolkit2@yahoo.se");
				cfg.println(service + "_INBOUND_EMAIL_TO=soitoolkit1@yahoo.se");
				cfg.println(service + "_INBOUND_EMAIL_SUBJECT=Inbound mail sent to Mule ESB");
		    }
		    
		    // POP3 properties
		    if (inboundTransport == POP3) {
				cfg.println(service + "_POP3_HOST=pop.mail.yahoo.com");
			    cfg.println(service + "_POP3_PORT=110");

		    	// Write to the security-property-file
			    sec.println(service + "_POP3_USR=soitoolkit1%40yahoo.se");
			    sec.println(service + "_POP3_PWD=soitoolkit1pwd");
		    }
		    
		    // IMAP properties
		    if (inboundTransport == IMAP) {
		    	// Write to the security-property-file
			    sec.println(service + "_IMAP_USR=soitoolkit1%40yahoo.se");
			    sec.println(service + "_IMAP_PWD=soitoolkit1pwd");
		    }

		    // SMTP properties
		    if (outboundTransport == SMTP) {
			    cfg.println(service + "_IMAP_TEST_USR=soitoolkit2%40yahoo.se");
			    cfg.println(service + "_IMAP_TEST_PWD=soitoolkit2pwd");

			    cfg.println(service + "_OUTBOUND_EMAIL_FROM=soitoolkit1@yahoo.se");
			    cfg.println(service + "_OUTBOUND_EMAIL_TEST_TO=soitoolkit2@yahoo.se");
			    cfg.println(service + "_OUTBOUND_EMAIL_SUBJECT=Outbound mail sent from Mule ESB");
		    }
		    
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (cfg != null) {cfg.close();}
			if (sec != null) {sec.close();}
		}
	}
	
	private void updateSqlDdlFilesAddExportTable() {
		String tbPrefix = m.getUppercaseService() + "_EXPORT";
		updateSqlDdlFiles(tbPrefix);		
	}

	private void updateSqlDdlFilesAddImportTable() {
		String tbPrefix = m.getUppercaseService() + "_IMPORT";
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
			out = openFileForAppend(outFolder + m.getArtifactId() + "-db-create-tables.sql");
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
			out = openFileForAppend(outFolder + m.getArtifactId() + "-db-drop-tables.sql");
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
			out = openFileForAppend(outFolder + m.getArtifactId() + "-db-insert-testdata.sql");

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null) {out.close();}
		}
    }

	private void updateJdbcConnectorFileWithExportSql() {
		String outFolder = gu.getOutputFolder() + "/src/main/resources/";
		String key       = m.getLowercaseJavaService() + "-export-query";
		String table     = m.getUppercaseService() + "_EXPORT_TB";

		PrintWriter out = null;
		try {
			String file = outFolder + m.getArtifactId() + "-jdbc-connector.xml";
			addQueryToMuleJdbcConnector(file, "<jdbc:query xmlns:jdbc=\"" + NAMESPACE_JDBC + "\" key=\"" + key + "\" value=\"SELECT ID, VALUE FROM " + table + " ORDER BY ID\"/>");
			addQueryToMuleJdbcConnector(file, "<jdbc:query xmlns:jdbc=\"" + NAMESPACE_JDBC + "\" key=\"" + key + ".ack\" value=\"DELETE FROM " + table + " WHERE ID = #[map-payload:ID]\"/>");

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null) {out.close();}
		}
    }

	private void updateJdbcConnectorFileWithImportSql() {
		String outFolder    = gu.getOutputFolder() + "/src/main/resources/";
		String key          = m.getLowercaseJavaService() + "-import-query";
		String key_teststub = m.getLowercaseJavaService() + "-teststub-export-query";
		String table        = m.getUppercaseService() + "_IMPORT_TB";

		PrintWriter out = null;
		try {
			String file = outFolder + m.getArtifactId() + "-jdbc-connector.xml";
			addQueryToMuleJdbcConnector(file, "<jdbc:query xmlns:jdbc=\"" + NAMESPACE_JDBC + "\" key=\"" + key          + "\" value=\"INSERT INTO  " + table + "(ID, VALUE) VALUES (#[map-payload:ID], #[map-payload:VALUE])\"/>");
			addQueryToMuleJdbcConnector(file, "<jdbc:query xmlns:jdbc=\"" + NAMESPACE_JDBC + "\" key=\"" + key_teststub + "\" value=\"SELECT ID, VALUE FROM " + table + " ORDER BY ID\"/>");
			addQueryToMuleJdbcConnector(file, "<jdbc:query xmlns:jdbc=\"" + NAMESPACE_JDBC + "\" key=\"" + key_teststub + ".ack\" value=\"DELETE FROM " + table + " WHERE ID = #[map-payload:ID]\"/>");
			
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
			namespaceMap.put("ns",   NAMESPACE_CORE);
			namespaceMap.put("jdbc", NAMESPACE_JDBC);

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
