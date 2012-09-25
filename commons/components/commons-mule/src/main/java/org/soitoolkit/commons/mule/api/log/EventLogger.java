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
package org.soitoolkit.commons.mule.api.log;

import org.soitoolkit.commons.logentry.schema.v1.LogLevelType;

public interface EventLogger {

	public void logInfoEvent(LogLevelType logLevel, EventLogMessage elm);

	public void logInfoEvent(EventLogMessage eventLogMessage);
	
	public void logErrorEvent(LogLevelType logLevel, Throwable error,
			EventLogMessage elm);

	public void logErrorEvent(Throwable error, EventLogMessage eventLogMessage);

	public void logErrorEvent(Throwable error, Object payload,
			EventLogMessage eventLogMessage);
}
