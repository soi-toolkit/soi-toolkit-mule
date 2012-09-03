package org.soitoolkit.commons.mule.cert;


import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import org.mule.api.MuleMessage;
import org.mule.api.expression.ExpressionEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class X509CertificateEvaluator implements ExpressionEvaluator {

	private static final Logger log = LoggerFactory.getLogger(X509CertificateEvaluator.class);
	
	public static final String NAME = "x509cert";
	public static final String EXPR_SENDERID = "sender-id";

	private String propertyName;
	
	public String getName() {
		return NAME;
	}

	/**
	 * Property propertyName
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
		log.debug("propertyName = {}", propertyName);
	}
	
	public void setName(String name) {
		throw new UnsupportedOperationException("setName");
	}

	public Object evaluate(String expression, MuleMessage message) {
		try {
			String value = null;
			
	        //Is the header optional? the '*' denotes optional
		    if (EXPR_SENDERID.equals(expression)) {
				value = getSenderIdFromCertificate(message, "PEER_CERTIFICATES");
				if (log.isDebugEnabled()) {
					String me = getSenderIdFromCertificate(message, "LOCAL_CERTIFICATES");
					log.debug("{} validates sender id: {}", me, value);
				}
		    }
		    
		    return value;
		    
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}
	
	private String getSenderIdFromCertificate(MuleMessage message, String certProperty) {

		String senderId = null;
		X509Certificate cert = getX509Certificate(message, certProperty);

		if (cert != null) {
			senderId = X509CertificateUtil.getPropertyFromX500Principal(cert, propertyName);
			if (senderId == null) {
				logAndThrowError("No senderId found in Certificate");
			}
		} else {
			logAndThrowError("No senderId found in Certificate: No certificate found from client");
		}
		
		// Check if this is coded in hex (HCC Funktionscertifikat does that!)
		if (senderId.startsWith("#")) {
			return convertFromHexToString(senderId.substring(5));
		} else {
			return senderId;			
		}
	}

	private X509Certificate getX509Certificate(MuleMessage message, String certProperty) {
		X509Certificate cert = null;
		
		Certificate[] certificateChain = (Certificate[]) message.getOutboundProperty(certProperty);

		if (certificateChain != null) {
			// Check type of first certificate in the chain, this should be the
			// clients certificate
			if (certificateChain[0] instanceof X509Certificate) {
				cert = (X509Certificate) certificateChain[0];
			} else {
				logAndThrowError("First certificate in chain is not X509Certificate: " + certificateChain[0]);
			}
		} else {
			logAndThrowError("No certificate chain found from client");
		}
		
		return cert;
	}
	
	private void logAndThrowError(String errorMessage) {
		log.error(errorMessage);
		throw new RuntimeException(errorMessage);
	}

	private String convertFromHexToString(String hexString) {
		byte [] txtInByte = new byte [hexString.length() / 2];
		int j = 0;
		for (int i = 0; i < hexString.length(); i += 2)
		{
			txtInByte[j++] = Byte.parseByte(hexString.substring(i, i + 2), 16);
		}
		return new String(txtInByte);
	}

}
