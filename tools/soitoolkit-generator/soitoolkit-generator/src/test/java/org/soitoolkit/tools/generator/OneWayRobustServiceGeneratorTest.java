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

import static org.junit.Assert.assertEquals;
import static org.soitoolkit.tools.generator.IntegrationComponentGeneratorTest.EXPECTED_NO_OF_IC_FILES_CREATED;
import static org.soitoolkit.tools.generator.model.enums.DeploymentModelEnum.STANDALONE_DEPLOY;
import static org.soitoolkit.tools.generator.model.enums.DeploymentModelEnum.WAR_DEPLOY;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.FILE;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.FTP;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.HTTP;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.JDBC;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.JMS;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.SERVLET;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.SFTP;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.VM;
import static org.soitoolkit.tools.generator.model.impl.ModelUtil.capitalize;
import static org.soitoolkit.tools.generator.util.MiscUtil.appendTransport;
import static org.soitoolkit.tools.generator.util.SystemUtil.BUILD_COMMAND;
import static org.soitoolkit.tools.generator.util.SystemUtil.CLEAN_COMMAND;
import static org.soitoolkit.tools.generator.util.SystemUtil.ECLIPSE_AND_TEST_REPORT_COMMAND;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.soitoolkit.tools.generator.model.IModel;
import org.soitoolkit.tools.generator.model.ModelFactory;
import org.soitoolkit.tools.generator.model.enums.DeploymentModelEnum;
import org.soitoolkit.tools.generator.model.enums.MuleVersionEnum;
import org.soitoolkit.tools.generator.model.enums.TransformerEnum;
import org.soitoolkit.tools.generator.model.enums.TransportEnum;
import org.soitoolkit.tools.generator.util.PreferencesUtil;
import org.soitoolkit.tools.generator.util.SystemUtil;

public class OneWayRobustServiceGeneratorTest {

	private static final List<TransportEnum> TRANSPORTS = new ArrayList<TransportEnum>();
	private static final String TEST_OUT_FOLDER = PreferencesUtil.getDefaultRootFolder() + "/jUnitTests";
	private static final String VERSION = "1.0-SNAPSHOT";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test all combinations of supported inbound and outbound endpoints with one common integration component.
	 *  but with two different component names to ensure that we don't have any hardcoded component names in the templates
	 * 
	 * @throws IOException
	 */
	@Test
	public void testOneWayRobustServicesInOneCommonIC() throws IOException {
		MuleVersionEnum[] muleVersions = MuleVersionEnum.values();
		
		for (int i = 0; i < muleVersions.length; i++) {
			if (!muleVersions[i].isEEVersion()) {
				doTestOneWayRobustServicesInOneCommonIC("org.soitoolkit.tool.generator", "onewayRobustSA-mule" + muleVersions[i].getVerNoNumbersOnly(), muleVersions[i], STANDALONE_DEPLOY);
			}
		}
	}
	
	/**
	 * Test all combinations of supported inbound and outbound endpoints with one common integration component but with another component names to ensure that we don't have any hardcoded component names in the templates
	 * 
	 * @throws IOException
	 */
	@Test
	public void testOneWayRobustServicesInOneCommonICWithOtherName() throws IOException {
		MuleVersionEnum[] muleVersions = MuleVersionEnum.values();
		
		for (int i = 0; i < muleVersions.length; i++) {
			if (!muleVersions[i].isEEVersion()) {
				doTestOneWayRobustServicesInOneCommonIC("org.soitoolkit.tool.generator-tests", "Oneway-Robust-Tests-SA-mule" + muleVersions[i].getVerNoNumbersOnly(), muleVersions[i], STANDALONE_DEPLOY);
			}
		}
	}

	/**
	 * Test all combinations of supported inbound and outbound endpoints with one integration component per service to ensure that each combination of in- and out-bound endpoints are self contained
	 * 
	 * @throws IOException
	 */
	@Test
	public void testOneWayRobustServicesOneICPerService() throws IOException {
		MuleVersionEnum[] muleVersions = MuleVersionEnum.values();
		
		for (int i = 0; i < muleVersions.length; i++) {
			if (!muleVersions[i].isEEVersion()) {
				doTestOneWayRobustServicesOneICPerService("org.soitoolkit.tool.generator", "onewayRobustSA-mule" + muleVersions[i].getVerNoNumbersOnly(), muleVersions[i], STANDALONE_DEPLOY);
			}
		}
	}

	private void doTestOneWayRobustServicesInOneCommonIC(String groupId, String artifactId, MuleVersionEnum muleVersion, DeploymentModelEnum deploymentModel) throws IOException {

		String projectFolder = TEST_OUT_FOLDER + "/" + artifactId;
		
		createEmptyIntegrationComponent(groupId, artifactId, muleVersion, deploymentModel, projectFolder);	

		for (TransportEnum inboundTransport : getInboundTransports(deploymentModel)) {
			for (TransportEnum outboundTransport : getOutboundTransports()) {
				createOneWayRobustService(groupId, artifactId, muleVersion, inboundTransport, outboundTransport, TransformerEnum.JAVA, projectFolder);
			}
		}

		performMavenBuild(projectFolder);
	}
	
