package org.soitoolkit.commons.studio.components.logger.impl;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.mule.api.ExceptionPayload;
import org.mule.api.MuleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.logentry.schema.v1.LogEvent;
import org.soitoolkit.commons.logentry.schema.v1.LogLevelType;
import org.soitoolkit.commons.studio.components.logger.api.EventLogger;
import org.soitoolkit.commons.studio.components.logger.api.LogEventCreator;
import org.soitoolkit.commons.studio.components.logger.api.LogEventFormatter;
import org.soitoolkit.commons.studio.components.logger.api.LogEventSender;
import org.springframework.context.annotation.Primary;

@Named
@Primary
public class DefaultEventLogger implements EventLogger {

	/*
     * Dependencies
     */
	private LogEventCreator   logEventCreator;
	private LogEventFormatter logEventFormatter;
	private LogEventSender    logEventSender;

	@Inject
    public void setLogEventCreator(LogEventCreator logEventCreator) {
    	this.logEventCreator = logEventCreator;
    }
	
	@Inject
    public void setLogEventFormatter(LogEventFormatter logEventFormatter) {
    	this.logEventFormatter = logEventFormatter;
    }
	
	@Inject
    public void setLogEventSender(LogEventSender logEventSender) {
    	this.logEventSender = logEventSender;
    }

	protected LogEventCreator getLogEventCreator() {
    	if (logEventCreator == null) {
    		// Fallback if classpath-scanning is missing, eg: <context:component-scan base-package="org.soitoolkit.commons.module.logger" />
    		logEventCreator = new DefaultLogEventCreator();
    	}
    	return logEventCreator;
    }

	protected LogEventFormatter getLogEventFormatter() {
    	if (logEventFormatter == null) {
    		// Fallback if classpath-scanning is missing, eg: <context:component-scan base-package="org.soitoolkit.commons.module.logger" />
    		logEventFormatter = new DefaultLogEventFormatter();
    	}
    	return logEventFormatter;
    }

	protected LogEventSender getLogEventSender() {
    	if (logEventSender == null) {
    		// Fallback if classpath-scanning is missing, eg: <context:component-scan base-package="org.soitoolkit.commons.module.logger" />
    		logEventSender = new DefaultLogEventSender();
    	}
    	return logEventSender;
    }

	private static final Logger messageLogger = LoggerFactory.getLogger("org.soitoolkit.commons.mule.messageLogger");

	@Override
	public void logEvent(
		MuleEvent muleEvent, 
		String logMessage, 
		LogLevelType logLevel, 
		String integrationScenario, 
		String contractId, 
		String correlationId,
		Map<String, String> extraInfo) {
   
    	// Exit if not at the right log-level
    	if (!checkLogLevel(logLevel)) return;
    	
		// Only extract payload if debug is enabled!
    	Object payload = (messageLogger.isDebugEnabled()) ? muleEvent.getMessage().getPayload() : "";

    	// Extract the exception, if any.
    	ExceptionPayload exceptionPayload = muleEvent.getMessage().getExceptionPayload();
		Throwable exception = (exceptionPayload == null) ? null : exceptionPayload.getException();
    	
    	// Create LogEvent
    	LogEvent logEvent = getLogEventCreator().createLogEvent(muleEvent, logLevel, logMessage, messageLogger.getName(), integrationScenario, contractId, correlationId, extraInfo, exception, payload);

    	// Format LogMessage and log it to local loggers
    	String logMsg = getLogEventFormatter().formatLogMsg(logEvent);
    	logMessage(logLevel, logMsg, exception);

    	// Send LogEvent for background processing
		getLogEventSender().sendLogEvent(logEvent);
	}

	protected boolean checkLogLevel(LogLevelType logLevel) {

		switch (logLevel) {
	    	case TRACE:   return messageLogger.isTraceEnabled();
	    	case DEBUG:   return messageLogger.isDebugEnabled();
	    	case INFO:    return messageLogger.isInfoEnabled();
	    	case WARNING: return messageLogger.isWarnEnabled();
	    	case ERROR:   return messageLogger.isErrorEnabled();
	    	case FATAL:   return messageLogger.isErrorEnabled();
	    	default:      return false;
		}
	}

	protected void logMessage(LogLevelType logLevelEnum, String logMsg, Throwable exception) {

		switch (logLevelEnum) {

    	case TRACE:
			if (messageLogger.isTraceEnabled()) messageLogger.trace(logMsg, exception);
			break;

    	case DEBUG:
			if (messageLogger.isDebugEnabled()) messageLogger.debug(logMsg, exception);
			break;

    	case INFO:
			if (messageLogger.isInfoEnabled()) messageLogger.info(logMsg, exception);
			break;

    	case WARNING:
			if (messageLogger.isWarnEnabled()) messageLogger.warn(logMsg, exception);
			break;

    	case ERROR:
			if (messageLogger.isErrorEnabled()) messageLogger.error(logMsg, exception);
			break;

    	case FATAL:
			if (messageLogger.isErrorEnabled()) messageLogger.error(logMsg, exception);
			break;

		default:
			break;
		}
	}
}
