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
import static org.soitoolkit.tools.generator.util.SystemUtil.BUILD_COMMAND;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.soitoolkit.tools.generator.util.SystemUtil;
import org.soitoolkit.tools.generator.util.PreferencesUtil;

public class SchemaComponentGeneratorTest {

	private static final String TEST_OUT_FOLDER = PreferencesUtil.getDefaultRootFolder() + "/jUnitTests";
	private static final String PROJECT = "dealernetwork";	
	private static final String SCHEMA  = "dealernetwork";	
	private static final String PROJECT_FOLDER = TEST_OUT_FOLDER + "/" + PROJECT + "-schemas";
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
	public void testGenerateSchemaWithDefaultOperation() throws IOException {
		SystemUtil.delDirs(PROJECT_FOLDER);
		assertEquals(0, SystemUtil.countFiles(PROJECT_FOLDER));
		
		new SchemaComponentGenerator(System.out, "se.callista.test", PROJECT, "1.1-SNAPSHOT", SCHEMA, null, TEST_OUT_FOLDER).startGenerator();
		assertEquals("Missmatch in expected number of created files and folders", 15, SystemUtil.countFiles(PROJECT_FOLDER));
		
/* FIXME: How do we launch maven in cloudbees Jeniks servers???

		System.out.println("*** EnvVars:");
		Map<String, String> env = System.getenv();
		Set<Entry<String, String>> envSet = env.entrySet();
		for (Entry<String, String> entry : envSet) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}
		
		System.out.println("\n*** Properties:");
		Set<Entry<Object, Object>> propSet = System.getProperties().entrySet();
		for (Entry<Object, Object> entry : propSet) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}
		
//		String home = "/Users/magnuslarsson";
//		home = "/home/hudson";
//		home = "/home/soi-toolkit/hudson_home";
//
//		System.out.println("*** ls " + home + "/.hudson");
//		SystemUtil.executeCommand("ls " + home + "/.hudson", home);
		
		System.out.println("*** whereis mvn:");
		SystemUtil.executeCommand("whereis mvn", PROJECT_FOLDER + "/trunk");

		System.out.println("*** mvn -version:");
		SystemUtil.executeCommand("mvn -version", PROJECT_FOLDER + "/trunk");
*/
		
		SystemUtil.executeCommand(MAVEN_HOME + "/bin/" + BUILD_COMMAND, PROJECT_FOLDER + "/trunk");
	}

	@Test
	public void testGenerateSchemaWithOperations() throws IOException {
		SystemUtil.delDirs(PROJECT_FOLDER);
		assertEquals(0, SystemUtil.countFiles(PROJECT_FOLDER));
		
		List<String> ops = new ArrayList<String>();
		ops.add("createOrder");
		ops.add("getOrderStatus");
		new SchemaComponentGenerator(System.out, "org.soitoolkit.refapps.dealernetwork", PROJECT, "1.0-SNAPSHOT", SCHEMA, ops, TEST_OUT_FOLDER).startGenerator();
		assertEquals("Missmatch in expected number of created files and folders", 15, SystemUtil.countFiles(PROJECT_FOLDER));
		
		SystemUtil.executeCommand(MAVEN_HOME + "/bin/" + BUILD_COMMAND, PROJECT_FOLDER + "/trunk");
	}

	@Test
	public void testGenerateSchemaVgrPicsara2melior() throws IOException {
		String grp = "se.vgregion.pilot";
		String name = "picsara2melior-sas-003";

		SystemUtil.delDirs(TEST_OUT_FOLDER + "/" + name + "-schemas");
		assertEquals(0, SystemUtil.countFiles(TEST_OUT_FOLDER + "/" + name + "-schemas"));
		
		new SchemaComponentGenerator(System.out, grp, name, "1.0-SNAPSHOT", name, null, TEST_OUT_FOLDER).startGenerator();
		assertEquals("Missmatch in expected number of created files and folders", 15, SystemUtil.countFiles(TEST_OUT_FOLDER + "/" + name + "-schemas"));
		
		SystemUtil.executeCommand(MAVEN_HOME + "/bin/" + BUILD_COMMAND, TEST_OUT_FOLDER + "/" + name + "-schemas/trunk");
	}

	@Test
	public void testGenerateSchemaVfSveFaktura() throws IOException {
		String grp = "se.volvofinans.servicedescription";
		String name = "vf-sd-faktura-external-svefaktura";
		String schema = "mySchema";
		// TODO Don't fix '-' right now...
		name = "vf_sd_faktura_external_svefaktura";

		SystemUtil.delDirs(TEST_OUT_FOLDER + "/" + name + "-schemas");
		assertEquals(0, SystemUtil.countFiles(TEST_OUT_FOLDER + "/" + name + "-schemas"));
		
		new SchemaComponentGenerator(System.out, grp, name, "1.0-SNAPSHOT", schema, null, TEST_OUT_FOLDER).startGenerator();
		assertEquals("Missmatch in expected number of created files and folders", 15, SystemUtil.countFiles(TEST_OUT_FOLDER + "/" + name + "-schemas"));
		
		SystemUtil.executeCommand(MAVEN_HOME + "/bin/" + BUILD_COMMAND, TEST_OUT_FOLDER + "/" + name + "-schemas/trunk");
	}
}
