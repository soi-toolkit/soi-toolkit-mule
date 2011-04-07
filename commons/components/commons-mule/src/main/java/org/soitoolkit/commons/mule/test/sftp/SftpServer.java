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
import java.security.PublicKey;
import java.util.Arrays;
import java.util.EnumSet;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.util.OsUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This SFTP-server is only intended for integration-testing in a local
 * environment. It does not verify login credentials.
 * 
 * Example: connect from Mac-terminal with: sftp -oPort=2222 muletest2@localhost
 * 
 * @author hakan
 */
public class SftpServer {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private SshServer sshd;
	private int port = 2222;

	public static void main(String[] args) {
		SftpServer server = new SftpServer();
		server.startServer();
	}

	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Inspired by code in org.apache.sshd.SftpTest and
	 * org.apache.sshd.SshServer.main().
	 */
	public void startServer() {
		logger.info("starting server on port {} ...", port);
		try {
			sshd = SshServer.setUpDefaultServer();
			sshd.setPort(port);

			// enable SFTP
			// sshd.setSubsystemFactories(Arrays
			// .<NamedFactory<Command>> asList(new SftpSubsystem.Factory()));

			// PATCH for SSHD version 0.5.0: remove extra bytes at end of
			// transferred file when used with Jsch
			sshd.setSubsystemFactories(Arrays
					.<NamedFactory<Command>> asList(new Patched050SftpSubsystem.Factory()));

			sshd.setCommandFactory(new ScpCommandFactory());
			// if (OsUtils.isUNIX()) {
			// sshd.setShellFactory(new ProcessShellFactory(new String[] {
			// "/bin/sh", "-i", "-l" }, EnumSet
			// .of(ProcessShellFactory.TtyOptions.ONlCr)));
			// } else {
			// sshd.setShellFactory(new ProcessShellFactory(
			// new String[] { "cmd.exe " }, EnumSet.of(
			// ProcessShellFactory.TtyOptions.Echo,
			// ProcessShellFactory.TtyOptions.ICrNl,
			// ProcessShellFactory.TtyOptions.ONlCr)));
			// }

			// set security related stuff
			// Note: always allow logins - this is only for local testing!
			sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(
					"key.ser"));
			sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
				public boolean authenticate(String username, String password,
						ServerSession session) {
					System.err.println("### AUTH using username + password");
					logger.debug("auth using username + password");

					// don't check credentials - for testing only
					return true;
				}
			});
			sshd.setPublickeyAuthenticator(new PublickeyAuthenticator() {
				public boolean authenticate(String username, PublicKey key,
						ServerSession session) {
					System.err.println("### AUTH using public key");
					logger.debug("auth using public key");

					// don't check credentials - for testing only
					return true;
				}
			});

			sshd.start();
			logger.info("started server on port {}", port);
		} catch (IOException e) {
			String errMsg = "Failed to start";
			logger.error(errMsg, e);
			throw new RuntimeException(errMsg, e);
		}
	}

	public void stopServer() {
		logger.info("stopping server...");
		try {
			if (sshd != null) {
				boolean stopImmediately = true;
				sshd.stop(stopImmediately);
				logger.info("stopped server");
			}
		} catch (InterruptedException e) {
			String errMsg = "Failed to stop";
			logger.error(errMsg, e);
			throw new RuntimeException(errMsg, e);
		}
	}

}
