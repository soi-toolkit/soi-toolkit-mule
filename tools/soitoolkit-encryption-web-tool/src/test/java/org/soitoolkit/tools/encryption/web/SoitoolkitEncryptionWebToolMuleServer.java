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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.test.StandaloneMuleServer;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

public class SoitoolkitEncryptionWebToolMuleServer {

	public static final String MULE_SERVER_ID = "soitoolkit-encryption-web-tool";

	private static final Logger logger = LoggerFactory
			.getLogger(SoitoolkitEncryptionWebToolMuleServer.class);
	private static final RecursiveResourceBundle rb = new RecursiveResourceBundle(
			"soitoolkit-encryption-web-tool-config");

	public static void main(String[] args) throws Exception {

		// setup environment for testing
		System.setProperty("app.home", "src/main/app");
		
		// Configure the mule-server:
		// 1. Specify the "soitoolkit-encryption-web-tool-teststubs.xml" file if
		// teststub-services are to be loaded
		// 2. Specify true if all files including the services are to be loaded
		// from the mule-deploy.properties - file
		// 3. Specify false if services are NOT to be loaded from the
		// mule-deploy.properties - file, only common config files will be
		// loaded
		// StandaloneMuleServer muleServer = new
		// StandaloneMuleServer(MULE_SERVER_ID,
		// "soitoolkit-encryption-web-tool-teststubs.xml", true);
		StandaloneMuleServer muleServer = new StandaloneMuleServer(
				MULE_SERVER_ID, null, true);

		// Start the server
		muleServer.run();
	}

	/**
	 * Address based on usage of the servlet-transport and a config-property for
	 * the URI-part
	 * 
	 * @param serviceUrlPropertyName
	 * @return
	 */
	public static String getAddress(String serviceUrlPropertyName) {

		String url = rb.getString(serviceUrlPropertyName);

		logger.info("URL: {}", url);
		return url;

	}
}