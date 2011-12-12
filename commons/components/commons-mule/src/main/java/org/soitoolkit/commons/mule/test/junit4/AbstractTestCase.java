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
package org.soitoolkit.commons.mule.test.junit4;

import static org.mule.context.notification.ComponentMessageNotification.COMPONENT_POST_INVOKE;
import static org.mule.context.notification.ExceptionNotification.EXCEPTION_ACTION;
import static org.junit.Assert.*;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.mule.api.MuleMessage;
import org.mule.api.context.notification.ComponentMessageNotificationListener;
import org.mule.api.context.notification.EndpointMessageNotificationListener;
import org.mule.api.context.notification.ExceptionNotificationListener;
import org.mule.api.context.notification.ServerNotification;
// FIXME: Mule 3.1, no clue what we will get in case of exceptions...
// import org.mule.api.service.ServiceException;
import org.mule.context.notification.ComponentMessageNotification;
import org.mule.context.notification.EndpointMessageNotification;
import org.mule.context.notification.ExceptionNotification;
import org.mule.module.client.MuleClient;
import org.mule.tck.junit4.FunctionalTestCase;
import org.soitoolkit.commons.mule.test.Dispatcher;
import org.soitoolkit.commons.mule.test.DispatcherMuleClientImpl;
import org.soitoolkit.commons.mule.util.MuleUtil;
import org.soitoolkit.commons.mule.util.ValueHolder;

/**
 * Extends the base class in Mule, org.mule.tck.junit4.FuntionalTestCase.
 * 
 * @author Magnus Larsson
 *
 */
public abstract class AbstractTestCase extends FunctionalTestCase {
    
    public AbstractTestCase() {
		super();

		// Ensure that CXF use LOG4J for logging
		System.setProperty("org.apache.cxf.Logger", "org.apache.cxf.common.logging.Log4jLogger");
	}

	/**
	 * Sends the <code>payload</code> and <code>headers</code> to the <code>inboundEndpointAddress</code> and waits <code>timeout</code> ms for a <code>MuleMessage</code> to arrive on outboundEndpoint with the name <code>outboundEndpointName</code>. 
	 * 
	 * Sample usage:
	 * <tt>
	 *	public void testTransferKorttransaktioner() throws Exception {
	 *		String expectedPayload = "Yada, yada, yada...";
	 *
	 *		MuleMessage message = dispatchAndWaitForDelivery(
	 *			"sftp://dfcx0346@vfin8003.volvofinans.net/sftp/vfkonto/ut",
	 *			expectedPayload,
	 *			createFileHeader("from_vfkonto.dat"),
	 *			"volvokort-test-endpoint",
	 *			TIMEOUT);
	 *
	 *		String actualPayload = message.getPayloadAsString();
	 *		assertEquals(expectedPayload, actualPayload); 
	 *	}	 
	 * </tt>
	 * 
	 * @param inboundEndpointAddress
	 * @param payload
	 * @param headers
	 * @param outboundEndpointName
	 * @param action as specified by org.mule.context.notification.EndpointMessageNotification: MESSAGE_RECEIVED, MESSAGE_DISPATCHED, MESSAGE_SENT or MESSAGE_REQUESTED
	 * @param timeout in ms
	 * @return the received MuleMEssage on the outboundEndpoint
	 */
	protected MuleMessage dispatchAndWaitForDelivery(String inboundEndpointAddress, Object payload, Map<String, String> headers, final String outboundEndpointName, final int action, long timeout) {

		// Declare MuleMessage to return
		final ValueHolder<MuleMessage> receivedMessageHolder = new ValueHolder<MuleMessage>();
		
		// Declare countdown latch and listener
		final CountDownLatch latch = new CountDownLatch(1);
		EndpointMessageNotificationListener listener = null;
		MuleClient muleClient = null;

		try {
			// First create a muleClient instance
			muleClient = new MuleClient(muleContext);

			// Next create a listener that listens for dispatch events on the outbound endpoint
			listener = new EndpointMessageNotificationListener() {
				public void onNotification(ServerNotification notification) {
					if (logger.isDebugEnabled()) logger.debug("notification received on " + notification.getResourceIdentifier() + " (action: " + notification.getActionName() + ")");

					// Only care about EndpointMessageNotification
					if (notification instanceof EndpointMessageNotification) {
						EndpointMessageNotification endpointNotification = (EndpointMessageNotification)notification;

						// Extract action and name of the endpoint
						int    actualAction   = endpointNotification.getAction();
						String actualEndpoint = MuleUtil.getEndpointName(endpointNotification);

						// If it is a dispatch event on our outbound endpoint then countdown the latch.
						if (logger.isDebugEnabled()) {
							logger.debug(actualAction == action);
							logger.debug(actualEndpoint.equals(outboundEndpointName));
						}
						if (actualAction == action && actualEndpoint.equals(outboundEndpointName)) {
							if (logger.isDebugEnabled()) logger.debug("Expected notification received on " + actualEndpoint + " (action: " + endpointNotification.getActionName() + "), time to countdown the latch");
							receivedMessageHolder.value = (MuleMessage)endpointNotification.getSource();
							latch.countDown();

						} else {
							if (logger.isDebugEnabled()) logger.debug("A not matching notification received on " + actualEndpoint + " (action: " + endpointNotification.getActionName() + "), continue to wait for the right one...");							
						}
					}
				}
			};

			// Now register the listener
			muleContext.getNotificationManager().addListener(listener);

			// Perform the actual dispatch
			muleClient.dispatch(inboundEndpointAddress, payload, headers);

			// Wait for the delivery to occur...
			if (logger.isDebugEnabled()) logger.debug("Waiting for message to be delivered to the endpoint...");
			boolean workDone = latch.await(timeout, TimeUnit.MILLISECONDS);
			if (logger.isDebugEnabled()) logger.debug((workDone) ? "Message delivered, continue..." : "No message delivered, timeout occurred!");

			// Raise a fault if the test timed out
			assertTrue("Test timed out. It took more than " + timeout + " milliseconds. If this error occurs the test probably needs a longer time out (on your computer/network)", workDone);

		} catch (Exception e) {
			e.printStackTrace();
			fail("An unexpected error occurred: " + e.getMessage());

		} finally {
			// Dispose muleClient 
			muleClient.dispose();

			// Always remove the listener if created
			if (listener != null) muleContext.getNotificationManager().removeListener(listener);
		}
		
		return receivedMessageHolder.value;		
    }

