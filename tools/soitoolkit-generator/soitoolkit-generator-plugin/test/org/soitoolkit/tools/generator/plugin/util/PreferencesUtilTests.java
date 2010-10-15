package org.soitoolkit.tools.generator.plugin.util;

import static org.junit.Assert.assertTrue;
import static org.soitoolkit.tools.generator.plugin.util.MiscUtil.convertStreamToString;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PreferencesUtilTests {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetDefaultSftpIdentityFile() throws FileNotFoundException {
    	File f = new File(PreferencesUtil.getDefaultSftpIdentityFile());
    	assertTrue(f.exists());
    	assertTrue(f.canRead());
    	assertTrue(convertStreamToString(new FileInputStream(f)).length() > 0);
	}
}
