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
package org.soitoolkit.commons.mule.crossref;

import static org.junit.Assert.*;

import java.util.MissingResourceException;

import org.junit.Test;

/**
 * Tests for the property file based implementation of CrossRef
 * 
 * @author magnus larsson
 *
 */
public class CrossRefPropertyFileImplTest {

	@Test
	public void testLookup() {
		CrossReferencePropertyFileImpl cr = new CrossReferencePropertyFileImpl();
		cr.setPropertyFile("crossRef-test");
		
		assertEquals("1", cr.lookup("A"));
		assertEquals("2", cr.lookup("B"));
		try {
			cr.lookup("C");
			fail("should have caused an MissingResourceException here!");
		} catch (CrossReferenceException e) {
			assertEquals("C", e.getKey());
			assertEquals("Unknown key=C", e.getMessage());
		}
	}

	@Test
	public void testLookupWithDefaultValue() {
		CrossReferencePropertyFileImpl cr = new CrossReferencePropertyFileImpl();
		cr.setPropertyFile("crossRef-test");
		
		assertEquals("1", cr.lookup("A", "0"));
		assertEquals("2", cr.lookup("B", "0"));
		assertEquals("0", cr.lookup("C", "0"));
	}

	@Test
	public void testErrorPropertyFileNotFound() {
		CrossReferencePropertyFileImpl cr = new CrossReferencePropertyFileImpl();

		try {
			cr.setPropertyFile("crossRef-test-missing");
			fail("should have caused an MissingResourceException here!");
		} catch (MissingResourceException e) {
		}

	}

}
