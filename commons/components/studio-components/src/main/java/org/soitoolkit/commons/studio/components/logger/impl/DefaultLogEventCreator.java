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
package org.soitoolkit.commons.studio.components.logger.impl;

import static org.mule.api.config.MuleProperties.MULE_ENDPOINT_PROPERTY;
import static org.mule.transport.http.HttpConnector.HTTP_METHOD_PROPERTY;
import static org.mule.transport.http.HttpConnector.HTTP_REQUEST_PROPERTY;
import static org.soitoolkit.commons.studio.components.fromcommonsmule.core.PropertyNames.SOITOOLKIT_BUSINESS_CONTEXT_ID;
import static org.soitoolkit.commons.studio.components.fromcommonsmule.core.PropertyNames.SOITOOLKIT_CONTRACT_ID;
import static org.soitoolkit.commons.studio.components.fromcommonsmule.core.PropertyNames.SOITOOLKIT_CORRELATION_ID;
import static org.soitoolkit.commons.studio.components.fromcommonsmule.core.PropertyNames.SOITOOLKIT_INTEGRATION_SCENARIO;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.mule.RequestContext;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.config.MuleConfiguration;
import org.mule.api.transport.PropertyScope;
import org.mule.config.DefaultMuleConfiguration;
import org.mule.config.ExceptionHelper;
import org.mule.module.xml.stax.ReversibleXMLStreamReader;
import org.mule.transport.jms.JmsMessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.logentry.schema.v1.LogEntryType;
import org.soitoolkit.commons.logentry.schema.v1.LogEntryType.ExtraInfo;
import org.soitoolkit.commons.logentry.schema.v1.LogEvent;
import org.soitoolkit.commons.logentry.schema.v1.LogLevelType;
import org.soitoolkit.commons.logentry.schema.v1.LogMessageExceptionType;
import org.soitoolkit.commons.logentry.schema.v1.LogMessageType;
import org.soitoolkit.commons.logentry.schema.v1.LogMetadataInfoType;
import org.soitoolkit.commons.logentry.schema.v1.LogRuntimeInfoType;
import org.soitoolkit.commons.logentry.schema.v1.LogRuntimeInfoType.BusinessContextId;
import org.soitoolkit.commons.studio.components.fromcommonsmule.util.MuleUtil;
import org.soitoolkit.commons.studio.components.fromcommonsmule.util.XmlUtil;
import org.soitoolkit.commons.studio.components.logger.api.LogEventCreator;
import org.springframework.context.annotation.Primary;

@Named
@Primary
public class DefaultLogEventCreator implements LogEventCreator {

	private static final Logger log = LoggerFactory.getLogger(DefaultLogEventCreator.class);

	private static final String CAUSE_EXCEPTION_HEADER = "CauseException";

	private static InetAddress HOST = null;
	private static String HOST_NAME = "UNKNOWN";
	private static String HOST_IP = "UNKNOWN";
	private static String PROCESS_ID = "UNKNOWN";

	private String serverId = null; // Can't read this one at class initialization because it is not set at that time. Can also be different for different loggers in the same JVM (e.g. multiple wars in one servlet container with shared classes?))

	static {
		try {
			// Let's give it a try, fail silently...
			HOST       = InetAddress.getLocalHost();
			HOST_NAME  = HOST.getHostName();
			HOST_IP    = HOST.getHostAddress();
			PROCESS_ID = ManagementFactory.getRuntimeMXBean().getName();
		} catch (Throwable ex) {
		}
	}

	private JAXBContext jaxbContext = null;

	/**
	 * Setter for the jaxbContext property
	 * 
	 * @param jaxbContext
	 */
	@Inject
	public void setJaxbContext(JAXBContext jaxbContext) {
		this.jaxbContext  = jaxbContext;
	}

