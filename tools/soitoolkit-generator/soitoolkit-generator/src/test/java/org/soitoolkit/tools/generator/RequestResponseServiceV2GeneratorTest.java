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
import static org.soitoolkit.tools.generator.IntegrationComponentV2GeneratorTest.EXPECTED_NO_OF_IC_FILES_CREATED;
import static org.soitoolkit.tools.generator.model.enums.DeploymentModelEnum.STANDALONE_DEPLOY;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.JMS;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.RESTHTTP;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.RESTHTTPS;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.SOAPHTTP;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.SOAPHTTPS;
import static org.soitoolkit.tools.generator.model.impl.ModelUtil.capitalize;

import java.io.File;
import java.io.IOException;
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
import org.soitoolkit.tools.generator.util.SystemUtil;

/**
 * The purpose of this generator test is to verify the behavior generated code 
 * for different combinations of the request-response service exchange pattern.
 */
public class RequestResponseServiceV2GeneratorTest extends AbstractGeneratorTest {

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
	 * Test all combinations of supported inbound and outbound endpoints in one common integration component.
	 * 
	 * @throws IOException if error occurs.
	 */
	@Test
	public void testRequestResponseServicesInOneCommonIC() throws IOException {
		MuleVersionEnum[] muleVersions = MuleVersionEnum.values();
		
		for (int i = 0; i < muleVersions.length; i++) {
			if (!muleVersions[i].isEEVersion()) {
				// && !muleVersions[i].equals(MuleVersionEnum.MULE_3_4_0)
				doTestRequestResponseServicesInOneCommonIC("org.soitoolkit.tool.generator",       "requestResponseSA-mule" +         muleVersions[i].getVerNoNumbersOnly(), muleVersions[i], STANDALONE_DEPLOY);
			}
		}
	}

	/**
	 * Test all combinations of supported inbound and outbound endpoints in one common integration component 
	 * but with another component names to ensure that we don't have any hardcoded component names in the templates.
	 * 
	 * @throws IOException if error occurs.
	 */
	@Test
	public void testRequestResponseServicesInOneCommonICWithOtherName() throws IOException {
		MuleVersionEnum[] muleVersions = MuleVersionEnum.values();
		
		for (int i = 0; i < muleVersions.length; i++) {
			if (!muleVersions[i].isEEVersion()) {
				//&& !muleVersions[i].equals(MuleVersionEnum.MULE_3_4_0)
				doTestRequestResponseServicesInOneCommonIC("org.soitoolkit.tool.generator-tests", "Request-Response-SA-Tests-mule" + muleVersions[i].getVerNoNumbersOnly(), muleVersions[i], STANDALONE_DEPLOY);
			}
		}
	}

	/**
	 * Test all combinations of supported inbound and outbound endpoints with one integration component 
	 * per service to ensure that each combination of inbound and outbound endpoints are self contained.
	 * 
	 * @throws IOException if error occurs.
	 */
	@Test
	public void testRequestResponseServicesOneICPerService() throws IOException {
		MuleVersionEnum[] muleVersions = MuleVersionEnum.values();
		
		for (int i = 0; i < muleVersions.length; i++) {
			if (!muleVersions[i].isEEVersion()) {
				//&& !muleVersions[i].equals(MuleVersionEnum.MULE_3_4_0)
				doTestRequestResponseServicesOneICPerService("org.soitoolkit.tool.generator", 
						"requestResponseSA-mule" + muleVersions[i].getVerNoNumbersOnly(), 
						muleVersions[i], 
						STANDALONE_DEPLOY);
			}
		}
	}

	private void doTestRequestResponseServicesInOneCommonIC(String groupId, String artifactId, MuleVersionEnum muleVersion, DeploymentModelEnum deploymentModel) throws IOException {

		String projectFolder = TEST_OUT_FOLDER + "/" + artifactId;

		createEmptyIntegrationComponent(groupId, artifactId, muleVersion, deploymentModel, projectFolder);	

		for (TransportEnum inboundTransport : getInboundTransports(deploymentModel)) {
			for (TransportEnum outboundTransport : getOutboundTransports()) {
				createRequestResponseService(groupId, artifactId, muleVersion, inboundTransport, outboundTransport, TransformerEnum.JAVA, projectFolder);
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
		TransportEnum[] inboundTransports = {SOAPHTTP, SOAPHTTPS, RESTHTTP, RESTHTTPS};
		return inboundTransports;
	}

	private TransportEnum[] getOutboundTransports() {
		TransportEnum[] outboundTransports = {SOAPHTTP, SOAPHTTPS, RESTHTTP, RESTHTTPS, JMS};
		return outboundTransports;
	}
	private void createEmptyIntegrationComponent(String groupId, String artifactId, MuleVersionEnum muleVersion, DeploymentModelEnum deploymentModel, String projectFolder) throws IOException {
		
		int noOfExpectedFiles = (deploymentModel == STANDALONE_DEPLOY) ? EXPECTED_NO_OF_IC_FILES_CREATED : 66;

		SystemUtil.delDirs(projectFolder);
		assertEquals(0, SystemUtil.countFiles(projectFolder));
		new IntegrationComponentV2Generator(System.out, groupId, artifactId, VERSION, muleVersion, deploymentModel, TRANSPORTS, TEST_OUT_FOLDER).startGenerator();
		assertEquals("Missmatch in expected number of created files and folders", noOfExpectedFiles, SystemUtil.countFiles(projectFolder));
	}

	private void createRequestResponseService(String groupId, String artifactId, MuleVersionEnum muleVersion, TransportEnum inboundTransport, TransportEnum outboundTransport, TransformerEnum transformerType, String projectFolder) throws IOException {

		String service = inboundTransport.name().toLowerCase() + "To" + capitalize(outboundTransport.name().toLowerCase() + "Using" + capitalize(transformerType.name().toLowerCase()));

		int noOfFilesBefore = SystemUtil.countFiles(projectFolder);
		
		List<File> filesBefore = SystemUtil.listFiles(projectFolder);

		// SSL
		// TODO: OLA D - MOVE TO JAR DEPENDENCY
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
		
		int expectedNoOfFiles = 14;
		
		new RequestResponseServiceV2Generator(System.out, groupId, artifactId, service, muleVersion, inboundTransport, outboundTransport, transformerType, projectFolder).startGenerator();
		
		int actualNoOfFiles = SystemUtil.countFiles(projectFolder) - noOfFilesBefore;
		
		// A new generation of the generator is on its way...
		if (inboundTransport == RESTHTTP || inboundTransport == RESTHTTPS) {
			// RAML
			expectedNoOfFiles += 1;
		}
		
		expectedNoOfFiles += expectedNoSslFiles;

		List<File> filesAfter = SystemUtil.listFiles(projectFolder);
		filesAfter.removeAll(filesBefore);
		
		System.out.println("Generated files:");
		for (File tmpFile : filesAfter) {
			System.out.println("File: " + tmpFile.getPath());
		}
		
		assertEquals("Missmatch in expected number of created files and folders. ArtifactId: " + artifactId + ", InboundTransport: " + inboundTransport + ", OutboundTransport: " + outboundTransport + ", MuleVersion" + muleVersion, expectedNoOfFiles, actualNoOfFiles);
	}
}