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
import static org.soitoolkit.tools.generator.util.SystemUtil.BUILD_COMMAND;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.soitoolkit.tools.generator.model.enums.MuleVersionEnum;
import org.soitoolkit.tools.generator.model.enums.TransportEnum;
import org.soitoolkit.tools.generator.util.SystemUtil;

public class IntegrationComponentV2GeneratorTest extends AbstractGeneratorTest {

	public static int getExpectedNoOfIcFilesCreated(MuleVersionEnum muleVersionEnum) {
		if (muleVersionEnum.isVersionEqualOrGreater(MuleVersionEnum.MULE_3_6_1)) {
			// Mule 3.6.0 and higher use log4j2 instead of log4j, no log4j.dtd files generated
			return 33;
		}
		else {
			return 35;
		}
	}

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
	public void testGenerateStandalone() throws Exception {
		runIcGeneratorAndVerify("org.soitoolkit.standalone.aaa", "integration-component-v2-aaa");
	}
	
	@Test
	public void testGenerateStandaloneWithDifferentNamespace() throws Exception {
		runIcGeneratorAndVerify("org.soitoolkit.standalone.bbb", "integration-component-v2-bbb");
	}
	
	private void runIcGeneratorAndVerify(String groupId, String baseArtifactId) throws Exception {
        for (MuleVersionEnum v: getMuleVersions()) {
			if (!v.isEEVersion()) {
				String artifactId = "integration-component-v2" + v;
				SystemUtil.delDirs(TEST_OUT_FOLDER + "/" + artifactId);
				assertEquals(0, SystemUtil.countFiles(TEST_OUT_FOLDER + "/" + artifactId));
				new IntegrationComponentV2Generator(System.out, groupId, artifactId, "1.0-SNAPSHOT", v, STANDALONE_DEPLOY, TRANSPORTS, TEST_OUT_FOLDER).startGenerator();
				assertEquals("Mismatch in expected number of created files and folders for mule version: " + v, getExpectedNoOfIcFilesCreated(v), SystemUtil.countFiles(TEST_OUT_FOLDER + "/" + artifactId));

				SystemUtil.executeCommand(BUILD_COMMAND, TEST_OUT_FOLDER + "/" + artifactId);
			}
		}		
	}
}