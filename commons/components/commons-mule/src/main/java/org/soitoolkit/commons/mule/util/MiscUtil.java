package org.soitoolkit.commons.mule.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Various helper methods that doesn't fit naturally elsewhere for the time being...
 * 
 * @author Magnus Larsson
 *
 */
public class MiscUtil {

	private final static Logger logger = LoggerFactory.getLogger(MiscUtil.class);
    private final static String placeholderPrefix = "${";
    private final static String placeholderSuffix = "}";

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

    static public String parseStringValue(String strVal, Properties props) {

    	StringBuffer buf = new StringBuffer(strVal);

    	int startIndex = strVal.indexOf(placeholderPrefix);
    	while (startIndex != -1) {
    		int endIndex = findPlaceholderEndIndex(buf, startIndex);
    		if (endIndex != -1) {
    			String placeholder = buf.substring(startIndex + placeholderPrefix.length(), endIndex);

    			String propVal = props.getProperty(placeholder);
    			
    			if (propVal != null) {
    				
					// Recursive invocation, parsing placeholders contained in the previously resolved placeholder value.
    				// E.g. a variable value like: VARIABLE1=Var${VARIABLE2}Value
					propVal = parseStringValue(propVal, props);

					buf.replace(startIndex, endIndex + placeholderSuffix.length(), propVal);
    				if (logger.isTraceEnabled()) {
    					logger.trace("Resolved placeholder '" + placeholder + "'");
    				}
    				startIndex = buf.indexOf(placeholderPrefix, startIndex + propVal.length());
    			}
    			else {
    				throw new RuntimeException("Could not resolve placeholder '" + placeholder + "'");
    			}
    		}
    		else {
    			startIndex = -1;
    		}
    	}
    	return buf.toString();
    }
    
    static private int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
    	int index = startIndex + placeholderPrefix.length();
    	int withinNestedPlaceholder = 0;
    	while (index < buf.length()) {
    		if (StringUtils.substringMatch(buf, index, placeholderSuffix)) {
    			if (withinNestedPlaceholder > 0) {
    				withinNestedPlaceholder--;
    				index = index + 1;
    			}
    			else {
    				return index;
    			}
    		}
    		else if (StringUtils.substringMatch(buf, index, placeholderPrefix)) {
    			withinNestedPlaceholder++;
    			index = index + placeholderPrefix.length();
    		}
    		else {
    			index++;
    		}
    	}
    	return -1;
    }    
    
}
