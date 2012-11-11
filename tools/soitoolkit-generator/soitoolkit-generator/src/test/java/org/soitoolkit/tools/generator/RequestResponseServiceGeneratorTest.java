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
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.JMS;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.RESTHTTP;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.RESTHTTPS;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.SERVLET;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.SOAPHTTP;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.SOAPHTTPS;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.SOAPSERVLET;
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
import org.soitoolkit.tools.generator.model.enums.DeploymentModelEnum;
import org.soitoolkit.tools.generator.model.enums.MuleVersionEnum;
import org.soitoolkit.tools.generator.model.enums.TransformerEnum;
import org.soitoolkit.tools.generator.model.enums.TransportEnum;
import org.soitoolkit.tools.generator.util.PreferencesUtil;
import org.soitoolkit.tools.generator.util.SystemUtil;

public class RequestResponseServiceGeneratorTest {

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
	public void testRequestResponseServicesInOneCommonIC() throws IOException {
		MuleVersionEnum[] muleVersions = MuleVersionEnum.values();
		
		for (int i = 0; i < muleVersions.length; i++) {
			doTestRequestResponseServicesInOneCommonIC("org.soitoolkit.tool.generator",       "requestResponseSA-mule" +         muleVersions[i].getVerNoNumbersOnly(), muleVersions[i], STANDALONE_DEPLOY);
		}
	}

	/**
	 * Test all combinations of supported inbound and outbound endpoints with one common integration component but with another component names to ensure that we don't have any hardcoded component names in the templates
	 * 
	 * @throws IOException
	 */
	@Test
	public void testRequestResponseServicesInOneCommonICWithOtherName() throws IOException {
		MuleVersionEnum[] muleVersions = MuleVersionEnum.values();
		
		for (int i = 0; i < muleVersions.length; i++) {

			doTestRequestResponseServicesInOneCommonIC("org.soitoolkit.tool.generator-tests", "Request-Response-SA-Tests-mule" + muleVersions[i].getVerNoNumbersOnly(), muleVersions[i], STANDALONE_DEPLOY);
		}
	}

	/**
	 * Test all combinations of supported inbound and outbound endpoints with one integration component per service to ensure that each combination of in- and out-bound endpoints are self contained
	 * 
	 * @throws IOException
	 */
	@Test
	public void testRequestResponseServicesOneICPerService() throws IOException {
		MuleVersionEnum[] muleVersions = MuleVersionEnum.values();
		
		for (int i = 0; i < muleVersions.length; i++) {
			doTestRequestResponseServicesOneICPerService("org.soitoolkit.tool.generator",     "requestResponseSA-mule" +        muleVersions[i].getVerNoNumbersOnly(), muleVersions[i], STANDALONE_DEPLOY);
		}
	}

//	/**
//	 * Also test with WAR deploy
//	 * 
//	 * @throws IOException
//	 */
//	@Test
//	public void testRequestResponseServicesInWarDeploy() throws IOException {
//		MuleVersionEnum[] muleVersions = MuleVersionEnum.values();
//		
//		for (int i = 0; i < muleVersions.length; i++) {
//			doTestRequestResponseServicesInOneCommonIC("org.soitoolkit.tool.generator",       "requestResponseWD-mule" +         muleVersions[i].getVerNoNumbersOnly(), muleVersions[i], WAR_DEPLOY);
//			doTestRequestResponseServicesInOneCommonIC("org.soitoolkit.tool.generator-tests", "Request-Response-WD-Tests-mule" + muleVersions[i].getVerNoNumbersOnly(), muleVersions[i], WAR_DEPLOY);
//		}
//	}

	private void doTestRequestResponseServicesInOneCommonIC(String groupId, String artifactId, MuleVersionEnum muleVersion, DeploymentModelEnum deploymentModel) throws IOException {

		String projectFolder = TEST_OUT_FOLDER + "/" + artifactId;

		createEmptyIntegrationComponent(groupId, artifactId, muleVersion, deploymentModel, projectFolder);	

		for (TransportEnum inboundTransport : getInboundTransports(deploymentModel)) {
			for (TransportEnum outboundTransport : getOutboundTransports()) {
				createRequestResponseService(groupId, artifactId, muleVersion, inboundTransport, outboundTransport, TransformerEnum.JAVA, projectFolder);
//				createRequestResponseService(groupId, artifactId, muleVersion, inboundTransport, outboundTransport, TransformerEnum.SMOOKS);
			}
		}

		performMavenBuild(projectFolder);
	}

	private void doTestRequestResponseServicesOneICPerService(String groupId, String artifactId, MuleVersionEnum muleVersion, DeploymentModelEnum deploymentModel) throws IOException {

		String orgArtifactId = artifactId;
		
		for (TransportEnum inboundTransport : getInboundTransports(deploymentModel)) {
			for (TransportEnum outboundTransport : getOutboundTransports()) {
				artifactId = orgArtifactId + "_" + inboundTransport.name() + "_to_" + outboundTransport.name();
				String projectFolder = TEST_OUT_FOLDER + "/" + artifactId;
				createEmptyIntegrationComponent(groupId, artifactId, muleVersion, deploymentModel, projectFolder);	
				createRequestResponseService(groupId, artifactId, muleVersion, inboundTransport, outboundTransport, TransformerEnum.JAVA, projectFolder);
				performMavenBuild(projectFolder);
			}
		}
	}
	private TransportEnum[] getInboundTransports(DeploymentModelEnum deploymentModel) {
		
//		TransportEnum[] inboundTransports  = {SOAPHTTP, SOAPHTTPS, RESTHTTP, RESTHTTPS};
		TransportEnum[] inboundTransports  = {SOAPHTTP, RESTHTTP};
		
		if (deploymentModel == WAR_DEPLOY) {
			inboundTransports = appendTransport(inboundTransports, SOAPSERVLET);
		}
		return inboundTransports;
	}

