
package org.soitoolkit.commons.mule.jaxb;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;

/**
 * Transform a JAXB v2 object to a XML String.
 * 
 * Sample usage for JAXB classes with an @XmlRootElement annotation:
 *
 * 	<custom-transformer name="StatusObjectToXml" class="org.soitoolkit.commons.mule.jaxb.JaxbObjectToXmlTransformer">
 * 		<spring:property name="contextPath"  value="se.callista.soalab.purchase.wsdl.v1"/>
 * 	</custom-transformer>
 * 
 * 
 * Sample usage for JAXB classes without an @XmlRootElement annotation:
 * (requires a separate transformer per JAXB class)
 *
 * 	<custom-transformer name="StatusObjectToXml" class="org.soitoolkit.commons.mule.jaxb.JaxbObjectToXmlTransformer">
 * 		<spring:property name="contextPath"  value="se.callista.soalab.purchase.wsdl.v1"/>
 * 		<spring:property name="namespaceURI" value="urn:soalab:purchase:v1"/>
 * 		<spring:property name="localPart"    value="Schedule"/>
 * 	</custom-transformer>
 * 
 * See http://weblogs.java.net/blog/kohsuke/archive/2006/03/why_does_jaxb_p.html for details regarding usage of the @XmlRootElement annotation.
 * 
 * @author Magnus Larsson
 *
 */
public class JaxbObjectToXmlTransformer extends AbstractJaxbTransformer {

	private String namespaceURI  = null; // urn:org.soitoolkit:purchase:v1
	private String localPart     = null; // Schedule

    public JaxbObjectToXmlTransformer() {
        registerSourceType(Object.class);
        setReturnClass(String.class);
    }

	public Object transform(MuleMessage message, String outputEncoding) throws TransformerException {

    	String xmlString = null;
    	
    	Object jaxbObject = message.getPayload();
        if (namespaceURI == null && localPart == null) {
        	xmlString = getJaxbUtil().marshal(jaxbObject);
        } else {
        	xmlString = getJaxbUtil().marshal(jaxbObject, namespaceURI, localPart);
        }

        return xmlString;
    }

	public String getNamespaceURI() {
		return namespaceURI;
	}

	public void setNamespaceURI(String namespaceURI) {
		this.namespaceURI = namespaceURI;
	}

	public String getLocalPart() {
		return localPart;
	}

	public void setLocalPart(String localPart) {
		this.localPart = localPart;
	}
}
