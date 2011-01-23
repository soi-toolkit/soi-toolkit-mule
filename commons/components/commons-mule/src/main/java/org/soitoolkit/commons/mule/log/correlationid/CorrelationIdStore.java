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
package org.soitoolkit.commons.mule.log.correlationid;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores correlationId's in a thread local variable.
 * Typically set by a synchronous request-processing and later on picked up by a synchronous response-processing, i.e. executing in the same thread.
 * 
 * TODO: Could a session scoped mule message property be used instead?
 * TODO: Does it have the same scope, i.e. surviving a complete sychronous request and response?
 * 
 * @author Magnus Larsson
 * 
 * @deprecated since soi-toolkit 0.2.1 this transformer are no longer required since the SOITOOLKIT_CORRELATION_ID is stored in the session scope
 */
public class CorrelationIdStore {
	private static final Logger log = LoggerFactory.getLogger(CorrelationIdStore.class);
	public static ThreadLocal<Map<String, String>> correlationIdMap = new ThreadLocal<Map<String, String>>();

    /**
     * Hidden constructor.
     */
    private CorrelationIdStore() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }
    
    public static String getCorrelationId(String id) {
    	String bcid = getCorrelationIdMap().get(id);
    	log.debug("BCID from TL: [{}:{}]", id, bcid);
    	return bcid;
    }

    public static void setCorrelationId(String id, String newCorrelationId) {
    	log.debug("BCID to TL: [{}:{}]", id, newCorrelationId);
    	getCorrelationIdMap().put(id, newCorrelationId);
    }
    
    private static Map<String, String> getCorrelationIdMap() {
    	Map<String, String> map = correlationIdMap.get();
    	if (map == null) {
    		map = new HashMap<String, String>();
    		correlationIdMap.set(map);
    	}
    	return map;
    }

}