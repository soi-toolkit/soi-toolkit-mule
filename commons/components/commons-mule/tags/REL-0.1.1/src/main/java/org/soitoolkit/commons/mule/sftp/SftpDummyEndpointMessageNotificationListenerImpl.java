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