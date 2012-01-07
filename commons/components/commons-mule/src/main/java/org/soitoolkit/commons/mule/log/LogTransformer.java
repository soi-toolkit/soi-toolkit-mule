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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.mule.api.ExceptionPayload;
import org.mule.api.MuleContext;
import org.mule.api.MuleMessage;
import org.mule.api.context.MuleContextAware;
import org.mule.api.transformer.TransformerException;
import org.mule.message.ExceptionMessage;
import org.mule.transformer.AbstractMessageTransformer;
import org.mule.transport.http.HttpConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.logentry.schema.v1.LogLevelType;
import org.soitoolkit.commons.mule.api.log.EventLogMessage;
import org.soitoolkit.commons.mule.api.log.EventLogger;
import org.soitoolkit.commons.mule.jaxb.JaxbObjectToXmlTransformer;


/**
 * Transformer used to log messages passing a specific endpoint using the event-logger
 * Configurable properties:
 * 
 * 1. logLevel, accepts the values: FATAL, ERROR, WARNING, INFO, DEBUG and TRACE. Defaults to INFO. 
 * 2. logType, any string, could be "req-in" for a inbound synchronous endpoint or "msg-out" of outbound asynchronous endpoint
 * 3. ...
 * 
 * @author Magnus Larsson
 */
public class LogTransformer extends AbstractMessageTransformer implements MuleContextAware {

	private static final Logger log = LoggerFactory.getLogger(LogTransformer.class);

	private EventLogger eventLogger;

