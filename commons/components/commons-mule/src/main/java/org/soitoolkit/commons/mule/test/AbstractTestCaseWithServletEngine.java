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

/**
 * Adds the capability to run Mule unit tests with a servlet engine, i.e. testing services depending on the servlet-transport.
 * 
 * @author Magnus Larsson
 *
 */
public abstract class AbstractTestCaseWithServletEngine extends AbstractTestCase {

    protected int httpPort = -1;
    protected String contextPath = null;
    protected String muleReceiverServletUri = null;
    protected String muleServerId = null;

	private ServletContainerWithMuleReceiverServlet servletContainer = null;

	
	public AbstractTestCaseWithServletEngine(String muleServerId, int httpPort, String contextPath, String muleReceiverServletUri) {
		super();

        // Activate the spring bean definition profile "soitoolkit-integrationtests"
        System.getProperties().put("spring.profiles.active", "soitoolkit-integrationtests");

	    this.httpPort = httpPort ;
	    this.contextPath = contextPath;
	    this.muleReceiverServletUri = muleReceiverServletUri;
	    this.muleServerId = muleServerId;

	    // Before launching Mule ESB set its server id
		System.setProperty("mule.serverId", muleServerId);
    }

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
		servletContainer = new ServletContainerWithMuleReceiverServlet(httpPort, contextPath, muleReceiverServletUri, muleContext, muleServerId);
        servletContainer.start();
    }

    @Override
    protected void doTearDown() throws Exception {
    	servletContainer.shutdown();
        super.doTearDown();
    }

}
