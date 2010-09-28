package org.soitoolkit.tools.generator.plugin.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class PropertyFileUtil {

	/**
     * Hidden constructor.
     */
    private PropertyFileUtil() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }

	static public PrintWriter openPropertyFileForAppend(String outputFolder, String propertyFile) throws IOException {
		String propFile = outputFolder + "/src/environment/" + propertyFile + ".properties";

		// TODO: Replace with sl4j!
		System.err.println("Appending to property file: " + propFile);
		
	    return new PrintWriter(new BufferedWriter(new FileWriter(propFile, true)));
	}
    
}