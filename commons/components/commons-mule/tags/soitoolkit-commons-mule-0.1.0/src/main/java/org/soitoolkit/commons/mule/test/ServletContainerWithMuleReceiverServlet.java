package org.soitoolkit.commons.mule.test;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mule.transport.servlet.MuleReceiverServlet;

/**
 * Minimal servlet container (based on jetty) that enables 
 * test of integrations and services that use the mule servlet transport 
 * without being forced to package the integration code in a war file and deploy it to a servlet container.
 * 
 * @author Magnus Larsson
 *
 */
public class ServletContainerWithMuleReceiverServlet {

	// Configuration parameters set by the constructor
    protected int httpPort = -1;
    protected String contextPath = null;
    protected String muleReceiverServletUri = null;

    // The underlying jetty servlet container
	protected Server httpServer = null;
    
	/**
	 * Constructor that takes configuration parameters
	 * 
	 * @param httpPort
	 * @param contextPath
	 * @param muleReceiverServletUri
	 */
	public ServletContainerWithMuleReceiverServlet(int httpPort, String contextPath, String muleReceiverServletUri) {
	    this.httpPort = httpPort ;
	    this.contextPath = contextPath;
	    this.muleReceiverServletUri = muleReceiverServletUri;
	}
	
	/**
	 * Start up the servlet container with the Mule Receiver Servlet
	 * 
	 * @throws Exception
	 */
	public void start() throws Exception {
		httpServer = new Server(httpPort);
        String path = contextPath;
        if (path.equals("")) path = "/";
        
        Context c = new Context(httpServer, path, Context.SESSIONS);
        c.addServlet(new ServletHolder(new MuleReceiverServlet()), muleReceiverServletUri + "/*");

        httpServer.start();
	}

	/**
	 * Shutdown the servlet container
	 * 
	 * @throws Exception
	 */
	public void shutdown() throws Exception {
		if (httpServer != null && httpServer.isStarted()) {
            httpServer.stop();
        }
	}
}
