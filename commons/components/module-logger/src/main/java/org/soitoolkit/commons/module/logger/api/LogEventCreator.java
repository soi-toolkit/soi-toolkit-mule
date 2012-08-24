package org.soitoolkit.commons.module.logger.api;

import java.util.Map;

import org.mule.api.MuleEvent;
import org.soitoolkit.commons.logentry.schema.v1.LogEvent;
import org.soitoolkit.commons.logentry.schema.v1.LogLevelType;

public interface LogEventCreator {
	
	public LogEvent createLogEvent(
			MuleEvent muleEvent, 
			LogLevelType logLevel,
			String logMessage,
			String loggerName,
			String integrationScenario, 
			String contractId, 
			String correlationId,
			Map<String, String> extraInfo,
			Throwable exception,
			Object payload);
}
