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

import javax.xml.stream.XMLStreamReader;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;

/**
 * Transform a XML payload to a JAXB v2 object.
 * 
 * Sample usage:
 *
 * <custom-transformer name="XmlToJaxbObject" class="org.soitoolkit.commons.mule.jaxb.XmlToJaxbObjectTransformer">
 *   <spring:property name="contextPath"  value="se.callista.soalab.purchase.wsdl.v1"/>
 * </custom-transformer>
 * 
 * NOTE: For JAXB classes that are not annotated with a @XmlRootElement we typically get a JAXBElement object when we unmarshall a XML string.
 * For simplified usage this transformer removes any JAXBElements and returns the actual object that we are interested in.
 *
 * See http://weblogs.java.net/blog/kohsuke/archive/2006/03/why_does_jaxb_p.html for details regarding usage of the @XmlRootElement annotation.
 * 
 * @author Magnus Larsson
 *
 */
public class XmlToJaxbObjectTransformer extends AbstractJaxbTransformer {

	public XmlToJaxbObjectTransformer()
    {
        registerSourceType(Object.class);
        setReturnClass(Object.class);
    }

	public Object transform(MuleMessage message, String outputEncoding) throws TransformerException
    {
        Object payload = message.getPayload();
        
    	// TODO: Sometimes we get a payload as an Object-array containing the XMLStreamReader, seems like a bug to me...
    	// Will ask on the maillist for an explanation.
    	if (payload instanceof Object[]) {
    		payload = getXMLStreamReader((Object[])payload);
    	}
        
        return getJaxbUtil().unmarshal(payload);
    }

	private Object getXMLStreamReader(Object[] payload) {
		XMLStreamReader reader = null;
		for (Object o : payload) {
			if (o instanceof XMLStreamReader) {
				reader = (XMLStreamReader)o;
				break;
			}
		}
		
		// If no XMLStreamReader was found then return the original payload
		return (reader == null) ? payload : reader;
	}
}