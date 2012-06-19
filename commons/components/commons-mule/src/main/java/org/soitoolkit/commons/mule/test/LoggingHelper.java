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
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * Helper for redirecting log statements to console only during test execution.
 * <p>
 * Enables us to have only one log4j.xml file with production settings
 * (rolling-file appender) and still get console output in test- and local
 * developer usage mode. See soi-tk issue #254.
 * 
 * @author Hakan Dahl
 */
public class LoggingHelper {
	private static final String LOG4J_LAYOUT_CONVERSION_PATTERN = "%d %-5p %-30C - %m%n";
	static final String LOG4J_CONSOLE_APPENDER_NAME = "soitoolkit-console-appender: "
			+ LoggingHelper.class.getName();
	private static String NEWLINE = System.getProperty("line.separator");
	private static final String LOG_TO_CONSOLE_DIRECT_RECORD_DELIMITER = "------------------------------------------------------------";
	private static final String LOG_TO_CONSOLE_DIRECT_RECORD_PREFIX = LOG_TO_CONSOLE_DIRECT_RECORD_DELIMITER
			+ NEWLINE + LoggingHelper.class.getName() + ": ";
	private static final String LOG_TO_CONSOLE_DIRECT_RECORD_SUFFIX = NEWLINE
			+ LOG_TO_CONSOLE_DIRECT_RECORD_DELIMITER;

	/**
	 * Redirect all logging to console only.
	 * <p>
	 * Will disable all other kinds of logging to file etc
	 * 
	 */
	public static void logToConsoleOnly() {
		new LoggingHelper().reconfigureLog4jAppendersForLogger(Logger
				.getRootLogger());
	}

	void reconfigureLog4jAppendersForLogger(Logger logger) {
		removeAllLog4jAppendersExceptOurOwnConsoleAppender(logger);

		if (logger.getAppender(LOG4J_CONSOLE_APPENDER_NAME) == null) {
			logToConsoleDirect("Adding log4j console appender programatically");
			logger.addAppender(createLog4jConsoleAppender());
		}
	}

	ConsoleAppender createLog4jConsoleAppender() {
		Layout layout = new PatternLayout(LOG4J_LAYOUT_CONVERSION_PATTERN);
		ConsoleAppender consoleAppender = new ConsoleAppender(layout,
				ConsoleAppender.SYSTEM_OUT);
		consoleAppender.setName(LOG4J_CONSOLE_APPENDER_NAME);
		return consoleAppender;
	}

	void removeAllLog4jAppendersExceptOurOwnConsoleAppender(Logger logger) {
		Enumeration<Appender> appenders = logger.getAllAppenders();
		while (appenders.hasMoreElements()) {
			Appender appender = appenders.nextElement();
			if (!LOG4J_CONSOLE_APPENDER_NAME.equals(appender.getName())) {
				logToConsoleDirect("Removing log4j appender: name="
						+ appender.getName() + ", class=" + appender.getClass());
				logger.removeAppender(appender);
			}
		}
	}

	/**
	 * Always log to console, regardless of any logging-framework setting.
	 * <p>
	 * Useful to make sure output will always appear, even if logging framework
	 * hasn't enabled a certain log-level.
	 * 
	 * @param msg
	 */
	void logToConsoleDirect(String msg) {
		StringBuilder sb = new StringBuilder();
		sb.append(LOG_TO_CONSOLE_DIRECT_RECORD_PREFIX);
		sb.append(msg);
		sb.append(LOG_TO_CONSOLE_DIRECT_RECORD_SUFFIX);
		System.out.println(sb);
	}
}
