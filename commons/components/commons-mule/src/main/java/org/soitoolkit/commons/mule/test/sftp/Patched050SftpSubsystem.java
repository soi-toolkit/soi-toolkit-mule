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

import java.io.IOException;

import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.util.Buffer;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Patch for the SftpSubsystem in SSHD release 0.5.0. Remove the extra byte at
 * the end of SSH_FXP_DATA packages that cause files fetched with SFTP to grow
 * one bytes at the end.
 * 
 * @author hakan
 */
public class Patched050SftpSubsystem extends SftpSubsystem {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	// produce instance of THIS class
	public static class Factory implements NamedFactory<Command> {

		public Factory() {
		}

		public Command create() {
			System.err
					.println("SSHD patch: instantiating Patched050SftpSubsystem");
			return new Patched050SftpSubsystem();
		}

		public String getName() {
			return "sftp";
		}
	}

	// override to intercept bad SSH_FXP_DATA package resulting from an
	// SSH_FXP_READ command
	protected void send(Buffer buffer) throws IOException {
		patch_SSH_FXP_DATA(buffer);
		super.send(buffer);
	}

	void patch_SSH_FXP_DATA(Buffer buffer) {
		// get the package type to send
		byte packageType = buffer.getByte();
		// reset read position in buffer
		buffer.rpos(buffer.rpos() - 1);

		if (packageType == SSH_FXP_DATA) {
			// set the buffer wpos-pointer to one position less to undo the
			// addition of the erronous buf.putBoolean(len == 0);
			System.err
					.println("SSHD patch: compensating for extra byte in SSH_FXP_DATA package");
			logger.info("SSHD patch: compensating for extra byte in SSH_FXP_DATA package");
			buffer.wpos(buffer.wpos() - 1);
		}
	}

}
