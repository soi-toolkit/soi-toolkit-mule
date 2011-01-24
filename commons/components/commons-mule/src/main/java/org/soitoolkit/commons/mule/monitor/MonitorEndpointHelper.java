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
package org.soitoolkit.commons.mule.monitor;

import static org.soitoolkit.commons.mule.core.PropertyNames.DEFAULT_MULE_JDBC_DATASOURCE;
import static org.soitoolkit.commons.mule.core.PropertyNames.DEFAULT_MULE_JMS_CONNECTOR;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.sql.DataSource;

import org.mule.api.MuleContext;
import org.mule.transport.jms.JmsConnector;
import org.soitoolkit.commons.mule.jdbc.JdbcUtil;
import org.soitoolkit.commons.mule.util.MiscUtil;
import org.soitoolkit.commons.mule.util.MuleUtil;

/**
 * Helper class for writing monitor services.
 * 
 * @author Magnus Larsson
 *
 */
public class MonitorEndpointHelper {

	public final static String OK_PREFIX = "OK";
	public final static String ERROR_PREFIX = "ERROR";

	/**
	 * Verify access to a HTTP endpoint by performing a HTTP-get operation and assures that a proper http-return code (e.g. 200) is returned.
	 * NOTE: This method is aimed for external http-endpoionts and not internal http-endpoints, e.g. servlet-endpoints.
	 * 
	 * TODO: What return codes should be handled as ok here? Simpl 2xx codes or som other codes as well?
	 * TODO: Needs to be implemented...
	 */
	public static String pingHttpEndpoint(String url) {
		return OK_PREFIX;
	}

	/**
	 * Verify access to a HTTPS endpoint by performing a HTTP-get operation and assures that a proper http-return code (e.g. 200) is returned.
	 * NOTE: This method is aimed for external http-endpoionts and not internal http-endpoints, e.g. servlet-endpoints.
	 * 
	 * TODO: What return codes should be handled as ok here? Simpl 2xx codes or som other codes as well?
	 * TODO: Needs to be implemented...
	 */
	public static String pingHttpsEndpoint(String url, String truststorePath, String truststorePassword, String privateKeyPassword) {
		return OK_PREFIX;		
	}
	
	/**
	 * Verify access to a JDBC endpoint by verifying that a specified table is accessible.
	 */
	public static String pingJdbcEndpoint(MuleContext muleContext, String tableName) {
		return pingJdbcEndpoint(muleContext, DEFAULT_MULE_JDBC_DATASOURCE, tableName);	}
	
	/**
	 * Verify access to a JDBC endpoint by verifying that a specified table is accessible.
	 * @param muleContext 
	 * @param muleJdbcDataSourceName
	 * @param tableName
	 */
	public static String pingJdbcEndpoint(MuleContext muleContext, String muleJdbcDataSourceName, String tableName) {
		
		DataSource ds = JdbcUtil.lookupDataSource(muleContext, muleJdbcDataSourceName);

		Connection c = null;
		Statement  s = null;
		ResultSet rs = null;

		try {
			c  = ds.getConnection();
			s  = c.createStatement();
			rs = s.executeQuery("select 1 from " + tableName);
			
		} catch (SQLException e) {
			return ERROR_PREFIX + ": The table " + tableName + " was not found in the data source " + muleJdbcDataSourceName + ", reason: " + e.getMessage();

		} finally {
	    	try {if (rs != null) rs.close();} catch (SQLException e) {}
	    	try {if ( s != null)  s.close();} catch (SQLException e) {}
	    	try {if ( c != null)  c.close();} catch (SQLException e) {}
		}
		
		return OK_PREFIX + ": The table " + tableName + " was found in the data source " + muleJdbcDataSourceName;
	}
	
	/**
	 * Verify access to a JMS endpoint by browsing a specified queue for messages.
	 */
	public static String pingJmsEndpoint(MuleContext muleContext, String queueName) {
		return pingJmsEndpoint(muleContext, DEFAULT_MULE_JMS_CONNECTOR, queueName);
	}

