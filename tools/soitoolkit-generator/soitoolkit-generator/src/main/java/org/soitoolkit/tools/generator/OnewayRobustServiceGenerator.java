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
package org.soitoolkit.tools.generator;

import static org.soitoolkit.commons.xml.XPathUtil.appendXmlFragment;
import static org.soitoolkit.commons.xml.XPathUtil.createDocument;
import static org.soitoolkit.commons.xml.XPathUtil.getXPathResult;
import static org.soitoolkit.commons.xml.XPathUtil.getXml;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.FILE;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.FTP;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.HTTP;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.IMAP;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.JDBC;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.JMS;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.POP3;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.SERVLET;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.SFTP;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.SMTP;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.VM;
import static org.soitoolkit.tools.generator.util.PropertyFileUtil.openPropertyFileForAppend;
import static org.soitoolkit.tools.generator.util.PropertyFileUtil.updateMuleDeployPropertyFileWithNewService;
import static org.soitoolkit.tools.generator.util.PropertyFileUtil.updateMuleDeployPropertyFileConfigFile;
import static org.soitoolkit.tools.generator.util.FileUtil.openFileForAppend;
import static org.soitoolkit.tools.generator.util.FileUtil.openFileForOverwrite;
import static org.soitoolkit.tools.generator.util.XmlFileUtil.updateCommonFileWithSpringImport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.soitoolkit.tools.generator.model.IModel;
import org.soitoolkit.tools.generator.model.enums.MuleVersionEnum;
import org.soitoolkit.tools.generator.model.enums.TransformerEnum;
import org.soitoolkit.tools.generator.model.enums.TransportEnum;
import org.soitoolkit.tools.generator.util.PreferencesUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * TODO: this class duplicates lots of code from: OnewayServiceGenerator
 *  
 */
public class OnewayRobustServiceGenerator implements Generator {

	private static final String NAMESPACE_CORE = "http://www.mulesoft.org/schema/mule/core";
	private static final String NAMESPACE_JDBC = "http://www.mulesoft.org/schema/mule/jdbc";
	
	GeneratorUtil gu;
	IModel m;
	
	public OnewayRobustServiceGenerator(PrintStream ps, String groupId, String artifactId, String serviceName, MuleVersionEnum muleVersion, TransportEnum inboundTransport, TransportEnum outboundTransport, TransformerEnum transformerType, String outputFolder) {
		gu = new GeneratorUtil(ps, groupId, artifactId, null, serviceName, muleVersion, inboundTransport, outboundTransport, transformerType, "/oneWayRobustService", outputFolder);
		m = gu.getModel();
	}
		
