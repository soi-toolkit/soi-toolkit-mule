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

import static org.soitoolkit.tools.generator.model.enums.TransportEnum.FILE;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.FTP;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.HTTP;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.HTTPS;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.JDBC;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.JMS;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.SFTP;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.VM;
import static org.soitoolkit.tools.generator.util.FileUtil.openFileForAppend;
import static org.soitoolkit.tools.generator.util.PropertyFileUtil.openPropertyFileForAppend;
import static org.soitoolkit.tools.generator.util.PropertyFileUtil.updateMuleDeployPropertyFileWithNewService;
import static org.soitoolkit.tools.generator.util.XmlFileUtil.updateCommonFileWithSpringImport;
import static org.soitoolkit.tools.generator.util.XmlFileUtil.updateSpringImportInXmlFile;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;

import org.soitoolkit.tools.generator.model.IModel;
import org.soitoolkit.tools.generator.model.enums.MuleVersionEnum;
import org.soitoolkit.tools.generator.model.enums.TransformerEnum;
import org.soitoolkit.tools.generator.model.enums.TransportEnum;
import org.soitoolkit.tools.generator.util.PreferencesUtil;
import org.soitoolkit.tools.generator.util.SourceFormatterUtil;

/**
 * One-Way service generator.
 */
public class OnewayServiceV2Generator implements Generator {

	GeneratorUtil gu;
	IModel m;
	
	public OnewayServiceV2Generator(PrintStream ps, String groupId, String artifactId, String serviceName, MuleVersionEnum muleVersion, TransportEnum inboundTransport, TransportEnum outboundTransport, TransformerEnum transformerType, String outputFolder) {
		gu = new GeneratorUtil(ps, groupId, artifactId, null, serviceName, muleVersion, inboundTransport, outboundTransport, transformerType, "/oneWayServiceV2", outputFolder);
		m = gu.getModel();
	}
		
