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
package org.soitoolkit.commons.mule.log.correlationid;

import static org.soitoolkit.commons.mule.core.PropertyNames.SOITOOLKIT_CORRELATION_ID;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageAwareTransformer;

/**
 * Save a correlation id message property in the CorrelationIdStore for later retrieval by a synchronous response-processing.
 * 
 * @author Magnus Larsson
 * 
 * @deprecated since soi-toolkit 0.2.1 this transformer are no longer required since the SOITOOLKIT_CORRELATION_ID is stored in the session scope
 */
public class SaveCorrelationIdTransformer extends AbstractMessageAwareTransformer {

	/*
	 * Property id 
	 */
	private String id = "";
	public void setId(String id) {
		this.id = id;
	}

	public Object transform(MuleMessage message, String outputEncoding) throws TransformerException {
		String correlationId = message.getInboundProperty(SOITOOLKIT_CORRELATION_ID, "Missing " + SOITOOLKIT_CORRELATION_ID);
		CorrelationIdStore.setCorrelationId(id, correlationId);

		if(logger.isDebugEnabled()) logger.debug("Saved property in threadLocal variable: " + SOITOOLKIT_CORRELATION_ID + " = " + correlationId);
		
		return message;
	}
}