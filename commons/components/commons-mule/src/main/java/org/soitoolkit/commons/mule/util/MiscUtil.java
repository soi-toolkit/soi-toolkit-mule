/* 
 * Licensed to the soi-toolkit project under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The soi-toolkit project licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.soitoolkit.commons.mule.util;

import static org.apache.commons.lang.StringUtils.chomp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Various helper methods that doesn't fit naturally elsewhere for the time being...
 * <b>WARNING:<b> Methods in this class can be moved to more appropriate classes in the future!
 * 
 * @author Magnus Larsson
 *
 */
public class MiscUtil {

	private static final String DEFAULT_CHARSET = "UTF-8";
	private final static Logger logger = LoggerFactory.getLogger(MiscUtil.class);
    private final static String placeholderPrefix = "${";
    private final static String placeholderSuffix = "}";

	/**
     * Hidden constructor.
     */
    private MiscUtil() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }

	public static String readFileAsString(String filename) {
		return readFileAsString(filename, DEFAULT_CHARSET);
    }

	public static String readFileAsString(String filename, String charset) {
	    try {
			return MiscUtil.convertStreamToString(new FileInputStream(filename), charset);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
    public static String convertStreamToString(InputStream is) {
    	return convertStreamToString(is, DEFAULT_CHARSET);
    }
    
    /**
     * Converts an InputStream to a String.
     * 
     * To convert an InputStream to a String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     * 
     * Closes the InputStream after completion of the processing.
     * 
     * @param is
     * @return
     */
    public static String convertStreamToString(InputStream is, String charset) {

    	if (is == null) return null;

    	StringBuilder sb = new StringBuilder();
        String line;

        long linecount = 0;
        long size = 0;
        try {
        	// TODO: Can this be a performance killer if many many lines or is BufferedReader handling that in a good way?
            boolean emptyBuffer = true;
        	BufferedReader reader = new BufferedReader(new InputStreamReader(new BOMStripperInputStream(is), charset));
            while ((line = reader.readLine()) != null) {
            	// Skip adding line break before the first line
            	if (emptyBuffer) {
            		emptyBuffer = false;
            	} else {
                	sb.append('\n');
                	size++;
            	}
            	sb.append(line);
            	linecount++;
            	size += line.length();

            	if (logger.isTraceEnabled()) {
	            	if (linecount % 50000 == 0) {
	    				logger.trace("Lines read: {}, {} characters and counting...", linecount, size);
	        			printMemUsage();            	
	            	}
            	}
            }
        } catch (IOException e) {
        	throw new RuntimeException(e);
		} finally {
        	if (logger.isTraceEnabled()) {
				logger.trace("Lines read: {}, {} characters", linecount, size);
				printMemUsage();            	
        	}
			// Ignore exceptions on call to the close method
            try {if (is != null) is.close();} catch (IOException e) {}
        }
        return sb.toString();
    }

	private static void printMemUsage() {
		int mb = 1024*1024;

		MemoryMXBean mxb = ManagementFactory.getMemoryMXBean(); 
		MemoryUsage hm  = mxb.getHeapMemoryUsage();
		MemoryUsage nhm = mxb.getNonHeapMemoryUsage();
//		int finalizable = mxb.getObjectPendingFinalizationCount();
		
		logger.trace("Heap Memory:  init/used/committed/max=" +  hm.getInit()/mb + "/" +  hm.getUsed()/mb + "/" +  hm.getCommitted()/mb + "/" +  hm.getMax()/mb);
		logger.trace("Non-Heap Mem: init/used/committed/max=" + nhm.getInit()/mb + "/" + nhm.getUsed()/mb + "/" + nhm.getCommitted()/mb + "/" + nhm.getMax()/mb);
//        			logger.trace("finalizable: " + finalizable);

		//Getting the runtime reference from system
		Runtime runtime = Runtime.getRuntime();

		logger.trace("Used/Free/Total/Max:"
			//Print used memory
			+ (runtime.totalMemory() - runtime.freeMemory()) / mb + "/"

			//Print free memory
			+ runtime.freeMemory() / mb + "/"

			//Print total available memory
			+ runtime.totalMemory() / mb + "/"

			//Print Maximum available memory
			+ runtime.maxMemory() / mb);
	}

    static public String parseStringValue(String strVal, ResourceBundle bundle) {
    	return parseStringValue(strVal, convertResourceBundleToProperties(bundle));
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
    
    /** 
     * Removes any leading new lines from the string.
     * A newline is one of &quot;<code>\n</code>&quot;,
     * &quot;<code>\r</code>&quot;, or &quot;<code>\r\n</code>&quot;.</p>
     *  
     * @param string
     * @return
     */
    static public String removeLeadingNewLines(String string) {
    	if (string == null) return string;
    	
    	int pos = 0;
    	int len = string.length();
    	boolean done = false;
    	while (!done) {
    		char c = string.charAt(pos);
    		if (c == '\n' || c == '\r') {
    			pos++;
    		} else {
    			done = true;
    		}

    		if (pos == len) {
    			done = true;
    		}
    	}
    	String result = string.substring(pos);
    	
    	logger.debug("removed " + pos + " new line characters");
    	return result;
    }

    
    /** 
     * Removes any trailing new lines from the string.
     * A newline is one of &quot;<code>\n</code>&quot;,
     * &quot;<code>\r</code>&quot;, or &quot;<code>\r\n</code>&quot;.</p>
     *  
     * @param string
     * @return
     */
    static public String removeTrailingNewLines(String string) {
    	if (string == null) return string;
    	
    	String trimmedString = chomp(string);

    	// Loop until the chomp-method does not remove any more trailing NewLines...
    	while (trimmedString.length() < string.length()) {
			// Try remove one more NewLine
    		string = trimmedString;
			trimmedString = chomp(string);
		}
    	
    	return trimmedString;
    	
    }

    
    // ---------------
    // PRIVATE METHODS
    // ---------------

    
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
    
    /**
     * Convert ResourceBundle into a Properties object.
     *
     * @param resource a resource bundle to convert.
     * @return Properties a properties version of the resource bundle.
     */
    static Properties convertResourceBundleToProperties(ResourceBundle resource) {
        Properties properties = new Properties();

        Enumeration<String> keys = resource.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            properties.put(key, resource.getString(key));
        }

        return properties;
    }
    
}
