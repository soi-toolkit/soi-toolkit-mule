package org.soitoolkit.tools.generator.plugin.model.impl;

import java.util.StringTokenizer;

public class ModelUtil {

	/**
     * Hidden constructor.
     */
    private ModelUtil() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }

    static public String capitalize(String name) {
		return name.substring(0,1).toUpperCase() + name.substring(1);
	}

	static public String makeJavaName(String name) {
		// Remove all '-' and capitalize word folloing after...
	    StringBuffer javaName = new StringBuffer();
	    StringTokenizer st = new StringTokenizer(name, "-");

	    while(st.hasMoreTokens()) {
	    	javaName.append(capitalize(st.nextToken()));
	    }

	    return javaName.toString(); 
	}


}