	@Override
	public LogEvent createLogEvent(
		MuleEvent muleEvent, 
		LogLevelType logLevel,
		String logMessage, 
		String loggerName,
		String argIntegrationScenario, // TODO integrationScenario, 
		String argContractId, // TODO contractId,
		String correlationId,
		Map<String, String> extraInfo, Throwable exception, 
		Object payload) {

		// --------------------------
		//
		// 1. Process input variables
		//
		// --------------------------

		MuleMessage message = muleEvent.getMessage();
		
		// TODO: Will event-context always be null when an error is reported?
		// If so then its probably better to move this code to the info-logger method.
	    String           serviceImplementation = "";
        MuleEventContext event       = RequestContext.getEventContext();
        if (event != null) {
		    serviceImplementation   = MuleUtil.getServiceName(event);
        }

        String endpoint = getEndpoint(message, event);
		
		String messageId             = "";
		String integrationScenarioId = ""; 
		String contractId            = ""; 
		String businessCorrelationId = correlationId;
		String propertyBusinessContextId = null;

		if (message != null) {

			if (log.isDebugEnabled()) {
				@SuppressWarnings("rawtypes")
				Set names = message.getPropertyNames(PropertyScope.INBOUND);
				for (Object object : names) {
					Object value = message.getInboundProperty(object.toString());
					log.debug(object + " = " + value + " (" + object.getClass().getName() + ")");
				}
			}
			
			messageId             = message.getUniqueId();
			contractId            = message.getInboundProperty(SOITOOLKIT_CONTRACT_ID, "");
			businessCorrelationId = message.getSessionProperty(SOITOOLKIT_CORRELATION_ID, "");
			integrationScenarioId = message.getInboundProperty(SOITOOLKIT_INTEGRATION_SCENARIO, "");
			propertyBusinessContextId = message.getInboundProperty(SOITOOLKIT_BUSINESS_CONTEXT_ID, null);
			
			// Override contract id from the message properties with the supplied one from the log-call, if any
			if (argContractId != null && argContractId.length() > 0) {
				contractId = argContractId;
			}

			// Override contract id from the message properties with the supplied one from the log-call, if any
			if (argIntegrationScenario != null && argIntegrationScenario.length() > 0) {
				integrationScenarioId = argIntegrationScenario;
			}
		}

		String componentId = getServerId(muleEvent);
	    String payloadASstring = getPayloadAsString(payload);
		

	    // -------------------------
	    //
	    // 2. Create LogEvent object
	    //
	    // -------------------------
		
		// Setup basic runtime information for the log entry
		LogRuntimeInfoType lri = new LogRuntimeInfoType();
		lri.setTimestamp(XmlUtil.convertDateToXmlDate(null));
		lri.setHostName(HOST_NAME);
		lri.setHostIp(HOST_IP);
		lri.setProcessId(PROCESS_ID);
		lri.setThreadId(Thread.currentThread().getName());
		lri.setComponentId(componentId);
		lri.setMessageId(messageId);
		lri.setBusinessCorrelationId(businessCorrelationId); 
		
//		// Add any business contexts
//		if (businessContextId != null) {
//			Set<Entry<String, String>> entries = businessContextId.entrySet();
//			for (Entry<String, String> entry : entries) {
//				BusinessContextId bxid = new BusinessContextId();
//				bxid.setName(entry.getKey());
//				bxid.setValue(entry.getValue());
//				lri.getBusinessContextId().add(bxid);
//			}
//		}
		
		// Also add any business contexts from message properties
		if (propertyBusinessContextId != null) {
			String[] propertyArr = propertyBusinessContextId.split(",");
			
			for (String property : propertyArr) {
				String[] nameValueArr = property.split("=");
				String name = nameValueArr[0];
				String value = (nameValueArr.length > 1) ? nameValueArr[1] : "";
				BusinessContextId bxid = new BusinessContextId();
				bxid.setName(name);
				bxid.setValue(value);
				lri.getBusinessContextId().add(bxid);				
			}
			
		}
		

		// Setup basic metadata information for the log entry
		LogMetadataInfoType lmi = new LogMetadataInfoType();
		lmi.setLoggerName(loggerName);
		lmi.setIntegrationScenarioId(integrationScenarioId);
		lmi.setContractId(contractId);
		lmi.setServiceImplementation(serviceImplementation);
		lmi.setEndpoint(endpoint);

		
		// Setup basic information of the log message for the log entry
		LogMessageType lm = new LogMessageType();
		lm.setLevel(logLevel);
		lm.setMessage(logMessage);
		
		
		// Setup exception information if present
		if (exception != null) {
			
			exception = (DefaultMuleConfiguration.verboseExceptions) ? exception : ExceptionHelper.summarise(exception, 5);
            
			LogMessageExceptionType lme = new LogMessageExceptionType();
			
			lme.setExceptionClass(exception.getClass().getName());
			lme.setExceptionMessage(exception.getMessage());
			StackTraceElement[] stArr = exception.getStackTrace();
			List<String> stList = new ArrayList<String>();
			for (int i = 0; i < stArr.length; i++) {
				stList.add(stArr[i].toString());
			}
			
			if (exception.getCause() != null) {
				Throwable ce = exception.getCause();
				ce = (DefaultMuleConfiguration.verboseExceptions) ? ce : ExceptionHelper.summarise(ce, 5);
				stList.add(CAUSE_EXCEPTION_HEADER + ": " + ce.getMessage());
				StackTraceElement[] ceStArr = ce.getStackTrace();
				for (int i = 0; i < ceStArr.length; i++) {
					stList.add(ceStArr[i].toString());
				}
			}

			if (!DefaultMuleConfiguration.verboseExceptions) {
				stList.add("*** set debug level logging or '-Dmule.verbose.exceptions=true' for full stacktrace ***");
			}
			lme.getStackTrace().addAll(stList);

			//			if (exception instanceof MuleException) {
//					MuleException de = (MuleException)exception;
//					System.err.println("Cause: " + de.getCause());
//					StackTraceElement[] st = de.getCause().getStackTrace();
//					for (int i = 0; i < st.length; i++) {
////						stList.add(st[i].toString());
//						System.err.println(st[i].toString());
//					}
////					System.err.println("Detailed: " + de.getDetailedMessage());
////					System.err.println("Summary: " + de.getSummaryMessage());
////					System.err.println("Verbose: " + de.getVerboseMessage());
//				}
			
			lm.setException(lme);
		}


		// Create the log entry object
		LogEntryType logEntry = new LogEntryType();
		logEntry.setMetadataInfo(lmi);
		logEntry.setRuntimeInfo(lri);
		logEntry.setMessageInfo(lm);
		logEntry.setPayload(payloadASstring);

		// Add any extra info
		if (extraInfo != null) {
			Set<Entry<String, String>> entries = extraInfo.entrySet();
			for (Entry<String, String> entry : entries) {
				ExtraInfo ei = new ExtraInfo();
				ei.setName(entry.getKey());
				ei.setValue(entry.getValue());
				logEntry.getExtraInfo().add(ei);
			}
		}
		
		// Create the final log event object
		LogEvent logEvent = new LogEvent();
		logEvent.setLogEntry(logEntry);
		
		
		// We are actually done :-)
		return logEvent;
	}

