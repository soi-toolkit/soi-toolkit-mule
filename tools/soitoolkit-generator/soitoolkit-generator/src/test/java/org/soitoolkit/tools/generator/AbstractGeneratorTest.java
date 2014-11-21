package org.soitoolkit.tools.generator;

import static org.soitoolkit.tools.generator.util.SystemUtil.BUILD_COMMAND;
import static org.soitoolkit.tools.generator.util.SystemUtil.CLEAN_COMMAND;
import static org.soitoolkit.tools.generator.util.SystemUtil.ECLIPSE_AND_TEST_REPORT_COMMAND;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.soitoolkit.tools.generator.model.enums.TransportEnum;
import org.soitoolkit.tools.generator.util.PreferencesUtil;
import org.soitoolkit.tools.generator.util.SystemUtil;

/**
 * Base class for generator tests.
 */
public abstract class AbstractGeneratorTest {
	
	protected static final List<TransportEnum> TRANSPORTS = new ArrayList<TransportEnum>();
	protected static final String TEST_OUT_FOLDER = PreferencesUtil.getDefaultRootFolder() + "/jUnitTests";
	protected static final String VERSION = "1.0-SNAPSHOT";
	
	protected void performMavenBuild(String projectFolder) throws IOException {

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
