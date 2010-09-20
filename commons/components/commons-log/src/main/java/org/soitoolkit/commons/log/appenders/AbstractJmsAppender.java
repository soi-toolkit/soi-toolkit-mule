package org.soitoolkit.commons.log.appenders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.soitoolkit.commons.logentry.schema.v1.LogEntryType;
import org.soitoolkit.commons.logentry.schema.v1.LogEvent;
import org.soitoolkit.commons.logentry.schema.v1.LogLevelType;
import org.soitoolkit.commons.logentry.schema.v1.LogMessageExceptionType;
import org.soitoolkit.commons.logentry.schema.v1.LogMessageType;
import org.soitoolkit.commons.logentry.schema.v1.LogMetadataInfoType;
import org.soitoolkit.commons.logentry.schema.v1.LogRuntimeInfoType;


/**
 * A base class for JMS appender that doesn't require JNDI and sends log events to queues 
 * instead of to a topic (as the default JMS Appender does)
 * 
 * @author Magnus Larsson
 */
public abstract class AbstractJmsAppender extends AppenderSkeleton {
	
	public static final String MSG_ID = "soi-toolkit.log";
	public static final String DEFAULT_LOG_INFO_QUEUE = "SOITOOLKIT.LOG.INFO";
	public static final String DEFAULT_LOG_ERROR_QUEUE = "SOITOOLKIT.LOG.ERROR";

	private static InetAddress HOST = null;
	private static String HOST_NAME = "UNKNOWN";
	private static String HOST_IP = "UNKNOWN";
	private static String PROCESS_ID = "UNKNOWN";

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

	public AbstractJmsAppender() {
		super();
 	}

	// Configurable parameters with default values;
	private String logInfoQueue  = DEFAULT_LOG_INFO_QUEUE;
	private String logErrorQueue = DEFAULT_LOG_ERROR_QUEUE;

	private QueueConnection queueConnection     = null;
	private QueueSession    queueSession        = null;
	private QueueSender     logInfoQueueSender  = null;
	private QueueSender     logErrorQueueSender = null;

	/*
	 * Methods to be implemented by a provider specific implementation
	 */
	protected abstract QueueConnection createQueueConnection() throws Exception;
	protected abstract Queue createQueue(String queueName) throws Exception;

	/*
	 * Getters and setters for configurable parameters
	 */
	public String getLogInfoQueue() {
		return logInfoQueue;
	}
	public void setLogInfoQueue(String logInfoQueue) {
		this.logInfoQueue = logInfoQueue;
	}
	public String getLogErrorQueue() {
		return logErrorQueue;
	}
	public void setLogErrorQueue(String logErrorQueue) {
		this.logErrorQueue = logErrorQueue;
	}
	
	public void initJms() {
		try {

			// Bail out if JMS objects already are initialized
			if (queueConnection != null) return;
			
			queueConnection = createQueueConnection();

			LogLog.debug("Creating non-transactional QueueSession in AUTO_ACKNOWLEDGE mode.");
			queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

			LogLog.debug("Creating QueueSender for queue [" + getLogInfoQueue() + "].");
			logInfoQueueSender = queueSession.createSender(createQueue(getLogInfoQueue()));

			LogLog.debug("Creating QueueSender for queue [" + getLogErrorQueue() + "].");
			logErrorQueueSender = queueSession.createSender(createQueue(getLogErrorQueue()));

			LogLog.debug("Starting QueueConnection.");
			queueConnection.start();

		} catch (Exception e) {
			errorHandler.error(
			"Error while activating options for appender named ["
							+ name + "].", e, ErrorCode.GENERIC_FAILURE);
		}
	}
	
	/**
	 * Options are activated and become effective only after calling this method.
	 */
	public void activateOptions() {

		// Defer initialization of JMS until first append-call, that ensures that embedded (in-emmory) JMS providers are fully initialized before we connect.
	}

