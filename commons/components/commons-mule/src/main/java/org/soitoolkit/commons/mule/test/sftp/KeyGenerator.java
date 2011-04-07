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

package org.soitoolkit.commons.mule.test.sftp;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;

/**
 * Create SSH-keys for use in test with embedded SFTP-server.
 * 
 * @author hakan
 */
public class KeyGenerator {
	private Logger logger = LoggerFactory.getLogger(getClass());

	private String keygenDirPath = "target/ssh";
	private String passphrase = "testonly";

	public static void main(String[] args) {
		new KeyGenerator().generateKeys();
	}

	public void setKeygenDirPath(String keygenDirPath) {
		this.keygenDirPath = keygenDirPath;
	}

	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}

	public void generateKeys() {
		try {
			String keyFilename = keygenDirPath + File.separatorChar + "id_dsa";
			String keyComment = "soitoolkit test key, embedded use only";

			// only create keys if they don't exist
			File privateKeyFile = new File(keyFilename);
			if (privateKeyFile.exists()) {
				logger.debug("keyfile exists, will not create new key: {}",
						privateKeyFile);
				return;
			}

			// create dirs
			File dirs = new File(keygenDirPath);
			if (!dirs.exists() && !dirs.mkdirs()) {
				throw new IOException("could not create dir(s): "
						+ keygenDirPath);
			}

			// generate keys
			logger.debug("generating keys in dir: {}", dirs);
			JSch jsch = new JSch();
			KeyPair kpair = KeyPair.genKeyPair(jsch, KeyPair.DSA);
			kpair.setPassphrase(passphrase);
			kpair.writePrivateKey(keyFilename);
			kpair.writePublicKey(keyFilename + ".pub", keyComment);
			kpair.dispose();
		} catch (Exception e) {
			String errMsg = "Failed to generate SSH keys";
			logger.error(errMsg, e);
			throw new RuntimeException(errMsg, e);
		}

	}
}
