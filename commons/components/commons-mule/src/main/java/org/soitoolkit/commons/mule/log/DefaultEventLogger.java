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

import static org.soitoolkit.commons.mule.core.PropertyNames.SOITOOLKIT_CONTRACT_ID;
import static org.soitoolkit.commons.mule.core.PropertyNames.SOITOOLKIT_CORRELATION_ID;
import static org.soitoolkit.commons.mule.core.PropertyNames.SOITOOLKIT_INTEGRATION_SCENARIO;
import static org.soitoolkit.commons.mule.core.PropertyNames.SOITOOLKIT_BUSINESS_CONTEXT_ID;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.mule.RequestContext;
import org.mule.api.MuleContext;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.config.MuleConfiguration;
import org.mule.api.context.MuleContextAware;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.config.DefaultMuleConfiguration;
import org.mule.config.ExceptionHelper;
import org.mule.module.xml.stax.ReversibleXMLStreamReader;
import org.mule.transport.jms.JmsConnector;
import org.mule.transport.jms.JmsMessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.soitoolkit.commons.logentry.schema.v1.LogEntryType;
import org.soitoolkit.commons.logentry.schema.v1.LogEntryType.ExtraInfo;
import org.soitoolkit.commons.logentry.schema.v1.LogEvent;
import org.soitoolkit.commons.logentry.schema.v1.LogLevelType;
import org.soitoolkit.commons.logentry.schema.v1.LogMessageExceptionType;
import org.soitoolkit.commons.logentry.schema.v1.LogMessageType;
import org.soitoolkit.commons.logentry.schema.v1.LogMetadataInfoType;
import org.soitoolkit.commons.logentry.schema.v1.LogRuntimeInfoType;
import org.soitoolkit.commons.logentry.schema.v1.LogRuntimeInfoType.BusinessContextId;
import org.soitoolkit.commons.mule.api.log.EventLogMessage;
import org.soitoolkit.commons.mule.api.log.EventLogger;
import org.soitoolkit.commons.mule.jaxb.JaxbObjectToXmlTransformer;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;
import org.soitoolkit.commons.mule.util.MuleUtil;
import org.soitoolkit.commons.mule.util.XmlUtil;

/**
 * Log events in a standardized way
 * 
 * @author Magnus Larsson
 *
 */
public class DefaultEventLogger implements EventLogger, MuleContextAware {

	private static final String CAUSE_EXCEPTION_HEADER = "CauseException";

	private static final Logger messageLogger = LoggerFactory.getLogger("org.soitoolkit.commons.mule.messageLogger");

	private static final Logger log = LoggerFactory.getLogger(DefaultEventLogger.class);

	// Creating JaxbUtil objects (i.e. JaxbContext objects)  are costly, so we only keep one instance.
	// According to https://jaxb.dev.java.net/faq/index.html#threadSafety this should be fine since they are thread safe!
	private static final JaxbUtil JAXB_UTIL = new JaxbUtil(LogEvent.class);

	private static final String MSG_ID = "soi-toolkit.log";
	private static final String LOG_EVENT_INFO = "logEvent-info";
	private static final String LOG_EVENT_ERROR = "logEvent-error";
	private static final String LOG_STRING = MSG_ID + 
		"\n** {}.start ***********************************************************" +
		"\nIntegrationScenarioId={}\nContractId={}\nLogMessage={}\nServiceImpl={}\nHost={} ({})\nComponentId={}\nEndpoint={}\nMessageId={}\nBusinessCorrelationId={}\nBusinessContextId={}\nExtraInfo={}\nPayload={}" + 
		"{}" + // Placeholder for stack trace info if an error is logged
		"\n** {}.end *************************************************************";

	private static InetAddress HOST = null;
	private static String HOST_NAME = "UNKNOWN";
	private static String HOST_IP = "UNKNOWN";
	private static String PROCESS_ID = "UNKNOWN";

	private String serverId = null; // Can't read this one at class initialization because it is not set at that time. Can also be different for different loggers in the same JVM (e.g. multiple wars in one servlet container with shared classes?))