	private void doTestOneWayRobustServicesOneICPerService(String groupId, String artifactId, MuleVersionEnum muleVersion, DeploymentModelEnum deploymentModel) throws IOException {

		String orgArtifactId = artifactId;
		
		for (TransportEnum inboundTransport : getInboundTransports(deploymentModel)) {
			for (TransportEnum outboundTransport : getOutboundTransports()) {
				artifactId = orgArtifactId + "_" + inboundTransport.name() + "_to_" + outboundTransport.name();
				String projectFolder = TEST_OUT_FOLDER + "/" + artifactId;
				createEmptyIntegrationComponent(groupId, artifactId, muleVersion, deploymentModel, projectFolder);	
				createOneWayRobustService(groupId, artifactId, muleVersion, inboundTransport, outboundTransport, TransformerEnum.JAVA, projectFolder);
				performMavenBuild(projectFolder);
			}
		}
	}

	private TransportEnum[] getInboundTransports(DeploymentModelEnum deploymentModel) {
		TransportEnum[] inboundTransports  = {FILE, FTP, VM};
		if (deploymentModel == WAR_DEPLOY) {
			inboundTransports = appendTransport(inboundTransports, SERVLET);
		}
		return inboundTransports;
	}

	private TransportEnum[] getOutboundTransports() {
		TransportEnum[] outboundTransports = {FILE, FTP, VM};
		return outboundTransports;
	}

	private void createEmptyIntegrationComponent(String groupId, String artifactId, MuleVersionEnum muleVersion, DeploymentModelEnum deploymentModel, String projectFolder) throws IOException {
		
		int noOfExpectedFiles = EXPECTED_NO_OF_IC_FILES_CREATED;
		
		TRANSPORTS.add(JMS);
		TRANSPORTS.add(FILE);
		TRANSPORTS.add(FTP);
		TRANSPORTS.add(SFTP);
		if (deploymentModel == WAR_DEPLOY) {
			TRANSPORTS.add(SERVLET);
		}

		SystemUtil.delDirs(projectFolder);
		assertEquals(0, SystemUtil.countFiles(projectFolder));
		new IntegrationComponentGenerator(System.out, groupId, artifactId, VERSION, muleVersion, deploymentModel, TRANSPORTS, TEST_OUT_FOLDER).startGenerator();
		assertEquals("Missmatch in expected number of created files and folders", noOfExpectedFiles, SystemUtil.countFiles(projectFolder));
	}

	private void createOneWayRobustService(String groupId, String artifactId, MuleVersionEnum muleVersion, TransportEnum inboundTransport, TransportEnum outboundTransport, TransformerEnum transformerType, String projectFolder) throws IOException {

		String service = inboundTransport.name().toLowerCase() + "To" + capitalize(outboundTransport.name().toLowerCase());

		int noOfFilesBefore = SystemUtil.countFiles(projectFolder);

		int expectedNoOfFiles = 27;
		
		IModel model = ModelFactory.newModel(groupId, artifactId, VERSION, service, muleVersion, inboundTransport, outboundTransport, transformerType);

		// Add one expected file is service is xa-transacted and the jdbc-trnsport is involved and the jdbc-xa-connector.xml file is not yet created....
		if (model.isServiceXaTransactional() && (inboundTransport == JDBC || outboundTransport == JDBC)) {
    	    if (!new File(projectFolder + "/src/main/app/" + artifactId + "-jdbc-xa-connector.xml").exists()) {
    	    	expectedNoOfFiles++;
    	    }
		}

		if (inboundTransport == HTTP || inboundTransport == SERVLET) {
			expectedNoOfFiles++;
		}
		if (inboundTransport == JDBC) {
			expectedNoOfFiles++; // from-db-transformer
		}
		if (outboundTransport == JDBC) {
			expectedNoOfFiles++; // to-db-transformer
		}
//		TODO: Wait with attachments... 	    
//		if (inboundTransport == POP3 || inboundTransport == IMAP) {
//			expectedNoOfFiles += 2; // png + pdf attachment
//		}
		
		new OnewayRobustServiceGenerator(System.out, groupId, artifactId, service, muleVersion, inboundTransport, outboundTransport, transformerType, projectFolder).startGenerator();
		
		assertEquals("Missmatch in expected number of created files and folders", expectedNoOfFiles, SystemUtil.countFiles(projectFolder) - noOfFilesBefore);
	}

	private void performMavenBuild(String projectFolder) throws IOException {

		boolean testOk = false;
		
		try {
			SystemUtil.executeCommand(BUILD_COMMAND, projectFolder);
			testOk = true;
		} finally {
			// Always try to create eclipsefiles and test reports 
			SystemUtil.executeCommand(ECLIPSE_AND_TEST_REPORT_COMMAND, projectFolder);
		}
		
		// If the build runs fine then also perform a clean-command to save GB's of diskspace...
		if (testOk) SystemUtil.executeCommand(CLEAN_COMMAND, projectFolder);
	}

}