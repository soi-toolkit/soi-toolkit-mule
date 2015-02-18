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
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.soitoolkit.tools.generator.model.enums.MuleVersionEnum;
import org.soitoolkit.tools.generator.model.enums.TransportEnum;
import org.soitoolkit.tools.generator.util.SystemUtil;

public class IntegrationComponentV2GeneratorTest extends AbstractGeneratorTest {

	private static final MuleVersionEnum MULE_VERSION = MAIN_MULE_VERSION;

	private static final String PROJECT = "integration-component-v2";	
	private static final String PROJECT_FOLDER = TEST_OUT_FOLDER + "/" + PROJECT;
	
	public  static final int EXPECTED_NO_OF_IC_FILES_CREATED = 35;

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
	public void testGenerateStandalone() throws IOException {

        SystemUtil.delDirs(PROJECT_FOLDER + "-standalone");
		assertEquals(0, SystemUtil.countFiles(PROJECT_FOLDER + "-standalone"));

        for (MuleVersionEnum v: getMuleVersions()) {
			if (!v.isEEVersion() && !v.equals(MuleVersionEnum.MULE_3_4_0)) {
                // FIXME: Why MULE_VERSION and not current version v???
				new IntegrationComponentV2Generator(System.out, "org.soitoolkit.standalone", PROJECT + "-standalone", "1.0-SNAPSHOT", MULE_VERSION, STANDALONE_DEPLOY, TRANSPORTS, TEST_OUT_FOLDER).startGenerator();
				assertEquals("Missmatch in expected number of created files and folders", EXPECTED_NO_OF_IC_FILES_CREATED-2, SystemUtil.countFiles(PROJECT_FOLDER + "-standalone"));
		
				SystemUtil.executeCommand(BUILD_COMMAND, PROJECT_FOLDER + "-standalone");
			}
		}
	}


	@Test
	public void testGenerateStandaloneWithDifferentNamespace() throws IOException {

		String grp = "org.soitoolkit.standalone.xxx";
		String name = PROJECT + "-standalone-xxx";
		
		SystemUtil.delDirs(TEST_OUT_FOLDER + "/" + name);
		assertEquals(0, SystemUtil.countFiles(TEST_OUT_FOLDER + "/" + name));

        for (MuleVersionEnum v: getMuleVersions()) {
			if (!v.isEEVersion() && !v.equals(MuleVersionEnum.MULE_3_4_0)) {
                // FIXME: Why MULE_VERSION and not current version v???
				new IntegrationComponentV2Generator(System.out, grp, name, "1.0-SNAPSHOT", MULE_VERSION, STANDALONE_DEPLOY, TRANSPORTS, TEST_OUT_FOLDER).startGenerator();
				assertEquals("Missmatch in expected number of created files and folders", EXPECTED_NO_OF_IC_FILES_CREATED, SystemUtil.countFiles(TEST_OUT_FOLDER + "/" + name));

				SystemUtil.executeCommand(BUILD_COMMAND, TEST_OUT_FOLDER + "/" + name);
			}
		}
	}
}