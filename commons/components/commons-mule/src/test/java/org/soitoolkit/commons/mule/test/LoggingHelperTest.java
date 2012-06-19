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
package org.soitoolkit.commons.mule.test;

import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class LoggingHelperTest {

	@Test
	@Ignore("ignore since this test sets a global state in the log4j-rootLogger, which affects the whole testsuite")
	public void testLogToConsoleOnly() {
		LoggingHelper.logToConsoleOnly();
	}

	@Test
	public void testReconfigureLog4jAppendersForLogger() {
		LoggingHelper loggingHelper = new LoggingHelper();
		// arrange
		Logger testLogger = Logger
				.getLogger("testReconfigureLog4jAppendersForLogger-do-not-share-this-logger");
		ConsoleAppender testAppender = new ConsoleAppender();
		testAppender.setName("testAppender");
		testLogger.addAppender(testAppender);

		// act
		loggingHelper.reconfigureLog4jAppendersForLogger(testLogger);

		// assert
		Appender theConsoleAppender = testLogger
				.getAppender(LoggingHelper.LOG4J_CONSOLE_APPENDER_NAME);
		Assert.assertNotNull(theConsoleAppender);

		Enumeration<Appender> appenders = testLogger.getAllAppenders();
		int noOfApppenders = 0;
		while (appenders.hasMoreElements()) {
			Appender appender = appenders.nextElement();
			noOfApppenders++;
		}
		Assert.assertEquals(1, noOfApppenders);

		// invoke again
		loggingHelper.reconfigureLog4jAppendersForLogger(testLogger);
		Assert.assertSame(theConsoleAppender, testLogger
				.getAppender(LoggingHelper.LOG4J_CONSOLE_APPENDER_NAME));
	}

}
