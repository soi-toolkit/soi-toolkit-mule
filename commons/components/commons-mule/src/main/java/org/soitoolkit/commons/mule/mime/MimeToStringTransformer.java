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
package org.soitoolkit.commons.mule.mime;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.ParseException;

import org.mortbay.log.Log;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.transformer.AbstractMessageAwareTransformer;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.core.ObjectToStringTransformer;
import org.soitoolkit.commons.mule.util.MiscUtil;

/**
 * Transform a Mime payload to a string by taking the first content-part and converting it to a string.
 * TODO: NOTE: This code is only on a R&D level, i.e. needs to mature and be stabilized before used in production.
 * 
 * @author Magnus Larsson
 *
 */
public class MimeToStringTransformer extends AbstractMessageTransformer {

	private static final Logger log = LoggerFactory.getLogger(MimeToStringTransformer.class);

	private ObjectToStringTransformer o2s = new ObjectToStringTransformer();
	
	public MimeToStringTransformer()
    {
        registerSourceType(Object.class);
        setReturnClass(String.class);
    }

	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
        Object payload = message.getPayload();

        String contentType = (String)message.getProperty("Content-Type", PropertyScope.INBOUND);
        if (contentType == null) {
        	contentType = (String)message.getProperty("content-type", PropertyScope.INBOUND);
        }
        

		try {
			log.debug("Parsing contentType: {}", contentType);
			ContentType ct = new ContentType(contentType);
			String baseType = ct.getBaseType();
			String charset = ct.getParameter("charset");
			if (charset == null) charset = "UTF-8";
			log.debug("Parse results: baseType = {}, charset = {}", baseType, charset);

	        if (baseType.equals("application/x-www-form-urlencoded")) {
				payload = message.getPropertyNames().iterator().next(); //   MiscUtil.convertStreamToString((InputStream)payload);
				log.info("Found payload of type x-www-form-urlencoded");
	        	
	        } else if (baseType.equals("multipart/form-data")) {
	        	payload = transformMultipartPayload(payload, contentType);
				log.info("Found payload of type multipart/form-data payload");

	        } else if (baseType.equals("text/xml")) {
	        	payload = MiscUtil.convertStreamToString((InputStream)payload, charset);
	        	log.info("Found payload of type text/xml");

	        } else if (baseType.equals("application/octet-stream")) {
	        	payload = MiscUtil.convertStreamToString((InputStream)payload, charset);
	        	log.info("Found payload of type application/octet-stream: {}", payload);

	        } else {
				log.warn("*** UNKNOWN CONTENT-TYPE FOUND: {}, PAYLOAD-TYPE: {}", contentType, payload.getClass().getName());
				// Last resort
				payload = o2s.transform(payload, outputEncoding);
	        }
	        
		} catch (ParseException e) {
			e.printStackTrace();
			log.warn("*** UNKNOWN CONTENT-TYPE FOUND: {}, PAYLOAD-TYPE: {}", contentType, payload.getClass().getName());
			// Last resort
			payload = o2s.transform(payload, outputEncoding);
		}
        
		
		log.debug("Returning: {}", payload);
        return payload;
    }

	private Object transformMultipartPayload(Object payload, String contentType) {
		if (payload instanceof InputStream) {
        	try {
				MimeMessage mm = new MimeMessage(null, (InputStream)payload);

				if (logger.isDebugEnabled()) logger.debug("MIME Content typ = " + mm.getContentType());
				
				Object content = mm.getContent();

				if (content instanceof String) {
					payload = removeTrailingMimeBoundary(contentType, (String)content);
					
					if (logger.isDebugEnabled()) logger.debug("*** Plain text FOUND");
					
				} else if (content instanceof MimeMultipart) {
					if (logger.isDebugEnabled()) logger.debug("*** MULTI-PART CONTENT FOUND");
					MimeMultipart multipart = (MimeMultipart)mm.getContent();
					
					// TODO iterate over all body parts
					InputStream cont = (InputStream) multipart.getBodyPart(0).getContent();
					
					payload = MiscUtil.convertStreamToString(cont);

				} else if (content instanceof InputStream) {
					if (logger.isDebugEnabled()) logger.debug("*** INPUT-STREAM CONTENT FOUND");
					String str = MiscUtil.convertStreamToString((InputStream)content);
					if (logger.isDebugEnabled()) logger.debug("*** INPUT-STREAM CONTENT = " + str);

					payload = removeTrailingMimeBoundary(contentType, str);
					
				} else {
					if (logger.isDebugEnabled()) logger.debug("*** UNKNOWN CONTENT FOUND");
					payload = content.toString();
				}

				if (logger.isDebugEnabled()) logger.debug("*** MULTI-PART PAYLOAD TYPE = " + payload.getClass().getName());
				if (logger.isDebugEnabled()) logger.debug("*** MULTI-PART PAYLOAD LENGTH = " + payload.toString().length());
				if (logger.isDebugEnabled()) logger.debug("*** MULTI-PART PAYLOAD = " + payload);
			} catch (MessagingException e) {
				throw new RuntimeException(e);  
			} catch (IOException e) {
				throw new RuntimeException(e);  
			}
        	
        }
		return payload;
	}

	private String removeTrailingMimeBoundary(String contentType, String str) {
		String payload;
		int boundaryStartPos = contentType.indexOf("boundary=");
		String boundary = contentType.substring(boundaryStartPos + "boundary=".length());
		if (logger.isDebugEnabled()) logger.debug("*** BOUNDARY = " + boundary);
		
		boundaryStartPos = str.indexOf("--" + boundary + "--");
		
		// Also strip off the new line char preceding the boundary-tag
		boundaryStartPos--;
		
		payload = str.subSequence(0, boundaryStartPos).toString();
		return payload;
	}
}
