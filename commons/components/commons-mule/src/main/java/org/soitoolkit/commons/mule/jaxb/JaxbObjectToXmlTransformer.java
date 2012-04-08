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

	public static final boolean LOG_OBJ_CREATION = false;

	private String namespaceURI  = null; // urn:org.soitoolkit:purchase:v1
	private String localPart     = null; // Schedule

    public JaxbObjectToXmlTransformer() {
        registerSourceType(Object.class);
        setReturnClass(String.class);
    }

    @Override
	public Object transform(MuleMessage message, String outputEncoding) throws TransformerException {
    	return transformJaxbObjectToXml(message.getPayload());
    }

	public String transformJaxbObjectToXml(Object jaxbObject) throws TransformerException {
		if (LOG_OBJ_CREATION) System.err.println("JAXB-TO-XML-TRANSFORMER#" + id + " TRANSFORM WITH NULL JAXB-UTIL-OBJECT: " + (getJaxbUtil() == null));

    	String xmlString = null;
    	
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
