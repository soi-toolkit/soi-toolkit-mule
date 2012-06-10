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
package org.soitoolkit.commons.mule.jaxb;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBException;

import org.mortbay.log.Log;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.transformer.AbstractMessageAwareTransformer;

/**
 * Base class for JAXB v2 transformers
 * 
 * @author Magnus Larsson
 */
public abstract class AbstractJaxbTransformer extends AbstractMessageAwareTransformer {
    private String   contextPath = null;
    private JaxbUtil jaxbUtil = null;

    private static Map<String, JaxbUtil> jaxbUtilMap = new HashMap<String, JaxbUtil>();
    
    @Override
    public void initialise() throws InitialisationException {
        try {
        	initializeCachedJaxbObject();
        	
        } catch (JAXBException e) {
            throw new InitialisationException(e, this);
		}
    }

	private void initializeCachedJaxbObject() throws JAXBException {

		// Get a cached jaxb context based on the transformer name, if any
		String transformerName = getName();
		JaxbUtil cachedJaxbContex = jaxbUtilMap.get(transformerName);
		
		if (cachedJaxbContex != null) {
			// Ok, we found a cached entry, use it!
			jaxbUtil = cachedJaxbContex;
			if (logger.isDebugEnabled()) logger.debug("Use cached JaxbUtil for transformer " + transformerName);
			return;
		}
		
		// Not found, let's create a jaxb context and update the Map.
		// But let's first assure that we are alone updating the map (avoid concurrent threads creating overlapping jaxb objects)
		synchronized (jaxbUtilMap) {
			// We are now alone updating the map :-)
			// Start with a final check to see if the entry is still missing 
			// (could have been inserted by some other thread while we waited for the lock...)
			cachedJaxbContex = jaxbUtilMap.get(transformerName);
			
			if (cachedJaxbContex != null) {
				// Some other thread created the entry while we were waiting on the lock, use it!
				jaxbUtil = cachedJaxbContex;
		    	if (logger.isDebugEnabled()) logger.debug("Use cached JaxbUtil for transformer (created during wait for lock!) " + transformerName);
		    	return;
			}
			
			// Ok, we are first out! 
			// So it's time to take the hit, let's create the heavyweight jaxb context object and place it in the map
			if (logger.isDebugEnabled()) logger.debug("Load JaxbUtil for " + getName() + " based on context path: " + contextPath);
			jaxbUtil = new JaxbUtil(contextPath);
			jaxbUtilMap.put(transformerName, jaxbUtil);
		}
	}

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = trimWhitespace(contextPath);
    }

	protected JaxbUtil getJaxbUtil() {
		
		if (jaxbUtil == null) {
	        try {
	    		logger.debug("null jaxbutil detected, calling initializeCachedJaxbObject()!");
	        	initializeCachedJaxbObject();
	        	
	        } catch (JAXBException e) {
	            throw new RuntimeException(e);
			}
		}

        return jaxbUtil;
    }

	protected String trimWhitespace(String contextPath) {
	    StringBuffer withoutSpaces = new StringBuffer();

	    StringTokenizer st = new StringTokenizer(contextPath);

	    while(st.hasMoreTokens()) {
	       withoutSpaces.append(st.nextToken());
	    }

	    return withoutSpaces.toString(); 
	}	
}
