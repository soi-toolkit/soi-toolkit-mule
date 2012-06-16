package org.soitoolkit.tools.generator.util;

import static org.junit.Assert.*;
import static org.soitoolkit.commons.xml.XPathUtil.createDocument;
import static org.soitoolkit.commons.xml.XPathUtil.getXPathResult;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlFileUtilTests {

	private static final Logger log = LoggerFactory.getLogger(XmlFileUtilTests.class);

	@Test
	public void testExistPackageMethod() {
		assertFalse(XmlFileUtil.existsPackageName("",    "org.soitoolkit.package1"));
		assertFalse(XmlFileUtil.existsPackageName(null,  "org.soitoolkit.package1"));

		assertTrue(XmlFileUtil.existsPackageName("org.soitoolkit.package1",  "org.soitoolkit.package1"));
		assertFalse(XmlFileUtil.existsPackageName("org.soitoolkit.package2", "org.soitoolkit.package1"));

		assertTrue(XmlFileUtil.existsPackageName("org.soitoolkit.package1:org.soitoolkit.package2:org.soitoolkit.package3",  "org.soitoolkit.package1"));
		assertTrue(XmlFileUtil.existsPackageName("org.soitoolkit.package1:org.soitoolkit.package2:org.soitoolkit.package3",  "org.soitoolkit.package2"));
		assertTrue(XmlFileUtil.existsPackageName("org.soitoolkit.package1:org.soitoolkit.package2:org.soitoolkit.package3",  "org.soitoolkit.package3"));
		assertFalse(XmlFileUtil.existsPackageName("org.soitoolkit.package2:org.soitoolkit.package3:org.soitoolkit.package4", "org.soitoolkit.package1"));
	}
	
	@Test
	public void testWordCount() {

		assertEquals (0, XmlFileUtil.packageCount("org.soitoolkit.package1:org.soitoolkit.package2:org.soitoolkit.package3",             "org.soitoolkit.package4"));
		assertEquals (1, XmlFileUtil.packageCount("org.soitoolkit.package1:org.soitoolkit.package2:org.soitoolkit.package3",             "org.soitoolkit.package1"));
		assertEquals (0, XmlFileUtil.packageCount("org.soitoolkit.package1.sub:org.soitoolkit.package2.sub:org.soitoolkit.package3.sub", "org.soitoolkit.package1"));
		assertEquals (1, XmlFileUtil.packageCount("org.soitoolkit.package1.sub:org.soitoolkit.package1",                                 "org.soitoolkit.package1"));
		assertEquals (2, XmlFileUtil.packageCount("org.soitoolkit.package1:org.soitoolkit.package2:org.soitoolkit.package1",             "org.soitoolkit.package1"));
	}
	
	@Test
	public void testAddingJaxbContexts() {

		doTestAddingJaxbContext("test-common-without-jaxb-context.xml",                       "org.soitoolkit.package1");
		doTestAddingJaxbContext("test-common-with-jaxb-context-but-without-java-package.xml", "org.soitoolkit.package1");
		doTestAddingJaxbContext("test-common-with-jaxb-context-and-java-package.xml",         "org.soitoolkit.package1");
		doTestAddingJaxbContext("test-common-with-jaxb-context-and-java-package.xml",         "org.soitoolkit.commons.logentry.schema.v1");
		doTestAddingJaxbContext("test-common-with-jaxb-context-and-java-package.xml",         "org.soitoolkit.refapps.sd.crudsample.schema.v1");
		doTestAddingJaxbContext("test-common-with-jaxb-context-and-java-package.xml",         "org.soitoolkit.commons.logentry");
	}

	void doTestAddingJaxbContext(String configFile, String newJavaPackage) {
		String xmlStr = null;
		xmlStr = XmlFileUtil.updateJaxbContextInConfigInputStream(getClass().getResourceAsStream(configFile), newJavaPackage);
		log.debug(xmlStr);
		
		Document configDoc = createDocument(xmlStr);

		Map<String, String> namespaceMap = new HashMap<String, String>();
		namespaceMap.put("mule", "http://www.mulesoft.org/schema/mule/core");
		namespaceMap.put("mulexml", "http://www.mulesoft.org/schema/mule/xml");
		
		// Lookup the packageNames - attribute
		NodeList testList = getXPathResult(configDoc, namespaceMap, "/mule:mule/mulexml:jaxb-context/@packageNames");
		Node packageNamesAttr = testList.item(0);
		String packageNames = packageNamesAttr.getNodeValue();

		// Verify that it exists and in only once
		assertTrue(XmlFileUtil.existsPackageName(packageNames, newJavaPackage));
		assertEquals(1, XmlFileUtil.packageCount(packageNames, newJavaPackage));
	}

}