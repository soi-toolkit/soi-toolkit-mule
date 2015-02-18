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
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.FILE;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.FTP;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.HTTP;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.HTTPS;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.JDBC;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.JMS;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.SERVLET;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.SFTP;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.VM;
import static org.soitoolkit.tools.generator.model.impl.ModelUtil.capitalize;

import java.io.File;
import java.io.IOException;
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
import org.soitoolkit.tools.generator.util.SystemUtil;

/**
 * The purpose of this generator test is to verify the behavior generated code 
 * for different combinations of the one-way service exchange pattern.
 */
public class OneWayServiceV2GeneratorTest extends AbstractGeneratorTest {

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
	 * 
	 * @throws IOException if error occurs.
	 */
	@Test
	public void testOneWayServicesInOneCommonIC() throws IOException {
        List<MuleVersionEnum> muleVersions = MuleVersionEnum.getNonDeprecatedVersions();

        for (MuleVersionEnum v: muleVersions) {
            if (!v.isEEVersion()) {
				//&& muleVersions[i].equals(MuleVersionEnum.MULE_3_4_0)
				doTestOneWayServicesInOneCommonIC("org.soitoolkit.tool.generator", "onewaySA-mule" + v.getVerNoNumbersOnly(), v, STANDALONE_DEPLOY);
			}
		}
	}

