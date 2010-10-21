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
package org.soitoolkit.commons.mule.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.xml.stream.XMLStreamReader;

import org.mule.api.MuleMessage;
import org.mule.api.config.MuleProperties;
import org.mule.api.transformer.TransformerException;
import org.mule.transport.jms.transformers.ObjectToJMSMessage;
import org.soitoolkit.commons.mule.util.XmlUtil;

/**
 * Extends the JMS-transport ObjectToJms-transformer.
 * 
 * Adds:
 * 1. Adjust mule-reply-to queue for wmq-transport, removing // in wmq://...queue
 * 2. Removes all non JMS standard properties in the transformed JMS Message
 * 
 * @author Magnus Larsson
 *
 */
public class ObjectToJMSMessageTransformer extends ObjectToJMSMessage {
	
	@Override
	public Object transform(MuleMessage message, String outputEncoding) throws TransformerException {

		// Add the capability to transform a XMLStreamReader to a String
		Object payload = message.getPayload();
		if (payload instanceof XMLStreamReader) {
			if (logger.isDebugEnabled()) logger.debug("XMLStreamReader detected, converting it to a String so that Mule's ObjectToJMSMessage - transformer can make a JMS out of it");
			payload = XmlUtil.convertXMLStreamReaderToString((XMLStreamReader)payload, outputEncoding);
			message.setPayload(payload);
		}

		return super.transform(message, outputEncoding);
	}

	@Override
	protected void setJmsProperties(MuleMessage message, Message msg) throws JMSException {

		// Remove some common http-related properties that otherwise cause warnings to be written to the log.
		// TODO: These properties should be removed earlier in the processing by the specific transport 
		message.removeProperty("http.method");
		message.removeProperty("Content-Type");
		message.removeProperty("Content-Length"); 
		message.removeProperty("User-Agent");
		message.removeProperty("http.context.path"); 
		message.removeProperty("http.request");
		message.removeProperty("http.request.path"); 

		Object muleReplyToObj = message.getProperty(MuleProperties.MULE_REPLY_TO_PROPERTY);
		String muleReplyTo = (muleReplyToObj == null) ? null : muleReplyToObj.toString();

		// The wmq-transport seems to add extra // to the reply queue, so we better remove them
		// TODO: Check why the extra // are added and see if it can't be fixed
		if (muleReplyTo != null && muleReplyTo.startsWith("wmq://")) {
			if (logger.isDebugEnabled()) logger.debug("WMQ-Transport detected, removing the two // in queuename!");
			String newMuleReplyQueue = "wmq:" + muleReplyTo.substring("wmq://".length());;
			message.setProperty(MuleProperties.MULE_REPLY_TO_PROPERTY, newMuleReplyQueue);
			if (logger.isDebugEnabled()) logger.debug("Replaced " + muleReplyTo + " with " + newMuleReplyQueue);
		}

		super.setJmsProperties(message, msg);
	}
}