	public void startGenerator() {
		
		gu.logInfo("Creates a \"one-way\"-service, inbound transport: " + m.getInboundTransport() + ", outbound transport: " + m.getOutboundTransport() + ", type of transformer: " + m.getTransformerType());
		
		TransportEnum inboundTransport  = TransportEnum.valueOf(m.getInboundTransport());
		TransportEnum outboundTransport = TransportEnum.valueOf(m.getOutboundTransport());
		TransformerEnum transformerType = TransformerEnum.valueOf(m.getTransformerType());
		
		//gu.generateContentAndCreateFile("src/main/app/__service__/__service__-service.xml.gt");
		gu.generateContentAndCreateFile("src/main/app/__lowercaseJavaService__/__service__-inbound-service.xml.gt");
		gu.generateContentAndCreateFile("src/main/app/__lowercaseJavaService__/__service__-process-service.xml.gt");
		if (outboundTransport != TransportEnum.JMS) {
			gu.generateContentAndCreateFile("src/main/app/__lowercaseJavaService__/__service__-outbound-service.xml.gt");
		}
		
		if (transformerType == TransformerEnum.JAVA) {
			gu.generateContentAndCreateFile("src/main/java/__javaPackageFilepath__/__lowercaseJavaService__/transformer/__capitalizedJavaService__Transformer.java.gt");
			gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/transformer/__capitalizedJavaService__TransformerTest.java.gt");
			gu.generateContentAndCreateFile("src/test/resources/testfiles/__lowercaseJavaService__/transformer/input-ok.txt.gt");
			gu.generateContentAndCreateFile("src/test/resources/testfiles/__lowercaseJavaService__/transformer/input-error.txt.gt");
			gu.generateContentAndCreateFile("src/test/resources/testfiles/__lowercaseJavaService__/transformer/expected-result-ok.txt.gt");
			
			gu.generateContentAndCreateFile("src/test/resources/testfiles/__lowercaseJavaService__/integrationtests/input-ok.txt.gt");
			gu.generateContentAndCreateFile("src/test/resources/testfiles/__lowercaseJavaService__/integrationtests/input-error.txt.gt");
			gu.generateContentAndCreateFile("src/test/resources/testfiles/__lowercaseJavaService__/integrationtests/expected-result-ok.txt.gt");
		}
		else {
			throw new IllegalArgumentException("Transformer type not supported for this kind of flow: " + transformerType);
		}
		
		
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__IntegrationTest.java.gt");
		if (outboundTransport == SFTP || outboundTransport == FTP || outboundTransport == HTTP || outboundTransport == HTTPS || outboundTransport == JDBC) {
			gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/outbound/__capitalizedJavaService__OutboundIntegrationTest.java.gt");
		}
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__TestReceiver.java.gt");
		gu.generateContentAndCreateFile("src/test/resources/teststub-services/__service__-teststub-service.xml.gt");

		// Update mule-deploy.properties
		updateMuleDeployPropertyFileWithNewService(gu.getOutputFolder(), m.getLowercaseJavaService() + "/" + m.getService() + "-inbound");
		updateMuleDeployPropertyFileWithNewService(gu.getOutputFolder(), m.getLowercaseJavaService() + "/" + m.getService() + "-process");
		
		if (outboundTransport != TransportEnum.JMS) {
			updateMuleDeployPropertyFileWithNewService(gu.getOutputFolder(), m.getLowercaseJavaService() + "/" + m.getService() + "-outbound");
		}
		
		// Update mule-standalone-with-teststub-config.xml
		updateSpringImportInXmlFile(gu, gu.getOutputFolder() + "/src/test/resources/mule-standalone-with-teststubs-config.xml", "", m.getLowercaseJavaService() + "/" + m.getService() + "-inbound-service.xml", null);
		updateSpringImportInXmlFile(gu, gu.getOutputFolder() + "/src/test/resources/mule-standalone-with-teststubs-config.xml", "", m.getLowercaseJavaService() + "/" + m.getService() + "-process-service.xml", null);
		
		if (outboundTransport != TransportEnum.JMS) {
			updateSpringImportInXmlFile(gu, gu.getOutputFolder() + "/src/test/resources/mule-standalone-with-teststubs-config.xml", "", m.getLowercaseJavaService() + "/" + m.getService() + "-outbound-service.xml", null);
		}

		updatePropertyFiles(inboundTransport, outboundTransport);
		
		// Add vm-connector to common file (one and the same for junit-tests and running mule server) if vm-transport is used for the first time
		if (inboundTransport == VM || outboundTransport == VM) {
			String comment = "Added " + new Date() + " since flow " + m.getService() + " uses the VM-transport";
    		updateCommonFileWithSpringImport(gu, comment, "soitoolkit-mule-vm-connector.xml");
		}
		
		// Add http-connector to common file (one and the same for junit-tests and running mule server) if http-transport is used for the first time
		if (inboundTransport == HTTP || outboundTransport == HTTP) {
			String comment = "Added " + new Date() + " since flow " + m.getService() + " uses the HTTP-transport";
    		updateCommonFileWithSpringImport(gu, comment, "soitoolkit-mule-http-connector.xml");
		}
		
		// Add http-connector to common file (one and the same for junit-tests and running mule server) if http-transport is used for the first time
		if (inboundTransport == TransportEnum.HTTPS || outboundTransport == HTTPS) {
			
			// Add a https-connector if not already existing + properties for it. 
			// Also add a http connector for the case where a developer wants to temporary degrade https to plain http, e.g. for troubleshooting
			String comment = "Added " + new Date() + " since flow " + m.getService() + " uses the HTTPS-transport";
    		boolean isUpdated = updateCommonFileWithSpringImport(gu, comment, "soitoolkit-mule-https-connector.xml");
    		
    		if (isUpdated) {
    			gu.copyContentAndCreateFile("src/test/certs/client.jks.gt");
    	    	gu.copyContentAndCreateFile("src/test/certs/truststore.jks.gt");
    	    	gu.copyContentAndCreateFile("src/test/certs/server.jks.gt");
    	    	gu.copyContentAndCreateFile("src/test/certs/readme.txt.gt");
    		
    	    	String httpsProperties =
    	    		"# Properties for the default soitoolkit-https-connector's\n" +
    	    		"# TODO: Update to reflect your settings\n" +
    	    		"SOITOOLKIT_HTTPS_CLIENT_SO_TIMEOUT=${SERVICE_TIMEOUT_MS}\n" +
    	    		"SOITOOLKIT_HTTPS_TLS_KEYSTORE=src/test/certs/server.jks\n" +
    	    		"SOITOOLKIT_HTTPS_TLS_KEY_TYPE=jks\n" +
    	    		"SOITOOLKIT_HTTPS_TLS_TRUSTSTORE=src/test/certs/truststore.jks\n" +
    	    		"SOITOOLKIT_HTTPS_TLS_TRUSTSTORE_REQUIRE_CLIENT_AUTH=true\n" +

    	    		"SOITOOLKIT_HTTPS_TLS_KEYSTORE_PASSWORD=password\n" +
    	    		"SOITOOLKIT_HTTPS_TLS_KEY_PASSWORD=password\n" +
    	    		"SOITOOLKIT_HTTPS_TLS_TRUSTSTORE_PASSWORD=password\n";
    	    	
    	    	addProperties(httpsProperties);
    		}
		}
		
		{
			String comment = "Added " + new Date() + " since flow " + m.getService() + " uses the JMS-transport\n";
			comment += "Import the JMS-provider used in production here, embedded JMS providers used for integration tests are loaded by the *IntegratIonTest.java classes directly";
    		updateCommonFileWithSpringImport(gu, comment, "soitoolkit-mule-jms-connector-activemq-external.xml", "default");
		}
		// Add file-connector
		if (inboundTransport == FILE || outboundTransport == FILE) {
			String comment = "Added " + new Date() + " since flow " + m.getService() + " uses the FILE-transport";
    		updateCommonFileWithSpringImport(gu, comment, "soitoolkit-mule-file-connector.xml");
		}
		// Add ftp-connector
		if (inboundTransport == FTP || outboundTransport == FTP) {
			String comment = "Added " + new Date() + " since flow " + m.getService() + " uses the FTP-transport";
			boolean isUpdated = updateCommonFileWithSpringImport(gu, comment, "soitoolkit-mule-ftp-connector-external.xml", "default");
    		
			if (isUpdated) {
				
				String defaultFtpUsername 			= m.getDefaultFtpUsername();
				String defaultFtpPassword     		= m.getDefaultFtpPassword();
				
				String ftpProperties = "# Properties for the ftp-transport\n"
						+ "# TODO: Update to reflect your settings\n"
						+ "SOITOOLKIT_FTP_USERNAME="+ defaultFtpUsername + "\n"
						+ "SOITOOLKIT_FTP_PASSWORD="+ defaultFtpPassword + "\n";

				addProperties(ftpProperties);
			}
		}
		// Add sftp-connector
		if (inboundTransport == SFTP || outboundTransport == SFTP) {
			String comment = "Added " + new Date() + " since flow " + m.getService() + " uses the SFTP-transport";
			boolean isUpdated = updateCommonFileWithSpringImport(gu, comment, "soitoolkit-mule-sftp-connector-external.xml", "default");
			if (isUpdated) {
				
				String defaultSftpUsername 				= m.getDefaultSftpUsername();
				String defaultSftpIdentityFile     		= m.getDefaultSftpIdentityFile();
				String defaultSftpIdentityPassphrase    = m.getDefaultSftpIdentityPassphrase();
				
				String sftpProperties =
    	    		"# Properties for the default soitoolkit-sftp-connector\n" +
    	    		"# Values below valid for embedded sftp-server - do not change\n" +
    	    		"# TODO: Update to reflect your settings in an override-properties file\n" +
    	    		"SOITOOLKIT_SFTP_USERNAME="+ defaultSftpUsername + "\n" +
    	    		"SOITOOLKIT_SFTP_IDENTITYFILE="+ defaultSftpIdentityFile + "\n" + 
    	    		"SOITOOLKIT_SFTP_IDENTITYFILE_PASSPHRASE="+ defaultSftpIdentityPassphrase + "\n";
    	    	
    	    	addProperties(sftpProperties);
			}
		}
		// Add db-module
		if (inboundTransport == JDBC || outboundTransport == JDBC) {
			String comment = "Added " + new Date() + " since flow " + m.getService() + " uses the db-module";
			
			boolean isUpdated = updateCommonFileWithSpringImport(gu, comment, m.getArtifactId() + "-jdbc-common.xml");
    		
			if (isUpdated) {
				gu.generateContentAndCreateFile("src/main/app/__artifactId__-jdbc-common.xml.gt");
				
				String artifactId     = m.getArtifactId();
				
				String jdbcProperties =
					"SOITOOLKIT_JDBC_USR=APP\n" +
					"SOITOOLKIT_JDBC_PWD=pwd\n\n" +
					"# Properties for the generic soitoolkit-mule-jdbc-datasource.xml.\n" +
					"# TODO: Update to reflect your settings\n" +
					"# For Derby:\n" +
					"# (see soitoolkit-mule-jdbc-datasource.xml for how to setup other databases, e.g. MySQL, SQL Server or Oracle)\n" +
					"# (see soitoolkit-mule-jdbc-xa-datasource-derby-external.xml for example of setup of XA DataSources)\n" +
					"SOITOOLKIT_JDBC_DATABASE="+artifactId+"Db\n" +
					"SOITOOLKIT_JDBC_HOST=localhost\n" +
					"SOITOOLKIT_JDBC_PORT=1527\n" +
					"SOITOOLKIT_JDBC_DRIVER=org.apache.derby.jdbc.ClientDriver\n" +
					"SOITOOLKIT_JDBC_URL=jdbc:derby://${SOITOOLKIT_JDBC_HOST}:${SOITOOLKIT_JDBC_PORT}/${SOITOOLKIT_JDBC_DATABASE};create=true\n" +

					"SOITOOLKIT_JDBC_CON_POLLING_MS=1000\n" +
					"SOITOOLKIT_JDBC_CON_POOL_INIT_SIZE=1\n" +
					"SOITOOLKIT_JDBC_CON_POOL_MAX_ACTIVE=10\n" +
					"SOITOOLKIT_JDBC_CON_POOL_MAX_IDLE=10\n";
	    	    	
	    	    addProperties(jdbcProperties);
			}
			
			comment = "Added " + new Date() + " since flows uses the JDBC";
			isUpdated = updateCommonFileWithSpringImport(gu, comment, "soitoolkit-mule-jdbc-xa-datasource-hsql-embedded.xml", "soitoolkit-integrationtests");
			if (isUpdated) {
				
			}
			
			comment = "Added " + new Date() + " since flows uses the JDBC\n";
			comment += "Import the JDBC datasource used in production here, embedded JDBC datasources used for integration tests are loaded by the *IntegratIonTest.java classes directly";
			isUpdated = updateCommonFileWithSpringImport(gu, comment, "soitoolkit-mule-jdbc-datasource.xml", "default");
			if (isUpdated) {
				
			}
			
			if (inboundTransport == JDBC) {
		    	updateSqlDdlFilesAddExportTable();
		    }
			
			if (outboundTransport == JDBC) {
		    	updateSqlDdlFilesAddImportTable();
		    }
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
			    
			    cfg.println(service + "_PROC_QUEUE=" + service + ".PROC.QUEUE");
			    cfg.println(service + "_PROC_DL_QUEUE=DLQ." + service + ".PROC.QUEUE");
			    cfg.println(service + "_OUT_QUEUE=" + service + ".OUT.QUEUE");
			    cfg.println(service + "_OUT_DL_QUEUE=DLQ." + service + ".OUT.QUEUE");
		    }
		    
		    if (outboundTransport == VM) {
			    cfg.println(service + "_OUT_VM_QUEUE=" + m.getJmsOutQueue());
		    }

		    // JMS properties
		    if (inboundTransport == JMS) {
			    cfg.println(service + "_IN_QUEUE="  + m.getJmsInQueue());
		    	cfg.println(service + "_PROC_QUEUE=" + service + ".PROC.QUEUE");
			    cfg.println(service + "_PROC_DL_QUEUE=DLQ." + service + ".PROC.QUEUE");
			    cfg.println(service + "_OUT_QUEUE=" + service + ".OUT.QUEUE");
			    cfg.println(service + "_OUT_DL_QUEUE=DLQ." + service + ".OUT.QUEUE");
		    }
		    
		    // HTTP properties
		    if (inboundTransport == HTTP) {
			    cfg.println(service + "_INBOUND_URL=http://0.0.0.0:" + m.getHttpPort() + "/" + artifactId + "/services/" + serviceName + "/inbound");
			    
			    cfg.println(service + "_PROC_QUEUE=" + service + ".PROC.QUEUE");
			    cfg.println(service + "_PROC_DL_QUEUE=DLQ." + service + ".PROC.QUEUE");
			    cfg.println(service + "_OUT_QUEUE=" + service + ".OUT.QUEUE");
			    cfg.println(service + "_OUT_DL_QUEUE=DLQ." + service + ".OUT.QUEUE");
		    }
		    
		    if (outboundTransport == HTTP) {
			    cfg.println(service + "_OUTBOUND_URL=${"+ service + "_TESTSTUB_INBOUND_URL}");
			    cfg.println(service + "_TESTSTUB_INBOUND_URL=http://0.0.0.0:" + m.getHttpTeststubPort() + "/" + artifactId + "/services/" + serviceName + "/inbound");
		    }
		    
		    // HTTPS properties
		    if (inboundTransport == HTTPS) {
			    cfg.println(service + "_INBOUND_URL=https://0.0.0.0:" + m.getHttpPort() + "/" + artifactId + "/services/" + serviceName + "/inbound");
			    
			    cfg.println(service + "_PROC_QUEUE=" + service + ".PROC.QUEUE");
			    cfg.println(service + "_PROC_DL_QUEUE=DLQ." + service + ".PROC.QUEUE");
			    cfg.println(service + "_OUT_QUEUE=" + service + ".OUT.QUEUE");
			    cfg.println(service + "_OUT_DL_QUEUE=DLQ." + service + ".OUT.QUEUE");
		    }
		    
		    if (outboundTransport == HTTPS) {
			    cfg.println(service + "_OUTBOUND_URL=${"+ service + "_TESTSTUB_INBOUND_URL}");
			    cfg.println(service + "_TESTSTUB_INBOUND_URL=https://0.0.0.0:" + m.getHttpTeststubPort() + "/" + artifactId + "/services/" + serviceName + "/inbound");
		    }
		    
		    // File properties
		    if (inboundTransport == FILE) {
				cfg.println(service + "_INBOUND_FOLDER=" + fileRootFolder + "/" + serviceName + "/inbound");
				if (m.getMuleVersion().getPomSuffix().startsWith("3.4")) {
					cfg.println(service + "_INBOUND_ARCHIVE_FILENAME=#[header:outbound:originalFilename]");
				} else {
					cfg.println(service + "_INBOUND_ARCHIVE_FILENAME=#[header:inbound:originalFilename]");
				}
				cfg.println(service + "_INBOUND_ARCHIVE_FOLDER=" + archiveFolder + "/" + serviceName);
				cfg.println(service + "_INBOUND_POLLING_MS=1500");
			    cfg.println(service + "_INBOUND_FILE_AGE_MS=100");
			    cfg.println(service + "_INBOUND_FILE_PATTERN=*.*");
			    
			    cfg.println(service + "_PROC_QUEUE=" + service + ".PROC.QUEUE");
			    cfg.println(service + "_PROC_DL_QUEUE=DLQ." + service + ".PROC.QUEUE");
			    cfg.println(service + "_OUT_QUEUE=" + service + ".OUT.QUEUE");
			    cfg.println(service + "_OUT_DL_QUEUE=DLQ." + service + ".OUT.QUEUE");
		    }
		    if (outboundTransport == FILE) {
				cfg.println(service + "_OUTBOUND_FOLDER=${" + service + "_TESTSTUB_INBOUND_FOLDER}");
				cfg.println(service + "_TESTSTUB_INBOUND_FOLDER=" + fileRootFolder + "/" + serviceName + "/outbound");
			    cfg.println(service + "_TESTSTUB_INBOUND_POLLING_MS=2000");
			    cfg.println(service + "_TESTSTUB_INBOUND_FILE_AGE_MS=100");	
			    
		    }
		    
		    // FTP properties
		    if (inboundTransport == FTP) {
		    	cfg.println("# URL for tests with embeddded FTP-server, replace with something like ${FTP_USERNAME}:${FTP_PASSWORD}@ftphost/~/path");
				cfg.println(service + "_INBOUND_FOLDER=" + ftpRootFolder + "/" + serviceName + "/inbound");
			    cfg.println(service + "_INBOUND_POLLING_MS=1000");
			    cfg.println(service + "_INBOUND_FILE_PATTERN=*.*");
			    
			    cfg.println(service + "_PROC_QUEUE=" + service + ".PROC.QUEUE");
			    cfg.println(service + "_PROC_DL_QUEUE=DLQ." + service + ".PROC.QUEUE");
			    cfg.println(service + "_OUT_QUEUE=" + service + ".OUT.QUEUE");
			    cfg.println(service + "_OUT_DL_QUEUE=DLQ." + service + ".OUT.QUEUE");
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
				cfg.println(service + "_INBOUND_SFTP_ARCHIVE_FOLDER=" + archiveFolder + "/" + serviceName);
			    cfg.println(service + "_INBOUND_SFTP_POLLING_MS=1000");
			    cfg.println(service + "_INBOUND_SFTP_SIZECHECK_MS=500");
			    cfg.println(service + "_INBOUND_SFTP_FILE_PATTERN=*.*");
			    
			    cfg.println(service + "_PROC_QUEUE=" + service + ".PROC.QUEUE");
			    cfg.println(service + "_PROC_DL_QUEUE=DLQ." + service + ".PROC.QUEUE");
			    cfg.println(service + "_OUT_QUEUE=" + service + ".OUT.QUEUE");
			    cfg.println(service + "_OUT_DL_QUEUE=DLQ." + service + ".OUT.QUEUE");
		    }
		    if (outboundTransport == SFTP) {
			    cfg.println(service + "_OUTBOUND_SFTP_FOLDER=${" + service + "_TESTSTUB_INBOUND_SFTP_FOLDER}");
			    cfg.println(service + "_TESTSTUB_INBOUND_SFTP_FOLDER=" + sftpRootFolder + "/" + serviceName + "/outbound");
			    cfg.println(service + "_TESTSTUB_INBOUND_SFTP_POLLING_MS=1000");
			    cfg.println(service + "_TESTSTUB_INBOUND_SFTP_SIZECHECK_MS=500");
		    }
		    
		    // JDBC properties
		    if (inboundTransport == JDBC) {
		    	cfg.println(service + "_PROC_QUEUE=" + service + ".PROC.QUEUE");
			    cfg.println(service + "_PROC_DL_QUEUE=DLQ." + service + ".PROC.QUEUE");
			    cfg.println(service + "_OUT_QUEUE=" + service + ".OUT.QUEUE");
			    cfg.println(service + "_OUT_DL_QUEUE=DLQ." + service + ".OUT.QUEUE");
		    }
		    
		    // Properties common to all filebased transports
		    if (m.isInboundEndpointFilebased() || m.isOutboundEndpointFilebased()) {
		    	//cfg.println(service + "_ARCHIVE_FOLDER=" + archiveFolder + "/" + serviceName);
		    }
		    if (m.isOutboundEndpointFilebased()) {
			    //cfg.println(service + "_ARCHIVE_RESEND_POLLING_MS=1000");

		    	// If we don't have a file based inbound endpoint (e.g. transport) we have to specify the name of the out-file ourself...
				if (!m.isInboundEndpointFilebased()) {
					cfg.println(service + "_OUTBOUND_FILE=#[function:datestamp:yyyyMMdd-HHmmss.SSS]_outfile.txt");
			    }
		    }
		    
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (cfg != null) {cfg.close();}
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

    private void addProperties(String properties) {
    	properties = SourceFormatterUtil.formatSource(properties);
    	PrintWriter cfg = null;
		try {
			cfg = openPropertyFileForAppend(gu.getOutputFolder(), m.getConfigPropertyFile());
			cfg.println("");
			cfg.println(properties);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (cfg != null) {cfg.close();}
		}
    }
}
