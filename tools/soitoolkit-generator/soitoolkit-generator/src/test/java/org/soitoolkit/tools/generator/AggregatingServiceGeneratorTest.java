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
import static org.soitoolkit.tools.generator.util.SystemUtil.CLEAN_COMMAND;
import static org.soitoolkit.tools.generator.util.SystemUtil.ECLIPSE_AND_TEST_REPORT_COMMAND;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.soitoolkit.tools.generator.model.enums.DeploymentModelEnum;
import org.soitoolkit.tools.generator.model.enums.MuleVersionEnum;
import org.soitoolkit.tools.generator.util.PreferencesUtil;
import org.soitoolkit.tools.generator.util.SystemUtil;

public class AggregatingServiceGeneratorTest {

	private static final String TEST_OUT_FOLDER = PreferencesUtil.getDefaultRootFolder() + "/jUnitTests";
	private static final String VERSION = "1.0.0-SNAPSHOT";

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
	 * 
	 * @throws IOException
	 */
	@Test
	public void testAggregatingServices() throws IOException {

		doTestAggregatingServices("clinicalprocess.activity.actions", "GetAggregatedGetActivity", MuleVersionEnum.MULE_3_3_0, STANDALONE_DEPLOY);

		/*
		MuleVersionEnum[] muleVersions = MuleVersionEnum.values();
		for (int i = 0; i < muleVersions.length; i++) {
			if (!muleVersions[i].isEEVersion()) {
				doTestAggregationServices("se.skltp.aggregatingservices.clinicalprocess.activity.actions", "GetAggregatedGetActivity" + muleVersions[i].getVerNoNumbersOnly(), muleVersions[i], STANDALONE_DEPLOY);
			}
		}
		*/
	}

	private void doTestAggregatingServices(String domainId, String artifactId, MuleVersionEnum muleVersion, DeploymentModelEnum deploymentModel) throws IOException {

		String projectFolder = TEST_OUT_FOLDER + "/" + artifactId + "-TEST";
		createAggregationService(domainId, artifactId, muleVersion, projectFolder);
		//GetAggregatedGetActivity-TEST/riv.clinicalprocess.activity.actions/GetAggregatedGetActivity/trunk
		performMavenBuild(projectFolder + "/" + "riv." + domainId + "/" + artifactId + "/trunk");
		performMavenBuild(projectFolder + "/" + artifactId + "-teststub");
	}

	private void createAggregationService(String domainId, String artifactId, MuleVersionEnum muleVersion, String projectFolder) throws IOException {

		SystemUtil.delDirs(projectFolder);

		int expectedNoOfFiles = 89;
		
		String service = artifactId;

		int noOfFilesBefore = SystemUtil.countFiles(projectFolder);

		new AggregatingServiceGenerator(System.out, domainId, artifactId, VERSION, service, muleVersion, projectFolder).startGenerator();
				
		int actualNoOfFiles = SystemUtil.countFiles(projectFolder) - noOfFilesBefore;
		
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

}