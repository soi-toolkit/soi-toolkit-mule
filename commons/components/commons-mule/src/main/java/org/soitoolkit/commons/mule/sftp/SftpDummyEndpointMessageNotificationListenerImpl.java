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

import org.mule.api.context.notification.EndpointMessageNotificationListener;
import org.mule.api.context.notification.ServerNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Current implementation of the sftp-transport (2.2.1-RC3) requires an endpoint-listener to be able to trigger sftp-events...
 * 
 * @author Magnus Larsson
 *
 */
public class SftpDummyEndpointMessageNotificationListenerImpl implements EndpointMessageNotificationListener {

	private static final Logger logger = LoggerFactory.getLogger(SftpTransportNotificationListenerImpl.class);
	
	public void onNotification(ServerNotification notification) {
		logger.debug("EndpointMessageEvent detected");
	}

}