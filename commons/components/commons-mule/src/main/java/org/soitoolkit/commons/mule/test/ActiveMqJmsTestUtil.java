package org.soitoolkit.commons.mule.test;

import javax.jms.QueueConnectionFactory;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * Concrete subclass that creates the ConnectionFactory in a ActiveMQ-way...
 * 
 * @author Magnus Larsson
 *
 */
public class ActiveMqJmsTestUtil extends AbstractJmsTestUtil {

    private final static String USR = ActiveMQConnection.DEFAULT_USER;
    private final static String PWD = ActiveMQConnection.DEFAULT_PASSWORD;

    protected String qmUrl = null;

	/**
	 * Create a connection factory for the provided ActiveMQ broker url
	 * 
	 * @param qmUrl, typically "vm://localhost" when using embedded amq and tcp://localhost:61616 when using amq standalone
	 */
	public ActiveMqJmsTestUtil(String qmUrl) {
		this.qmUrl = qmUrl;
		init();
	}

	/**
	 * Create a connection factory for use of embedded ActiveMQ
	 * 
	 */
	public ActiveMqJmsTestUtil() {
		this("tcp://localhost:61616");
//		this("vm://localhost");
	}

	@Override
	protected QueueConnectionFactory createQueueConnectionFactory() throws Exception {
        logger.debug("Creating ActiveMQ Queue Connection Factory using URL: {}", this.qmUrl);

        return new ActiveMQConnectionFactory(USR, PWD, qmUrl);
	}

	@Override
	protected String getUsername() {
		return USR;
	}

	@Override
	protected String getPassword() {
		return PWD;
	}	
}