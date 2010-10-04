package org.soitoolkit.commons.mule.test;

import static org.mule.context.notification.ComponentMessageNotification.COMPONENT_POST_INVOKE;
import static org.mule.context.notification.ExceptionNotification.EXCEPTION_ACTION;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.mule.api.MuleMessage;
import org.mule.api.context.notification.ComponentMessageNotificationListener;
import org.mule.api.context.notification.EndpointMessageNotificationListener;
import org.mule.api.context.notification.ExceptionNotificationListener;
import org.mule.api.context.notification.ServerNotification;
import org.mule.api.service.ServiceException;
import org.mule.context.notification.ComponentMessageNotification;
import org.mule.context.notification.EndpointMessageNotification;
import org.mule.context.notification.ExceptionNotification;
import org.mule.module.client.MuleClient;
import org.mule.tck.FunctionalTestCase;
import org.soitoolkit.commons.mule.util.ValueHolder;

/**
 * Extends the base class in Mule, FuntionalTestCase.
 * 
 * @author Magnus Larsson
 *
 */
public abstract class AbstractTestCase extends FunctionalTestCase {
    
    protected String junitTestCaseName;
    
    public AbstractTestCase() {
		super();

		// Ensure that CXF use LOG4J for logging
		System.setProperty("org.apache.cxf.Logger", "org.apache.cxf.common.logging.Log4jLogger");
	}

	/**
     * Fix for Mule 2.2.2 problem with presentation of test-name i Eclipse
     * testrunner that comes from override in
     * <code>org.mule.tck.AbstractMuleTestCase.setName()</code>. 
     * 
     * @see org.mule.tck.AbstractMuleTestCase#getName()
     */
    @Override
    public String getName() {
      return junitTestCaseName;
    }

    /**
     * Fix for Mule 2.2.2 problem with presentation of test-name i Eclipse
     * testrunner that comes from override in
     * <code>org.mule.tck.AbstractMuleTestCase.setName()</code>. 
     * 
     * @see org.mule.tck.AbstractMuleTestCase#getName()
     */
    @Override
    public void setName(String name) {
        junitTestCaseName = name;
        super.setName(junitTestCaseName);
    }

    /**
     * Fix for Mule 2.2.2 where init of test timeout moved from setUp to
     * constructor.
     * This method MUST be called from the constructor of an inherited TestCase
     * to have any effect, calling this method from doSetUp() or any other
     * method is too late.
     * 
     * @see org.mule.tck.AbstractMuleTestCase#AbstractMuleTestCase()
     * @see org.mule.tck.AbstractMuleTestCase#initTestTimeoutSecs()
     */
    protected void setTestTimeoutSecs(int seconds) {
        logger.info("Setting test timeout to (seconds): " + seconds);
        String strSeconds = String.valueOf(seconds);
        System.setProperty(PROPERTY_MULE_TEST_TIMEOUT, strSeconds);

    
        // initTestTimeoutSecs();
        Method initTimeoutMethod = null;
        try {
        	initTimeoutMethod = getClass().getMethod("initTestTimeoutSecs", null);
        	initTimeoutMethod.invoke(this, null);
		} catch (NoSuchMethodException e) {
			// mule version < 2.2.2, do nothing
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	
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
			muleClient = new MuleClient();

			// Next create a listener that listens for dispatch events on the outbound endpoint
			listener = new EndpointMessageNotificationListener() {
				public void onNotification(ServerNotification notification) {
//					System.err.println("notification received on " + notification.getResourceIdentifier() + " (action: " + notification.getActionName() + ")");

					// Only care about EndpointMessageNotification
					if (notification instanceof EndpointMessageNotification) {
						EndpointMessageNotification endpointNotification = (EndpointMessageNotification)notification;

						// Extract action and name of the endpoint
						int    actualAction   = endpointNotification.getAction();
						String actualEndpoint = endpointNotification.getEndpoint().getName();

						// If it is a dispatch event on our outbound endpoint then countdown the latch.
//						System.err.println(actualAction == action);
//						System.err.println(actualEndpoint.equals(outboundEndpointName));
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
	 * Sends the <code>payload</code> and <code>headers</code> to the <code>inboundEndpointAddress</code> and waits <code>timeout</code> ms for a <code>MuleMessage</code> to be processed by a service component with the name <code>serviceComponentName</code>. 
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
	 * @param serviceComponentName
	 * @param timeout in ms
	 * @return the received MuleMEssage on the outboundEndpoint
	 */
	protected MuleMessage dispatchAndWaitForServiceComponent(String inboundEndpointAddress, Object payload, Map<String, String> headers, final String serviceComponentName, long timeout)
    {

		// Declare MuleMessage to return
		final ValueHolder<MuleMessage> receivedMessageHolder = new ValueHolder<MuleMessage>();
		
		// Declare countdown latch and listener
		final CountDownLatch latch = new CountDownLatch(1);
		ComponentMessageNotificationListener listener = null;
		MuleClient muleClient = null;

		try {
			// First create a muleClient instance
			muleClient = new MuleClient();

			// Next create a listener that listens for dispatch events on the outbound endpoint
			listener = new ComponentMessageNotificationListener() {
				public void onNotification(ServerNotification notification) {

//					System.err.println("notification received on " + notification.getResourceIdentifier() + " (action: " + notification.getActionName());

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
     * Helper method for initiating a test and wait for an exception to be throwed
     *
     * @param inboundEndpointAddress
     * @param payload
     * @param headers
     * @param timeout
     * @return
     */
    protected Exception dispatchAndWaitForException(String inboundEndpointAddress, Object payload, Map<String, String> headers, long timeout)
    {
		// Declare MuleMessage to return
		final ValueHolder<Throwable> exceptionHolder = new ValueHolder<Throwable>();

    	// Declare countdown latch and listener
		final CountDownLatch latch = new CountDownLatch(1);
		ExceptionNotificationListener listener = null;
		MuleClient muleClient = null;

		try {
			// First create a muleClient instance
			muleClient = new MuleClient();

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
    protected Exception dispatchAndWaitForException(String inboundEndpointAddress, Object payload, Map<String, String> headers, final String serviceName, long timeout)
    {
		// Declare MuleMessage to return
		final ValueHolder<Exception> exceptionHolder = new ValueHolder<Exception>();

    	// Declare countdown latch and listener
		final CountDownLatch latch = new CountDownLatch(1);
		ExceptionNotificationListener listener = null;
		MuleClient muleClient = null;

		try {
			// First create a muleClient instance
			muleClient = new MuleClient();

			// Next create a listener that listens for exception on the connector
			listener = new ExceptionNotificationListener() {
				public void onNotification(ServerNotification notification) {

					System.err.println("notification received on " + notification.getResourceIdentifier() + " (action: " + notification.getActionName() + ")");

					// Only care about ExceptionNotification
					if (notification instanceof ExceptionNotification) {
						ExceptionNotification exceptionNotification = (ExceptionNotification)notification;

						// Only handle ServiceExceptions
						// TODO: Should probably also need to be able to handle ConnectorExceptions
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
