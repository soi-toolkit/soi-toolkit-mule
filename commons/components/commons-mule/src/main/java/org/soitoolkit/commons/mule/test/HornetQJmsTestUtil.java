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

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.hornetq.api.core.SimpleString;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.management.ObjectNameBuilder;
import org.hornetq.api.core.management.QueueControl;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.hornetq.jms.client.HornetQQueueConnectionFactory;
import org.hornetq.jms.client.HornetQTopicConnectionFactory;
import org.soitoolkit.commons.mule.test.AbstractJmsTestUtil;

/**
 * Concrete subclass that creates the ConnectionFactory in a HornetQ-way...
 * 
 * @author Magnus Larsson
 *
 */
public class HornetQJmsTestUtil extends AbstractJmsTestUtil {

    protected String host;
    protected int    port;

	/**
	 * Create a connection factory for use of embedded ActiveMQ
	 * 
	 */
	public HornetQJmsTestUtil() {
		this("localhost", 5445);
	}

	/**
	 * Create a connection factory for HornetQ and specify a clientId used for durable topic subscriber operations
     * 
	 * @param host, e.g localhost
	 * @param port, e.g. 5445
     * @param clientId, the durable topic subscriber clientId 
     */
    public HornetQJmsTestUtil(String host, int port, String clientId) {
		this.host = host;
		this.port = port;
		init(clientId);
	}
    
	/**
	 * Create a connection factory for HornetQ 
	 * 
	 * @param host, e.g localhost
	 * @param port, e.g. 5445
	 */
    public HornetQJmsTestUtil(String host, int port) {
    	this(host, port, null);
	}

	@Override
	protected QueueConnectionFactory createQueueConnectionFactory() throws Exception {
        logger.debug("Creating HornetQ Queue Connection Factory using host:port {}:{}", host, port);

        Map<String, Object> connectionParams = new HashMap<String, Object>();
        connectionParams.put(TransportConstants.HOST_PROP_NAME, host);
        connectionParams.put(TransportConstants.PORT_PROP_NAME, port);

        TransportConfiguration transportConfiguration = 
            new TransportConfiguration(
            "org.hornetq.core.remoting.impl.netty.NettyConnectorFactory", 
            connectionParams);

        QueueConnectionFactory factory = new HornetQQueueConnectionFactory(false, transportConfiguration);
        
        return factory;
	}

	@Override
	protected TopicConnectionFactory createTopicConnectionFactory() throws Exception {
        logger.debug("Creating HornetQ Queue Connection Factory using host:port {}:{}", host, port);

        Map<String, Object> connectionParams = new HashMap<String, Object>();
        connectionParams.put(TransportConstants.HOST_PROP_NAME, host);
        connectionParams.put(TransportConstants.PORT_PROP_NAME, port);

        TransportConfiguration transportConfiguration = 
            new TransportConfiguration(
            "org.hornetq.core.remoting.impl.netty.NettyConnectorFactory", 
            connectionParams);

        TopicConnectionFactory factory = new HornetQTopicConnectionFactory(false, transportConfiguration);
        
        return factory;
	}

	@Override
	protected String getUsername() {
		return null;
	}

	@Override
	protected String getPassword() {
		return null;
	}	

	@Override
    public int getNoOfMsgsIncludingPendingForRetry(String queue) {
        try {
            MBeanServer  mBeanServer  = ManagementFactory.getPlatformMBeanServer();
            SimpleString queueId      = new SimpleString("jms.queue." + queue);
            ObjectName   on           = ObjectNameBuilder.DEFAULT.getQueueObjectName(queueId, queueId);
            QueueControl queueControl = MBeanServerInvocationHandler.newProxyInstance(mBeanServer, on, QueueControl.class, false);
            return (int)queueControl.getMessageCount();
        
        } catch (Exception ex) {
        	logger.error("Error", ex);
            throw new RuntimeException(ex);
        }
    }

}