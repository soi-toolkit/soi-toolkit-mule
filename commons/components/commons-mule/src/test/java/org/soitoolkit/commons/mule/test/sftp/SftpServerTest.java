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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.sshd.common.Session;
import org.apache.sshd.server.SshFile;
import org.junit.Test;

public class SftpServerTest {

	@Test
	public void testGetModifiedNativeFileSystemFactory() {
		SftpServer sftpServer = new SftpServer() {
			@Override
			String getUsername(Session session) {
				return "anyuser";
			}
		};

		String subDirname = SftpServerTest.class.getSimpleName() + "-test";
		File subdir = new File(SftpServer.TARGET_DIR_NAME + File.separator
				+ subDirname);
		subdir.mkdirs();
		assertTrue(subdir.exists());

		Session session = null;
		SshFile sshFile = sftpServer.getModifiedNativeFileSystemFactory()
				.createFileSystemView(session).getFile(subDirname);

		assertTrue(sshFile.doesExist());
		assertTrue(sshFile.isDirectory());
		assertEquals(subdir.getAbsolutePath(), sshFile.getAbsolutePath());
	}
}
