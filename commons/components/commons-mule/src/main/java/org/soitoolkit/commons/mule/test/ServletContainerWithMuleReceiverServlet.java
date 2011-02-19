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

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mule.api.MuleContext;
import org.mule.transport.servlet.MuleReceiverServlet;
import org.mule.transport.servlet.MuleServletContextListener;

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
    protected MuleContext muleContext = null;
    protected String muleServerId = null;
    
    // The underlying jetty servlet container
	protected Server httpServer = null;
    
	/**
	 * Constructor that takes configuration parameters
	 * 
	 * @param httpPort
	 * @param contextPath
	 * @param muleReceiverServletUri
	 */
	public ServletContainerWithMuleReceiverServlet(int httpPort, String contextPath, String muleReceiverServletUri, MuleContext muleContext, String muleServerId) {
	    this.httpPort = httpPort ;
	    this.contextPath = contextPath;
	    this.muleReceiverServletUri = muleReceiverServletUri;
	    this.muleContext = muleContext;
	    this.muleServerId = muleServerId;
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
        c.addEventListener(new MuleServletContextListener(muleContext, muleServerId));

        MuleReceiverServlet servlet = new MuleReceiverServlet();
		c.addServlet(new ServletHolder(servlet), muleReceiverServletUri + "/*");

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
