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
package org.soitoolkit.commons.mule.sftp;

import org.mule.api.MuleMessage;
import org.mule.api.context.notification.ServerNotification;
import org.mule.transport.sftp.notification.SftpTransportNotification;
import org.mule.transport.sftp.notification.SftpTransportNotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.log.EventLogger;

/**
 * SFTP Transport notification listener that uses the event-logger for logging
 * 
 * @author Magnus Larsson
 */
public class SftpTransportNotificationListenerImpl implements SftpTransportNotificationListener {

	private static final Logger logger = LoggerFactory.getLogger(SftpTransportNotificationListenerImpl.class);

	private static final EventLogger eventLogger = new EventLogger();

	public SftpTransportNotificationListenerImpl() {
		logger.debug("SftpTransportNotificationListenerImpl Created");
	}

	public void onNotification(ServerNotification notification) {

		// Return at once if info-logging is disabled!
		if (!logger.isInfoEnabled()) return;
		
		SftpTransportNotification sftpNotification = null;
		if (notification instanceof SftpTransportNotification) {
			sftpNotification = (SftpTransportNotification)notification;
		} else {
			logger.debug("Received an unknown type of notification: {}", notification.getClass().getName());
			return;
		}

		MuleMessage message     = (MuleMessage)notification.getSource();
		String      action      = notification.getActionName();
		String      info        = sftpNotification.getInfo();
		long        size        = sftpNotification.getSize();

		// Concatenate action, info and size and ensure that no NPRE occurs :-)
		if (action == null) action = "";
		if (info == null) info = "";

		// Add on info to action if present
		if (info.length() != 0) {
			// If size is present then also add it to the info
			if (size > 0) info += ", size = " + size;
			action += " (" + info + ")";
		}

		eventLogger.logInfoEvent(message, action, null);
	}

}