package org.soitoolkit.tools.generator.plugin.generator;

import static org.junit.Assert.assertEquals;
import static org.soitoolkit.tools.generator.plugin.util.SystemUtil.BUILD_COMMAND;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.soitoolkit.tools.generator.plugin.util.PreferencesUtil;
import org.soitoolkit.tools.generator.plugin.util.SystemUtil;

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
		assertEquals("Missmatch in expected number of created files and folders", 16, SystemUtil.countFiles(PROJECT_FOLDER));
		
		SystemUtil.executeCommand(MAVEN_HOME + "/bin/" + BUILD_COMMAND, PROJECT_FOLDER + "/trunk");
	}
/*
	@Test
	public void testGenerateSchemaVgrPicsara2melior() throws IOException {
		String grp = "se.vgregion.pilot";
		String name = "picsara2melior-sas-003";
		// TODO Don't fix '-' right now...
		name = "picsara2melior_sas_003";

		SystemUtil.delDirs(TEST_OUT_FOLDER + "/" + name + "-schemas");
		assertEquals(0, SystemUtil.countFiles(TEST_OUT_FOLDER + "/" + name + "-schemas"));
		
		new SchemaComponentGenerator(System.out, grp, name, "1.0-SNAPSHOT", name, null, TEST_OUT_FOLDER).startGenerator();
		assertEquals("Missmatch in expected number of created files and folders", 15, SystemUtil.countFiles(TEST_OUT_FOLDER + "/" + name + "-schemas"));
		
		SystemUtil.executeCommand(BUILD_COMMAND, TEST_OUT_FOLDER + "/" + name + "-schemas/trunk");
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
		
		SystemUtil.executeCommand(BUILD_COMMAND, TEST_OUT_FOLDER + "/" + name + "-schemas/trunk");
	}
*/
	
}
