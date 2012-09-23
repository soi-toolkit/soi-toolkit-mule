package org.soitoolkit.commons.studio.components.logger.impl;

import javax.inject.Named;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.mule.RequestContext;
import org.mule.api.MuleContext;
import org.mule.api.context.MuleContextAware;
import org.mule.transport.jms.JmsConnector;
import org.soitoolkit.commons.logentry.schema.v1.LogEvent;
import org.soitoolkit.commons.studio.components.fromcommonsmule.jaxb.JaxbUtil;
import org.soitoolkit.commons.studio.components.fromcommonsmule.util.MuleUtil;
import org.soitoolkit.commons.studio.components.logger.api.LogEventSender;
import org.springframework.context.annotation.Primary;

@Named
@Primary
public class DefaultLogEventSender implements LogEventSender, MuleContextAware {

	private static final JaxbUtil JAXB_UTIL = new JaxbUtil(LogEvent.class);

    /*
     * Dependencies
     */
	private MuleContext muleContext;

	@Override
	public void setMuleContext(MuleContext muleContext) {
		this.muleContext = muleContext;
	}	

	@SuppressWarnings("deprecation")
	protected MuleContext getMuleContext() {
    	if (muleContext == null) {
    		// Fallback if classpath-scanning is missing, eg: <context:component-scan base-package="org.soitoolkit.commons.module.logger" />
    		muleContext = RequestContext.getEvent().getMuleContext();
    	}
    	return muleContext;
    }
	
	@Override
	public void sendLogEvent(LogEvent logEvent) {

		String xmlString = JAXB_UTIL.marshal(logEvent);
		
		switch (logEvent.getLogEntry().getMessageInfo().getLevel()) {

    	case TRACE:
    	case DEBUG:
    	case INFO:
    		dispatchEvent("SOITOOLKIT.LOG.INFO", xmlString);
			break;

    	case WARNING:
    	case ERROR:
    	case FATAL:
    		dispatchEvent("SOITOOLKIT.LOG.ERROR", xmlString);
			break;

		default:
			break;
		}
	}

	protected void dispatchEvent(String queue, String msg) {
		try {

			Session s = null;
			try {
				s = getSession();
				sendOneTextMessage(s, queue, msg);
			} finally {
	    		if (s != null) s.close(); 
			}
			
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
	}

	protected Session getSession() throws JMSException {
		JmsConnector jmsConn = (JmsConnector)MuleUtil.getSpringBean(getMuleContext(), "soitoolkit-jms-connector");
		Connection c = jmsConn.getConnection();
		Session s = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
		return s;
	}

	protected void sendOneTextMessage(Session session, String queueName, String message) {

        MessageProducer publisher = null;

	    try {
	    	publisher = session.createProducer(session.createQueue(queueName));
	        TextMessage textMessage = session.createTextMessage(message);  
	        publisher.send(textMessage);   
	
	    } catch (JMSException e) {
	        throw new RuntimeException(e);
	    } finally {
	    	try {
	    		if (publisher != null) publisher.close(); 
	    	} catch (JMSException e) {}
	    }
	}
}
