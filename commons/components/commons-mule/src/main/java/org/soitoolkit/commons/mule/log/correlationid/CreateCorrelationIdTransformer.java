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

import static org.soitoolkit.commons.mule.core.PropertyNames.SOITOOLKIT_CONTRACT_ID;
import static org.soitoolkit.commons.mule.core.PropertyNames.SOITOOLKIT_CORRELATION_ID;
import static org.soitoolkit.commons.mule.core.PropertyNames.SOITOOLKIT_INTEGRATION_SCENARIO;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.transformer.AbstractMessageAwareTransformer;
import org.mule.util.UUID;

/**
 * Create a correlation id and set is as a property on the message
 * Can also set message properties for integration scenario and contractId
 * 
 * TODO: This transformer seems to be a bit overloaded, setting integration scenario and contractId should be handled by other components.
 * 
 * @author Magnus Larsson
 *
 */
public class CreateCorrelationIdTransformer extends AbstractMessageAwareTransformer {

	private String integrationScenario = null;
	public void setIntegrationScenario(String integrationScenario) {
		this.integrationScenario = integrationScenario;
	}

	private String contractId = null;
	public void setContractId(String contractId) {
		this.contractId = contractId;
	}

	public Object transform(MuleMessage message, String outputEncoding) throws TransformerException {
		String correlationId = UUID.getUUID();
		message.setProperty(SOITOOLKIT_CORRELATION_ID, correlationId, PropertyScope.SESSION);
		
		if (integrationScenario != null) {
			message.setProperty(SOITOOLKIT_INTEGRATION_SCENARIO, integrationScenario, PropertyScope.SESSION);
		}

		if (contractId != null) {
			message.setProperty(SOITOOLKIT_CONTRACT_ID, contractId, PropertyScope.SESSION);
		}

		if(logger.isDebugEnabled()) logger.debug("Created property: " + SOITOOLKIT_CORRELATION_ID + " = " + correlationId);
		
		return message;
	}

}