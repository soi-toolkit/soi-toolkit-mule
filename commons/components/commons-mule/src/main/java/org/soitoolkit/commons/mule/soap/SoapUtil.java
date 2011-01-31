/* 
 * Licensed to the soi-toolkit project under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The soi-toolkit project licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.soitoolkit.commons.mule.soap;

import javax.net.ssl.TrustManager;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.transport.http.HTTPConduit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General utility methods for working with SOAP using Apache CXF.

 * @author Magnus Larsson
 */
public class SoapUtil {

	private static Logger log = LoggerFactory.getLogger(SoapUtil.class);

	/**
     * Hidden constructor.
     */
    private SoapUtil() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }

    /**
     * Disables validation of https server certificates, only to be used during development and tests!!!
     * 
     * @param client
     */
	public static void disableTlsServerCertificateCheck(Client client) {
		
		if (!(client.getConduit() instanceof HTTPConduit)) {
			log.warn("Conduit not of type HTTPConduit (" + client.getConduit().getClass().getName() + ") , skip disabling server certification validation.");
			return;
		}

		log.warn("Disables server certification validation for: " + client.getEndpoint().getEndpointInfo().getAddress());
		
		HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
        TLSClientParameters tlsParams = new TLSClientParameters();
        TrustManager[] trustAllCerts = new TrustManager[] { new FakeTrustManager() };
        tlsParams.setTrustManagers(trustAllCerts);
        tlsParams.setDisableCNCheck(true);
        httpConduit.setTlsClientParameters(tlsParams);
	}
}
