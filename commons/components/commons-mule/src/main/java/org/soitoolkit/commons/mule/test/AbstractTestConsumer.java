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
package org.soitoolkit.commons.mule.test;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Base class for test consumer of services based on the servlet-transport
 * 
 * @author Magnus Larsson
 *
 */
abstract public class AbstractTestConsumer {

    /**
     * Address based on usage of the servlet-transport
     * 
     * @param serviceUriPropertyName
     * @return
     */
    public static String getAddress(String uri, int httpPort, String ctxPath, String servletUri) {
	    String url = "http://localhost" + ":" + httpPort + ctxPath + servletUri + "/" + uri;
	    System.err.println("URL: " + url);
	    return url;    	
    }

    protected URL createEndpointUrlFromServiceAddress(String serviceAddress) {
        try {
            return new URL(serviceAddress + "?wsdl");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL Exception: " + e.getMessage());
        }
    }
}