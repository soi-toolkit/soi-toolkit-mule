package org.soitoolkit.tools.generator.plugin.generator;

import static org.junit.Assert.*;
import static org.soitoolkit.tools.generator.plugin.util.SystemUtil.BUILD_COMMAND;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum;
import org.soitoolkit.tools.generator.plugin.util.PreferencesUtil;
import org.soitoolkit.tools.generator.plugin.util.SystemUtil;

public class JmsToJmsServiceGeneratorTest {

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
		SystemUtil.delDirs(PROJECT_FOLDER);
		assertEquals(0, SystemUtil.countFiles(PROJECT_FOLDER));
		new IntegrationComponentGenerator(System.out, "org.soitoolkit.refapps.dealernetwork", PROJECT, "1.0-SNAPSHOT", TRANSPORTS, TEST_OUT_FOLDER).startGenerator();
		assertEquals("Missmatch in expected number of created files and folders", 59, SystemUtil.countFiles(PROJECT_FOLDER));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGenerateOrderMgm() throws IOException {

		int noOfFilesBefore = SystemUtil.countFiles(PROJECT_FOLDER);
		
		new JmsToJmsServiceGenerator(System.out, "org.soitoolkit.refapps.dealernetwork", PROJECT, "processOrder", PROJECT_FOLDER + "/trunk").startGenerator();
		assertEquals("Missmatch in expected number of created files and folders", 7, SystemUtil.countFiles(PROJECT_FOLDER) - noOfFilesBefore);
		
		SystemUtil.executeCommand(MAVEN_HOME + "/bin/" + BUILD_COMMAND, PROJECT_FOLDER + "/trunk");
	}
}