package org.soitoolkit.tools.generator.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Matchers.anyString;
import static org.soitoolkit.commons.xml.XPathUtil.createDocument;
import static org.soitoolkit.commons.xml.XPathUtil.getXPathResult;
import static org.soitoolkit.tools.generator.util.XmlFileUtil.updateSpringImportInXmlInputStream;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.tools.generator.GeneratorUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlFileUtilTests {

	private static final Logger log = LoggerFactory.getLogger(XmlFileUtilTests.class);

	static private GeneratorUtil gu = null;

	@BeforeClass
	static public void setup() {
		Answer<Object> answer = new Answer<Object>() {
	        public Object answer(InvocationOnMock invocation) {
	            Object[] args = invocation.getArguments();
	            String name = invocation.getMethod().getName();
	            System.err.println(name + ": " + args[0]);
	            return null;
	        }
	    };
		gu = mock(GeneratorUtil.class);
		doAnswer(answer).when(gu).logDebug(anyString());
		doAnswer(answer).when(gu).logInfo(anyString());
		doAnswer(answer).when(gu).logWarn(anyString());
	}
	
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
	
	@Test
	public void testUpdateXmlFileWithSpringImport() {
		
		doTestUpdateXmlFileWithSpringImport("test-common-beans-without-beans-element.xml",             "soitoolkit-mule-http-connector.xml", null, true);
		doTestUpdateXmlFileWithSpringImport("test-common-beans-with-beans-element-without-import.xml", "soitoolkit-mule-http-connector.xml", null, true);
		doTestUpdateXmlFileWithSpringImport("test-common-beans-with-beans-element-with-import.xml",    "soitoolkit-mule-http-connector.xml", null, false);

		doTestUpdateXmlFileWithSpringImport("test-common-default-profile-without-profile.xml",       "soitoolkit-mule-ftp-connector-external.xml", "default", true);
		doTestUpdateXmlFileWithSpringImport("test-common-default-profile-without-ftp-connector.xml", "soitoolkit-mule-ftp-connector-external.xml", "default", true);
		doTestUpdateXmlFileWithSpringImport("test-common-default-profile-with-ftp-connector.xml",    "soitoolkit-mule-ftp-connector-external.xml", "default", false);
	}

	private void doTestUpdateXmlFileWithSpringImport(String configFile, String xmlFragment, String profile, boolean expectedToBeUpdated) {
		
		Holder<Boolean> wasUpdated = new Holder<Boolean>();
		String xmlStr = updateSpringImportInXmlInputStream(gu, getClass().getResourceAsStream(configFile), "some new connector...", xmlFragment, profile, wasUpdated);		
		log.info(xmlStr);
		System.err.println("Updated: " + wasUpdated.value); // + ", xml: " + xmlStr);

		assertEquals(expectedToBeUpdated, wasUpdated.value);
		
		Document configDoc = createDocument(xmlStr);

		Map<String, String> namespaceMap = new HashMap<String, String>();
		namespaceMap.put("mule", "http://www.mulesoft.org/schema/mule/core");
		namespaceMap.put("spring", "http://www.springframework.org/schema/beans");

		// Look up the beans-element, with or without profile-attribute
		String xpathExpression = "/mule:mule/spring:beans";
		if (profile == null) {
			// With no profile we look for the first <beans> - element and set it as root
			xpathExpression += "[not(@profile)]";
		} else {
			// With a profile we look for the <beans> - profile attribute that match the specified profile and set its parent (i.e. the its <beans> - element as root
			xpathExpression += "[@profile='" + profile + "']";
		}
		xpathExpression += "/spring:import/@resource[.='classpath:" + xmlFragment + "']";
		
		NodeList testList = getXPathResult(configDoc, namespaceMap, xpathExpression);
		assertEquals(1, testList.getLength());
	}

	void doTestAddingJaxbContext(String configFile, String newJavaPackage) {
		String xmlStr = XmlFileUtil.updateJaxbContextInConfigInputStream(getClass().getResourceAsStream(configFile), newJavaPackage);
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