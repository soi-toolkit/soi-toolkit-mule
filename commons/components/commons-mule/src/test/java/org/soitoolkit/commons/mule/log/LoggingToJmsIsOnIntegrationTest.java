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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.junit.Test;
import org.mule.api.MuleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.test.AbstractJmsTestUtil;
import org.soitoolkit.commons.mule.test.ActiveMqJmsTestUtil;
import org.soitoolkit.commons.mule.test.junit4.AbstractTestCase;

public class LoggingToJmsIsOnIntegrationTest extends AbstractTestCase {
	private static final Logger log = LoggerFactory
			.getLogger(LoggingToJmsIsOnIntegrationTest.class);

	private static final String TEST_LOGGING_TO_JMS_IN_VM_QUEUE = "testLoggingToJmsInQueue";
	private static final String TEST_LOGGING_TO_JMS_OUT_VM_QUEUE = "testLoggingToJmsOutQueue";

	// default queues are:
	// SOITOOLKIT_LOG_INFO_QUEUE=SOITOOLKIT.LOG.INFO
	// SOITOOLKIT_LOG_ERROR_QUEUE=SOITOOLKIT.LOG.ERROR
	static final String SOITOOLKIT_LOG_INFO_QUEUE = "TEST.LOGGING.TO.JMS.LOG.INFO.QUEUE";
	static final String SOITOOLKIT_LOG_ERROR_QUEUE = "TEST.LOGGING.TO.JMS.LOG.ERROR.QUEUE";
	// static final String TP_DO_LOG_TO_JMS_TRUE = "true";
	static final String SOITOOLKIT_DO_LOG_TO_JMS_FALSE = "false";
	static final String TEST_MULE_SERVER_ID = "test-logging-to-jms-mule-server-id";
	private StringWriter logWriter;
	private WriterAppender log4jAppender;
	private AbstractJmsTestUtil jmsUtil = null;
	protected int expectedNumberOfJMSInfoMessages = 2;

	public LoggingToJmsIsOnIntegrationTest() {
		// Only start up Mule once to make the tests run faster...
		// Set to false if tests interfere with each other when Mule is started
		// only once.
		setDisposeContextPerClass(true);

		// set properties used in flow as system-properties (we are not using
		// any properties file here)
		// System.setProperty("TP_DO_LOG_TO_JMS", TP_DO_LOG_TO_JMS_TRUE);
		// clear property to make sure the default behaviour works as expected
		System.clearProperty("SOITOOLKIT_DO_LOG_TO_JMS");
		System.setProperty("SOITOOLKIT_LOG_INFO_QUEUE",
				SOITOOLKIT_LOG_INFO_QUEUE);
		System.setProperty("SOITOOLKIT_LOG_ERROR_QUEUE",
				SOITOOLKIT_LOG_ERROR_QUEUE);

		// set mule server id (is done automatically in the context by Mule when
		// doing a standalone deployment)
		// Note: needed to secure that logs contain the serverId
		System.setProperty("mule.serverId", TEST_MULE_SERVER_ID);
	}

	protected String getConfigResources() {
		// set properties used in flow as system-properties (we are not using
		// any properties file here)
		System.setProperty("TEST_LOGGING_TO_JMS_IN_VM_QUEUE", TEST_LOGGING_TO_JMS_IN_VM_QUEUE);
		System.setProperty("TEST_LOGGING_TO_JMS_OUT_VM_QUEUE",
				TEST_LOGGING_TO_JMS_OUT_VM_QUEUE);
		return "soitoolkit-mule-jms-connector-activemq-embedded.xml,"
				+ "logtest/test-logging-to-jms-flow.xml,"
				+ "logtest/teststub-logging-to-jms-flow.xml";
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		doSetUpJms();
		addLog4jAppenderForLogVerification();
	}

	@Override
	protected void doTearDown() throws Exception {
		super.doTearDown();
		removeLog4jAppenderForLogVerification();
	}

	private void doSetUpJms() {
		// TODO: Fix lazy init of JMS connection et al so that we can create
		// jmsutil in the declaration
		// (The embedded ActiveMQ queue manager is not yet started by Mule when
		// jmsutil is delcared...)
		if (jmsUtil == null)
			jmsUtil = new ActiveMqJmsTestUtil();
		// Clear queues used for error handling
		jmsUtil.clearQueues(SOITOOLKIT_LOG_INFO_QUEUE,
				SOITOOLKIT_LOG_ERROR_QUEUE);
	}

	protected void addLog4jAppenderForLogVerification() throws Exception {
		logWriter = new StringWriter();
		PatternLayout layout = new PatternLayout("%d %-5p [%t] %-30c - %m%n");
		log4jAppender = new WriterAppender(layout, logWriter);
		org.apache.log4j.Logger.getRootLogger().addAppender(log4jAppender);
	}

	protected void removeLog4jAppenderForLogVerification() throws Exception {
		org.apache.log4j.Logger.getRootLogger().addAppender(log4jAppender);
	}

	private void assertMuleServerIdIsPresentInLog() {
		String logStr = logWriter.toString();
		assertTrue("mule server id present",
				logStr.contains("ComponentId=test-logging-to-jms-mule-server-id"));
		assertFalse("mule server id not undefined",
				logStr.contains("ComponentId=UNKNOWN.MULE_CONTEXT"));
	}

	@Test
	public void testLogging() throws JMSException {
		Map<String, String> props = new HashMap<String, String>();
		String receivingService = "teststub-logging-to-jms-flow";
		final int timeout = 5000;

		String input = "test message";

		// Setup inbound endpoint for vm-transport
		String inboundEndpoint = "vm://" + TEST_LOGGING_TO_JMS_IN_VM_QUEUE
				+ "?connector=soitoolkit-vm-connector";

		// Invoke the service and wait for the transformed message to arrive at
		// the receiving teststub service
		MuleMessage reply = dispatchAndWaitForServiceComponent(inboundEndpoint,
				input, props, receivingService, timeout);

		assertTrue("assert flow works so we can trust logging to work",
				reply.getPayload() instanceof String);
		assertEquals("assert flow works so we can trust logging to work",
				input, (String) reply.getPayload());

		assertLoggingQueues();
		assertMuleServerIdIsPresentInLog();
	}

	private void assertLoggingQueues() {
		assertEquals("JMS info message(s)", expectedNumberOfJMSInfoMessages,
				jmsUtil.browseMessagesOnQueue(SOITOOLKIT_LOG_INFO_QUEUE).size());
		assertEquals("JMS error message(s)", 0,
				jmsUtil.browseMessagesOnQueue(SOITOOLKIT_LOG_ERROR_QUEUE)
						.size());

	}

}