	// Used to transform payloads that are jaxb-objects into a xml-string
	private JaxbObjectToXmlTransformer jaxbToXml = null;

	{
		try {
			// Let's give it a try, fail silently...
			HOST       = InetAddress.getLocalHost();
			HOST_NAME  = HOST.getCanonicalHostName();
			HOST_IP    = HOST.getHostAddress();
			PROCESS_ID = ManagementFactory.getRuntimeMXBean().getName();
		} catch (Throwable ex) {
		}
	}

	public DefaultEventLogger() {
		log.debug("constructor");
	}


	/*
	 * Property muleContext 
	 */
	private MuleContext muleContext = null;
	public void setMuleContext(MuleContext muleContext) {
		log.debug("MuleContext injected");
		this.muleContext = muleContext;
	}
	
	/**
	 * Setter for the jaxbToXml property
	 * 
	 * @param jaxbToXml
	 */
	public void setJaxbToXml(JaxbObjectToXmlTransformer jaxbToXml) {
		this.jaxbToXml  = jaxbToXml;
	}
	
	// ----------
	//
	// PUBLIC API
	//
	// ----------

	/**
	 * Implements EventLogger.logInfoEvent()
	 */
	public void logInfoEvent(EventLogMessage elm) {
		logInfoEvent(elm.getMuleMessage(), elm.getLogMessage(), elm.getIntegrationScenario(),
				elm.getContractId(), elm.getBusinessContextId(), elm.getExtraInfo());		
	}

	/**
	 * Implements EventLogger.logErrorEvent()
	 */
	public void logErrorEvent(Throwable error, EventLogMessage elm) {
		logErrorEvent(error, elm.getMuleMessage(), elm.getIntegrationScenario(),
				elm.getContractId(), elm.getBusinessContextId(), elm.getExtraInfo());
	}

	/**
	 * Implements EventLogger.logErrorEvent()
	 */
	public void logErrorEvent(Throwable error, Object payload,
			EventLogMessage elm) {
		logErrorEvent(error, payload, elm.getBusinessContextId(), elm.getExtraInfo());
	}


	/**
	 * Old logInfoEvent() wrapped by the new
	 */
	private void logInfoEvent (
		MuleMessage message,
		String      logMessage,
		String      integrationScenario, 
		String      contractId, 
		Map<String, String> businessContextId,
		Map<String, String> extraInfo) {
		
		if (messageLogger.isInfoEnabled()) {
			LogEvent logEvent = createLogEntry(LogLevelType.INFO, message, logMessage, integrationScenario, contractId, businessContextId, extraInfo, message.getPayload(), null);
			
			String logMsg = formatLogMessage(LOG_EVENT_INFO, logEvent);
			messageLogger.info(logMsg);

			// TODO: Move JAXB processing into the dispatch method
			String xmlString = JAXB_UTIL.marshal(logEvent);
			dispatchInfoEvent(xmlString);
		}
	}

	/**
	 * Old logErrorEvent() wrapped by the new
	 */
	private void logErrorEvent (
		Throwable   error,
		MuleMessage message,
		String      integrationScenario, 
		String      contractId, 
		Map<String, String> businessContextId,
		Map<String, String> extraInfo) {

		LogEvent logEvent = createLogEntry(LogLevelType.ERROR, message, error.toString(), integrationScenario, contractId, businessContextId, extraInfo, message.getPayload(), error);
		
		String logMsg = formatLogMessage(LOG_EVENT_ERROR, logEvent);
		messageLogger.error(logMsg);

		// TODO: Move JAXB processing into the dispatch method
		String xmlString = JAXB_UTIL.marshal(logEvent);
		dispatchErrorEvent(xmlString);
	}

