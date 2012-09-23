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
package org.soitoolkit.commons.studio.components.fromcommonsmule.jaxb;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Helper class that simplify marshalling and unmarshalling java-object to and from XML using JAXB v2.
 * 
 * @author Magnus Larsson
 *
 */
public class JaxbUtil {
    private JAXBContext jaxbContext = null;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	// Lazy instantiation through getters
    private Map<String, Object> unmarshallProps = null;
	private Map<String, Object> marshallProps = null;
	
	
    @SuppressWarnings("rawtypes")
	public JaxbUtil(Class... classesToBeBound) {
        try {
        	if (logger.isDebugEnabled()) logger.debug("Load JAXBContext based on classes: " + Arrays.toString(classesToBeBound));
            jaxbContext = JAXBContext.newInstance(classesToBeBound);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
		}    	
    }

    public JaxbUtil(String contextPath) {
    	setContextPath(contextPath);
    }

    public void setContextPath(String contextPath) {
        try {
        	if (contextPath == null || contextPath.length() == 0) {
            	if (logger.isDebugEnabled()) logger.debug("No context path, let's wait with creating the jaxbContext");
        		
        	} else {
            	if (logger.isDebugEnabled()) logger.debug("Load JAXBContext based on context path: " + contextPath);
                jaxbContext = JAXBContext.newInstance(contextPath);
        	}
        } catch (JAXBException e) {
            throw new RuntimeException(e);
		}    	
    }
    
    public void addMarshallProperty(String name, Object value) {
    	if (marshallProps == null) {
    		marshallProps = new HashMap<String, Object>();
    	}
    	marshallProps.put(name, value);    	
    }

    public void addUnmarshallProperty(String name, Object value) {
    	if (unmarshallProps == null) {
    		unmarshallProps = new HashMap<String, Object>();
    	}
    	unmarshallProps.put(name, value);    	
    }
     
    /** 
     * Marshal a JAXB object to a XML-string
     * 
     * @param jaxbObject
     * @return the XML string
     */
	public String marshal(Object jaxbObject) {

		// Precondition, check that the jaxbContext is set!
    	if (jaxbContext == null) {
    		logger.error("Trying to marshal with a null jaxbContext, returns null. Check your configuration, e.g. jaxb-transformers!");
    		return null;
    	}

    	try {

            StringWriter writer = new StringWriter();
        	Marshaller marshaller = jaxbContext.createMarshaller();
        	if (marshallProps != null) {
	        	for (Entry<String, Object> entry : marshallProps.entrySet()) {
	        		marshaller.setProperty(entry.getKey(), entry.getValue());
				}
        	}
        	marshaller.marshal(jaxbObject, writer);

        	String xml = writer.toString();
        	
            if (logger.isDebugEnabled()) logger.debug("marshalled jaxb object of type {}, returns xml: {}",  jaxbObject.getClass().getSimpleName(), xml);
          
            return xml;
	    } catch (JAXBException e) {
	        throw new RuntimeException(e);
		}
    }

    /**
     * Marshal version for JAXB objects that doesn't contain any @XmlRootElement annotation
     * 
     * @param jaxbObject
     * @param namespaceURI namespace for the root element
     * @param localPart    name for the root element
     * @return the xml string
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public String marshal(Object jaxbObject, String namespaceURI, String localPart) {
        jaxbObject = new JAXBElement(new QName(namespaceURI, localPart), jaxbObject.getClass(), jaxbObject);
        return marshal(jaxbObject);
	}


    /**
     * Unmarshal a xml payload into a JAXB object.
     * Removes any leading JAXBElement, happens typically when the JAXB class doesn't contain any @XmlRootElement annotation
     * Supports the following added types of xml payloads: String, and byte[] + all the stardard types that the JAXB Unmarshaller supports
     * 
     * @param payload
     * @return
     */
    @SuppressWarnings("rawtypes")
	public Object unmarshal(Object payload) {

		// Precondition, check that the jaxbContext is set!
    	if (jaxbContext == null) {
    		logger.error("Trying to unmarshal with a null jaxbContext, returns null. Check your configuration, e.g. jaxb-transformers!");
    		return null;
    	}

    	try {
        	Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        	if (unmarshallProps != null) {
	        	for (Entry<String, Object> entry : unmarshallProps.entrySet()) {
	        		unmarshaller.setProperty(entry.getKey(), entry.getValue());
				}
        	}
            Object jaxbObject = null;
           
            // Unmarshal depending on the type of source
            if (payload instanceof String) {
        		String src = (String)payload;
        		jaxbObject = unmarshaller.unmarshal(new StringReader(src));

        	} else if (payload instanceof byte[]) {
        		byte[] src = (byte[])payload;
        		jaxbObject = unmarshaller.unmarshal(new ByteArrayInputStream(src));
        		
        	// Rely on standard JAXB unmarshaller
        	} else if (payload instanceof File) {
        		jaxbObject = unmarshaller.unmarshal((File)payload);
        		
        	} else if (payload instanceof InputSource) {
        		jaxbObject = unmarshaller.unmarshal((InputSource)payload);
        		
        	} else if (payload instanceof InputStream) {
        		jaxbObject = unmarshaller.unmarshal((InputStream)payload);
        		
        	} else if (payload instanceof Reader) {
        		jaxbObject = unmarshaller.unmarshal((Reader)payload);

        	} else if (payload instanceof URL) {
        		jaxbObject = unmarshaller.unmarshal((URL)payload);
        		
        	} else if (payload instanceof InputSource) {
        		jaxbObject = unmarshaller.unmarshal((InputSource)payload);
        		
        	} else if (payload instanceof Node) {
        		jaxbObject = unmarshaller.unmarshal((Node)payload);

        	} else if (payload instanceof Source) {
        		jaxbObject = unmarshaller.unmarshal((Source)payload);
        		
        	} else if (payload instanceof XMLEventReader) {
        		jaxbObject = unmarshaller.unmarshal((XMLEventReader)payload);
        		
        	} else if (payload instanceof XMLStreamReader) {
        		jaxbObject = unmarshaller.unmarshal((XMLStreamReader)payload);

        	} else {
        		// Out of alternatives, have to throw a unknown source type exception...
        		throw new RuntimeException("Unknown sourcetype of the xml payload: " + payload.getClass().getName());
        	}
        		
            // Unmarshal done, postprocess by replacing any JAXBElement with the actual jaxb-object, see comment in the class-doc.
            if (jaxbObject instanceof JAXBElement) {
                if (logger.isDebugEnabled()) logger.debug("Found a JAXBElement, returns it value");
                jaxbObject = ((JAXBElement)jaxbObject).getValue();
            }

            logger.debug("unmarshalled xml payload of type: {}, returns jaxb object of type {}", payload.getClass().getName(), jaxbObject.getClass().getName());

            return jaxbObject;
        }
        catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

}
