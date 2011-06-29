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
import static org.soitoolkit.tools.generator.model.enums.DeploymentModelEnum.WAR_DEPLOY;
import static org.soitoolkit.tools.generator.model.enums.MuleVersionEnum.MULE_3_1_2;
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
import org.soitoolkit.tools.generator.util.SystemUtil;
import org.soitoolkit.tools.generator.util.PreferencesUtil;

public class IntegrationComponentGeneratorTest {

	private static final MuleVersionEnum MULE_VERSION = MULE_3_1_2;
	private static final List<TransportEnum> TRANSPORTS = new ArrayList<TransportEnum>();
	private static final String TEST_OUT_FOLDER = PreferencesUtil.getDefaultRootFolder() + "/jUnitTests";
	private static final String PROJECT = "ordermgm";	
	private static final String PROJECT_FOLDER = TEST_OUT_FOLDER + "/" + PROJECT;
	private static final String MAVEN_HOME = PreferencesUtil.getMavenHome();

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
	public void testGenerateOrderMgm_Standalone() throws IOException {
		SystemUtil.delDirs(PROJECT_FOLDER + "-standalone");
		assertEquals(0, SystemUtil.countFiles(PROJECT_FOLDER + "-standalone"));
				
		new IntegrationComponentGenerator(System.out, "org.soitoolkit.refapps.dealernetwork", PROJECT + "-standalone", "1.0-SNAPSHOT", MULE_VERSION, STANDALONE_DEPLOY, TRANSPORTS, TEST_OUT_FOLDER).startGenerator();
		assertEquals("Missmatch in expected number of created files and folders", 51, SystemUtil.countFiles(PROJECT_FOLDER + "-standalone"));
		
		SystemUtil.executeCommand(MAVEN_HOME + "/bin/" + BUILD_COMMAND, PROJECT_FOLDER + "-standalone" + "/trunk");
	}

	@Test
	public void testGenerateOrderMgm_War() throws IOException {
		SystemUtil.delDirs(PROJECT_FOLDER + "-war");
		assertEquals(0, SystemUtil.countFiles(PROJECT_FOLDER + "-war"));
				
		new IntegrationComponentGenerator(System.out, "org.soitoolkit.refapps.dealernetwork", PROJECT + "-war", "1.0-SNAPSHOT", MULE_VERSION, WAR_DEPLOY, TRANSPORTS, TEST_OUT_FOLDER).startGenerator();
		assertEquals("Missmatch in expected number of created files and folders", 63, SystemUtil.countFiles(PROJECT_FOLDER + "-war"));
		
		SystemUtil.executeCommand(MAVEN_HOME + "/bin/" + BUILD_COMMAND, PROJECT_FOLDER + "-war" + "/trunk");
	}

	@Test
	public void testGenerateShipping() throws IOException {
		SystemUtil.delDirs(TEST_OUT_FOLDER + "/shipping");
		assertEquals(0, SystemUtil.countFiles(TEST_OUT_FOLDER + "/shipping"));

		new IntegrationComponentGenerator(System.out, "org.soitoolkit.refapps.dealernetwork", "shipping", "1.0-SNAPSHOT", MULE_VERSION, STANDALONE_DEPLOY, TRANSPORTS, TEST_OUT_FOLDER).startGenerator();
		assertEquals("Missmatch in expected number of created files and folders", 51, SystemUtil.countFiles(TEST_OUT_FOLDER + "/shipping"));

		SystemUtil.executeCommand(MAVEN_HOME + "/bin/" + BUILD_COMMAND, TEST_OUT_FOLDER + "/shipping/trunk");
	}

	@Test
	public void testGenerateVgrPicsara2melior() throws IOException {
		String grp = "se.vgregion.pilot";
		String name = "picsara2melior-sas-003";
		
		SystemUtil.delDirs(TEST_OUT_FOLDER + "/" + name);
		assertEquals(0, SystemUtil.countFiles(TEST_OUT_FOLDER + "/" + name));

		new IntegrationComponentGenerator(System.out, grp, name, "1.0-SNAPSHOT", MULE_VERSION, STANDALONE_DEPLOY, TRANSPORTS, TEST_OUT_FOLDER).startGenerator();
		assertEquals("Missmatch in expected number of created files and folders", 49, SystemUtil.countFiles(TEST_OUT_FOLDER + "/" + name));

		SystemUtil.executeCommand(MAVEN_HOME + "/bin/" + BUILD_COMMAND, TEST_OUT_FOLDER + "/" + name + "/trunk");
	}

	@Test
	public void testGenerateVfSveFasktura() throws IOException {
		String grp = "se.volvofinans.infobus.faktura";
		String name = "faktura3";
		
		SystemUtil.delDirs(TEST_OUT_FOLDER + "/" + name);
		assertEquals(0, SystemUtil.countFiles(TEST_OUT_FOLDER + "/" + name));

		new IntegrationComponentGenerator(System.out, grp, name, "1.0-SNAPSHOT", MULE_VERSION, STANDALONE_DEPLOY, TRANSPORTS, TEST_OUT_FOLDER).startGenerator();
		assertEquals("Missmatch in expected number of created files and folders", 51, SystemUtil.countFiles(TEST_OUT_FOLDER + "/" + name));

		SystemUtil.executeCommand(MAVEN_HOME + "/bin/" + BUILD_COMMAND, TEST_OUT_FOLDER + "/" + name + "/trunk");
	}
}