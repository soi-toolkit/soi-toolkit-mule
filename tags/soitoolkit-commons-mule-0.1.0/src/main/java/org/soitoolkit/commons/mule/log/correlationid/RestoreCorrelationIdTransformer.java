package org.soitoolkit.commons.mule.log.correlationid;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageAwareTransformer;

import static org.soitoolkit.commons.mule.core.PropertyNames.SOITOOLKIT_CORRELATION_ID;


/**
 * Restore a correlation id as a message property from the CorrelationIdStore, typically used in a synchronous response-processing.
 * 
 * @author Magnus Larsson
 *
 */
public class RestoreCorrelationIdTransformer extends AbstractMessageAwareTransformer {

	public Object transform(MuleMessage message, String outputEncoding) throws TransformerException {

		String correlationId = CorrelationIdStore.getCorrelationId();
		message.setProperty(SOITOOLKIT_CORRELATION_ID, correlationId);

		if(logger.isDebugEnabled()) logger.debug("Restored property from threadLocal variable: " + SOITOOLKIT_CORRELATION_ID + " = " + correlationId);
		
		return message;
	}
}