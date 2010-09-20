package org.soitoolkit.tools.generator.plugin.util;


import static org.soitoolkit.tools.generator.plugin.util.XmlUtil.*;
import static junit.framework.Assert.*;

import java.io.InputStream;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

public class XmlUtilTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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
	public void testExtractGroupIdAndArtifactIdFromPom() {

		String nsPrefix = "ns";
		String nsURI = "http://maven.apache.org/POM/4.0.0";
		InputStream content = getClass().getClassLoader().getResourceAsStream("org/soitoolkit/tools/generator/plugin/util/pom.xml");
		Document doc = createDocument(content);

		// XPath Query for showing all nodes value

		String parentArtifactId = getFirstValue(getXPathResult(doc, nsPrefix, nsURI, "/ns:project/ns:parent/ns:artifactId/text()"));
		String parentGroupId    = getFirstValue(getXPathResult(doc, nsPrefix, nsURI, "/ns:project/ns:parent/ns:groupId/text()"));

		String artifactId = getFirstValue(getXPathResult(doc, nsPrefix, nsURI, "/ns:project/ns:artifactId/text()"));
		String groupId    = getFirstValue(getXPathResult(doc, nsPrefix, nsURI, "/ns:project/ns:groupId/text()"));

		assertEquals("sample1-parent", parentArtifactId);
		assertEquals("org.sample",     parentGroupId);
		assertEquals("sample1",        artifactId);
		assertNull(                    groupId);
	}

}
