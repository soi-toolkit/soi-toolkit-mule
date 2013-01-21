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

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;

/**
 * A wrapper around the Jasypt encryption lib.
 * <p>
 * Encrypting with the same options like the Jasypt command line tools with
 * arguments "input" and "password".
 * 
 * @see org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI
 */
public class EncryptionUtil {

	public static String encrypt(String input, String password) {
		return getEncryptor(password).encrypt(input);
	}

	public static String decrypt(String input, String password) {
		return getEncryptor(password).decrypt(input);
	}

	private static StandardPBEStringEncryptor getEncryptor(String password) {
		EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();
		config.setPassword(password);

		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setConfig(config);
		return encryptor;
	}

}
