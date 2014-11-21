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
package org.soitoolkit.commons.mule.soap;

import java.text.MessageFormat;

import org.mule.api.ExceptionPayload;
import org.mule.api.MuleMessage;
import org.mule.api.config.MuleProperties;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transformer.TransformerMessagingException;
import org.mule.api.transport.DispatchException;
import org.mule.api.transport.PropertyScope;
import org.mule.transformer.AbstractMessageTransformer;
import org.mule.transport.http.HttpConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transformer to be used as a responseTransformer for a SOAP based service.
 * Creates a SOAP Fault structure with exception information as response if an error has occurred in the processing of the request.
 * 
 * @author Magnus Larsson
 *
 */
public class CreateSoapFaultIfExceptionTransformer extends AbstractMessageTransformer {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
    static String SOAP_FAULT_V11 = 
    	"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
    	"  <soapenv:Header/>" + 
    	"  <soapenv:Body>" + 
		"    <soap:Fault xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
		"      <faultcode>soap:Server</faultcode>\n" + 
		"      <faultstring>{0}</faultstring>\n" +
		"      <faultactor>{1}</faultactor>\n" +
		"      <detail>\n" +
		"        {2}\n" +
		"      </detail>\n" + 
		"    </soap:Fault>" + 
		"  </soapenv:Body>" + 
		"</soapenv:Envelope>";
    
    @Override
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

    	logger.debug("transform() called");
    		
		// Take care of any error message and send it back as a SOAP Fault!
		// Is there an exception-payload?
		ExceptionPayload ep = message.getExceptionPayload();
        if (ep == null) {
        	
        	// No, it's no, just bail out returning what we got
        	logger.debug("No error, return origin message");
        	return message;
        }

		logger.debug("ExceptionPayload detected as well, let's create a SOAP-FAULT!");

    	String soapFault = createSoapFaultFromExceptionPayload(ep);
    	logger.debug("Created soapFault: {}", soapFault);

        // Now the exception payload is transformed to a SOAP-Fault, remove the ExceptionPayload!
		logger.debug("Set ExceptionPayload to null and outbound http.status=500");
        message.setExceptionPayload(null);
//        message.removeProperty("http.status", PropertyScope.OUTBOUND);
        message.setProperty("http.status", 500, PropertyScope.OUTBOUND);
        message.setPayload(soapFault);
        return message;
	        
	}
    
    protected String createSoapFaultFromExceptionPayload(ExceptionPayload exceptionPayload) {
    	
    	/* Use the root exception if any, otherwise the actual exception. */
        logger.debug("Exception: " + exceptionPayload.getException() + ", "
            + exceptionPayload.getException().getClass().getName());
        logger.debug("RootException: " + exceptionPayload.getRootException() + ", "
            + exceptionPayload.getRootException().getClass().getName());
        Throwable e =
            (exceptionPayload.getRootException() != null) ? exceptionPayload.getRootException() : exceptionPayload
                .getException();

        String errMsg = e.getMessage();
        String endpoint = null;

        ImmutableEndpoint ie = getEndpoint();
        if (ie != null) {
            endpoint = getEndpoint().getEndpointURI().getAddress();
        } else {
            if (exceptionPayload.getException() instanceof DispatchException) {
                DispatchException de = (DispatchException) exceptionPayload.getException();
                endpoint =
                    de.getEvent().getMessage()
                        .getProperty(MuleProperties.MULE_ENDPOINT_PROPERTY, PropertyScope.OUTBOUND);
            } else if (exceptionPayload.getException() instanceof TransformerMessagingException) {
                TransformerMessagingException tme = (TransformerMessagingException) exceptionPayload.getException();
                endpoint =
                    tme.getEvent().getMessage()
                        .getProperty(HttpConnector.HTTP_CONTEXT_URI_PROPERTY, PropertyScope.INBOUND);
            }
        }
        String detail = e.getMessage();
        return createSoapFault(errMsg, endpoint, detail);
	}

	protected String createSoapFault(String errMsg, String endpoint, String details) {
		return MessageFormat.format(SOAP_FAULT_V11, errMsg, endpoint, details);
	}

}			