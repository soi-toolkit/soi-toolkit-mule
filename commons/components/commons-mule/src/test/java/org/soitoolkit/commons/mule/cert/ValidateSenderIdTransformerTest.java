package org.soitoolkit.commons.mule.cert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the validate sender id transformer with various usage scenarios
 * 
 */
public class ValidateSenderIdTransformerTest {

	private final static Logger log = LoggerFactory.getLogger(ValidateSenderIdTransformerTest.class);
	
	private ValidateSenderIdTransformer transformer = null; 
	
	@Before
	public void setup() {
	}
	
	@Test
	public void transformMessage() throws TransformerException {
		
		final MuleMessage muleMessageMock = mock(MuleMessage.class);
		
		transformer = new ValidateSenderIdTransformer() {
			protected X509CertificateEvaluator getX509CertificateEvaluator(String property) {
				X509CertificateEvaluator evaluator = mock(X509CertificateEvaluator.class);
				evaluator.setPropertyName("CN");
				when(evaluator.evaluate(X509CertificateEvaluator.EXPR_SENDERID, muleMessageMock)).thenReturn("1234");
				return evaluator; 
			};
		};
		
		transformer.setValidSenderIds("1234");
		
		transformer.transformMessage(muleMessageMock, null);
		
		verify(muleMessageMock, times(1)).setSessionProperty("Sender", "1234");
	}
	
	@Test
	public void transformMessageInvalidSenderId() throws TransformerException {
		
		final MuleMessage muleMessageMock = mock(MuleMessage.class);
		
		transformer = new ValidateSenderIdTransformer() {
			protected X509CertificateEvaluator getX509CertificateEvaluator(String property) {
				X509CertificateEvaluator evaluator = mock(X509CertificateEvaluator.class);
				evaluator.setPropertyName("CN");
				when(evaluator.evaluate(X509CertificateEvaluator.EXPR_SENDERID, muleMessageMock)).thenReturn("987");
				return evaluator; 
			};
		};
		
		transformer.setValidSenderIds("1234");
		
		try {
			transformer.transformMessage(muleMessageMock, null);
			fail();
		} catch(Exception e) {
			assertEquals("Invalid senderId [987], allowed senderIds are: [1234]", e.getCause().getMessage());
		}
		verify(muleMessageMock, times(1)).setSessionProperty("Sender", "987");
	}
	
	@Test
	public void transformMessageInvalidSenderIdNull() throws TransformerException {
		
		final MuleMessage muleMessageMock = mock(MuleMessage.class);
		
		transformer = new ValidateSenderIdTransformer() {
			protected X509CertificateEvaluator getX509CertificateEvaluator(String property) {
				X509CertificateEvaluator evaluator = mock(X509CertificateEvaluator.class);
				evaluator.setPropertyName("CN");
				when(evaluator.evaluate(X509CertificateEvaluator.EXPR_SENDERID, muleMessageMock)).thenReturn(null);
				return evaluator; 
			};
		};
		
		transformer.setValidSenderIds("1234");
		
		try {
			transformer.transformMessage(muleMessageMock, null);
			fail();
		} catch(Exception e) {
			assertEquals("Invalid senderId [null], allowed senderIds are: [1234]", e.getCause().getMessage());
		}
		verify(muleMessageMock, times(1)).setSessionProperty("Sender", "");
	}
}
