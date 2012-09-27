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
import static org.soitoolkit.tools.generator.model.enums.DeploymentModelEnum.STANDALONE_DEPLOY;
import static org.soitoolkit.tools.generator.model.enums.MuleVersionEnum.MAIN_MULE_VERSION;
import static org.soitoolkit.tools.generator.util.SystemUtil.BUILD_COMMAND;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.soitoolkit.tools.generator.model.enums.MuleVersionEnum;
import org.soitoolkit.tools.generator.model.enums.TransportEnum;
import org.soitoolkit.tools.generator.util.PreferencesUtil;
import org.soitoolkit.tools.generator.util.SystemUtil;

public class IntegrationComponentTeststubGeneratorTest {

	private static final MuleVersionEnum MULE_VERSION = MAIN_MULE_VERSION;
	private static final List<TransportEnum> TRANSPORTS = new ArrayList<TransportEnum>();
	private static final String TEST_OUT_FOLDER = PreferencesUtil
			.getDefaultRootFolder() + "/jUnitTests";

	public static final String IC_ARTIFACTID = "ictostub";
	public static final String IC_GROUPID = IntegrationComponentTeststubGeneratorTest.class
			.getPackage().getName();
	public static final String IC_VERSION = "1.0.0-SNAPSHOT";
	public static final int EXPECTED_NO_OF_IC_FILES_CREATED = 33;
	public static final int EXPECTED_NO_OF_TESTSTUB_FILES_CREATED = 19;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TRANSPORTS.add(TransportEnum.JMS);
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
	public void testGenerateIcAndTeststub() throws IOException {
		generateIc();
		generateIcTeststub();
	}

	private void generateIc() throws IOException {
		String projectDir = TEST_OUT_FOLDER + "/" + IC_ARTIFACTID;

		SystemUtil.delDirs(projectDir);
		assertEquals(0, SystemUtil.countFiles(projectDir));

		new IntegrationComponentGenerator(System.out, IC_GROUPID,
				IC_ARTIFACTID, IC_VERSION, MULE_VERSION, STANDALONE_DEPLOY,
				TRANSPORTS, TEST_OUT_FOLDER).startGenerator();
		assertEquals(
				"Mismatch in expected number of created files and folders",
				EXPECTED_NO_OF_IC_FILES_CREATED,
				SystemUtil.countFiles(projectDir));

		SystemUtil.executeCommand(BUILD_COMMAND, projectDir);
	}

	private void generateIcTeststub() throws IOException {
		String teststubArtifactId = IC_ARTIFACTID + "-teststub";
		String projectDir = TEST_OUT_FOLDER + "/" + teststubArtifactId;

		SystemUtil.delDirs(projectDir);
		assertEquals(0, SystemUtil.countFiles(projectDir));

		new IntegrationComponentTeststubGenerator(System.out, IC_GROUPID,
				IC_ARTIFACTID, IC_VERSION, STANDALONE_DEPLOY, TEST_OUT_FOLDER)
				.startGenerator();
		assertEquals(
				"Mismatch in expected number of created files and folders",
				EXPECTED_NO_OF_TESTSTUB_FILES_CREATED,
				SystemUtil.countFiles(projectDir));

		SystemUtil.executeCommand(BUILD_COMMAND, projectDir);
	}

}