	// FIXME: Mule 3.1. To be removed since it's already in base class for Mule 3.1
	/*
	 * Property muleContext 
	 * /
	private MuleContext muleContext = null;
	*/
	@Override
	public void setMuleContext(MuleContext muleContext) {
		super.setMuleContext(muleContext);

		log.debug("MuleContext injected");
		
		// Also inject the muleContext in the event-logger (since we create the event-logger for now)
		eventLogger = EventLoggerFactory.getEventLogger(muleContext);
		// TODO: this is an ugly workaround for injecting the jaxbObjToXml dependency ...
		if (eventLogger instanceof DefaultEventLogger) {
			((DefaultEventLogger) eventLogger).setJaxbToXml(jaxbObjectToXml);
		}
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
	private String logType = "";
	public void setLogType(String logType) {
		this.logType = logType;
	}

	/*
	 * Property integrationScenario 
	 */
	private String integrationScenario = "";
	public void setIntegrationScenario(String integrationScenario) {
		this.integrationScenario = integrationScenario;
	}

	/*
	 * Property contractId 
	 */
	private String contractId = "";
	public void setContractId(String contractId) {
		this.contractId = contractId;
	}

	/*
	 * Property businessContextId 
	 * 
     * <custom-transformer name="logKivReqIn" class="org.soitoolkit.commons.mule.log.LogTransformer">
	 * 	<spring:property name="logType"  value="Received"/>
	 * 	<spring:property name="jaxbObjectToXml"  ref="objToXml"/>
	 *    <spring:property name="businessContextId">
	 *      <spring:map>
	 *        <spring:entry key="id1" value="123"/>
	 *        <spring:entry key="id2" value="456"/>
	 *      </spring:map>
	 *    </spring:property>
     * </custom-transformer>
	 * 
	 */
	private Map<String, String> businessContextId;
	public void setBusinessContextId(Map<String, String> businessContextId) {
		this.businessContextId = businessContextId;
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
		//eventLogger = new DefaultEventLogger();		
	}

	/**
	 * Setter for the jaxbToXml property
	 * 
	 * @param jaxbToXml
	 */
	private JaxbObjectToXmlTransformer jaxbObjectToXml;
	public void setJaxbObjectToXml(JaxbObjectToXmlTransformer jaxbToXml) {
		this.jaxbObjectToXml = jaxbToXml;
	}
	
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

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
    		

    		Map<String, String> evaluatedExtraInfo         = evaluateMapInfo(extraInfo, message);
    		Map<String, String> evaluatedBusinessContextId = evaluateMapInfo(businessContextId, message);

    		if (log.isDebugEnabled()) {
	    		if (evaluatedBusinessContextId == null) {
	    			log.debug("Null businessContextId");
	    		} else {
	    			Set<Entry<String, String>> es = evaluatedBusinessContextId.entrySet();
	    			for (Entry<String, String> e : es) {
	    				log.debug(e.getKey() + "=" + e.getValue());
					}
	    		}
    		}
    		
    		switch (logLevel) {
			case INFO:
			case DEBUG:
			case TRACE:
				//eventLogger.logInfoEvent(message, logType, integrationScenario, contractId, null, extraInfo);
				EventLogMessage infoMsg = new EventLogMessage();
				infoMsg.setMuleMessage(message);
				infoMsg.setLogMessage(logType);
				infoMsg.setIntegrationScenario(integrationScenario);
				infoMsg.setContractId(contractId);
				infoMsg.setBusinessContextId(evaluatedBusinessContextId);
				infoMsg.setExtraInfo(evaluatedExtraInfo);
				
				eventLogger.logInfoEvent(infoMsg);
				break;

			case FATAL:
			case ERROR:
			case WARNING:
				//eventLogger.logErrorEvent(new RuntimeException(logType), message, integrationScenario, contractId, null, extraInfo);
				EventLogMessage errorMsg = new EventLogMessage();
				errorMsg.setMuleMessage(message);
				//errorMsg.setLogMessage(logType);
				errorMsg.setIntegrationScenario(integrationScenario);
				errorMsg.setContractId(contractId);
				errorMsg.setBusinessContextId(evaluatedBusinessContextId);
				errorMsg.setExtraInfo(evaluatedExtraInfo);
				
				if (message.getPayload() instanceof ExceptionMessage) {
					ExceptionMessage me = (ExceptionMessage)message.getPayload();
					Throwable ex = me.getException();
					if (ex.getCause() != null) {
						ex = ex.getCause();
					}
					eventLogger.logErrorEvent(ex, errorMsg);
				} else {
					String evaluatedLogType = evaluateValue("logType", logType, message);
					eventLogger.logErrorEvent(new RuntimeException(evaluatedLogType), errorMsg);
				}
				break;
			}

			return message;
		} catch (Exception e) {
			// be specific about where logging failed, failure might be data-related
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("failed to log event, logType: ");
			errMsg.append(logType);
			errMsg.append(", integrationScenario: ");
			errMsg.append(integrationScenario);
			errMsg.append(", contractId: ");
			errMsg.append(contractId);
			errMsg.append(", businessContextId: ");
			if (businessContextId != null) {
				for (String key : businessContextId.keySet()) {
					errMsg.append("\n  key: ");
					errMsg.append(key);
					errMsg.append(", value: ");
					errMsg.append(businessContextId.get(key));
				}
			}
			errMsg.append(", extraInfo: ");
			if (extraInfo != null) {
				for (String key : extraInfo.keySet()) {
					errMsg.append("\n  key: ");
					errMsg.append(key);
					errMsg.append(", value: ");
					errMsg.append(extraInfo.get(key));
				}
			}
			else {
				errMsg.append("null");
			}
						
			log.error(errMsg.toString(), e);
			
			// TODO: should we really re-throw in cases like this where logging have failed? or
			// should we continue and don't let logging fail message flow?
			throw new RuntimeException(errMsg.toString(), e);
		}
    }

	private Map<String, String> evaluateMapInfo(Map<String, String> map, MuleMessage message) {
		
		if (map == null) return null;
		
		Set<Entry<String, String>> ei = map.entrySet();
		Map<String, String> evaluatedMap = new HashMap<String, String>();
		for (Entry<String, String> entry : ei) {
			String key = entry.getKey();
			String value = entry.getValue();
			value = evaluateValue(key, value, message);
		    evaluatedMap.put(key, value);
		}
		return evaluatedMap;
	}

	private String evaluateValue(String key, String value, MuleMessage message) {
		try {
			if(isValidExpression(value)) {
		    	String before = value;
		    	Object eval = muleContext.getExpressionManager().evaluate(value.toString(), message);

		    	if (eval == null) {
		    		value = "UNKNOWN";

		    	} else if (eval instanceof List) {
		    		@SuppressWarnings("rawtypes")
					List l = (List)eval;
		    		value = l.get(0).toString();

		    	} else {
		    		value = eval.toString();
		    	}
		    	if (log.isDebugEnabled()) {
		    		log.debug("Evaluated extra-info for key: " + key + ", " + before + " ==> " + value);
		    	}
		    }
		} catch (Throwable ex) {
			String errMsg = "Faild to evaluate expression: " + key + " = " + value;
			log.warn(errMsg, ex);
			value = errMsg + ", " + ex;
		}
		return value;
	}

	private boolean isValidExpression(String expression) {
		try {
			return muleContext.getExpressionManager().isValidExpression(expression);
		} catch (Throwable ex) {
			return false;
		}
	}
}