	public void startGenerator() {
		gu.logInfo("Creates a OneWay-Robust-service, inbound transport: " + m.getInboundTransport() + ", outbound transport: " + m.getOutboundTransport() + ", type of transformer: " + m.getTransformerType());
		TransportEnum inboundTransport  = TransportEnum.valueOf(m.getInboundTransport());
		TransportEnum outboundTransport = TransportEnum.valueOf(m.getOutboundTransport());
		TransformerEnum transformerType = TransformerEnum.valueOf(m.getTransformerType());
		
		// inbound service
		gu.generateContentAndCreateFile("src/main/app/__service__-inbound-service.xml.gt");
		gu.generateContentAndCreateFileUsingGroovyGenerator(getClass().getResource("GenerateMinimalMflow.groovy"), "flows/__service__-inbound-service.mflow");
		// inbound service - test support 
		gu.generateContentAndCreateFile("src/test/resources/teststub-services/__service__-inbound-teststub-service.xml.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/inbound/__capitalizedJavaService__InboundIntegrationTest.java.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/inbound/__capitalizedJavaService__TestReceiver.java.gt");
		
		// process service
		gu.generateContentAndCreateFile("src/main/app/__service__-process-service.xml.gt");
		gu.generateContentAndCreateFileUsingGroovyGenerator(getClass().getResource("GenerateMinimalMflow.groovy"), "flows/__service__-process-service.mflow");
		if (transformerType == TransformerEnum.JAVA) {
			gu.generateContentAndCreateFile("src/main/java/__javaPackageFilepath__/__lowercaseJavaService__/process/__capitalizedJavaService__Transformer.java.gt");
			gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/process/__capitalizedJavaService__TransformerTest.java.gt");
			gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__/input.txt.gt");
			gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__/expected-result.txt.gt");
		}
		else if (transformerType == TransformerEnum.EE_DATAMAPPER) {
			gu.generateContentAndCreateFile("src/main/java/__javaPackageFilepath__/__lowercaseJavaService__/process/MappingHelper.java.gt");
			gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/process/MappingHelperTest.java.gt");
			gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__/input.xml.gt");
			gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__/expected-result.xml.gt");
		}
		else {
			throw new IllegalArgumentException("Transformer type not supported for this kind of flow: " + transformerType);
		}		
		// process service - test support 
		gu.generateContentAndCreateFile("src/test/resources/teststub-services/__service__-process-teststub-service.xml.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/process/__capitalizedJavaService__ProcessIntegrationTest.java.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/process/__capitalizedJavaService__TestReceiver.java.gt");

		// outbound service
		gu.generateContentAndCreateFile("src/main/app/__service__-outbound-service.xml.gt");
		gu.generateContentAndCreateFileUsingGroovyGenerator(getClass().getResource("GenerateMinimalMflow.groovy"), "flows/__service__-outbound-service.mflow");
		// outbound service - test support 
		gu.generateContentAndCreateFile("src/test/resources/teststub-services/__service__-outbound-teststub-service.xml.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/outbound/__capitalizedJavaService__OutboundIntegrationTest.java.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/outbound/__capitalizedJavaService__TestReceiver.java.gt");
		
		// end-to-end - test support
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__IntegrationTest.java.gt");		

		// Update mule-deploy.properties files with the new service
		updateMuleDeployPropertyFileWithNewService(gu.getOutputFolder(), m.getService() + "-inbound");
		updateMuleDeployPropertyFileWithNewService(gu.getOutputFolder(), m.getService() + "-process");
		updateMuleDeployPropertyFileWithNewService(gu.getOutputFolder(), m.getService() + "-outbound");

		updatePropertyFiles(inboundTransport, outboundTransport);
		// add properties for internal JMS queues
		updatePropertyFiles(TransportEnum.JMS, TransportEnum.JMS);

		// Add vm-connector to common file (one and the same for junit-tests and running mule server) if vm-transport is used for the first time
		// Used by VM-IN, VM-OUT and SFTP-OUT
		if (inboundTransport == VM || outboundTransport == VM) {
			String comment = "Added " + new Date() + " since flow " + m.getService() + " uses the VM-transport";
    		updateCommonFileWithSpringImport(gu, comment, "soitoolkit-mule-vm-connector.xml");
		}

		// Add file-connector to common file (one and the same for junit-tests and running mule server) if file-transport is used for the first time
		// Used by FILE-IN, FILE-OUT and SFTP-OUT
		if (inboundTransport == FILE || outboundTransport == FILE) {
			String comment = "Added " + new Date() + " since flow " + m.getService() + " uses the FILE-transport";
    		updateCommonFileWithSpringImport(gu, comment, "soitoolkit-mule-file-connector.xml");
		}

		// Add ftp-connector to config file (separate connectors used for junit-tests and running mule server) if ftp-transport is used for the first time
		if (inboundTransport == FTP || outboundTransport == FTP) {
			String comment = "Added " + new Date() + " since flow " + m.getService() + " uses the FTP-transport";
    		updateCommonFileWithSpringImport(gu, comment, "soitoolkit-mule-ftp-connector-external.xml", "default");
		}
		
	}
	
