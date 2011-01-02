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

import static org.junit.Assert.assertEquals;
import static org.soitoolkit.tools.generator.plugin.model.enums.MuleVersionEnum.MULE_2_2_1;
import static org.soitoolkit.tools.generator.plugin.model.enums.MuleVersionEnum.MULE_2_2_5;
import static org.soitoolkit.tools.generator.plugin.util.SystemUtil.BUILD_COMMAND;
import static org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.soitoolkit.tools.generator.plugin.model.IModel;
import org.soitoolkit.tools.generator.plugin.model.ModelFactory;
import org.soitoolkit.tools.generator.plugin.model.enums.MuleVersionEnum;
import org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum;
import org.soitoolkit.tools.generator.plugin.util.PreferencesUtil;
import org.soitoolkit.tools.generator.plugin.util.SystemUtil;
import static org.soitoolkit.tools.generator.plugin.model.impl.ModelUtil.capitalize;

public class RequestResponseServiceGeneratorTest {

	private static final List<TransportEnum> TRANSPORTS = new ArrayList<TransportEnum>();
	private static final String TEST_OUT_FOLDER = PreferencesUtil.getDefaultRootFolder() + "/jUnitTests";
	private static final String VERSION = "1.0-SNAPSHOT";
	private static final String MAVEN_HOME = PreferencesUtil.getMavenHome();

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

	@Test
	public void testRequestResponseServices221() throws IOException {
		doTestRequestResponseServices("org.soitoolkit.tool.generator", "requestResponse221", MULE_2_2_1);
		doTestRequestResponseServices("org.soitoolkit.tool.generator-tests", "Request-Response-Tests-221", MULE_2_2_1);
	}

	@Test
	public void testRequestResponseServices225() throws IOException {
		doTestRequestResponseServices("org.soitoolkit.tool.generator", "requestResponse225", MULE_2_2_5);
		doTestRequestResponseServices("org.soitoolkit.tool.generator-tests", "Request-Response-Tests-225", MULE_2_2_5);
	}

	private void doTestRequestResponseServices(String groupId, String artifactId, MuleVersionEnum muleVersion) throws IOException {
		TransportEnum[] inboundTransports  = {SOAP};
		TransportEnum[] outboundTransports = {JMS}; 

		createEmptyIntegrationComponent(groupId, artifactId, muleVersion);	

		for (TransportEnum inboundTransport : inboundTransports) {
			for (TransportEnum outboundTransport : outboundTransports) {
				createRequestResponseService(groupId, artifactId, inboundTransport, outboundTransport);
			}
		}

		performMavenBuild(groupId, artifactId);
	}

	private void createEmptyIntegrationComponent(String groupId, String artifactId, MuleVersionEnum muleVersion) throws IOException {
		String projectFolder = TEST_OUT_FOLDER + "/" + artifactId;

		TRANSPORTS.add(JMS);
		TRANSPORTS.add(SERVLET);
		TRANSPORTS.add(SOAP);

		SystemUtil.delDirs(projectFolder);
		assertEquals(0, SystemUtil.countFiles(projectFolder));
		new IntegrationComponentGenerator(System.out, groupId, artifactId, VERSION, muleVersion, TRANSPORTS, TEST_OUT_FOLDER).startGenerator();
		assertEquals("Missmatch in expected number of created files and folders", 61, SystemUtil.countFiles(projectFolder));
	}

	private void createRequestResponseService(String groupId, String artifactId, TransportEnum inboundTransport, TransportEnum outboundTransport) throws IOException {
		String projectFolder = TEST_OUT_FOLDER + "/" + artifactId;

		String service = inboundTransport.name().toLowerCase() + "To" + capitalize(outboundTransport.name().toLowerCase());

		int noOfFilesBefore = SystemUtil.countFiles(projectFolder);

		IModel model = ModelFactory.newModel(groupId, artifactId, VERSION, service, null, null);
		new RequestResponseServiceGenerator(System.out, groupId, artifactId, service, inboundTransport, outboundTransport, projectFolder + "/trunk/" + model.getServiceProjectFilepath()).startGenerator();
		
		int expectedNoOfFiles = 11;
		assertEquals("Missmatch in expected number of created files and folders", expectedNoOfFiles, SystemUtil.countFiles(projectFolder) - noOfFilesBefore);
	}

	private void performMavenBuild(String groupId, String artifactId) throws IOException {
		String PROJECT_FOLDER = TEST_OUT_FOLDER + "/" + artifactId;
		
		SystemUtil.executeCommand(MAVEN_HOME + "/bin/" + BUILD_COMMAND, PROJECT_FOLDER + "/trunk");
	}

}