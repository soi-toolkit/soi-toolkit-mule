package org.soitoolkit.commons.mule.log;

import static org.junit.Assert.*;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.junit.Test;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.Transformer;
import org.mule.api.transformer.TransformerException;
import org.mule.tck.junit4.FunctionalTestCase;
import org.soitoolkit.commons.logentry.schema.v1.LogEvent;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;
import org.soitoolkit.commons.mule.test.AbstractJmsTestUtil;
import org.soitoolkit.commons.mule.test.ActiveMqJmsTestUtil;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

public class LogIntegrationTest extends FunctionalTestCase {

	private static final String INFO_LOG_QUEUE = "SOITOOLKIT.LOG.INFO";
	private AbstractJmsTestUtil jmsUtil = null;

	@Override
	protected String getConfigResources() {
		return "soitoolkit-mule-jms-connector-activemq-embedded.xml," + 
		       "logtest/sample-common.xml";
	}

    @Override
	protected void doSetUp() throws Exception {
		super.doSetUp();

		doSetUpJms();
    }

	private void doSetUpJms() {
		// TODO: Fix lazy init of JMS connection et al so that we can create jmsutil in the declaration
		// (The embedded ActiveMQ queue manager is not yet started by Mule when jmsutil is delcared...)
		if (jmsUtil == null) jmsUtil = new ActiveMqJmsTestUtil();
		
		// Clear queues used for info logging
		jmsUtil.clearQueues(INFO_LOG_QUEUE);
    }

	@Test
	public void testLogDebugWithJaxbPayload() throws TransformerException, JMSException {
		
        Transformer transformer = (Transformer)muleContext.getRegistry().lookupTransformer("logMsgIn");
        assertNotNull(transformer);
		
		JaxbPayload payload = new JaxbPayload();
		payload.setId("1234");
		MuleMessage msg = new DefaultMuleMessage(payload, muleContext);

		Object result = transformer.transform(msg);
		assertTrue(result instanceof MuleMessage);

		MuleMessage resultMsg = (MuleMessage)result;
		assertTrue(resultMsg.getPayload() instanceof JaxbPayload);

		JaxbPayload jaxbResult = (JaxbPayload)resultMsg.getPayload();
		assertEquals(payload.getId(),jaxbResult.getId());
		
		// Read the info-log-queue and assert that the expected payload is in the log event
		assertLogMessages(1, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><JaxbPayload><id>1234</id></JaxbPayload>");
	}

	private void assertLogMessages(int noOfExpectedLogMsgs, String expectedPayload) throws JMSException {
        List<Message> msgs = jmsUtil.browseMessagesOnQueue(INFO_LOG_QUEUE);
        assertEquals("Incorrect number of error-messages", noOfExpectedLogMsgs, msgs.size());
        int i = 0;
        for (Message errMessage : msgs) {
            i++;
            String infoMsg = ((TextMessage)errMessage).getText();
            JaxbUtil ju = new JaxbUtil(LogEvent.class);
            LogEvent le = (LogEvent) ju.unmarshal(infoMsg);
            String payload = le.getLogEntry().getPayload();
            assertTrue("Expected payload not found in info-log-message #" + i, expectedPayload.equals(payload));
        }
    }
}
