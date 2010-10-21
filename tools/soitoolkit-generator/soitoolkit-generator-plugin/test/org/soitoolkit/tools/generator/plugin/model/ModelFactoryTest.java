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
package org.soitoolkit.tools.generator.plugin.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;

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
		IModel model = ModelFactory.newModel("groupId", "artifactId", "version", "service", null, null);
		assertEquals("artifactId-services", model.getServiceProject());
		assertEquals("artifactId-web", model.getWebProject());
	}

	@Test
	public void testCustomModelImpl() throws InstantiationException, IllegalAccessException {
		try {
			ModelFactory.setModelClass(CustomizedModelImpl.class);
			IModel model = ModelFactory.newModel("groupId", "artifactId", "version", "service", null, null);
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
			IModel model = ModelFactory.newModel("groupId", "artifactId", "version", "service", null, null);
			assertEquals("artifactId-module-intsvc", model.getServiceProject());
			assertEquals("modules/intsvc", model.getServiceProjectFilepath());
			assertEquals("applications/integrations", model.getWebProjectFilepath());
			assertEquals("applications/integration-teststubs", model.getTeststubWebProjectFilepath());
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
