package org.soitoolkit.commons.log.appenders;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.log4j.helpers.LogLog;

/**
 * A JMS appender tailored for usage with IBM WebSphere MQ.
 * 
 * Sample configuration in log4j.properties:
 * 
 * 		log4j.rootCategory=INFO,console,jms
 * 
 * 		log4j.appender.jms=se.volvofinans.commons.log.JmsActiveMqAppender
 * 		log4j.appender.jms.brokerURL=tcp://localhost:61616
 * 
 * 
 * Optional parameter (provided a default value if not specified):
 * 
 * 		log4j.appender.jms.logInfoQueue=MY-INFO-LOG-QUEUE
 * 		log4j.appender.jms.logErrorQueue=MY-ERROR-LOG-QUEUE
 * 
 * 
 * @author Magnus Larsson
 */
public class JmsActiveMqAppender extends AbstractJmsAppender {

	private String brokerURL = null;
	
	@Override
	protected QueueConnection createQueueConnection() throws JMSException {
		ActiveMQConnectionFactory amqcf = new ActiveMQConnectionFactory(brokerURL);
		
		LogLog.debug("Create a ActiveMQ QueueConnection using:" + 
			"\n - brokerURL = " + brokerURL);
		return amqcf.createQueueConnection();
	}

	@Override
	protected Queue createQueue(String queueName) throws JMSException {
		LogLog.debug("Create a ActiveMQ Queue using:" + 
				"\n - queueName = " + queueName);
		return (Queue) new ActiveMQQueue(queueName);
	}

	public String getBrokerURL() {
		return brokerURL;
	}

	public void setBrokerURL(String brokerURL) {
		this.brokerURL = brokerURL;
	}
}