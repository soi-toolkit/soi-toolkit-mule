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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for JMS utility methods, relies on specializations that creates JMS provider specific ConnectionFactories...
 * 
 * @author Magnus Larsson
 *
 */
public abstract class AbstractJmsTestUtil {

    protected QueueConnection connection = null;
    protected QueueSession    session    = null;

	protected static final Logger logger = LoggerFactory.getLogger(AbstractJmsTestUtil.class);

	/*
	 * Methods to be implemented by a provider specific implementation
	 */
	protected abstract QueueConnectionFactory createQueueConnectionFactory() throws Exception;
	protected abstract String getUsername();
	protected abstract String getPassword();

	
	/**
	 * @return
	 */
	public QueueSession getSession() {
		return session;
	}
	
	/**
	 * Sends a text messages to a queue. 
	 * 
	 * @param queueName
	 * @param the messages
	 */
	public void sendOneTextMessage(String queueName, String message) {

        MessageProducer publisher = null;

	    try {
	    	publisher = session.createProducer(session.createQueue(queueName));
	        TextMessage textMessage = session.createTextMessage(message);  
	        publisher.send(textMessage);   
	
	    } catch (JMSException e) {
	        throw new RuntimeException(e);
	    } finally {
	    	try {
	    		if (publisher != null) publisher.close(); 
	    	} catch (JMSException e) {}
	    }
	}

	/**
	 * Browse messages on a queue. 
	 * 
	 * @param queueName
	 * @return the messages
	 */
	@SuppressWarnings("rawtypes")
	public List<Message> browseMessagesOnQueue(String queueName) {

	    QueueBrowser browser = null;

	    try {
		    browser = session.createBrowser(session.createQueue(queueName));
			List<Message> result = new ArrayList<Message>();
			
			Enumeration messages = browser.getEnumeration();
			while (messages.hasMoreElements()) {
				Message message = (Message)messages.nextElement();
				result.add(message);
			}
	
	        return result;
	
	    } catch (JMSException e) {
	        throw new RuntimeException(e);
	    } finally {
	    	try {
	    		if (browser != null) browser.close(); 
	    	} catch (JMSException e) {}
	    }
	}
		
	/**
	 * Consume messages on a queue. 
	 * 
	 * @param queueName
	 * @return the messages
	 */
 	public List<Message> consumeMessagesOnQueue(String queueName) {

	    MessageConsumer consumer = null;

	    try {
		    consumer = session.createConsumer(session.createQueue(queueName));
			List<Message> result = new ArrayList<Message>();
			
			Message message = null;
			while ((message = consumer.receive(100)) != null) {
				result.add(message);
			}	
	        return result;

	    } catch (JMSException e) {
	        throw new RuntimeException(e);
	    } finally {
	    	try {
	    		if (consumer != null) consumer.close(); 
	    	} catch (JMSException e) {}
	    }
	}
 	
	/**
	 * Consumes one messages on a queue and waits specified time if none exists up front. 
	 * 
	 * @param queueName
	 * @param timeout 
	 * @return the text message, null if none was consumed during the specified time period
	 */
 	public String consumeOneTextMessage(String queueName, long timeout) {
 		String textMessage = null;
 		Message msg = consumeOneMessage(queueName, timeout); 
 		
 		if (msg != null) {
 			try {
				textMessage = ((TextMessage)msg).getText();
			} catch (JMSException e) {
				throw new RuntimeException(e);
			}
 		}
 		
 		return textMessage;
	}
 	
	/**
	 * Consumes one messages on a queue and waits specified time if none exists up front. 
	 * 
	 * @param queueName
	 * @param timeout 
	 * @return the message, null if none was consumed during the specified time period
	 */
 	public Message consumeOneMessage(String queueName, long timeout) {

	    MessageConsumer consumer = null;

	    try {
		    consumer = session.createConsumer(session.createQueue(queueName));
			return consumer.receive(timeout); 
	
	    } catch (JMSException e) {
	        throw new RuntimeException(e);
	    } finally {
	    	try {
	    		if (consumer != null) consumer.close(); 
	    	} catch (JMSException e) {}
	    }
	}

	/**
	 * Clears a number of queues, i.e. consume all messages in the queues. 
	 * 
	 * @param queueName...
	 * @return
	 */
 	public void clearQueues(String... queueNames) {

 		if (logger.isInfoEnabled()) {
 	 		StringBuffer queues = new StringBuffer();
 	 		for (int i = 0; i < queueNames.length; i++) {
 	 			queues.append(queueNames[i]);
 	 			queues.append(' ');
 			}
 	 		logger.info("Clearing messages on queues: {}", queues);
 		}

 		for (int i = 0; i < queueNames.length; i++) {
 			List<Message> messages = consumeMessagesOnQueue(queueNames[i]);			
 			logger.debug("CONSUMED {} MESSAGES FROM QUEUE {}", messages.size(), queueNames[i]);
		}
	}

 	//
 	// To be called from the specializations constructors...
 	//

	protected void init() {
        boolean ok = false;
        try {
	        QueueConnectionFactory connectionFactory = createQueueConnectionFactory();

	        String username = getUsername();
	        if (username == null) {
	        	connection = connectionFactory.createQueueConnection();
	        } else {
	        	connection = connectionFactory.createQueueConnection(username, getPassword());
	        	
	        }
	        connection.start();
	
	        session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

	        ok = true;
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    } finally {
	    	if (!ok) cleanup();
	    }
	    
    }

	/**
	 * TODO: ML FIX
	 */
	public void cleanup() {
        try {
        	if (session    != null) session.close(); 
        	if (connection != null) {
        		connection.stop();
        		connection.close(); 
        	}
	    } catch (JMSException e) {
	        throw new RuntimeException(e);
	    }
    }	

}