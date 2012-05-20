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
package org.soitoolkit.commons.xml;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.soitoolkit.commons.xml.XPathUtil.appendXmlFragment;
import static org.soitoolkit.commons.xml.XPathUtil.createDocument;
import static org.soitoolkit.commons.xml.XPathUtil.getDocumentComment;
import static org.soitoolkit.commons.xml.XPathUtil.getFirstValue;
import static org.soitoolkit.commons.xml.XPathUtil.getXPathResult;
import static org.soitoolkit.commons.xml.XPathUtil.getXml;
import static org.soitoolkit.commons.xml.XPathUtil.lookupParameterValue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XPathUtilTest {

	private final String nsPrefix = "ns";
	private final String nsURI = "http://maven.apache.org/POM/4.0.0";
	private final String docComment=" \n"+
"  Licensed to the soi-toolkit project under one or more\n"+
"  contributor license agreements.  See the NOTICE file distributed with\n"+
"  this work for additional information regarding copyright ownership.\n"+
"  The soi-toolkit project licenses this file to You under the Apache License, Version 2.0\n"+
"  (the \"License\"); you may not use this file except in compliance with\n"+
"  the License.  You may obtain a copy of the License at\n"+
" \n"+
"      http://www.apache.org/licenses/LICENSE-2.0\n"+
" \n"+
"  Unless required by applicable law or agreed to in writing, software\n"+
"  distributed under the License is distributed on an \"AS IS\" BASIS,\n"+
"  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"+
"  See the License for the specific language governing permissions and\n"+
"  limitations under the License.\n"+
"\n" +
"DON'T CHANGE THE CONTENT OF THIS COMMENT UNLESS YOU REALLY KNOW WHAT YOU ARE DOING!\n"+
"[soi-toolkit.gen.version=n.n.n]\n"+
"[soi-toolkit.gen.type=services]\n"+
"[soi-toolkit.gen.createDate=2010-09-28T17:48:20.465+02:00]\n"+
"[soi-toolkit.gen.artifactId=sample1]\n";
	
	Document pomDoc = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		InputStream content = getClass().getClassLoader().getResourceAsStream("test-pom.xml");
		pomDoc = createDocument(content);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testXMLDate() {
		System.err.println(XPathUtil.convertDateToXmlDate(new Date()));
	}

	@Test
	public void testGetDocComment() {
		String comment = getDocumentComment(pomDoc);
		assertEquals(docComment, comment);
	}

	@Test
	public void testDocCommentProperties() {
		String comment = getDocumentComment(pomDoc);
		assertEquals("n.n.n", lookupParameterValue("soi-toolkit.gen.version", comment));
	}

	@Test
	public void testRegExp() {
		
		String  s = "dfsdfsd [st1=a] [st2=b] dsfsdf [st3=c]";
		Pattern p = Pattern.compile("\\[st1=[^\\]]*\\]");
		Matcher m = p.matcher(s);
		m.find();
    	String f = m.group();
    	int p1 = f.indexOf('=');
    	int p2 = f.indexOf(']');
    	assertEquals("a", f.substring(p1+1, p2));
	}

	@Test
	public void testExtractGroupIdAndArtifactIdFromPom() {

//		InputStream content = getClass().getClassLoader().getResourceAsStream("org/soitoolkit/tools/generator/util/pom.xml");
//		Document doc = createDocument(content);

		// XPath Query for showing all nodes value

		String parentArtifactId = getFirstValue(getXPathResult(pomDoc, nsPrefix, nsURI, "/ns:project/ns:parent/ns:artifactId/text()"));
		String parentGroupId    = getFirstValue(getXPathResult(pomDoc, nsPrefix, nsURI, "/ns:project/ns:parent/ns:groupId/text()"));

		String artifactId = getFirstValue(getXPathResult(pomDoc, nsPrefix, nsURI, "/ns:project/ns:artifactId/text()"));
		String groupId    = getFirstValue(getXPathResult(pomDoc, nsPrefix, nsURI, "/ns:project/ns:groupId/text()"));

		assertEquals("sample1-parent",   parentArtifactId);
		assertEquals("org.sample",       parentGroupId);
		assertEquals("sample1-services", artifactId);
		assertNull(                      groupId);
	}
	
	@Test
	public void testNormalizer() {
		String xml1 = readFileAsString("src/test/resources/response-expected-result-1.xml");
		String xml2 = readFileAsString("src/test/resources/response-expected-result-2.xml");

		xml1 = XPathUtil.normalizeXmlString(xml1);
		xml2 = XPathUtil.normalizeXmlString(xml2);

		assertEquals(xml1, xml2);
	}
	
	@Test
	public void testLookupSpringImportInConfigFile() throws IOException, SAXException, ParserConfigurationException {
		assertEquals(1, doLookupAndAddSpringImportInConfigFile("test-config-with-searched-element.xml"));
		assertEquals(1, doLookupAndAddSpringImportInConfigFile("test-config-without-searched-element.xml"));
		assertEquals(1, doLookupAndAddSpringImportInConfigFile("test-config-without-spring-beans-element.xml"));
	}

	private int doLookupAndAddSpringImportInConfigFile(String filename) throws IOException, SAXException, ParserConfigurationException {
		InputStream content = getClass().getClassLoader().getResourceAsStream(filename);

		Document configDoc = createDocument(content);

		Map<String, String> namespaceMap = new HashMap<String, String>();
		namespaceMap.put("mule", "http://www.mulesoft.org/schema/mule/core");
		namespaceMap.put("spring", "http://www.springframework.org/schema/beans");
		String xmlFragmentId = "classpath:soitoolkit-mule-jms-xa-connector-activemq-external.xml";
		
		// Lookup the fragment...
		NodeList testList = getXPathResult(configDoc, namespaceMap, "/mule:mule/spring:beans/spring:import/@resource[.='" + xmlFragmentId + "']");

		if (testList.getLength() > 0) return testList.getLength();

	
		// Not found, ok try to add it...
		NodeList rootList = getXPathResult(configDoc, namespaceMap, "/mule:mule/spring:beans");
		Node root = rootList.item(0);
	    
		String xmlFragment = 
			"    <spring:import xmlns:spring=\"http://www.springframework.org/schema/beans\" resource=\"" + xmlFragmentId + "\"/>";

		if (root == null) {
			xmlFragment = 
				"    <spring:beans xmlns:spring=\"http://www.springframework.org/schema/beans\">\n" +
				"    " + xmlFragment + "\n" +
				"    </spring:beans>";
			rootList = getXPathResult(configDoc, namespaceMap, "/mule:mule");
			root = rootList.item(0);
		}
		
    	appendXmlFragment(root, xmlFragment);
		
	    String xml = getXml(configDoc);
	    System.err.println("file: " + filename + ":\n" + xml);
	
		// Lookup the fragment again...
		testList = getXPathResult(configDoc, namespaceMap, "/mule:mule/spring:beans/spring:import/@resource[.='" + xmlFragmentId + "']");
	
		return testList.getLength();
	}

	// -----------

	private static final String DEFAULT_CHARSET = "UTF-8";

	public static String readFileAsString(String filename) {
		return readFileAsString(filename, DEFAULT_CHARSET);
    }

	public static String readFileAsString(String filename, String charset) {
	    try {
			return convertStreamToString(new FileInputStream(filename), charset);
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
        	BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
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

//            	if (logger.isTraceEnabled()) {
//	            	if (linecount % 50000 == 0) {
//	    				logger.trace("Lines read: {}, {} characters and counting...", linecount, size);
//	            	}
//            	}
            }
        } catch (IOException e) {
        	throw new RuntimeException(e);
		} finally {
//        	if (logger.isTraceEnabled()) {
//				logger.trace("Lines read: {}, {} characters", linecount, size);
//        	}
			// Ignore exceptions on call to the close method
            try {if (is != null) is.close();} catch (IOException e) {}
        }
        return sb.toString();
    }
	
	
	
}
