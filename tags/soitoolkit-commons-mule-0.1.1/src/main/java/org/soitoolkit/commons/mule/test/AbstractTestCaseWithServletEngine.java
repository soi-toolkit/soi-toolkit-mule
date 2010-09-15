package org.soitoolkit.commons.mule.test;

/**
 * Adds the capability to run Mule unit tests with a servlet engine, i.e. testing services depending on the servlet-transport.
 * 
 * @author Magnus Larsson
 *
 */
public abstract class AbstractTestCaseWithServletEngine extends AbstractTestCase {

	private ServletContainerWithMuleReceiverServlet servletContainer = null;

	public AbstractTestCaseWithServletEngine(String muleServerId, int httpPort, String contextPath, String muleReceiverServletUri) {
		super();

		// Before launching Mule ESB set its server id
		System.setProperty("mule.serverId", muleServerId);
		
		servletContainer = new ServletContainerWithMuleReceiverServlet(httpPort, contextPath, muleReceiverServletUri);
    }

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        servletContainer.start();
    }

    @Override
    protected void doTearDown() throws Exception {
    	servletContainer.shutdown();
        super.doTearDown();
    }

}
