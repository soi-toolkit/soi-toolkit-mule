package org.soitoolkit.tools.generator.plugin.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class SystemUtil {
	
	public static final String TEST_OUT_FOLDER = System.getProperty("user.home") + "/Documents/temp/_test";
	public static final String BUILD_COMMAND = "mvn" + (SwtUtil.isWindows() ? ".bat" : "") + " install eclipse:m2eclipse";
//	public static final String BUILD_COMMAND = "/Users/magnuslarsson/Applications/apache-maven-2.2.1/bin/mvn install eclipse:m2eclipse";
//	public static final String BUILD_COMMAND = "mvn -version";
//	public static final String BUILD_COMMAND = "mvn install eclipse:m2eclipse";
//	public static final String BUILD_COMMAND = "mvn -o eclipse:m2eclipse ";

	/**
     * Hidden constructor.
     */
    private SystemUtil() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }

    public static void delDirs(String rootfolder) throws IOException {
		doDelete(new File(rootfolder));
	}

	public static void doDelete(File path) throws IOException {
		if (!path.exists()) return;
		
		if (path.isDirectory()) {
			for (File child : path.listFiles()) {
				doDelete(child);
			}
		}
		if (!path.delete()) {
			throw new IOException("Could not delete " + path);
		}
	}

	public static void executeCommand(String command, String workingDirectory) throws IOException {
		executeCommand(command, workingDirectory, System.out);
	}

	public static void executeCommand(String command, String workingDirectory, PrintStream out) throws IOException {
		BufferedReader input = null;
		try {
			Process p = Runtime.getRuntime().exec(command, null, new File(workingDirectory));
			input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = input.readLine()) != null) {
				out.println(line);
			}

			try {p.waitFor();} catch (InterruptedException e) {}
			
			int retStatus = p.exitValue();
			if (retStatus != 0) {
				throw new IOException("Failed to execute command: [" + command + "]: ret-status = " + retStatus);
			}
		
		} finally {
			if (input != null) try {input.close();} catch (IOException e) {}
		}
	}

	public static int countFiles(String path) throws IOException {
		return countFiles(new File(path));
	}

	public static int countFiles(File path) throws IOException {
		int count = 0;

		if (!path.exists()) return count;

		if (path.isDirectory()) {
			for (File child : path.listFiles()) {
				count += countFiles(child);
			}
		}
		
		return ++count;
	}

}