	private TransportEnum[] getOutboundTransports() {
//		TransportEnum[] outboundTransports = {SOAPHTTP, SOAPHTTPS, RESTHTTP, RESTHTTPS, JMS};
		TransportEnum[] outboundTransports = {SOAPHTTP, RESTHTTP, JMS};

		return outboundTransports;
	}
	private void createEmptyIntegrationComponent(String groupId, String artifactId, MuleVersionEnum muleVersion, DeploymentModelEnum deploymentModel, String projectFolder) throws IOException {
		
		int noOfExpectedFiles = (deploymentModel == STANDALONE_DEPLOY) ? EXPECTED_NO_OF_IC_FILES_CREATED : 66;

		TRANSPORTS.add(JMS);
		TRANSPORTS.add(SOAPHTTP);
		if (deploymentModel == WAR_DEPLOY) {
			TRANSPORTS.add(SERVLET);
		}

		SystemUtil.delDirs(projectFolder);
		assertEquals(0, SystemUtil.countFiles(projectFolder));
		new IntegrationComponentGenerator(System.out, groupId, artifactId, VERSION, muleVersion, deploymentModel, TRANSPORTS, TEST_OUT_FOLDER).startGenerator();
		assertEquals("Missmatch in expected number of created files and folders", noOfExpectedFiles, SystemUtil.countFiles(projectFolder));
	}

	private void createRequestResponseService(String groupId, String artifactId, MuleVersionEnum muleVersion, TransportEnum inboundTransport, TransportEnum outboundTransport, TransformerEnum transformerType, String projectFolder) throws IOException {

		String service = inboundTransport.name().toLowerCase() + "To" + capitalize(outboundTransport.name().toLowerCase() + "Using" + capitalize(transformerType.name().toLowerCase()));

		int noOfFilesBefore = SystemUtil.countFiles(projectFolder);

		// SSL 
		File cxf = new File(projectFolder + "/src/test/resources/cxf-test-consumer-config.xml");
		File certDir = new File(projectFolder + "/src/test/certs");
		
		int expectedNoSslFiles = 0;
		if (!cxf.exists() && inboundTransport == SOAPHTTPS) {
			// cxf.xml is expected to be generated...
			expectedNoSslFiles += 1;
		}
	
		if (!certDir.exists() && (inboundTransport == SOAPHTTPS || outboundTransport == SOAPHTTPS || inboundTransport == RESTHTTPS || outboundTransport == RESTHTTPS)) {
			// certs is expected to be generated...
			expectedNoSslFiles += 5;
		}
		
		// END SSL
		
		int expectedNoOfFiles = (outboundTransport == JMS) ? 19 : 17;
		
//		IModel model = ModelFactory.newModel(groupId, artifactId, VERSION, service, null, null, null);
		new RequestResponseServiceGenerator(System.out, groupId, artifactId, service, muleVersion, inboundTransport, outboundTransport, transformerType, projectFolder).startGenerator();
		
		if (inboundTransport == RESTHTTP || inboundTransport == RESTHTTPS) expectedNoOfFiles += 2;
		
		int actualNoOfFiles = SystemUtil.countFiles(projectFolder) - noOfFilesBefore;
		
		// Compensate for the case where transformer = SMOOKS and the transformers-folder already is created (then there will be one file less created...)
		if (transformerType == TransformerEnum.SMOOKS) {
			if (expectedNoOfFiles - actualNoOfFiles == 1) {
				// Ok, seems like the transformers-folder already was created, lower the expected no of files by one
				expectedNoOfFiles--;
			}
		}

		// A new generation of the generator is on its way...
		if ((inboundTransport == RESTHTTP || inboundTransport == RESTHTTPS) && (outboundTransport == SOAPHTTP || outboundTransport == SOAPHTTPS)) {
			expectedNoOfFiles = 12;
		}
		
		expectedNoOfFiles += expectedNoSslFiles;

		assertEquals("Missmatch in expected number of created files and folders." , expectedNoOfFiles, actualNoOfFiles);
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

	@SuppressWarnings("unused")
	private void performMavenBuild_old(String groupId, String artifactId) throws IOException {
		String PROJECT_FOLDER = TEST_OUT_FOLDER + "/" + artifactId;
		
		SystemUtil.executeCommand(BUILD_COMMAND, PROJECT_FOLDER);
		
		// If the build runs fine then also perform a clean-command to save GB's of diskspace...
		SystemUtil.executeCommand(CLEAN_COMMAND, PROJECT_FOLDER);
	}

}