	/**
	 * Waits <code>timeout</code> ms for a <code>MuleMessage</code> to be processed by a service component with the name <code>serviceComponentName</code>. 
	 * 
	 * Sample usage: TBS
	 * 
	 * @param serviceComponentName
	 * @param timeout in ms
	 * @return the MuleMessage sent to the named service component
	 */
	protected MuleMessage waitForServiceComponent(final String serviceComponentName, long timeout) {
		return dispatchAndWaitForServiceComponent(null, serviceComponentName, timeout);
    }

	/**
	 * Use the Dispatcher to send a asynchronous message and waits <code>timeout</code> ms for a <code>MuleMessage</code> to be processed by a service component with the name <code>serviceComponentName</code>. 
	 * 
	 * Sample usage: TBS
	 * 
	 * @param dispatcher
	 * @param serviceComponentName
	 * @param timeout in ms
	 * @return the MuleMessage sent to the named service component
	 */
	protected MuleMessage dispatchAndWaitForServiceComponent(Dispatcher dispatcher, final String serviceComponentName, long timeout) {
		// Declare MuleMessage to return
		final ValueHolder<MuleMessage> receivedMessageHolder = new ValueHolder<MuleMessage>();
		
		// Declare countdown latch and listener
		final CountDownLatch latch = new CountDownLatch(1);
		ComponentMessageNotificationListener listener = null;

		try {
			// Create a listener that listens for invoke events on the named component
			listener = new ComponentMessageNotificationListener() {
				public void onNotification(ServerNotification notification) {

					if (logger.isDebugEnabled()) logger.debug("notification received on " + notification.getResourceIdentifier() + " (action: " + notification.getActionName());

					// Only care about ComponentMessageNotification
					if (notification instanceof ComponentMessageNotification) {
						ComponentMessageNotification componentNotification = (ComponentMessageNotification)notification;

						// Extract action and name of the component
						int    action    = componentNotification.getAction();
						String component = componentNotification.getResourceIdentifier();

						// If it is a post-invoke event (i.e. the processing is done) on our component then countdown the latch.
						if (action == COMPONENT_POST_INVOKE && component.equals(serviceComponentName)) {
							if (logger.isDebugEnabled()) logger.debug("Expected notification received on " + serviceComponentName + " (action: " + componentNotification.getActionName() + "), time to countdown the latch");
							receivedMessageHolder.value = (MuleMessage)componentNotification.getSource();
							latch.countDown();
						}
					}
				}
			};

			// Now register the listener
			muleContext.getNotificationManager().addListener(listener);

			// Perform the actual dispatch, if any...
			if (dispatcher != null) {
				dispatcher.doDispatch();
			}

			// Wait for the delivery to occur...
			if (logger.isDebugEnabled()) logger.debug("Waiting for message to be delivered to the endpoint...");
			boolean workDone = latch.await(timeout, TimeUnit.MILLISECONDS);
			if (logger.isDebugEnabled()) logger.debug((workDone) ? "Message delivered, continue..." : "No message delivered, timeout occurred!");

			// Raise a fault if the test timed out
			assertTrue("Test timed out. It took more than " + timeout + " milliseconds. If this error occurs the test probably needs a longer time out (on your computer/network)", workDone);

		} catch (Exception e) {
			e.printStackTrace();
			fail("An unexpected error occurred: " + e.getMessage());

		} finally {
			// Always remove the listener if created
			if (listener != null) muleContext.getNotificationManager().removeListener(listener);
		}
		
		return receivedMessageHolder.value;		
    }

