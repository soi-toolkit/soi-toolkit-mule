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
package org.soitoolkit.commons.mule.log;

public class LoggingToJmsIsOffIntegrationTest extends LoggingToJmsIsOnIntegrationTest {

	public LoggingToJmsIsOffIntegrationTest() {
		super();

		expectedNumberOfJMSInfoMessages = 0;

		// set properties used in flow as system-properties (we are not using
		// any properties file here)
		System.setProperty("SOITOOLKIT_DO_LOG_TO_JMS", SOITOOLKIT_DO_LOG_TO_JMS_FALSE);
		System.setProperty("SOITOOLKIT_LOG_INFO_QUEUE",
				SOITOOLKIT_LOG_INFO_QUEUE);
		System.setProperty("SOITOOLKIT_LOG_ERROR_QUEUE",
				SOITOOLKIT_LOG_ERROR_QUEUE);
	}

}