	/**
	 * Verify access to a JMS endpoint by browsing a specified queue for messages.
	 */
	public static String pingJmsEndpoint(MuleContext muleContext, String muleJmsConnectorName, String queueName) {
	    try {
	    	
	    	isQueueEmpty(muleContext, muleJmsConnectorName, queueName);
		    return OK_PREFIX + ": The queue " + queueName + " was found in the queue manager " + muleJmsConnectorName; 
	
	    } catch (JMSException e) {
		    return ERROR_PREFIX + ": The queue " + queueName + " was not found in the queue manager " + muleJmsConnectorName + ", reason: " + e.getMessage(); 
	    }	
	}


	/**
	 * Verify access to a JMS backout queue by browsing the backout queue and ensure that no messages exists.
	 * 
	 * @param queueName
	 * @return
	 */
	public static String pingJmsBackoutQueue(MuleContext muleContext, String queueName) {
		return pingJmsBackoutQueue(muleContext, DEFAULT_MULE_JMS_CONNECTOR, queueName);
	}

	/**
	 * Verify access to a JMS backout queue by browsing the backout queue and ensure that no messages exists.
	 * 
	 * @param muleJmsConnectorName
	 * @param queueName
	 * @return
	 */
	public static String pingJmsBackoutQueue(MuleContext muleContext, String muleJmsConnectorName, String queueName) {
	    try {
	    	if (isQueueEmpty(muleContext, muleJmsConnectorName, queueName)) {
	    	    return OK_PREFIX + ": The queue " + queueName + " in the queue manager " + muleJmsConnectorName + " is empty"; 
	    		
	    	} else {
	    	    return ERROR_PREFIX + ": The queue " + queueName + " in the queue manager " + muleJmsConnectorName + " is not empty"; 
	    	}
	
	    } catch (JMSException e) {
		    return ERROR_PREFIX + ": The queue " + queueName + " was not found in the queue manager " + muleJmsConnectorName + ", reason: " + e.getMessage(); 
	    }	
	}

	/**
	 * Verify access to a SOAP/HTTP endpoint by browsing its wsdl and ensure that is returns a wsdl.
	 * 
	 * @param soapHttpUrl
	 * @return
	 */
	public static String pingSoapHttpUrl(String soapHttpUrl) {
		soapHttpUrl += "?wsdl";
		try {
			URL url = new URL(soapHttpUrl);
			Object content = url.getContent();
			InputStream is = (InputStream)content;
			String c = MiscUtil.convertStreamToString(is);
			
			boolean isWsdl = c.contains("http://schemas.xmlsoap.org/wsdl");
			
			if (isWsdl) {
				return OK_PREFIX + ": The url " + soapHttpUrl + " responded with a wsdl as expected"; 
			} else {
			    return ERROR_PREFIX + ": The url " + soapHttpUrl + " does not seem to expose a wsdl, unexpected response: " + c; 
				
			}

		} catch (MalformedURLException e) {
		    return ERROR_PREFIX + ": The url " + soapHttpUrl + " did not respon as expected, reason: " + e; 
		} catch (IOException e) {
		    return ERROR_PREFIX + ": The url " + soapHttpUrl + " did not respon as expected, reason: " + e; 
		}   	
	}
	
	/**
	 * Browse a queue for messages
	 * 
	 * @param muleJmsConnectorName
	 * @param queueName
	 * @return
	 * @throws JMSException
	 */
	@SuppressWarnings("rawtypes")
	private static boolean isQueueEmpty(MuleContext muleContext, String muleJmsConnectorName, String queueName) throws JMSException {
	    JmsConnector muleCon = (JmsConnector)MuleUtil.getSpringBean(muleContext, muleJmsConnectorName);

	    Session s = null;
	    QueueBrowser b = null;

	    try {
	    	// Get a jms connection from mule and create a jms session
	    	s = muleCon.getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
	    	Queue q = s.createQueue(queueName);

	    	b = s.createBrowser(q);
			Enumeration e = b.getEnumeration();

			return !e.hasMoreElements();
			
	    } finally {
	    	try {if (b != null) b.close();} catch (JMSException e) {}
	    	try {if (s != null) s.close();} catch (JMSException e) {}
	    }	
	}
}