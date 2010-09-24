package org.soitoolkit.tools.generator.plugin.model;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import groovy.lang.GroovyClassLoader;

import org.codehaus.groovy.control.CompilationFailedException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ModelFactoryTest {
	
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
	public void testDefaultModelImpl() {
		IModel model = ModelFactory.newModel("groupId", "artifactId", "version", "service", null);
		assertEquals("artifactId", model.getServiceProject());
		assertEquals("artifactId-web", model.getWebProject());
	}

	@Test
	public void testCustomModelImpl() throws InstantiationException, IllegalAccessException {
		try {
			ModelFactory.setModelClass(CustomizedModelImpl.class);
			IModel model = ModelFactory.newModel("groupId", "artifactId", "version", "service", null);
			assertEquals("artifactId-intsvc", model.getServiceProject());
			assertEquals("artifactId-web", model.getWebProject());
		} finally {
			ModelFactory.resetModelClass();
		}
	}
	
	@Test
	public void testGroovyModelImpl() throws CompilationFailedException, IOException {

		try {
//			URL url = new URL("http://soi-toolkit.googlecode.com/svn/trunk/tools/soitoolkit-generator/soitoolkit-generator-plugin/test/org/soitoolkit/tools/generator/plugin/model/GroovyModelImpl.groovy");
			URL url = new URL("file:test/org/soitoolkit/tools/generator/plugin/model/GroovyModelImpl.groovy");	
			assertNotNull("Groovy class not found", url);

			ModelFactory.setModelGroovyClass(url);
			IModel model = ModelFactory.newModel("groupId", "artifactId", "version", "service", null);
			assertEquals("artifactId-svc", model.getServiceProject());
			assertEquals("composites/artifactId-svc", model.getServiceProjectFilepath());
			assertEquals("modules/artifactId-web", model.getWebProjectFilepath());
			assertEquals("modules/artifactId-teststub-web", model.getTeststubWebProjectFilepath());
		} finally {
			ModelFactory.resetModelClass();
		}
	}

	@Test
	public void testCustomModelImpl_error() throws InstantiationException, IllegalAccessException {
		try {
			ModelFactory.setModelClass(String.class);
			fail("Expected IllegalArgumentException to be throwed");
		} catch (IllegalArgumentException e) {
			// Expected exception catched!
		}
	}	
}
