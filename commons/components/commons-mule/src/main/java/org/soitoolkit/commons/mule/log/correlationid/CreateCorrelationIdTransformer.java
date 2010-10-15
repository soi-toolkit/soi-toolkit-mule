package org.soitoolkit.commons.mule.log.correlationid;

import static org.soitoolkit.commons.mule.core.PropertyNames.SOITOOLKIT_CONTRACT_ID;
import static org.soitoolkit.commons.mule.core.PropertyNames.SOITOOLKIT_CORRELATION_ID;
import static org.soitoolkit.commons.mule.core.PropertyNames.SOITOOLKIT_INTEGRATION_SCENARIO;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
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
		message.setProperty(SOITOOLKIT_CORRELATION_ID, correlationId);
		
		if (integrationScenario != null) {
			message.setProperty(SOITOOLKIT_INTEGRATION_SCENARIO, integrationScenario);
		}

		if (contractId != null) {
			message.setProperty(SOITOOLKIT_CONTRACT_ID, contractId);
		}

		if(logger.isDebugEnabled()) logger.debug("Created property: " + SOITOOLKIT_CORRELATION_ID + " = " + correlationId);
		
		return message;
	}

}