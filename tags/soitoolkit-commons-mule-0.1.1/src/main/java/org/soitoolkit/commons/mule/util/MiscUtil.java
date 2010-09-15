package org.soitoolkit.commons.mule.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Various helper methods that doesn't fit naturally elsewhere for the time being...
 * 
 * @author Magnus Larsson
 *
 */
public class MiscUtil {

    /**
     * Hidden constructor.
     */
    private MiscUtil() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }

    public static String convertStreamToString(InputStream is) {
    	return convertStreamToString(is, "UTF-8");
    }
    
    /**
     * To convert an InputStream to a String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     * 
     * @param is
     * @return
     */
    public static String convertStreamToString(InputStream is, String charset) {

    	if (is == null) return null;

    	StringBuilder sb = new StringBuilder();
        String line;

        try {
        	// TODO: Can this be a performance killer if many many lines or is BufferedReader handling that in a good way?
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
        	throw new RuntimeException(e);
		} finally {
			// Ignore exceptions on call to the close method
            try {is.close();} catch (IOException e) {}
        }
        return sb.toString();
    }

}
