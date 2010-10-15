package org.soitoolkit.commons.mule.test;

import org.mule.MuleServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Minimal configuration of mule together with a separate servlet container (based on jetty) that enables
 * test of integrations and services that use the mule servlet transport 
 * without being forced to package the integration code in a war file and deploy it to a servlet container.
 * 
 * @author Magnus Larsson
 *
 */
public class MuleServerWithServletContainer {

	private static Logger log = LoggerFactory.getLogger(MuleServerWithServletContainer.class);
	
	private static final int WAITTIME_MULE_SERVLET_TRANSPORT = 5000;

	// Configuration parameters set by the constructor
	protected String muleServerId = null;
	protected String muleConfig = null;
    protected int httpPort = -1;
    protected String contextPath = null;
    protected String muleReceiverServletUri = null;
    
    // The underlying mule server and servlet container
	MuleServer muleServer = null;
	ServletContainerWithMuleReceiverServlet servletContainer = null;

	/**
	 * Constructor that takes configuration parameters
	 * 
	 * @param muleConfig
	 * @param httpPort
	 * @param contextPath
	 * @param muleReceiverServletUri
	 */
    public MuleServerWithServletContainer(String muleServerId, String muleConfig, int httpPort, String contextPath, String muleReceiverServletUri) {
    	this.muleServerId = muleServerId;
    	this.muleConfig = muleConfig;
    	this.httpPort = httpPort ;
	    this.contextPath = contextPath;
	    this.muleReceiverServletUri = muleReceiverServletUri;
	}

    /**
     * Convenience method that both starts and stops both mule and the servlet container 
     * 
     * @throws Exception
     */
	public void run() throws Exception {

		// Start me up...
        log.info("Startup mule and servlet container with mule redirect servlet");
		start();

        // Run until the return key is hit...
        log.info("Hit the RETURN - key to shutdown");
        System.in.read();

        // Bye, bye...
        log.info("Shutdown...");
        shutdown();
        log.info("Shutdown complete");
	}

	/**
	 * Start up mule and the servlet container with the Mule Receiver Servlet
	 * 
	 * @throws InterruptedException
	 * @throws Exception
	 */
	public void start() throws InterruptedException, Exception {
		
		// Before launching Mule ESB set its server id
		System.setProperty("mule.serverId", muleServerId);

		// Before launching Mule ESB alse ensure that CXF use LOG4J for logging
		System.setProperty("org.apache.cxf.Logger", "org.apache.cxf.common.logging.Log4jLogger");

		// Startup Mule ESB in the background
		muleServer = new MuleServer(muleConfig);
        muleServer.start(true, true);

        // Wait for a while so that mule and its servlet transport gets time to get started
    	Thread.sleep(getWaittimeMuleServletTransport());

    	// Startup the servlet container and the mule receiver servlet once mule servlet transport is ready
		servletContainer = new ServletContainerWithMuleReceiverServlet(httpPort, contextPath, muleReceiverServletUri);
        servletContainer.start();
	}

	/**
	 * Shutdown mule and the servlet container
	 * 
	 * @throws Exception
	 */
	public void shutdown() throws Exception {
		// Shutdown servlet container and mule server
        servletContainer.shutdown();	
        muleServer.shutdown();
	}

	/**
	 * Returns the default wait time until mue has started the servlet transport
	 * (the servlet container should not be started before the servlet transport is started in mule)
	 * 
	 * Can be overridden if the default wait time is inappropriate
     * 
     * @return
     */
    protected int getWaittimeMuleServletTransport() {
		return WAITTIME_MULE_SERVLET_TRANSPORT;
	}
}