	/**
	 * Pick up the most relevant endpoint information:
	 * 
	 * 1. First from the outbound property MULE_ENDPOINT_PROPERTY if found
	 * 2. Secondly from the inbound property MULE_ENDPOINT_PROPERTY if found
	 * 3. Last try with the mule-event's endpoint-info
	 * 
	 * @param message
	 * @param event
	 * @return
	 */
	protected String getEndpoint(MuleMessage message, MuleEventContext event) {
	    try {
	    	if (message != null) {
	        	String outEp = message.getOutboundProperty(MULE_ENDPOINT_PROPERTY);
	        	if (outEp != null) {
	        		// If http endpoint then try to add the http-method
	        		if (outEp.startsWith("http")) {
	        			String httpMethod = message.getOutboundProperty(HTTP_METHOD_PROPERTY);
	        			if (httpMethod != null) {
	        				outEp += " (" + httpMethod + ")";
	        			}
	        		}
	        		return outEp;
	        	}
	        	
	        	String inEp  = message.getInboundProperty(MULE_ENDPOINT_PROPERTY);
	        	if (inEp != null) {
	        		// If http endpoint then try to add the http-method
	        		if (inEp.startsWith("http")) {
	        			String httpMethod = message.getInboundProperty(HTTP_METHOD_PROPERTY);
	        			if (httpMethod != null) {
	        				inEp += " (" + httpMethod + ")";
	        			}
	        		}
	        		return inEp;
	        	}
	    	}
	
	    	if (event != null) {
			    URI endpointURI = event.getEndpointURI();
				String ep = (endpointURI == null)? "" : endpointURI.toString();
	    		if (ep.startsWith("http")) {
	    			String httpMethod  = message.getInboundProperty(HTTP_METHOD_PROPERTY);
	    			String httpRequest = message.getInboundProperty(HTTP_REQUEST_PROPERTY);
	    			if (httpMethod != null) {
	    				ep += " (" + httpMethod + " on " + httpRequest + ")";
	    			}
	    		}
				
				
				return ep;
	        }
	
	    	// No luck at all this time :-(
	    	return "";
	
	    } catch (Throwable ex) {
	    	// Really bad...
	    	return "GET-ENDPOINT ERROR: " + ex.getMessage();
	    }
	}
	
