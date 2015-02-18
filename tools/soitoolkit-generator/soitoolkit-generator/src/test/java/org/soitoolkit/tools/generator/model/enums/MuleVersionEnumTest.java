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
package org.soitoolkit.tools.generator.model.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;

public class MuleVersionEnumTest {
    @Test
    public void testIsEEVersion() {
        assertTrue(MuleVersionEnum.MULE_3_4_0_EE.isEEVersion());
        assertFalse(MuleVersionEnum.MULE_3_4_0.isEEVersion());
    }

    @Test
    public void testIsDeprecatedVersion() {
        assertTrue(MuleVersionEnum.MULE_3_3_1_DEPRECATED.isDeprecatedVersion());
        assertFalse(MuleVersionEnum.MULE_3_4_0.isDeprecatedVersion());
    }

    @Test
	public void testVerNoNumbersOnly() {
		assertEquals("340", MuleVersionEnum.MULE_3_4_0_EE.getVerNoNumbersOnly());
		assertEquals("340", MuleVersionEnum.MULE_3_4_0.getVerNoNumbersOnly());
	}

    @Test
    public void testAllVersions() {
        assertEquals("[MULE_3_3_1_DEPRECATED, MULE_3_4_0, MULE_3_4_0_EE, MULE_3_5_0]", Arrays.asList(MuleVersionEnum.values()).toString());
    }

    @Test
    public void testNonDeprecatedVersions() {
        assertEquals("[MULE_3_4_0, MULE_3_4_0_EE, MULE_3_5_0]", MuleVersionEnum.getNonDeprecatedVersions().toString());
    }

}