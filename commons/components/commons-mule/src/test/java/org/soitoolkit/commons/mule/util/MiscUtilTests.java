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

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

public class MiscUtilTests {

	@Test
	public void testParseStringValue_ok() {
		String text = "aaa${VARIABLE}bbb";
		String expectedresult = "aaaVarValuebbb";
    	Properties vars = new Properties();
    	vars.put("VARIABLE", "VarValue");
		assertEquals(expectedresult, MiscUtil.parseStringValue(text, vars));
	}

	@Test
	public void testParseStringValue_NoMatch() {
		String text = "aaa${VARIABLE}bbb";
		String expectedErrorMessage = "Could not resolve placeholder 'VARIABLE'";
		Properties vars = new Properties();
		try {
			MiscUtil.parseStringValue(text, vars);
			fail("Expected exception to the throwed here!");
		} catch (RuntimeException e) {
			assertEquals(RuntimeException.class, e.getClass());
			assertEquals(expectedErrorMessage, e.getMessage());
		}
	}

	@Test
	public void testParseStringValue_NestedVarValues() {
		String text = "aaa${VARIABLE1}bbb";
		String expectedresult = "aaaVarNestedValuebbb";
    	Properties vars = new Properties();
    	vars.put("VARIABLE1", "Var${VARIABLE2}Value");
    	vars.put("VARIABLE2", "Nested");
		assertEquals(expectedresult, MiscUtil.parseStringValue(text, vars));
	}

}
