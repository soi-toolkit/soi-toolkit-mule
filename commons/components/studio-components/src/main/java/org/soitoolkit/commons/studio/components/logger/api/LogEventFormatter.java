package org.soitoolkit.commons.studio.components.logger.api;

import org.soitoolkit.commons.logentry.schema.v1.LogEvent;

public interface LogEventFormatter {

	String formatLogMsg(LogEvent logEvent);

}
