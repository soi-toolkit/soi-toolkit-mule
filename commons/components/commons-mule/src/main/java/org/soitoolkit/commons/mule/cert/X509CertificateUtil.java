package org.soitoolkit.commons.mule.cert;

import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class X509CertificateUtil {

	private static final Logger log = LoggerFactory.getLogger(X509CertificateUtil.class);

	private X509CertificateUtil() {
	}

	public static String getPropertyFromX500Principal(X509Certificate cert, String propertyName) { 
		String propertyValue = null;
		
		String principalName = cert.getSubjectX500Principal().getName();
		log.debug("Found principalName = {}", principalName);
		
		Pattern pattern = createPattern(propertyName);
		Matcher matcher = pattern.matcher(principalName);
		if (matcher.find()) {
			propertyValue = matcher.group(1);
			log.debug("Found principal property {} = {}", propertyName, propertyValue);
		} else {
			//FIXED: Replaced: "principalName" with "propertyName"
			logAndThrowError("Principal property "  + propertyName + " not found in Certificate");
		}
		return propertyValue;
	}
	
	private static Pattern createPattern(String propertyName) {
		Pattern ptrn = Pattern.compile(propertyName + "=([^,]+)");
		if (log.isInfoEnabled()) {
			log.info("propertyName set to: " + propertyName);
		}
		return ptrn;
	}
	
	private static void logAndThrowError(String errorMessage) {
		log.error(errorMessage);
		throw new RuntimeException(errorMessage);
	}
}