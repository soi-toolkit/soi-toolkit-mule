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
package org.soitoolkit.commons.mule.test;

import org.mule.api.MuleContext;
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
public class MuleServerWithServletContainer extends StandaloneMuleServer {

	private static Logger log = LoggerFactory.getLogger(MuleServerWithServletContainer.class);
	
	private static final int WAITTIME_MULE_SERVLET_TRANSPORT = 500;

	// Configuration parameters set by the constructor
    protected int httpPort = -1;
    protected String contextPath = null;
    protected String muleReceiverServletUri = null;
    
	ServletContainerWithMuleReceiverServlet servletContainer = null;

	/**
	 * Constructor that takes configuration parameters
	 * 
	 * @param muleServerId
	 * @param muleConfig
	 * @param httpPort
	 * @param contextPath
	 * @param muleReceiverServletUri
	 */
    public MuleServerWithServletContainer(String muleServerId, String muleConfig, int httpPort, String contextPath, String muleReceiverServletUri) {
    	super(muleServerId, muleConfig, true);
    	this.httpPort = httpPort ;
	    this.contextPath = contextPath;
	    this.muleReceiverServletUri = muleReceiverServletUri;
	}

	/**
	 * Start up mule and the servlet container with the Mule Receiver Servlet
	 * 
	 * @throws InterruptedException
	 * @throws Exception
	 */
    @Override
	public void start() throws InterruptedException, Exception {

    	// First startup Mule...
    	super.start();
		
    	boolean muleStarted = false;
    	
        // Wait for a while so that mule and its servlet transport gets time to get started
    	while (!muleStarted) {
    		MuleContext mc = muleServer.getMuleContext();
    		log.debug("MuleContext = {}", mc);
    		if (mc != null) {
    			if (log.isDebugEnabled()) { 
	    			log.debug("Mule isInitialising = " + mc.isInitialising());
	    			log.debug("Mule isInitialised  = " + mc.isInitialised());
	    			log.debug("Mule isStarting     = " + mc.isStarting());
	    			log.debug("Mule isStarted      = " + mc.isStarted());
    			}
    	    	muleStarted = mc.isStarted();
    		}
	    	Thread.sleep(getWaittimeMuleServletTransport());
    	}

        log.info("Startup Servlet container with Mule Receiver Servlet...");

        // Startup the servlet container and the mule receiver servlet once mule servlet transport is ready
		servletContainer = new ServletContainerWithMuleReceiverServlet(httpPort, contextPath, muleReceiverServletUri, muleServer.getMuleContext(), muleServerId);
        servletContainer.start();
	}

	/**
	 * Shutdown mule and the servlet container
	 * 
	 * @throws Exception
	 */
    @Override
	public void shutdown() throws Exception {

    	log.info("Shutdown Servlet container...");

        // Shutdown servlet container and mule server
        servletContainer.shutdown();	
        
        // Also shutdown Mule
        super.shutdown();
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