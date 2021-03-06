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
package org.soitoolkit.tools.encryption.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class EncryptionUtilTest {

	@Test
	public void testEncryptDecrypt() {
		String input = "the string i like to encrypt";
		String password = "my very secret password for testing ...";

		String result = EncryptionUtil.encrypt(input, password);

		assertFalse(input.equals(result));
		assertFalse(password.equals(result));

		assertEquals(EncryptionUtil.decrypt(result, password), input);
	}

}
