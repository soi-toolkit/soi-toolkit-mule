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

public class OneWayServiceGeneratorTest {

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
	public void testOneWayServices221() throws IOException {
		doTestOneWayServices("org.soitoolkit.tool.generator", "oneway221", MULE_2_2_1);
		doTestOneWayServices("org.soitoolkit.tool.generator-tests", "Oneway-Tests-221", MULE_2_2_1);
	}

	@Test
	public void testOneWayServices225() throws IOException {
		doTestOneWayServices("org.soitoolkit.tool.generator", "oneway225", MULE_2_2_5);
		doTestOneWayServices("org.soitoolkit.tool.generator-tests", "Oneway-Tests-225", MULE_2_2_5);
	}

	private void doTestOneWayServices(String groupId, String artifactId, MuleVersionEnum muleVersion) throws IOException {
		TransportEnum[] inboundTransports  = {VM, JMS, JDBC, FILE, SFTP, SERVLET, IMAP}; // FTP, POP3
		TransportEnum[] outboundTransports = {VM, JMS, JDBC, FILE, SFTP, SMTP}; // FTP, 

		createEmptyIntegrationComponent(groupId, artifactId, muleVersion);	

		for (TransportEnum inboundTransport : inboundTransports) {
			for (TransportEnum outboundTransport : outboundTransports) {
				if (inboundTransport == JMS  && outboundTransport == JDBC) continue;
				if (inboundTransport == JDBC && outboundTransport == JMS)  continue;
				createOneWayService(groupId, artifactId, inboundTransport, outboundTransport);
			}
		}

		performMavenBuild(groupId, artifactId);
	}

	private void createEmptyIntegrationComponent(String groupId, String artifactId, MuleVersionEnum muleVersion) throws IOException {
		String projectFolder = TEST_OUT_FOLDER + "/" + artifactId;

		TRANSPORTS.add(VM);
		TRANSPORTS.add(JMS);
		TRANSPORTS.add(JDBC);
		TRANSPORTS.add(FILE);
		TRANSPORTS.add(FTP);
		TRANSPORTS.add(SFTP);
		TRANSPORTS.add(SERVLET);
		TRANSPORTS.add(POP3);
		TRANSPORTS.add(IMAP);
		TRANSPORTS.add(SMTP);

		SystemUtil.delDirs(projectFolder);
		assertEquals(0, SystemUtil.countFiles(projectFolder));
		new IntegrationComponentGenerator(System.out, groupId, artifactId, VERSION, muleVersion, TRANSPORTS, TEST_OUT_FOLDER).startGenerator();
		assertEquals("Missmatch in expected number of created files and folders", 66, SystemUtil.countFiles(projectFolder));
	}

	private void createOneWayService(String groupId, String artifactId, TransportEnum inboundTransport, TransportEnum outboundTransport) throws IOException {
		String projectFolder = TEST_OUT_FOLDER + "/" + artifactId;

		String service = inboundTransport.name().toLowerCase() + "To" + capitalize(outboundTransport.name().toLowerCase());

		int noOfFilesBefore = SystemUtil.countFiles(projectFolder);

		IModel model = ModelFactory.newModel(groupId, artifactId, VERSION, service, null, null);
		new OnewayServiceGenerator(System.out, groupId, artifactId, service, inboundTransport, outboundTransport, projectFolder + "/trunk/" + model.getServiceProjectFilepath()).startGenerator();
		
		int expectedNoOfFiles = 10;
		if (inboundTransport == SERVLET) {
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
		assertEquals("Missmatch in expected number of created files and folders", expectedNoOfFiles, SystemUtil.countFiles(projectFolder) - noOfFilesBefore);
	}

	private void performMavenBuild(String groupId, String artifactId) throws IOException {
		String PROJECT_FOLDER = TEST_OUT_FOLDER + "/" + artifactId;
		
		SystemUtil.executeCommand(MAVEN_HOME + "/bin/" + BUILD_COMMAND, PROJECT_FOLDER + "/trunk");
	}

}