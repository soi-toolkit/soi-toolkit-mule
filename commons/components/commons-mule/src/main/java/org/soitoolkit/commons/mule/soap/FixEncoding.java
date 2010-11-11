package org.soitoolkit.commons.mule.soap;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.interceptor.Fault;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamWriter;

import com.ctc.wstx.sw.SimpleNsStreamWriter;
import java.io.Writer;
import java.io.IOException;

/**
 * Based on a solution published in http://www.mulesoft.org/jira/browse/MULE-4011
 * Is according to the jira fixed in v3.0.1 and v3.1.0 so until then we will need this one!
 * 
 * Used by inbound-endpoints like: 
 * 
 * <pre>
 *   <cxf:inbound-endpoint address="http://localhost:65082/services/EchoUMO" serviceClass="org.mule.example.echo.Echo"> 
 *       <cxf:outInterceptors> 
 *           <spring:bean class="org.mule.example.echo.FixEncoding" /> 
 *       </cxf:outInterceptors> 
 *   </cxf:inbound-endpoint> 
 * </pre>
 * @author magnuslarsson
 *
 */
public class FixEncoding extends AbstractPhaseInterceptor<Message> {
    protected final Log logger = LogFactory.getLog(getClass());

    public FixEncoding() {
        super(Phase.PRE_PROTOCOL);
    }

    public void handleMessage(Message message) {
        String encoding = getEncoding(message);
        logger.debug("======= encoding: "+encoding);
        
        try {
            XMLStreamWriter origWriter = message.getContent(XMLStreamWriter.class);
            SimpleNsStreamWriter simpleNSWriter = (SimpleNsStreamWriter)origWriter;
            Writer writer = (Writer)simpleNSWriter.getProperty("com.ctc.wstx.outputUnderlyingWriter");
            writer.write("<?xml version='1.0' encoding='"+ encoding +"'?>\n");
        } catch (IOException e) {
            throw new Fault(e);
        }
    }

    private String getEncoding(Message message) {
        Exchange ex = message.getExchange();
        String encoding = (String)message.get(Message.ENCODING);
        if (encoding == null && ex.getInMessage() != null) {
            encoding = (String) ex.getInMessage().get(Message.ENCODING);
            message.put(Message.ENCODING, encoding);
        }
        
        if (encoding == null) {
            encoding = "UTF-8";
            message.put(Message.ENCODING, encoding);
        }
        return encoding;
    }

    public void handleFault(Message messageParam) {
    }
}