	protected boolean checkEntryConditions() {
		String fail = null;

		if (queueConnection == null) {
			fail = "No queueConnection";
		} else if (queueSession == null) {
			fail = "No queueSession";
		} else if (logInfoQueueSender == null) {
			fail = "No logInfoQueueSender";
		} else if (logErrorQueueSender == null) {
			fail = "No logErrorQueueSender";
		}

		if (fail != null) {
			errorHandler.error(fail + " for JMSAppender named [" + name + "].");
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Close this JMSAppender. Closing releases all resources used by the
	 * appender. A closed appender cannot be re-opened.
	 */
	public synchronized void close() {
		// The synchronized modifier avoids concurrent append and close operations

		if (this.closed)
			return;

		LogLog.debug("Closing appender [" + name + "].");
		this.closed = true;

		try {
			if (logInfoQueueSender  != null) logInfoQueueSender.close();
			if (logErrorQueueSender != null) logErrorQueueSender.close();
			if (queueSession        != null) queueSession.close();
			if (queueConnection     != null) queueConnection.close();
		} catch (Exception e) {
			LogLog.error("Error while closing Jms*Appender [" + name + "].", e);
		}
		// Help garbage collection
		logInfoQueueSender = null;
		logErrorQueueSender = null;
		queueSession = null;
		queueConnection = null;
	}

	/**
	 * This method called by {@link AppenderSkeleton#doAppend} method to do most
	 * of the real appending work.
	 */
	public void append(LoggingEvent event) {

		try {
			initJms();
			
			if (!checkEntryConditions()) return;

			LogEvent logEntry  = createLogEntry(event);						
			String       xmlString = logEntryToXml(logEntry);			
			TextMessage  message   = queueSession.createTextMessage();
			message.setText(xmlString);
			// Send TRACE, DEBUG and INFO messages to the info-log and
			// send WARNING, ERROR and FATAL messages to the error-log
			switch (logEntry.getLogEntry().getMessageInfo().getLevel()) {

			case TRACE:
			case DEBUG:
			case INFO:
				logInfoQueueSender.send(message);
				break;

			case WARNING:
			case ERROR:
			case FATAL:
				logErrorQueueSender.send(message);
				break;

			}

		} catch (Exception e) {
			errorHandler.error("Could not publish message in Jms*Appender ["
					+ name + "].", e, ErrorCode.GENERIC_FAILURE);
		}
	}

	private LogEvent createLogEntry(LoggingEvent event) {

		// Setup basic runtime information for the log entry
		LogRuntimeInfoType lri = new LogRuntimeInfoType();
		lri.setTimestamp(convertDateTimeToXMLGregorianCalendar(null));
		lri.setHostName(HOST_NAME);
		lri.setHostIp(HOST_IP);
		lri.setProcessId(PROCESS_ID);
		lri.setComponentId("");
		lri.setThreadId(event.getThreadName());
		
		
		// Setup basic metadata information for the log entry
		LogMetadataInfoType lmi = new LogMetadataInfoType();
		lmi.setLoggerName(event.getLoggerName());

		
		// Setup basic information of the log message for the log entry
		LogMessageType lm = new LogMessageType();
		lm.setLevel(getLogLevel(event));
		lm.setMessage(event.getMessage().toString());
		
		
		// Setup exception information if present
		ThrowableInformation ti = event.getThrowableInformation();
		if (ti != null) {
			LogMessageExceptionType lme = new LogMessageExceptionType();
			
			lme.setExceptionClass(ti.getThrowable().getClass().getName());
			List<String> list = Arrays.asList(ti.getThrowableStrRep());
			lme.getStackTrace().addAll(list);
			
			lm.setException(lme);
		}


		// Handle GIM-INFO specific information if provided
		String message = event.getMessage().toString();
		String payload = "";
		if (message.startsWith(MSG_ID)) {

			// Ok, we found GIM-INFO specific information, parse it and create corresponding information in the log entry object
			// Parse the line: 
			//
			//	gim.infobus.log
			//	ContractId=volvokort-test-endpoint
			//	Action=sftp.get (/~/sftp/volvokort/in/sending-to-teststub/from_vfkonto_20100201164451561.dat, size = 19)
			//	ResourceId=volvokort-test-receiver
			//	Host=dse31413.volvofinans.net (10.128.2.149)
			//	Server=ml-server
			//	Endpoint=sftp://muletest1@localhost/~/sftp/volvokort/in
			//	MessageId=bc244699-0f48-11df-a53e-17d6f6bc4c9b
			//	BusinessCorrelationId=null
			//	BusinessContextId=

/*
gim.infobus.log
** log.event.start ***********************************************************
ContractId=SAMPLE-CONTRACT_ID
Action=req-in
ResourceId=getPersoninformation
Host=dse31413.volvofinans.net (10.128.2.95)
Server=00d8cd00-575c-11df-8b60-d3864b0f36b8
Endpoint=servlet://getpersoninformation/v1
MessageId=0aba8d6c-575c-11df-8b60-d3864b0f36b8
BusinessCorrelationId=SAMPLE-BUS-CORR-ID
Payload=...
BusinessContextId=
-SAMPLE-BC-NAME-3 = SAMPLE-BC-VALUE-3
-SAMPLE-BC-NAME-2 = SAMPLE-BC-VALUE-2
-SAMPLE-BC-NAME-1 = SAMPLE-BC-VALUE-1
** log.event.end *************************************************************
*/
			
			// FIXME: Get rid of this reverse engineering of a log message!!!
			// TODO: Align log-events with the structure of the LogEvent-xsd!
			BufferedReader br = null;
			try {
				br = new BufferedReader(new StringReader(message));
				br.readLine(); 						                    // Consume "gim.infobus.log"...
				br.readLine(); 						                    // Consume "** log.event.start ******..."
				lmi.setIntegrationScenarioId(getNextValue(br));         // Consume IntegrationScenarioId...
				lmi.setContractId(getNextValue(br));                    // Consume ContractId...
				lm.setMessage(getNextValue(br));                        // Consume Action...
				lmi.setServiceImplementation(getNextValue(br));         // Consume ResourceId...
				br.readLine();                                          // Consume Host...
				lri.setComponentId(getNextValue(br));					// Consume Server...
				lmi.setEndpoint(getNextValue(br));                      // Consume Endpoint...
				lri.setMessageId(getNextValue(br));                     // Consume MessageId...
				lri.setBusinessCorrelationId(getNextValue(br));         // Consume BusinessCorrelationId...
				payload = getPayload(br);                               // Consume Payload...			
																		// TODO: Current impl of getPayload() consumes the line that starts with "BusinessContextId="
																		// Consume BusinessContextId...			
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				try {if (br != null) br.close();} catch (IOException e) {}
			}
		}

		
		// Create the log entry object
		LogEntryType logEntry = new LogEntryType();
		logEntry.setMetadataInfo(lmi);
		logEntry.setRuntimeInfo(lri);
		logEntry.setMessageInfo(lm);
		logEntry.setPayload(payload);

		// Create the final log event object
		LogEvent logEvent = new LogEvent();
		logEvent.setLogEntry(logEntry);
		return logEvent;
	}
	private String getPayload(BufferedReader br) throws IOException {
		String payload;
		payload = getNextValue(br);
		boolean done = false;
		while (!done) {
			String line = br.readLine();
			if (line.startsWith("BusinessContextId=")) {
				done = true;
			} else {
				payload += "\n" + line;
			}
		}
		return payload;
	}
	private LogLevelType getLogLevel(LoggingEvent event) {
		String logLevel = event.getLevel().toString();
		
		if (logLevel.equals("WARN")) {
			logLevel = "WARNING";
		}
		return LogLevelType.fromValue(logLevel);
	}
	private String getNextValue(BufferedReader br) throws IOException {
		String line = br.readLine();
		String value = line.substring(line.indexOf('=') + 1);
		return value;
	}

	private String logEntryToXml(LogEvent logEvent) {
    	return jabxObjectToXml(logEvent);
	}

	/**
	 * The Jms*Appender sends serialized events and consequently does not require a layout.
	 */
	public boolean requiresLayout() {
		return false;
	}

	/*
	 * TODO: Potentially reusable utility methods
	 */ 
	static private final DatatypeFactory DF = initDatatypeFactory();
	public static XMLGregorianCalendar convertDateTimeToXMLGregorianCalendar(Date date) {
		GregorianCalendar gc = new GregorianCalendar();
		if (date != null) {
			gc.setTime(date);
		}
		return DF.newXMLGregorianCalendar(gc);
	}
	private static DatatypeFactory initDatatypeFactory() {
		try {
			return DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException(e);
		}
	}	
	
	
	
    private static JAXBContext jaxbContext = null;
    private static final Class<LogEvent> JAXB_CONTEXT_PATH = LogEvent.class;

	public String jabxObjectToXml(Object jaxbObject) {
        try {

        	Marshaller marshaller = getJaxbContext().createMarshaller();
            StringWriter writer = new StringWriter();

           	marshaller.marshal(jaxbObject, writer);

            return writer.toString();
	    } catch (JAXBException e) {
	        throw new RuntimeException(e);
		}
	}

	public LogEvent xmlToJabxObject(String xml) {
        try {

        	Unmarshaller unmarshaller = getJaxbContext().createUnmarshaller();
           	return (LogEvent)unmarshaller.unmarshal(new StringReader(xml));
	    } catch (JAXBException e) {
	        throw new RuntimeException(e);
		}
	}

    private JAXBContext getJaxbContext() {
        try {
            if (jaxbContext == null) {
            	jaxbContext = JAXBContext.newInstance(JAXB_CONTEXT_PATH);
            }
            return jaxbContext;
        } catch (JAXBException e) {
            throw new RuntimeException(e);
		}
    }
}