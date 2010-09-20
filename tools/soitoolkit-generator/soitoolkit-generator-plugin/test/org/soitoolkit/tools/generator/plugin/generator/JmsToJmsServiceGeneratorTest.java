package org.soitoolkit.tools.generator.plugin.generator;

import static org.junit.Assert.*;
import static org.soitoolkit.tools.generator.plugin.util.SystemUtil.BUILD_COMMAND;
import static org.soitoolkit.tools.generator.plugin.util.SystemUtil.TEST_OUT_FOLDER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum;
import org.soitoolkit.tools.generator.plugin.util.SystemUtil;

public class JmsToJmsServiceGeneratorTest {

	private static List<TransportEnum> transports = new ArrayList<TransportEnum>();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		transports.add(TransportEnum.JMS);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		SystemUtil.delDirs(TEST_OUT_FOLDER + "/ordermgm");
		assertEquals(0, SystemUtil.countFiles(TEST_OUT_FOLDER + "/ordermgm"));
		new IntegrationComponentGenerator(System.out, "org.soitoolkit.refapps.dealernetwork", "ordermgm", "1.0-SNAPSHOT", transports, TEST_OUT_FOLDER).startGenerator();
		assertEquals("Missmatch in expected number of created files and folders", 59, SystemUtil.countFiles(TEST_OUT_FOLDER + "/ordermgm"));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGenerateOrderMgm() throws IOException {

		int noOfFilesBefore = SystemUtil.countFiles(TEST_OUT_FOLDER + "/ordermgm");
		
		new JmsToJmsServiceGenerator(System.out, "org.soitoolkit.refapps.dealernetwork", "ordermgm", "processOrder", TEST_OUT_FOLDER + "/ordermgm/trunk").startGenerator();
		assertEquals("Missmatch in expected number of created files and folders", 7, SystemUtil.countFiles(TEST_OUT_FOLDER + "/ordermgm") - noOfFilesBefore);
		
		SystemUtil.executeCommand(BUILD_COMMAND, TEST_OUT_FOLDER + "/ordermgm/trunk");
	}
}