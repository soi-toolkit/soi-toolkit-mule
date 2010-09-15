package org.soitoolkit.commons.mule.test;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Base class for test consumer of services based on the servlet-transport
 * 
 * @author Magnus Larsson
 *
 */
abstract public class AbstractTestConsumer {

    /**
     * Address based on usage of the servlet-transport
     * 
     * @param serviceUriPropertyName
     * @return
     */
    public static String getAddress(String uri, int httpPort, String ctxPath, String servletUri) {
	    String url = "http://localhost" + ":" + httpPort + ctxPath + servletUri + "/" + uri;
	    System.err.println("URL: " + url);
	    return url;    	
    }

    protected URL createEndpointUrlFromServiceAddress(String serviceAddress) {
        try {
            return new URL(serviceAddress + "?wsdl");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL Exception: " + e.getMessage());
        }
    }
}