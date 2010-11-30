package org.soitoolkit.commons.mule.test;

import java.util.Map;

import org.mule.api.MuleException;
import org.mule.module.client.MuleClient;

public class DispatcherMuleClientImpl implements Dispatcher {

	private String inboundEndpointAddress;
	private Object payload;
	private Map<String, String> headers;

	public DispatcherMuleClientImpl (String inboundEndpointAddress, Object payload, Map<String, String> headers) {
		this.inboundEndpointAddress = inboundEndpointAddress;
		this.payload = payload;
		this.headers = headers;
	}
	
	public void doDispatch() {
		MuleClient muleClient = null;

		try {
 			// First create a muleClient instance
			muleClient = new MuleClient();

			// Perform the actual dispatch
			muleClient.dispatch(inboundEndpointAddress, payload, headers);

		} catch (MuleException e) {
			throw new RuntimeException(e);
			
		} finally {
			// Dispose muleClient 
			muleClient.dispose();
		}
	}
}
