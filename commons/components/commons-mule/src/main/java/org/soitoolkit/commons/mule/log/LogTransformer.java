package org.soitoolkit.commons.mule.log;

import static org.soitoolkit.commons.logentry.schema.v1.LogLevelType.INFO;

import org.mule.RequestContext;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.service.Service;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageAwareTransformer;
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
public class LogTransformer extends AbstractMessageAwareTransformer {

	private static final Logger log = LoggerFactory.getLogger(LogTransformer.class);

	private final EventLogger eventLogger;

	private LogLevelType logLevel = INFO;

	public void setLogLevel(LogLevelType logLevel) {
		this.logLevel = logLevel;
	}

	private String logType;
	public void setLogType(String logType) {
		this.logType = logType;
	}

	private JaxbObjectToXmlTransformer jaxb2xml = null;
	public void setJaxbObjectToXml(JaxbObjectToXmlTransformer jaxb2xml) {
		this.jaxb2xml = jaxb2xml;
	}
	
	public LogTransformer() {
		eventLogger = new EventLogger(jaxb2xml);		
	}

	public Object transform(MuleMessage message, String outputEncoding) throws TransformerException {

    	try {
			// Skip logging if an error has occurred, then the error is logged by an error handler
    		if (message.getExceptionPayload() != null) {
    			log.debug("Skip logging message, exception detected!");
    			return message;
    		}

    		// Skip logging if service name starts with "_cxfServiceComponent" (Mule 2.2.1) or ends with "_cxfComponent" (Mule 2.2.5) and endpoint contains "?wsdl" or "?xsd", then it's just tons of WSDL and XSD lookup calls, nothing to log...
            MuleEventContext event       = RequestContext.getEventContext();
            Service          service     = event.getService();
            String           serviceName = (service == null)? null : service.getName();
    		if (serviceName != null && (serviceName.startsWith("_cxfServiceComponent") || serviceName.endsWith("_cxfComponent"))) {
        	    EndpointURI      endpointURI = event.getEndpointURI();
    			if (endpointURI != null) {
    				String ep = endpointURI.toString();
    				if ((ep.contains("?wsdl")) || (ep.contains("?xsd"))) {
    	    			log.debug("Skip logging message, CXF ...?WSDL/XSD call detected!");
    					return message;
    				}
    			}
    		}

    		switch (logLevel) {
			case INFO:
			case DEBUG:
			case TRACE:
				eventLogger.logInfoEvent(message, logType, null);
				break;

			case FATAL:
			case ERROR:
			case WARNING:
				eventLogger.logErrorEvent(new RuntimeException(logType), message);
				break;
			}

			return message;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }
}