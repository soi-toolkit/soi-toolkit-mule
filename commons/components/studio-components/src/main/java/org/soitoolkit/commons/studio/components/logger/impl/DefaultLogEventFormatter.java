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

import java.util.List;

import javax.inject.Named;

import org.slf4j.helpers.MessageFormatter;
import org.soitoolkit.commons.logentry.schema.v1.LogEntryType.ExtraInfo;
import org.soitoolkit.commons.logentry.schema.v1.LogEvent;
import org.soitoolkit.commons.logentry.schema.v1.LogMessageExceptionType;
import org.soitoolkit.commons.logentry.schema.v1.LogMessageType;
import org.soitoolkit.commons.logentry.schema.v1.LogMetadataInfoType;
import org.soitoolkit.commons.logentry.schema.v1.LogRuntimeInfoType;
import org.soitoolkit.commons.logentry.schema.v1.LogRuntimeInfoType.BusinessContextId;
import org.soitoolkit.commons.studio.components.logger.api.LogEventFormatter;
import org.springframework.context.annotation.Primary;

@Named
@Primary
public class DefaultLogEventFormatter implements LogEventFormatter {

	private static final String CAUSE_EXCEPTION_HEADER = "CauseException";
	private static final String MSG_ID = "soi-toolkit.log";
	private static final String LOG_STRING = MSG_ID + 
		"\n** {}.start ***********************************************************" +
		"\nIntegrationScenarioId={}\nContractId={}\nLogMessage={}\nServiceImpl={}\nHost={} ({})\nComponentId={}\nEndpoint={}\nMessageId={}\nBusinessCorrelationId={}\nBusinessContextId={}\nExtraInfo={}\nPayload={}" + 
		"{}" + // Placeholder for stack trace info if an error is logged
		"\n** {}.end *************************************************************";

	/**
	 * Formats a LogEvent for logging in the log-system (log4j)
	 */
	@Override
	public String formatLogMsg(LogEvent logEvent) {
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
		String logEventName            = "logEvent-" + messageInfo.getLevel().toString().toLowerCase();

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
		return MessageFormatter.arrayFormat(LOG_STRING, new String[] {logEventName, integrationScenarioId, contractId, logMessage, serviceImplementation, runtimeInfo.getHostName(), runtimeInfo.getHostIp(), componentId, endpoint, messageId, businessCorrelationId, businessContextIdString, extraInfoString, payload, stackTrace.toString(), logEventName}).getMessage();
	}

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

}
