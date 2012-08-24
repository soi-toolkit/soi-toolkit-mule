package org.soitoolkit.commons.module.logger.api;

import java.util.Map;

import org.mule.api.MuleEvent;
import org.soitoolkit.commons.logentry.schema.v1.LogLevelType;

public interface EventLogger {

	public void logEvent(
		MuleEvent muleEvent, 
		String logMessage, 
		LogLevelType logLevel, 
		String integrationScenario, 
		String contractId, 
		String correlationId,
		Map<String, String> extraInfo);
}
