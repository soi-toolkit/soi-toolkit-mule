package org.soitoolkit.commons.mule.soap;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;

import org.mule.api.ExceptionPayload;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.transformer.AbstractMessageAwareTransformer;
import org.mule.transport.NullPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transformer to be used as a responseTransformer for a SOAP based service.
 * Creates a SOAP Fault structure with exception information as response if an error has occurred in the processing of the request.
 * 
 * @author Magnus Larsson
 *
 */
public class CreateSoapFaultIfExceptionTransformer extends AbstractMessageAwareTransformer {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
    static String SOAP_FAULT_V11 = 
		"<soap:Fault xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
		"  <faultcode>soap:Server</faultcode>\n" + 
		"  <faultstring>{0}</faultstring>\n" +
		"  <faultactor>{1}</faultactor>\n" +
//		"  <detail>\n" +
//		"    {2}\n" +
//		"  </detail>\n" + 
		"</soap:Fault>";
    
    @Override
    public Object transform(MuleMessage message, String outputEncoding) throws TransformerException {

    	System.err.println("CreateSoapFaultIfExceptionTransformer.transform() called");
    	Object payload = message.getPayload();
    		
		// Take care of any error message and send it back as a SOAP Fault!
    	// TODO: Do we need to actually check both NullPayload and presence of an ExceptionPayload???
		if (payload instanceof NullPayload) {

			System.err.println("NullPayload detected, let's look for an ExceptionPayload as well...");
			logger.debug("NullPayload detected, let's look for an ExceptionPayload as well...");

			// We got a null-payload, let's see if there is an exception-payload instead...
			ExceptionPayload ep = message.getExceptionPayload();
	        if (ep != null) {

				System.err.println("### ExceptionPayload detected as well, let's create a SOAP-FAULT!");
				logger.debug("ExceptionPayload detected as well, let's create a SOAP-FAULT!");
	        	
	        	String soapFault = createSoapFaultFromExceptionPayload(ep);
	        	
				System.err.println("### Set ExceptionPayload to null and outbound http.status=500");
				logger.debug("Set ExceptionPayload to null and outbound http.status=500");
	            // Now the exception payload is transformed to a SOAP-Fault, remove the ExceptionPayload!
	            message.setExceptionPayload(null);
	        	message.setProperty("http.status", 500, PropertyScope.OUTBOUND);
	            message.setPayload(soapFault);
	            return message;
	        }
		}
	        
    	System.err.println("No error, return origin message");
		// No, no exception could be found, let's return the original MuleMessage
		return message;
	}

    protected String createSoapFaultFromExceptionPayload(ExceptionPayload ep) {
        String errMsg   = ep.getCode() + ": " + ep.getMessage();
        String endpoint = getEndpoint().getEndpointURI().getAddress();
        String detail   = stackTraceToString(ep.getException());
        return createSoapFault(errMsg, endpoint, detail);
	}

	protected String stackTraceToString(Throwable ex) {
		StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

	protected String createSoapFault(String errMsg, String endpoint, String details) {
		return MessageFormat.format(SOAP_FAULT_V11, errMsg, endpoint, details);
	}

}			