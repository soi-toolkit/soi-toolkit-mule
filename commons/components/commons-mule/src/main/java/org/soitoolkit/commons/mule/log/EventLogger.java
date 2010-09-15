package org.soitoolkit.commons.mule.log;

import static org.soitoolkit.commons.mule.core.PropertyNames.SOITOOLKIT_INTEGRATION_SCENARIO;
import static org.soitoolkit.commons.mule.core.PropertyNames.SOITOOLKIT_CORRELATION_ID;
import static org.soitoolkit.commons.mule.core.PropertyNames.SOITOOLKIT_CONTRACT_ID;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.mule.MuleServer;
import org.mule.RequestContext;
import org.mule.api.MuleContext;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.config.MuleConfiguration;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.service.Service;
import org.mule.api.transformer.TransformerException;
import org.mule.module.xml.stax.ReversibleXMLStreamReader;
import org.mule.transport.jms.JmsMessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.soitoolkit.commons.mule.jaxb.JaxbObjectToXmlTransformer;
import org.soitoolkit.commons.mule.util.XmlUtil;

/**
 * Log events in a standardized way
 * 
 * @author Magnus Larsson
 *
 */
public class EventLogger {
	
	private static final Logger messageLogger = LoggerFactory.getLogger("org.soitoolkit.commons.mule.messageLogger");

	private static final String MSG_ID = "soi-toolkit.log";
	private static final String LOG_EVENT_INFO = "logEvent-info";
	private static final String LOG_EVENT_ERROR = "logEvent-error";
	private static final String LOG_STRING = MSG_ID + 
		"\n** {}.start ***********************************************************" +
		"\nIntegrationScenario={}\nContractId={}\nAction={}\nService={}\nHost={} ({})\nServer={}\nEndpoint={}\nMessageId={}\nBusinessCorrelationId={}\nPayload={}\nBusinessContextId={}" + 
		"\n** {}.end *************************************************************";

	private static InetAddress HOST;
	private static String HOST_NAME;
	private static String HOST_IP;

	private String serverId = null; // Can't read this one at class initialization because it is not set at that time. Can also be different for different loggers in the same JVM (e.g. multiple wars in one servlet container with shared classes?))

	// Used to transform payloads that are jaxb-objects into a xml-string
	private JaxbObjectToXmlTransformer jaxb2xml = null;

	{
		try {
			HOST = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		HOST_NAME = HOST.getCanonicalHostName();
		HOST_IP   = HOST.getHostAddress();
	}

	public EventLogger() {
	}

	public EventLogger(JaxbObjectToXmlTransformer jaxb2xml) {
		this.jaxb2xml  = jaxb2xml;
	}

	public void logInfoEvent (
		MuleMessage message,
		String      action,
		Map<String, String> businessContextId) {

		if (messageLogger.isInfoEnabled()) {
			messageLogger.info(formatLogMessage(LOG_EVENT_INFO, message, action, businessContextId, message.getPayload()));
		}
	}

	public void logErrorEvent (
		Throwable   error,
		MuleMessage message) {

		messageLogger.error(formatLogMessage(LOG_EVENT_ERROR, message, error.toString(), null, message.getPayload()), error);
	}

	public void logErrorEvent (
		Throwable   error,
		Object      payload) {

		messageLogger.error(formatLogMessage(LOG_EVENT_ERROR, null, error.toString(), null, payload), error);
	}

	private String formatLogMessage(
		String logEventName,
		MuleMessage message, 
		String action,
		Map<String, String> businessContextId,
		Object payload) {

		// TODO: Will event-context always be null when an error is reported?
		// If so then its probably better to move this code to the info-logger method.
	    String           serviceName = "";
		String           endpoint    = "";
        MuleEventContext event       = RequestContext.getEventContext();
        if (event != null) {
	        Service          service     = event.getService();
		    EndpointURI      endpointURI = event.getEndpointURI();
		    serviceName = (service == null)? "" : service.getName();
			endpoint    = (endpointURI == null)? "" : endpointURI.toString();
        }
		
		String messageId                  = "";
		String integrationScenario        = ""; 
		String contractId                 = ""; 
		String correlationId              = "";
		String businessContextIdString    = "";

		if (message != null) {
			messageId           = message.getUniqueId();
			contractId          = message.getStringProperty(SOITOOLKIT_CONTRACT_ID, "");
			correlationId       = message.getStringProperty(SOITOOLKIT_CORRELATION_ID, "");
			integrationScenario = message.getStringProperty(SOITOOLKIT_INTEGRATION_SCENARIO, "");
		}
		businessContextIdString = businessContextIdToString(businessContextId);

		// Only extract payload if debug is enabled!
	    String payloadASstring = (messageLogger.isDebugEnabled())? getPayloadAsString(payload) : "";
		
		return MessageFormatter.arrayFormat(LOG_STRING, new String[] {logEventName, integrationScenario, contractId, action, serviceName, HOST_NAME, HOST_IP, getServerId(), endpoint, messageId, correlationId, payloadASstring, businessContextIdString, logEventName});
	}
	
	private String businessContextIdToString(Map<String, String> businessContextId) {
		
		if (businessContextId == null) return "";
		
		StringBuffer businessContextIdString = new StringBuffer();
		for (String key : businessContextId.keySet()) {
			String value = businessContextId.get(key);
			businessContextIdString.append("\n-").append(key).append(" = ").append(value);
		}
		return businessContextIdString.toString();
	}

	private String getServerId() {

		if (serverId != null) return serverId;
		
		MuleContext mCtx = MuleServer.getMuleContext();
		if (mCtx == null) return "UNKNOWN.MULE_CONTEXT"; 

		MuleConfiguration mConf = mCtx.getConfiguration();
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

	@SuppressWarnings("unchecked")
	private String getJaxbContentAsString(Object jaxbObject, String outputEncoding) {
		String content;
		if (jaxb2xml == null) {
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
				content = (String) jaxb2xml.doTransform(jaxbObject,
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
