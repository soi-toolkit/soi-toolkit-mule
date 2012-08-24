package org.soitoolkit.commons.module.logger.api;

import org.soitoolkit.commons.logentry.schema.v1.LogEvent;

public interface LogEventSender {

	public void sendLogEvent(LogEvent logEvent);
}
