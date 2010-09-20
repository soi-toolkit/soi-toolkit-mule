package org.soitoolkit.tools.generator.plugin.model;

import static org.junit.Assert.*;

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
		ModelFactory.setModelClass(CustomizedModelImpl.class);
		IModel model = ModelFactory.newModel("groupId", "artifactId", "version", "service", null);
		assertEquals("artifactId-intsvc", model.getServiceProject());
		assertEquals("artifactId-web", model.getWebProject());
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