    public void startGenerator_oneWayService_20130613_ORIGINAL() {

    	gu.logInfo("Creates a OneWay-service, inbound transport: " + m.getInboundTransport() + ", outbound transport: " + m.getOutboundTransport() + ", type of transformer: " + m.getTransformerType());
		TransportEnum inboundTransport  = TransportEnum.valueOf(m.getInboundTransport());
		TransportEnum outboundTransport = TransportEnum.valueOf(m.getOutboundTransport());

		// FIXME. MULE STUDIO.
//    	gu.generateContentAndCreateFile("src/main/resources/services/__service__-service.xml.gt");
    	gu.generateContentAndCreateFile("src/main/app/__service__-service.xml.gt");
    	gu.generateContentAndCreateFileUsingGroovyGenerator(getClass().getResource("GenerateMinimalMflow.groovy"), "flows/__service__-service.mflow");
		gu.generateContentAndCreateFile("src/main/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__Transformer.java.gt");

		gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__/input.txt.gt");
		gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__/expected-result.txt.gt");
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
//			gu.copyContentAndCreateFile("src/test/resources/testfiles/__service__/input-attachment.pdf.gt");
//			gu.copyContentAndCreateFile("src/test/resources/testfiles/__service__/input-attachment.png.gt");
//	    }

		updatePropertyFiles(inboundTransport, outboundTransport);
		
		// Update mule-deploy.properties files with the new service
		// (Everything in the folder src/main/app is loaded by mule-deploy.properties) so skip updating *ConfigXmlFile.
		// updateConfigXmlFileWithNewService(gu.getOutputFolder(), m.getArtifactId(), m.getService());
		// updateTeststubsAndServicesConfigXmlFileWithNewService(gu.getOutputFolder(), m.getArtifactId(), m.getService());
		updateMuleDeployPropertyFileWithNewService(gu.getOutputFolder(), m.getService());


		// Add vm-connector to common file (one and the same for junit-tests and running mule server) if vm-transport is used for the first time
		// Used by VM-IN, VM-OUT and SFTP-OUT
		if (inboundTransport == VM || outboundTransport == VM || outboundTransport == SFTP) {
			String comment = "Added " + new Date() + " since flow " + m.getService() + " uses the VM-transport";
    		updateCommonFileWithSpringImport(gu, comment, "soitoolkit-mule-vm-connector.xml");
		}

		// Add http-connector to common file (one and the same for junit-tests and running mule server) if http-transport is used for the first time
		if (inboundTransport == HTTP || outboundTransport == HTTP) {
			String comment = "Added " + new Date() + " since flow " + m.getService() + " uses the HTTP-transport";
    		updateCommonFileWithSpringImport(gu, comment, "soitoolkit-mule-http-connector.xml");
		}

		// Add file-connector to common file (one and the same for junit-tests and running mule server) if file-transport is used for the first time
		// Used by FILE-IN, FILE-OUT and SFTP-OUT
		if (inboundTransport == FILE || outboundTransport == FILE || outboundTransport == SFTP) {
			String comment = "Added " + new Date() + " since flow " + m.getService() + " uses the FILE-transport";
    		updateCommonFileWithSpringImport(gu, comment, "soitoolkit-mule-file-connector.xml");
		}

		// TODO: Do the same with JDBC and SFTP as for FTP so we can eliminate selection of transport in the Create Integration Component Wizard!

		// Add ftp-connector to config file (separate connectors used for junit-tests and running mule server) if ftp-transport is used for the first time
		if (inboundTransport == FTP || outboundTransport == FTP) {
			String comment = "Added " + new Date() + " since flow " + m.getService() + " uses the FTP-transport";
    		updateCommonFileWithSpringImport(gu, comment, "soitoolkit-mule-ftp-connector-external.xml", "default");
		}

		// Is this flow based on a XA-transaction?
	    if (m.isServiceXaTransactional()) {
	    	
	    	// Add JMS-XA-Connector if any endpoint is based on the JMS-transport 
	    	if (inboundTransport == JMS || outboundTransport == JMS) {
				String comment = "Added " + new Date() + " since flow " + m.getService() + " uses JMS under a XA transaction";
	    		updateCommonFileWithSpringImport(gu, comment, "soitoolkit-mule-jms-xa-connector-activemq-external.xml", "default");
	    	}

	    	// Add JDBC-XA-DataSource if any endpoint is based on the JDBC-transport 
	    	if (inboundTransport == JDBC || outboundTransport == JDBC) {

	    		// Create XA-connector for JDBC if it not already exists 
    			String jdbcXaConnectorConfigFilename = m.getArtifactId() + "-jdbc-xa-connector.xml";
	    	    if (!new File(gu.getOutputFolder() + "/src/main/app/" + jdbcXaConnectorConfigFilename).exists()) {
	    	    	gu.generateContentAndCreateFile("src/main/app/__artifactId__-jdbc-xa-connector.xml.gt");
	    			updateMuleDeployPropertyFileConfigFile(gu.getOutputFolder(), jdbcXaConnectorConfigFilename);
	    		}

	    	    String comment = "Added " + new Date() + " since flow " + m.getService() + " uses JDBC under a XA transaction";
	    		updateCommonFileWithSpringImport(gu, comment, "soitoolkit-mule-jdbc-xa-datasource-derby-external.xml", "default");
	    	}
	    }
				
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
		try {
			cfg = openPropertyFileForAppend(gu.getOutputFolder(), m.getConfigPropertyFile());

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
		    // Robust file archive properties
		    if (inboundTransport == JMS) {
		    	// file archive props
		    	cfg.println("# Archiving: default is to archive in-files and out-files in separate dirs");
		    	cfg.println(service + "_ARCHIVE_FOLDER_IN=" + archiveFolder + "/" + serviceName + "/in");
		    	cfg.println(service + "_ARCHIVE_FOLDER_OUT=" + archiveFolder + "/" + serviceName + "/out");
		    	cfg.println(service + "_ARCHIVE_FILENAME_IN=#[function:datestamp:yyyy-MM-dd]/#[function:datestamp:yyyyMMdd-HHmmss.SSS]_#[message.inboundProperties[org.mule.transport.file.FileConnector.PROPERTY_ORIGINAL_FILENAME]]");
		    	cfg.println(service + "_ARCHIVE_FILENAME_OUT=#[function:datestamp:yyyy-MM-dd]/#[function:datestamp:yyyyMMdd-HHmmss.SSS]_#[message.inboundProperties[org.mule.transport.file.FileConnector.PROPERTY_ORIGINAL_FILENAME]]");		    	
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
				// Mule 3.4.0 bug workaround for MULE-7160: set a low value of fileAge compared to polling interval to reduce risk of hitting the bug
			    //cfg.println(service + "_INBOUND_POLLING_MS=1000");
			    //cfg.println(service + "_INBOUND_FILE_AGE_MS=500");
			    cfg.println(service + "_INBOUND_POLLING_MS=2000");
			    cfg.println(service + "_INBOUND_FILE_AGE_MS=500");
		    }
		    if (outboundTransport == FILE) {
				cfg.println(service + "_OUTBOUND_FOLDER=${" + service + "_TESTSTUB_INBOUND_FOLDER}");
				cfg.println(service + "_TESTSTUB_INBOUND_FOLDER=" + fileRootFolder + "/" + serviceName + "/outbound");
				// Mule 3.4.0 bug workaround for MULE-7160: set a low value of fileAge compared to polling interval to reduce risk of hitting the bug
			    //cfg.println(service + "_TESTSTUB_INBOUND_POLLING_MS=1000");
			    //cfg.println(service + "_TESTSTUB_INBOUND_FILE_AGE_MS=500");
			    cfg.println(service + "_TESTSTUB_INBOUND_POLLING_MS=1500");
			    cfg.println(service + "_TESTSTUB_INBOUND_FILE_AGE_MS=100");				
		    }

		    // FTP properties
		    if (inboundTransport == FTP) {
		    	cfg.println("# URL for tests with embeddded FTP-server, replace with something like ${FTP_USERNAME}:${FTP_PASSWORD}@ftphost/~/path");
				cfg.println(service + "_INBOUND_FOLDER=" + ftpRootFolder + "/" + serviceName + "/inbound");
			    cfg.println(service + "_INBOUND_POLLING_MS=1000");
		    }
		    if (outboundTransport == FTP) {
		    	cfg.println("# URL for tests with embeddded FTP-server, replace with something like ${FTP_USERNAME}:${FTP_PASSWORD}@ftphost/~/path");
				cfg.println(service + "_OUTBOUND_FOLDER=${" + service + "_TESTSTUB_INBOUND_FOLDER}");
				cfg.println(service + "_TESTSTUB_INBOUND_FOLDER=" + ftpRootFolder + "/" + serviceName + "/outbound");
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
		    if (m.isOutboundEndpointFilebased()) {
			    
		    	// If we don't have a file based inbound endpoint (e.g. transport) we have to specify the name of the out-file ourself...
				if (!m.isInboundEndpointFilebased()) {
			    	cfg.println(service + "_OUTBOUND_FILE=#[function:datestamp:yyyyMMdd-HHmmss.SSS]_outfile.txt");
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

		    // SMTP properties
		    if (outboundTransport == SMTP) {
			    cfg.println(service + "_IMAP_TEST_USR=soitoolkit2%40yahoo.se");
			    cfg.println(service + "_IMAP_TEST_PWD=soitoolkit2pwd");

			    cfg.println(service + "_OUTBOUND_EMAIL_FROM=soitoolkit1@yahoo.se");
			    cfg.println(service + "_OUTBOUND_EMAIL_TEST_TO=soitoolkit2@yahoo.se");
			    cfg.println(service + "_OUTBOUND_EMAIL_SUBJECT=Outbound mail sent from Mule ESB");
		    }
		    
		    // POP3 properties
		    if (inboundTransport == POP3) {
				cfg.println(service + "_POP3_HOST=pop.mail.yahoo.com");
			    cfg.println(service + "_POP3_PORT=110");

			    printSecurityHeaderForService(cfg);
			    cfg.println(service + "_POP3_USR=soitoolkit1%40yahoo.se");
			    cfg.println(service + "_POP3_PWD=soitoolkit1pwd");
		    }
		    
		    // IMAP properties
		    if (inboundTransport == IMAP) {
		    	printSecurityHeaderForService(cfg);
			    cfg.println(service + "_IMAP_USR=soitoolkit1%40yahoo.se");
			    cfg.println(service + "_IMAP_PWD=soitoolkit1pwd");
		    }
		    
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (cfg != null) {cfg.close();}
		}
	}

	private void printSecurityHeaderForService(PrintWriter cfg) {
		// Print header for this service's properties if any security related properties are required
		cfg.println("");
		cfg.println("# Security related properties for service \"" + m.getService() + "\"");
		cfg.println("# TODO: Update to reflect your settings");
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
			String filename = outFolder + m.getArtifactId() + "-db-create-tables.sql";
			gu.logDebug("Appending to file: " + filename);
			out = openFileForAppend(filename);
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
			String filename = outFolder + m.getArtifactId() + "-db-drop-tables.sql";
			gu.logDebug("Appending to file: " + filename);
			out = openFileForAppend(filename);
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
			String filename = outFolder + m.getArtifactId() + "-db-insert-testdata.sql";
			gu.logDebug("Appending to file: " + filename);
			out = openFileForAppend(filename);

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null) {out.close();}
		}
    }

	private void updateJdbcConnectorFileWithExportSql() {
		String outFolder = gu.getOutputFolder() + "/src/main/app/";
		String key       = m.getLowercaseJavaService() + "-export-query";
		String table     = m.getUppercaseService() + "_EXPORT_TB";

		PrintWriter out = null;
		try {
			String file;
			if (m.isServiceXaTransactional()) {
				file = outFolder + m.getArtifactId() + "-jdbc-xa-connector.xml";
			} else {
				file = outFolder + m.getArtifactId() + "-jdbc-connector.xml";
			}
			addQueryToMuleJdbcConnector(file, "<jdbc:query xmlns:jdbc=\"" + NAMESPACE_JDBC + "\" key=\"" + key + "\" value=\"SELECT ID, VALUE FROM " + table + " ORDER BY ID\"/>");
			addQueryToMuleJdbcConnector(file, "<jdbc:query xmlns:jdbc=\"" + NAMESPACE_JDBC + "\" key=\"" + key + ".ack\" value=\"DELETE FROM " + table + " WHERE ID = #[map-payload:ID]\"/>");

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null) {out.close();}
		}
    }

