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
package org.soitoolkit.commons.mule.log;

import static org.soitoolkit.commons.logentry.schema.v1.LogLevelType.INFO;

import java.util.Map;

import org.mule.api.ExceptionPayload;
import org.mule.api.MuleContext;
import org.mule.api.MuleMessage;
import org.mule.api.context.MuleContextAware;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageAwareTransformer;
import org.mule.transport.http.HttpConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.logentry.schema.v1.LogLevelType;
import org.soitoolkit.commons.mule.jaxb.JaxbObjectToXmlTransformer;


/**
 * Transformer used to log messages passing a specific endpoint using the event-logger
 * Configurable properties:
 * 
 * 1. logLevel, accepts the values: FATAL, ERROR, WARNING, INFO, DEBUG and TRACE. Defaults to INFO. 
 * 2. logType, any string, could be "req-in" for a inbound synchronous endpoint or "msg-out" of outbound asynchronous endpoint
 * 
 * @author Magnus Larsson
 */
public class LogTransformer extends AbstractMessageAwareTransformer implements MuleContextAware {

	private static final Logger log = LoggerFactory.getLogger(LogTransformer.class);

	private final EventLogger eventLogger;

	// FIXME: Mule 3.1. To be removed since it's already in base class for Mule 3.1
	/*
	 * Property muleContext 
	 */
	private MuleContext muleContext = null;
	public void setMuleContext(MuleContext muleContext) {
		log.debug("MuleContext injected");
		this.muleContext = muleContext;
		
		// Also inject the muleContext in the event-logger (since we create the event-logger for now)
		eventLogger.setMuleContext(this.muleContext);
	}

	/*
	 * Property logLevel 
	 */
	private LogLevelType logLevel = INFO;
	public void setLogLevel(LogLevelType logLevel) {
		this.logLevel = logLevel;
	}

	/*
	 * Property logType 
	 */
	private String logType;
	public void setLogType(String logType) {
		this.logType = logType;
	}

	/*
	 * Property extraInfo 
	 * 
     * <custom-transformer name="logKivReqIn" class="org.soitoolkit.commons.mule.log.LogTransformer">
	 * 	<spring:property name="logType"  value="Received"/>
	 * 	<spring:property name="jaxbObjectToXml"  ref="objToXml"/>
	 *    <spring:property name="extraInfo">
	 *      <spring:map>
	 *        <spring:entry key="id1" value="123"/>
	 *        <spring:entry key="id2" value="456"/>
	 *      </spring:map>
	 *    </spring:property>
     * </custom-transformer>
	 * 
	 */
	private Map<String, String> extraInfo;
	public void setExtraInfo(Map<String, String> extraInfo) {
		this.extraInfo = extraInfo;
	}

	public LogTransformer() {
		eventLogger = new EventLogger();		
	}

	/**
	 * Setter for the jaxbToXml property
	 * 
	 * @param jaxbToXml
	 */
	public void setJaxbObjectToXml(JaxbObjectToXmlTransformer jaxbToXml) {
		eventLogger.setJaxbToXml(jaxbToXml);
	}
	
	public Object transform(MuleMessage message, String outputEncoding) throws TransformerException {

    	try {
			// Skip logging if an error has occurred, then the error is logged by an error handler
    		ExceptionPayload exp = message.getExceptionPayload();
    		if (exp != null) {
    			log.debug("Skip logging message, exception detected! " + exp.getException().getMessage());
    			return message;
    		}

    		// Skip logging requests like http://...?wsdl and ...?xsd

    		// Pre-historic logic for Mule v2.2.1, v2.2.5 and v3.1:
//    		// Skip logging if service name starts with "_cxfServiceComponent" (Mule 2.2.1) or ends with "_cxfComponent" (Mule 2.2.5) and endpoint contains "?wsdl" or "?xsd", then it's just tons of WSDL and XSD lookup calls, nothing to log...
//            MuleEventContext event       = RequestContext.getEventContext();
//            String           serviceName = MuleUtil.getServiceName(event);
//    		if (serviceName != null) { // FIXME: Mule 3.1 Does not have these services... && (serviceName.startsWith("_cxfServiceComponent") || serviceName.endsWith("_cxfComponent"))) {
//        	    EndpointURI      endpointURI = event.getEndpointURI();
//    			if (endpointURI != null) {
//    				String ep = endpointURI.toString();
//    				if ((ep.contains("?wsdl")) || (ep.contains("?xsd"))) {
//    	    			log.debug("Skip logging message, CXF ...?WSDL/XSD call detected!");
//    					return message;
//    				}
//    			}
//    		}
    		String httpReq = message.getInboundProperty(HttpConnector.HTTP_REQUEST_PROPERTY);
    		if (httpReq != null) {
				if ((httpReq.endsWith("?wsdl")) || (httpReq.contains("?xsd"))) {
	    			log.debug("Skip logging message, CXF ...?WSDL/XSD call detected!");
					return message;
	    		}
    		}
    		
    		switch (logLevel) {
			case INFO:
			case DEBUG:
			case TRACE:
				eventLogger.logInfoEvent(message, logType, null, extraInfo);
				break;

			case FATAL:
			case ERROR:
			case WARNING:
				eventLogger.logErrorEvent(new RuntimeException(logType), message, null, extraInfo);
				break;
			}

			return message;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }
}