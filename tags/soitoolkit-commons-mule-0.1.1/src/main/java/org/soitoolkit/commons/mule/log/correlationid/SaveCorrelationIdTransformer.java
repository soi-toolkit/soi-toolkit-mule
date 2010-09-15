package org.soitoolkit.commons.mule.log.correlationid;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageAwareTransformer;

import static org.soitoolkit.commons.mule.core.PropertyNames.SOITOOLKIT_CORRELATION_ID;

/**
 * Save a correlation id message property in the CorrelationIdStore for later retrieval by a synchronous response-processing.
 * 
 * @author Magnus Larsson
 *
 */
public class SaveCorrelationIdTransformer extends AbstractMessageAwareTransformer {

	public Object transform(MuleMessage message, String outputEncoding) throws TransformerException {
		String correlationId = message.getStringProperty(SOITOOLKIT_CORRELATION_ID, "Missing " + SOITOOLKIT_CORRELATION_ID);
		CorrelationIdStore.setCorrelationId(correlationId);

		if(logger.isDebugEnabled()) logger.debug("Saved property in threadLocal variable: " + SOITOOLKIT_CORRELATION_ID + " = " + correlationId);
		
		return message;
	}
}