	/**
	 * Sends the <code>payload</code> and <code>headers</code> to the <code>inboundEndpointAddress</code> and waits <code>timeout</code> ms for a <code>MuleMessage</code> to be processed by a service component with the name <code>serviceComponentName</code>. 
	 * 
	 * Sample usage:
	 * <tt>
	 *	Map<String, String> props = new HashMap<String, String>();
	 *	String message            = "Annn";
     *  String expectedResult     = "1nnn";
     *  String receivingService   = "some-teststub-service";
	 *	int    timeout            = 5000;
	 *
	 *	// Setup inbound endpoint for jms
	 *	String inboundEndpoint = "jms://" + IN_QUEUE;
	 *
	 *	// Invoke the service and wait for the transformed message to arrive at the receiving teststub service
	 *	MuleMessage reply = dispatchAndWaitForServiceComponent(inboundEndpoint, message, props, receivingService, timeout);
	 *	String transformedMessage = reply.getPayload().toString();
	 *
	 *	// Verify the result, i.e. the transformed message
     *   assertEquals(expectedResult, transformedMessage);
	 * </tt>
	 * 
	 * @param inboundEndpointAddress
	 * @param payload
	 * @param headers
	 * @param serviceComponentName
	 * @param timeout in ms
	 * @return the received MuleMEssage on the outboundEndpoint
	 */
	protected MuleMessage dispatchAndWaitForServiceComponent(String inboundEndpointAddress, Object payload, Map<String, String> headers, final String serviceComponentName, long timeout) {
		return dispatchAndWaitForServiceComponent(new DispatcherMuleClientImpl(muleContext, inboundEndpointAddress, payload, headers), serviceComponentName, timeout);
    }

    /**
     * Helper method for initiating a test and wait for an exception to be throwed
     *
     * @param inboundEndpointAddress
     * @param payload
     * @param headers
     * @param timeout
     * @return
     */
    protected Exception dispatchAndWaitForException(String inboundEndpointAddress, Object payload, Map<String, String> headers, long timeout) {
		// Declare MuleMessage to return
		final ValueHolder<Throwable> exceptionHolder = new ValueHolder<Throwable>();

    	// Declare countdown latch and listener
		final CountDownLatch latch = new CountDownLatch(1);
		ExceptionNotificationListener listener = null;
		MuleClient muleClient = null;

		try {
			// First create a muleClient instance
			muleClient = new MuleClient(muleContext);

			// Next create a listener that listens for exception on the connector
			listener = new ExceptionNotificationListener() {
				public void onNotification(ServerNotification notification) {

					// Only care about ExceptionNotification
					if (notification instanceof ExceptionNotification) {
						ExceptionNotification exceptionNotification = (ExceptionNotification)notification;

						Throwable exception = (Throwable)exceptionNotification.getSource();
						int       action    = exceptionNotification.getAction();
						
						// If it is a exception event then countdown the latch.
						if (action == EXCEPTION_ACTION ) {
							if (logger.isDebugEnabled()) logger.debug("Expected exception occurred: " + exception.getMessage() + ", time to countdown the latch");
							exceptionHolder.value = exception;
							latch.countDown();
							
						} else {
							if (logger.isDebugEnabled()) logger.debug("Unexpected exception-action: " + exceptionNotification.getActionName() + " , continue to wait for the exception...");							
						}
					}
				}
			};

			// Now register an exception-listener on the connector that expects to fail
			muleContext.getNotificationManager().addListener(listener);

			// Perform the actual dispatch
			muleClient.dispatch(inboundEndpointAddress, payload, headers);

			// Wait for the exception to occur...
			if (logger.isDebugEnabled()) logger.debug("Waiting for an exception to occur...");
			boolean workDone = latch.await(timeout, TimeUnit.MILLISECONDS);
			if (logger.isDebugEnabled()) logger.debug((workDone) ? "Exception occurred, continue..." : "No exception occurred, instead a timeout occurred!");

			// Raise a fault if the test timed out
			assertTrue("Test timed out. It took more than " + timeout + " milliseconds. If this error occurs the test probably needs a longer time out (on your computer/network)", workDone);

		} catch (Exception e) {
			e.printStackTrace();
			fail("An unexpected error occurred: " + e.getMessage());

		} finally {
			// Dispose muleClient
			muleClient.dispose();

			// Always reset the current listener
			if (listener != null) muleContext.getNotificationManager().removeListener(listener);
		}

		return (Exception)exceptionHolder.value;
    }