	private void updateJdbcConnectorFileWithImportSql() {
		String outFolder    = gu.getOutputFolder() + "/src/main/app/";
		String key          = m.getLowercaseJavaService() + "-import-query";
		String key_teststub = m.getLowercaseJavaService() + "-teststub-export-query";
		String table        = m.getUppercaseService() + "_IMPORT_TB";

		PrintWriter out = null;
		try {
			String file = outFolder + m.getArtifactId() + "-jdbc-connector.xml";
			addQueryToMuleJdbcConnector(file, "<jdbc:query xmlns:jdbc=\"" + NAMESPACE_JDBC + "\" key=\"" + key_teststub + "\" value=\"SELECT ID, VALUE FROM " + table + " ORDER BY ID\"/>");
			addQueryToMuleJdbcConnector(file, "<jdbc:query xmlns:jdbc=\"" + NAMESPACE_JDBC + "\" key=\"" + key_teststub + ".ack\" value=\"DELETE FROM " + table + " WHERE ID = #[map-payload:ID]\"/>");

			if (m.isServiceXaTransactional()) {
				file = outFolder + m.getArtifactId() + "-jdbc-xa-connector.xml";
			}
			addQueryToMuleJdbcConnector(file, "<jdbc:query xmlns:jdbc=\"" + NAMESPACE_JDBC + "\" key=\"" + key          + "\" value=\"INSERT INTO  " + table + "(ID, VALUE) VALUES (#[map-payload:ID], #[map-payload:VALUE])\"/>");
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null) {out.close();}
		}
		
    }

	private void addQueryToMuleJdbcConnector(String file, String xmlFragment) {
		InputStream content = null;
		String xml = null;
		try {
			gu.logDebug("Add: " + xmlFragment + " to " + file);
			content = new FileInputStream(file);
			Document doc = createDocument(content);

			Map<String, String> namespaceMap = new HashMap<String, String>();
			namespaceMap.put("ns",   NAMESPACE_CORE);
			namespaceMap.put("jdbc", NAMESPACE_JDBC);

			NodeList rootList = getXPathResult(doc, namespaceMap, "/ns:mule/jdbc:connector");
			Node root = rootList.item(0);
			gu.logDebug("Root node: " + ((root == null) ? " NULL" : root.getLocalName()));		    
		    
		    appendXmlFragment(root, xmlFragment);
			
			xml = getXml(doc);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (content != null) {try {content.close();} catch (IOException e) {}}
		}

		PrintWriter pw = null;
		try {
			gu.logDebug("Overwrite file: " + file);
			pw = openFileForOverwrite(file);
			pw.print(xml);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (pw != null) {pw.close();}
		}
	
		gu.logInfo("Updated: " + file);
	}

}
