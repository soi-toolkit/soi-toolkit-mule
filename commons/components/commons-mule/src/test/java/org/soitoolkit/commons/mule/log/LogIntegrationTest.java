package org.soitoolkit.commons.mule.log;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.junit.Test;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.Transformer;
import org.mule.api.transformer.TransformerException;
import org.mule.message.ExceptionMessage;
import org.mule.tck.junit4.FunctionalTestCase;
import org.soitoolkit.commons.logentry.schema.v1.LogEvent;
import org.soitoolkit.commons.logentry.schema.v1.LogLevelType;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;
import org.soitoolkit.commons.mule.test.AbstractJmsTestUtil;
import org.soitoolkit.commons.mule.test.ActiveMqJmsTestUtil;

public class LogIntegrationTest extends FunctionalTestCase {

	private static final String INFO_LOG_QUEUE = "SOITOOLKIT.LOG.INFO";
	private static final String ERROR_LOG_QUEUE = "SOITOOLKIT.LOG.ERROR";
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
		
		jmsUtil.clearQueues(ERROR_LOG_QUEUE);
    }

	/**
	 * Test the old way of handling logging of JAXB payloads, using soi-toolkits deprecated jaxb-transformer, see config-file for details
	 * 
	 * @throws TransformerException
	 * @throws JMSException
	 */
	@Test
	public void testLogDebugWithJaxbPayload_oldStyle() throws TransformerException, JMSException {
	    doTestLogDebugWithJaxbPayload("logMsgIn");
	}

	/**
	 * Test the new way of handling logging of JAXB payloads, using mule's build-in support for jaxb, see config-file for details
	 * 
	 * @throws TransformerException
	 * @throws JMSException
	 */
	@Test
	public void testLogDebugWithJaxbPayload_newStyle() throws TransformerException, JMSException {
		doTestLogDebugWithJaxbPayload("logMsgOut");
	}
	
	/**
	 * Test the new way of handling logging of JAXB payloads, using mule's build-in support for jaxb, see config-file for details
	 * 
	 * @throws TransformerException
	 * @throws JMSException
	 */
	@Test
	public void testLogWarnWithJaxbPayload_newStyle() throws TransformerException, JMSException {
		doTestLogWarningWithJaxbPayload("logWarnMsg");
	}
	
	

	private void doTestLogDebugWithJaxbPayload(String transformerName) throws TransformerException, JMSException {

		Transformer transformer = (Transformer)muleContext.getRegistry().lookupTransformer(transformerName);
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
		
		// Read the info-log-queue and assert that the eexpected payload is in the log event
		assertLogMessages(1, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><JaxbPayload><id>1234</id></JaxbPayload>", LogLevelType.INFO);
	}
	
	private void assertLogMessages(int noOfExpectedLogMsgs, String expectedPayload, LogLevelType expectedLogLevel) throws JMSException {
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
            assertEquals("Expected loglevel not found in log-message #" + i, expectedLogLevel, le.getLogEntry().getMessageInfo().getLevel());
        }
    }
	
	private void doTestLogWarningWithJaxbPayload(String transformerName) throws TransformerException, JMSException {

		Transformer transformer = (Transformer)muleContext.getRegistry().lookupTransformer(transformerName);
	    assertNotNull(transformer);
		
		JaxbPayload payload = new JaxbPayload();
		payload.setId("1234");

		String expectedExceptionMessage = "My Exception Message";
		
		MuleEvent muleEventMock = mock(MuleEvent.class);
		MuleMessage orginalPayload = new DefaultMuleMessage(payload, muleContext);
		when(muleEventMock.getMessage()).thenReturn(orginalPayload);
		ExceptionMessage errorMessage = new ExceptionMessage(muleEventMock, new RuntimeException(expectedExceptionMessage), "", null);
		
		MuleMessage msg = new DefaultMuleMessage(errorMessage, muleContext);
		
		Object result = transformer.transform(msg);
		
		assertTrue(result instanceof MuleMessage);
		MuleMessage resultMsg = (MuleMessage)result;
		assertTrue(resultMsg.getPayload() instanceof ExceptionMessage);
		assertTrue(((ExceptionMessage)resultMsg.getPayload()).getPayload() instanceof JaxbPayload);

		JaxbPayload jaxbResult = (JaxbPayload)((ExceptionMessage)resultMsg.getPayload()).getPayload();
		assertEquals(payload.getId(),jaxbResult.getId());
		
		// Read the error-log-queue and assert that the expected payload is in the log event
		assertErrorLogMessages(1, expectedExceptionMessage, LogLevelType.WARNING);
	}
	
	private void assertErrorLogMessages(int noOfExpectedLogMsgs, String expectedExceptionMessage, LogLevelType expectedLogLevel) throws JMSException {
        List<Message> msgs = jmsUtil.browseMessagesOnQueue(ERROR_LOG_QUEUE);
        assertEquals("Incorrect number of error-messages", noOfExpectedLogMsgs, msgs.size());
        int i = 0;
        for (Message errMessage : msgs) {
            i++;
            String infoMsg = ((TextMessage)errMessage).getText();
            JaxbUtil ju = new JaxbUtil(LogEvent.class);
            LogEvent le = (LogEvent) ju.unmarshal(infoMsg);
            
            assertEquals("Expected exception message not found in error-log-message #" + i, expectedExceptionMessage, le.getLogEntry().getMessageInfo().getException().getExceptionMessage());
            assertEquals("Expected loglevel not found in error-log-message #" + i, expectedLogLevel, le.getLogEntry().getMessageInfo().getLevel());
        }
    }
}