	/**
     * Helper method for initiating a test and wait for an exception to be throwed
     *
     * TODO: Only handles ServiceExceptions today, need probably to be able to handle ConnectorExceptions as see in the future...
     * TODO: Do we even need this variant of dispatchAndWaitForException()-method?
     * 
     * @param inboundEndpointAddress
     * @param payload
     * @param headers
     * @param serviceName
     * @param timeout
     * @return
     */
    protected Exception dispatchAndWaitForException(String inboundEndpointAddress, Object payload, Map<String, String> headers, final String serviceName, long timeout) {
		// Declare MuleMessage to return
		final ValueHolder<Exception> exceptionHolder = new ValueHolder<Exception>();

    	// Declare countdown latch and listener
		final CountDownLatch latch = new CountDownLatch(1);
		ExceptionNotificationListener listener = null;
		MuleClient muleClient = null;

		try {
			// First create a muleClient instance
			muleClient = new MuleClient(muleContext);

			// Next create a listener that listens for exception on the connector
			listener = new ExceptionNotificationListener() {
				public void onNotification(ServerNotification notification) {

					if (logger.isDebugEnabled()) logger.debug("notification received on " + notification.getResourceIdentifier() + " (action: " + notification.getActionName() + ")");

					// Only care about ExceptionNotification
					// FIXME: Mule 3.1, no clue what we will get in case of exceptions...
					System.err.println("### AbstractTestCase.dispatchAndWaitForException(...) received an notification of type: " + notification.getClass().getName());
					if (notification instanceof ExceptionNotification) {
						ExceptionNotification exceptionNotification = (ExceptionNotification)notification;

						// Only handle ServiceExceptions
						// TODO: Should probably also need to be able to handle ConnectorExceptions
// FIXME: Mule 3.1, no clue what we will get in case of exceptions...
/* FIXME: Mule 3.1, STARTS HERE * /
						if (exceptionNotification.getSource() instanceof ServiceException) {
							ServiceException exception = (ServiceException)exceptionNotification.getSource();
							int    action  = exceptionNotification.getAction();
							String service = exception.getService().getName();
							
							// If it is a exception event on our component then countdown the latch.
							if (action == EXCEPTION_ACTION && service.equals(serviceName)) {
								if (logger.isDebugEnabled()) logger.debug("Expected exception occurred: " + exception.getMessage() + ", time to countdown the latch");
								exceptionHolder.value = exception;
								latch.countDown();
								
							} else {
								if (logger.isDebugEnabled()) logger.debug("Unexpected exception (" + exception.getMessage() + ") occurred on service: " + serviceName  + ", continue to wait for exception the right service...");							
							}
						}
/ * FIXME: Mule 3.1, ENDS HERE */
					}
				}
			};

			// Now register an exception-listener on the connector that expects to fail
			muleContext.getNotificationManager().addListener(listener);

			// Perform the actual dispatch
			muleClient.dispatch(inboundEndpointAddress, payload, headers);

			// Wait for the exception to occur...
			if (logger.isDebugEnabled()) logger.debug("Waiting for an exception to occur...");
			boolean workDone = latch.await(timeout, TimeUnit.MILLISECONDS);
			if (logger.isDebugEnabled()) logger.debug((workDone) ? "Exception occurred, continue..." : "No exception occurred, instead a timeout occurred!");

			// Raise a fault if the test timed out
			assertTrue("Test timed out. It took more than " + timeout + " milliseconds. If this error occurs the test probably needs a longer time out (on your computer/network)", workDone);

		} catch (Exception e) {
			e.printStackTrace();
			fail("An unexpected error occurred: " + e.getMessage());

		} finally {
			// Dispose muleClient
			muleClient.dispose();

			// Always reset the current listener
			if (listener != null) muleContext.getNotificationManager().removeListener(listener);
		}

		return (Exception)exceptionHolder.value;
    }
}