	/**
	 * Test all combinations of supported inbound and outbound endpoints with one common integration component 
	 * but with another component names to ensure that we don't have any hardcoded component names in the templates.
	 * 
	 * @throws IOException if error occurs.
	 */
	@Test
	public void testOneWayServicesInOneCommonICWithOtherName() throws IOException {
        List<MuleVersionEnum> muleVersions = MuleVersionEnum.getNonDeprecatedVersions();

        for (MuleVersionEnum v: muleVersions) {
            if (!v.isEEVersion()) {
				//&& muleVersions[i].equals(MuleVersionEnum.MULE_3_5_0)
				doTestOneWayServicesInOneCommonIC("org.soitoolkit.tool.generator-tests", "Oneway-Tests-SA-mule" + v.getVerNoNumbersOnly(), v, STANDALONE_DEPLOY);
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
	public void testOneWayServicesOneICPerService() throws IOException {
        List<MuleVersionEnum> muleVersions = MuleVersionEnum.getNonDeprecatedVersions();

        for (MuleVersionEnum v: muleVersions) {
            if (!v.isEEVersion()) {
				//&& muleVersions[i].equals(MuleVersionEnum.MULE_3_5_0)
				doTestOneWayServicesOneICPerService("org.soitoolkit.tool.generator",     "onewaySA-mule" +        v.getVerNoNumbersOnly(), v, STANDALONE_DEPLOY);
			}
		}
	}

	private void doTestOneWayServicesInOneCommonIC(String groupId, String artifactId, MuleVersionEnum muleVersion, DeploymentModelEnum deploymentModel) throws IOException {

		String projectFolder = TEST_OUT_FOLDER + "/" + artifactId;
		
		createEmptyIntegrationComponent(groupId, artifactId, muleVersion, deploymentModel, projectFolder);	

		for (TransportEnum inboundTransport : getInboundTransports(deploymentModel)) {
			for (TransportEnum outboundTransport : getOutboundTransports()) {
				if (!(muleVersion.equals(MuleVersionEnum.MULE_3_4_0) && (inboundTransport == JDBC || outboundTransport == JDBC)))
					createOneWayService(groupId, artifactId, muleVersion, inboundTransport, outboundTransport, TransformerEnum.JAVA, projectFolder);
			}
		}
		performMavenBuild(projectFolder);
	}

	private void doTestOneWayServicesOneICPerService(String groupId, String artifactId, MuleVersionEnum muleVersion, DeploymentModelEnum deploymentModel) throws IOException {

		String orgArtifactId = artifactId;
		
		for (TransportEnum inboundTransport : getInboundTransports(deploymentModel)) {
			for (TransportEnum outboundTransport : getOutboundTransports()) {
				artifactId = orgArtifactId + "_" + inboundTransport.name() + "_to_" + outboundTransport.name();
				String projectFolder = TEST_OUT_FOLDER + "/" + artifactId;

				createEmptyIntegrationComponent(groupId, artifactId, muleVersion, deploymentModel, projectFolder);
				if (!(muleVersion.equals(MuleVersionEnum.MULE_3_4_0) && (inboundTransport == JDBC || outboundTransport == JDBC)))
					createOneWayService(groupId, artifactId, muleVersion, inboundTransport, outboundTransport, TransformerEnum.JAVA, projectFolder);
				performMavenBuild(projectFolder);
			}
		}
	}

	private TransportEnum[] getInboundTransports(DeploymentModelEnum deploymentModel) {
		TransportEnum[] inboundTransports  = {VM, JMS, JDBC, FILE, FTP, SFTP, HTTP, HTTPS};
		return inboundTransports;
	}

	private TransportEnum[] getOutboundTransports() {
		TransportEnum[] outboundTransports = {VM, JMS, JDBC, FILE, FTP, SFTP, HTTP, HTTPS};
		return outboundTransports;
	}

	private void createEmptyIntegrationComponent(String groupId, String artifactId, MuleVersionEnum muleVersion, DeploymentModelEnum deploymentModel, String projectFolder) throws IOException {
		
		SystemUtil.delDirs(projectFolder);
		assertEquals(0, SystemUtil.countFiles(projectFolder));
		new IntegrationComponentV2Generator(System.out, groupId, artifactId, VERSION, muleVersion, deploymentModel, TRANSPORTS, TEST_OUT_FOLDER).startGenerator();
		assertEquals("Missmatch in expected number of created files and folders.", EXPECTED_NO_OF_IC_FILES_CREATED, SystemUtil.countFiles(projectFolder));
	}

	private void createOneWayService(String groupId, String artifactId, MuleVersionEnum muleVersion, TransportEnum inboundTransport, TransportEnum outboundTransport, TransformerEnum transformerType, String projectFolder) throws IOException {

		String service = inboundTransport.name().toLowerCase() + "To" + capitalize(outboundTransport.name().toLowerCase());

		int noOfFilesBefore = SystemUtil.countFiles(projectFolder);
		
		List<File> filesBefore = SystemUtil.listFiles(projectFolder);

		int expectedNoOfFiles = 22;
		
		//IModel model = ModelFactory.newModel(groupId, artifactId, VERSION, service, muleVersion, inboundTransport, outboundTransport, transformerType);

		//No outbound service.
		if (outboundTransport == JMS) {
			expectedNoOfFiles--;
		}
		
		// Adding outbound integration tests.
		if (outboundTransport == HTTP || outboundTransport == HTTPS || outboundTransport == JDBC || outboundTransport == SFTP || outboundTransport == FTP) {
			expectedNoOfFiles += 2;
		}
		
		if (inboundTransport == HTTPS || outboundTransport == HTTPS) {
			//TODO: Add to test-dependency
			File certFolder = new File(projectFolder + "/src/test/certs/");
			if (!certFolder.exists()) {
				expectedNoOfFiles += 5;
			}
		}
		
		if (inboundTransport == JDBC || outboundTransport == JDBC) {
			File dbCommon = new File(projectFolder + "/src/main/app/" +artifactId + "-jdbc-common.xml");
			if (!dbCommon.exists()) {
				// Adding: src/environment/setup, <artifact-id>-db-create-tables.sql, <artifact-id>-db-drop-tables.sql,<artifact-id>-db-insert-tables.sql, /src/main/app/<artifact-id>-jdbc-common.xml
				expectedNoOfFiles += 5;
			}
		}

		new OnewayServiceV2Generator(System.out, groupId, artifactId, service, muleVersion, inboundTransport, outboundTransport, transformerType, projectFolder).startGenerator();
		
		List<File> filesAfter = SystemUtil.listFiles(projectFolder);
		filesAfter.removeAll(filesBefore);
		
		System.out.println("Generated files:");
		for (File tmpFile : filesAfter) {
			System.out.println("File: " + tmpFile.getPath());
		}
		
		assertEquals("Missmatch in expected number of created files and folders. ArtifactId: " + artifactId + ", InboundTransport: " + inboundTransport + ", OutboundTransport: " + outboundTransport + ", MuleVersion" + muleVersion, expectedNoOfFiles, SystemUtil.countFiles(projectFolder) - noOfFilesBefore);
	}
}