	/**
	 * Old logErrorEvent() wrapped by the new
	 */
	private void logErrorEvent (
		Throwable   error,
		Object      payload,
		Map<String, String> businessContextId,
		Map<String, String> extraInfo) {

		LogEvent logEvent = createLogEntry(LogLevelType.ERROR, null, error.toString(), null, null, businessContextId, extraInfo, payload, error);

		String logMsg = formatLogMessage(LOG_EVENT_ERROR, logEvent);
		messageLogger.error(logMsg);

		// TODO: Move JAXB processing into the dispatch method
		String xmlString = JAXB_UTIL.marshal(logEvent);
		dispatchErrorEvent(xmlString);
	}

	// ---------------------
	//
	// HOOKS FOR SUB-CLASSES
	//
	// ---------------------

	/**
	 * Creates a LogEvent
	 */
	protected LogEvent createLogEntry(
		LogLevelType logLevel,
		MuleMessage message, 
		String logMessage,
		String argIntegrationScenario, String argContractId, Map<String, String> businessContextId,
		Map<String, String> extraInfo,
		Object payload,
		Throwable exception) {

		// --------------------------
		//
		// 1. Process input variables
		//
		// --------------------------

		// TODO: Will event-context always be null when an error is reported?
		// If so then its probably better to move this code to the info-logger method.
	    String           serviceImplementation = "";
		String           endpoint    = "";
        MuleEventContext event       = RequestContext.getEventContext();
        if (event != null) {
		    serviceImplementation   = MuleUtil.getServiceName(event);
		    EndpointURI endpointURI = event.getEndpointURI();
			endpoint                = (endpointURI == null)? "" : endpointURI.toString();
        }
		
		String messageId             = "";
		String integrationScenarioId = ""; 
		String contractId            = ""; 
		String businessCorrelationId = "";
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

		String componentId = getServerId();

		// Only extract payload if debug is enabled!
	    String payloadASstring = (messageLogger.isDebugEnabled())? getPayloadAsString(payload) : "";
		

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
		
		// Add any business contexts
		if (businessContextId != null) {
			Set<Entry<String, String>> entries = businessContextId.entrySet();
			for (Entry<String, String> entry : entries) {
				BusinessContextId bxid = new BusinessContextId();
				bxid.setName(entry.getKey());
				bxid.setValue(entry.getValue());
				lri.getBusinessContextId().add(bxid);
			}
		}
		
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
		lmi.setLoggerName(messageLogger.getName());
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
	 * Formats a LogEvent for logging in the log-system (log4j)
	 */
	protected String formatLogMessage(String logEventName, LogEvent logEvent) {
		LogMessageType      messageInfo  = logEvent.getLogEntry().getMessageInfo();
		LogMetadataInfoType metadataInfo = logEvent.getLogEntry().getMetadataInfo();
		LogRuntimeInfoType  runtimeInfo  = logEvent.getLogEntry().getRuntimeInfo();

		String integrationScenarioId   = metadataInfo.getIntegrationScenarioId();
		String contractId              = metadataInfo.getContractId();
		String logMessage              = messageInfo.getMessage();
		String serviceImplementation   = metadataInfo.getServiceImplementation();
		String componentId             = runtimeInfo.getComponentId();
		String endpoint                = metadataInfo.getEndpoint();
		String messageId               = runtimeInfo.getMessageId();
		String businessCorrelationId   = runtimeInfo.getBusinessCorrelationId();
		String payload                 = logEvent.getLogEntry().getPayload();
		String businessContextIdString = businessContextIdToString(runtimeInfo.getBusinessContextId());
		String extraInfoString         = extraInfoToString(logEvent.getLogEntry().getExtraInfo());
		
		StringBuffer stackTrace = new StringBuffer();
		LogMessageExceptionType lmeException = logEvent.getLogEntry().getMessageInfo().getException();
		if (lmeException != null) {
			String ex = lmeException.getExceptionClass();
			String msg = lmeException.getExceptionMessage();
			List<String> st = lmeException.getStackTrace();

			stackTrace.append('\n').append("Stacktrace=").append(ex).append(": ").append(msg);
			for (String stLine : st) {
				if (stLine.startsWith(CAUSE_EXCEPTION_HEADER)) {
					stackTrace.append("\n\n").append(stLine);
					
				} else {
					stackTrace.append("\n\t at ").append(stLine);
				}
			}
		}
		return MessageFormatter.arrayFormat(LOG_STRING, new String[] {logEventName, integrationScenarioId, contractId, logMessage, serviceImplementation, HOST_NAME, HOST_IP, componentId, endpoint, messageId, businessCorrelationId, businessContextIdString, extraInfoString, payload, stackTrace.toString(), logEventName}).getMessage();
	}
	
	/**
	 * Dispatch an info event for background processing outside the log-system (log4j).
	 */
	protected void dispatchInfoEvent(String msg) {
		dispatchEvent("SOITOOLKIT.LOG.INFO", msg);
//		dispatchEvent("vm://soitoolkit-info-log", msg);
//		dispatchEvent("soitoolkit-info-log-endpoint", msg);
	}

	/**
	 * Dispatch an error event for background processing outside the log-system (log4j).
	 */
	protected void dispatchErrorEvent(String msg) {
		dispatchEvent("SOITOOLKIT.LOG.ERROR", msg);
//		dispatchEvent("vm://soitoolkit-error-log", msg);
//		dispatchEvent("soitoolkit-error-log-endpoint", msg);
	}


	// ---------------
	//
	// PRIVATE METHODS
	//
	// ---------------
	
	// ------------------------------------------------------------
	// Private methods for dispatchInfoEvent and dispatchErrorEvent
	// ------------------------------------------------------------

	private void dispatchEvent(String queue, String msg) {
		try {

			Session s = null;
			try {
				s = getSession();
				sendOneTextMessage(s, queue, msg);
			} finally {
	    		if (s != null) s.close(); 
			}
			
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
	}

	private Session getSession() throws JMSException {
//		JmsConnector jmsConn = (JmsConnector)MuleServer.getMuleContext().getRegistry().lookupConnector("soitoolkit-jms-connector");
		JmsConnector jmsConn = (JmsConnector)MuleUtil.getSpringBean(muleContext, "soitoolkit-jms-connector");
		Connection c = jmsConn.getConnection();
		Session s = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
		return s;
	}

	private void sendOneTextMessage(Session session, String queueName, String message) {

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

	// ------------------------------------
	// Private methods for formatLogMessage
	// ------------------------------------

	private String businessContextIdToString(List<BusinessContextId> businessContextIds) {
		
		if (businessContextIds == null) return "";
		
		StringBuffer businessContextIdString = new StringBuffer();
		for (BusinessContextId bci : businessContextIds) {
			businessContextIdString.append("\n-").append(bci.getName()).append("=").append(bci.getValue());
		}
		return businessContextIdString.toString();
	}

	private String extraInfoToString(List<ExtraInfo> extraInfo) {
		
		if (extraInfo == null) return "";
		
		StringBuffer extraInfoString = new StringBuffer();
		for (ExtraInfo ei : extraInfo) {
			extraInfoString.append("\n-").append(ei.getName()).append("=").append(ei.getValue());
		}
		return extraInfoString.toString();
	}

	private String getServerId() {

		if (serverId != null) return serverId;
		
		if (muleContext == null) return "UNKNOWN.MULE_CONTEXT"; 

		MuleConfiguration mConf = muleContext.getConfiguration();
		if (mConf == null) return "UNKNOWN.MULE_CONFIGURATION"; 
		
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
		if (jaxbToXml == null) {
			content = "Missing jaxb2xml injection, can't marshal JAXB object of type: "
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
				content = (String) jaxbToXml.doTransform(jaxbObject,
						outputEncoding);
			} catch (TransformerException e) {
				e.printStackTrace();
				content = "JAXB object marshalling failed: " + e.getMessage();
			}
		}
		return content;
	}

	private String getJaxbWrapperElementName(Object payload) {
		String name = payload.getClass().getSimpleName();
		String elementName = name.substring(0, 1).toLowerCase()
				+ name.substring(1);
		return elementName;
	}

}