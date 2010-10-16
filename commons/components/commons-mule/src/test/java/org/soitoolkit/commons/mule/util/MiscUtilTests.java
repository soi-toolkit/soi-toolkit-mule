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
