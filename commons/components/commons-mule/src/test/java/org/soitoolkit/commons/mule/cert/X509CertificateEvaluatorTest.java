package org.soitoolkit.commons.mule.cert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

import org.junit.Before;
import org.junit.Test;
import org.mule.api.MuleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the X509 certificate evaluator with various usage scenarios
 * 
 */
public class X509CertificateEvaluatorTest {
	
	private final static Logger log = LoggerFactory.getLogger(X509CertificateEvaluatorTest.class);

	X509CertificateEvaluator evaluator = null;
	
	@Before
	public void setup() {
		evaluator = new X509CertificateEvaluator();
		evaluator.setPropertyName("CN");
	}
	
	@Test
	public void evaluate() {
		
		MuleMessage muleMessageMock = mock(MuleMessage.class);
		X509Certificate certMock = mock(X509Certificate.class);
		X500Principal principal = new X500Principal("CN=Duke, OU=JavaSoft, O=Sun Microsystems, C=US");
		
		Certificate[] certificateChain = new Certificate[] {certMock};
		when(muleMessageMock.getOutboundProperty("PEER_CERTIFICATES")).thenReturn(certificateChain);
		when(certMock.getSubjectX500Principal()).thenReturn(principal);
		
		Object senderId = evaluator.evaluate(X509CertificateEvaluator.EXPR_SENDERID, muleMessageMock);
		
		assertNotNull(senderId);
		assertEquals("Duke", senderId);
	}
	
	@Test
	public void evaluateNoClientCertException() {
		
		MuleMessage muleMessge = mock(MuleMessage.class);
		try {
			evaluator.evaluate(X509CertificateEvaluator.EXPR_SENDERID, muleMessge);
			fail();
		} catch(Exception e) {
			assertEquals("No certificate chain found from client", e.getCause().getMessage());
		}
	}
}
