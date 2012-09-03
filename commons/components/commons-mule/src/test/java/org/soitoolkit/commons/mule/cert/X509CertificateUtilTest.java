package org.soitoolkit.commons.mule.cert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the X509 certificate util with various usage scenarios
 * 
 */
public class X509CertificateUtilTest {

	private final static Logger log = LoggerFactory.getLogger(X509CertificateUtilTest.class);

	@Before
	public void setup() {
	}
	
	@Test
	public void getPropertyFromX500Principal() {
		X509Certificate cert = mock(X509Certificate.class);
		X500Principal principal = new X500Principal("CN=Duke, OU=JavaSoft, O=Sun Microsystems, C=US");
		
		when(cert.getSubjectX500Principal()).thenReturn(principal);
		
		String value = X509CertificateUtil.getPropertyFromX500Principal(cert, "CN");
		
		assertNotNull(value);
		assertEquals("Duke", value);
	}
	
	@Test
	public void getPropertyFromX500PrincipalNoPrincipalProperty() {
		X509Certificate cert = mock(X509Certificate.class);
		X500Principal principal = new X500Principal("OU=JavaSoft, O=Sun Microsystems, C=US");
		
		when(cert.getSubjectX500Principal()).thenReturn(principal);
		
		try {
			X509CertificateUtil.getPropertyFromX500Principal(cert, "CN");
			fail();
		} catch (Exception e) {
			assertEquals("Principal property CN not found in Certificate", e.getMessage());
		}
	}
}
