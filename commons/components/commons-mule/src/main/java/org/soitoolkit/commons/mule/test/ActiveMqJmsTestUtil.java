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

import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;

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

    public ActiveMqJmsTestUtil(String qmUrl, String clientId) {
		this.qmUrl = qmUrl;
		init(clientId);
	}
    
	/**
	 * Create a connection factory for the provided ActiveMQ broker url
	 * 
	 * @param qmUrl, typically "vm://localhost" when using embedded amq and tcp://localhost:61616 when using amq standalone
	 */
	public ActiveMqJmsTestUtil(String qmUrl) {
		this.qmUrl = qmUrl;
		init(null);
	}

	/**
	 * Create a connection factory for use of embedded ActiveMQ
	 * 
	 */
	public ActiveMqJmsTestUtil() {
		this("vm://localhost");
	}

	@Override
	protected QueueConnectionFactory createQueueConnectionFactory() throws Exception {
        logger.debug("Creating ActiveMQ Queue Connection Factory using URL: {}", this.qmUrl);

        return new ActiveMQConnectionFactory(USR, PWD, qmUrl);
	}
	
	@Override
	protected TopicConnectionFactory createTopicConnectionFactory() throws Exception {
        logger.debug("Creating ActiveMQ Topic Connection Factory using URL: {}", this.qmUrl);

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