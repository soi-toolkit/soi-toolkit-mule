package org.soitoolkit.commons.mule.soap;

import javax.net.ssl.X509TrustManager;

public class FakeTrustManager implements X509TrustManager {
 	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
 		return null;
 	}
 	public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
 	public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
}
