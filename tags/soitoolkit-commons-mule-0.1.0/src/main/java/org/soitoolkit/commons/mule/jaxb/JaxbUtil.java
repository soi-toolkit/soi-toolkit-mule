package org.soitoolkit.commons.mule.jaxb;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

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

    @SuppressWarnings("rawtypes")
	public JaxbUtil(Class... classesToBeBound) {
        try {
        	if (logger.isDebugEnabled()) logger.debug("Load JAXBContext based on classes: " + classesToBeBound);
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

     
    /** 
     * Marshal a JAXB object to a XML-string
     * 
     * @param jaxbObject
     * @return the XML string
     */
	public String marshal(Object jaxbObject) {

        try {

        	Marshaller marshaller = jaxbContext.createMarshaller();
            StringWriter writer = new StringWriter();

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
        try {

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Object       jaxbObject   = null;
           
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
        catch (JAXBException e)
        {
            throw new RuntimeException(e);
        }
    }
}
