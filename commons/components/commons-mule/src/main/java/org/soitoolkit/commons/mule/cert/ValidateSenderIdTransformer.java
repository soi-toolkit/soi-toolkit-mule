package org.soitoolkit.commons.mule.cert;

import java.util.Arrays;
import java.util.List;

import org.mule.api.MuleMessage;
import org.mule.api.context.MuleContextAware;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Validates sender-id in X509 client certificate
 * 
 * Sample configuration:
 * 
 * <custom-transformer name="validateSenderId" class="org.soitoolkit.commons.mule.cert.ValidateSenderIdTransformer">
 * 	  <spring:property name="senderIdPropertyName" value="OU"/>
 * 	  <spring:property name="validSenderIds"       value="CGM-ES,SIE"/>
 * </custom-transformer>
 * 
 */
public class ValidateSenderIdTransformer extends AbstractMessageTransformer implements MuleContextAware {

	private static final Logger log = LoggerFactory.getLogger(ValidateSenderIdTransformer.class);

	private String  senderIdPropertyName; // E.g. "CN" or "OU"
	private List<String> validSenderIds;
	
	/**
	 * Property senderIdPropertyName
	 */
	public void setSenderIdPropertyName(String senderIdPropertyName) {
		this.senderIdPropertyName = senderIdPropertyName;
	}

	/**
	 * Property validSenderIds
	 */
	public void setValidSenderIds(String validSenderIds) {
		this.validSenderIds = Arrays.asList(validSenderIds.split(","));
	}
	
	protected X509CertificateEvaluator getX509CertificateEvaluator(String property) {
		X509CertificateEvaluator eval = new X509CertificateEvaluator();
		eval.setPropertyName(senderIdPropertyName);
		return eval; 
	}

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		X509CertificateEvaluator eval = this.getX509CertificateEvaluator(senderIdPropertyName);
		
		String senderId = (String) eval.evaluate(X509CertificateEvaluator.EXPR_SENDERID, message);
		
		// add to message header
		if (senderId != null) {
			message.setSessionProperty("Sender", senderId);
		} else {
			message.setSessionProperty("Sender", "");
		}
			
		if (validSenderIds.contains(senderId)) {
			log.debug("{} is a valid senderId!", senderId);
		} else {
			logAndThrowError("Invalid senderId [" + senderId + "], allowed senderIds are: " + validSenderIds);
		}

		return message;
    }

	private void logAndThrowError(String errorMessage) throws TransformerException {
		logger.error(errorMessage);
		throw new TransformerException(this, new RuntimeException(errorMessage));
	}
	
}