package org.soitoolkit.tools.generator.plugin.generator;

import static org.junit.Assert.*;
import static org.soitoolkit.tools.generator.plugin.util.SystemUtil.BUILD_COMMAND;
import static org.soitoolkit.tools.generator.plugin.util.SystemUtil.TEST_OUT_FOLDER;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum;
import org.soitoolkit.tools.generator.plugin.util.SystemUtil;

public class IntegrationComponentGeneratorTest {

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
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGenerateOrderMgm() throws IOException {
		SystemUtil.delDirs(TEST_OUT_FOLDER + "/ordermgm");
		assertEquals(0, SystemUtil.countFiles(TEST_OUT_FOLDER + "/ordermgm"));
				
		new IntegrationComponentGenerator(System.out, "org.soitoolkit.refapps.dealernetwork", "ordermgm", "1.0-SNAPSHOT", transports, TEST_OUT_FOLDER).startGenerator();
		assertEquals("Missmatch in expected number of created files and folders", 59, SystemUtil.countFiles(TEST_OUT_FOLDER + "/ordermgm"));
		
		SystemUtil.executeCommand(BUILD_COMMAND, TEST_OUT_FOLDER + "/ordermgm/trunk");
	}

	@Test
	public void testGenerateShipping() throws IOException {
		SystemUtil.delDirs(TEST_OUT_FOLDER + "/shipping");
		assertEquals(0, SystemUtil.countFiles(TEST_OUT_FOLDER + "/shipping"));

		new IntegrationComponentGenerator(System.out, "org.soitoolkit.refapps.dealernetwork", "shipping", "1.0-SNAPSHOT", transports, TEST_OUT_FOLDER).startGenerator();
		assertEquals("Missmatch in expected number of created files and folders", 59, SystemUtil.countFiles(TEST_OUT_FOLDER + "/shipping"));

		SystemUtil.executeCommand(BUILD_COMMAND, TEST_OUT_FOLDER + "/shipping/trunk");
	}

	@Test
	public void testGenerateVgrPicsara2melior() throws IOException {
		String grp = "se.vgregion.pilot";
		String name = "picsara2melior-sas-003";
		// TODO Don't fix '-' right now...
		name = "picsara2melior_sas_003";
		
		SystemUtil.delDirs(TEST_OUT_FOLDER + "/" + name);
		assertEquals(0, SystemUtil.countFiles(TEST_OUT_FOLDER + "/" + name));

		new IntegrationComponentGenerator(System.out, grp, name, "1.0-SNAPSHOT", transports, TEST_OUT_FOLDER).startGenerator();
		assertEquals("Missmatch in expected number of created files and folders", 57, SystemUtil.countFiles(TEST_OUT_FOLDER + "/" + name));

		SystemUtil.executeCommand(BUILD_COMMAND, TEST_OUT_FOLDER + "/" + name + "/trunk");
	}

	@Test
	public void testGenerateVfSveFasktura() throws IOException {
		String grp = "se.volvofinans.infobus.faktura";
		String name = "faktura3";
		
		SystemUtil.delDirs(TEST_OUT_FOLDER + "/" + name);
		assertEquals(0, SystemUtil.countFiles(TEST_OUT_FOLDER + "/" + name));

		new IntegrationComponentGenerator(System.out, grp, name, "1.0-SNAPSHOT", transports, TEST_OUT_FOLDER).startGenerator();
		assertEquals("Missmatch in expected number of created files and folders", 59, SystemUtil.countFiles(TEST_OUT_FOLDER + "/" + name));

		SystemUtil.executeCommand(BUILD_COMMAND, TEST_OUT_FOLDER + "/" + name + "/trunk");
	}
}