	private String getServerId(MuleEvent muleEvent) {

		// Return serverId if already set
		if (serverId != null) return serverId;

		// Try to get the serverId;
		if (muleEvent == null) return "UNKNOWN.NULL_MULE_EVENT"; 

		MuleContext muleContext = muleEvent.getMuleContext();
		if (muleContext == null) return "UNKNOWN.NULL_MULE_CONTEXT"; 

		MuleConfiguration mConf = muleContext.getConfiguration();
		if (mConf == null) return "UNKNOWN.NULL_MULE_CONFIGURATION"; 
		
		// Ok, we got! Save and return it.
		return serverId = mConf.getId();
	}

	private String getPayloadAsString(Object payload) {
		String content = null;
		if (payload instanceof Object[]) {
			Object[] arr = (Object[]) payload;
			int i = 0;
			for (Object object : arr) {
				String arrContent = "[" + i++ + "]: "
						+ getContentAsString(object);
				if (i == 1) {
					content = arrContent;
				} else {
					content += "\n" + arrContent;
				}
			}

		} else {
			content = getContentAsString(payload);
		}
		return content;
	}

	private String getContentAsString(Object payload) {
		String content = null;

		if (payload == null) {
			return null;

		} else if (payload instanceof byte[]) {
			content = getByteArrayContentAsString(payload);

		} else if (payload instanceof ReversibleXMLStreamReader) {
			content = XmlUtil.convertReversibleXMLStreamReaderToString(
					(ReversibleXMLStreamReader) payload, "UTF-8");

		} else if (payload instanceof Message) {
			content = convertJmsMessageToString(payload, "UTF-8");

		} else if (isJabxObject(payload)) {
			content = getJaxbContentAsString(payload, "UTF-8");

			// } else if (payload instanceof ChunkedInputStream) {
			// contents = message.getPayloadAsString();
			// message.setPayload(contents);

		} else {
			// Using message.getPayloadAsString() consumes InputStreams causing
			// exceptions after the logging...
			// contents = message.getPayloadAsString();
			content = payload.toString();
		}

		return content;
	}

	private String convertJmsMessageToString(Object payload, String outputEncoding) {
		try {
			return JmsMessageUtils.toObject((Message) payload, null,
					outputEncoding).toString();
		} catch (JMSException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String getByteArrayContentAsString(Object payload) {
		String content;
		StringBuffer byteArray = new StringBuffer();
		byte[] bytes = (byte[]) payload;
		for (int i = 0; i < bytes.length; i++) {
			byteArray.append((char) bytes[i]);
		}
		content = byteArray.toString();
		return content;
	}

	private boolean isJabxObject(Object payload) {
		return payload.getClass().isAnnotationPresent(XmlType.class);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private String getJaxbContentAsString(Object jaxbObject, String outputEncoding) {
		String content;
		if (jaxbContext == null) {
			content = "Missing jaxbContext injection, can't marshal JAXB object of type: "
					+ jaxbObject.getClass().getName();
		} else {

			if (!jaxbObject.getClass()
					.isAnnotationPresent(XmlRootElement.class)) {
				// We are missing element end namespace info, let's create a
				// wrapper xml-root-element
				QName wrapperQName = new QName("class:"
						+ jaxbObject.getClass().getName(),
						getJaxbWrapperElementName(jaxbObject));
				jaxbObject = new JAXBElement(wrapperQName, jaxbObject
						.getClass(), null, jaxbObject);
			}

			try {
		    	content = marshalJaxbObject(jaxbObject);
			} catch (Throwable e) {
				e.printStackTrace();
				content = "JAXB object marshalling failed: " + e.getMessage();
			}
		}
		return content;
	}

	private String marshalJaxbObject(Object jaxbObject) {
		try {
		    StringWriter writer = new StringWriter();
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.marshal(jaxbObject, writer);
			return writer.toString();
			
		} catch (JAXBException e) {
		    throw new RuntimeException(e);
		}
	}

	private String getJaxbWrapperElementName(Object payload) {
		String name = payload.getClass().getSimpleName();
		String elementName = name.substring(0, 1).toLowerCase()
				+ name.substring(1);
		return elementName;
	}
}
