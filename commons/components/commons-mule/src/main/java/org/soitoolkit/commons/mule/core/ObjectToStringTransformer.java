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
package org.soitoolkit.commons.mule.core;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamReader;

import org.mule.api.transformer.TransformerException;
import org.mule.config.i18n.CoreMessages;
import org.mule.transformer.simple.ObjectToString;
import org.soitoolkit.commons.mule.util.BOMStripperInputStream;
import org.soitoolkit.commons.mule.util.XmlUtil;

/**
 * Extends the ObjectToString-transformer in Mule ESB
 * 
 * @author Magnus Larsson
 *
 */
public class ObjectToStringTransformer extends ObjectToString {

	@Override
	public Object doTransform(Object src, String encoding) throws TransformerException {

		// Add the capability to transform a XMLStreamReader to a String
		if (src instanceof XMLStreamReader) {
			if (logger.isDebugEnabled()) logger.debug("XMLStreamReader detected, converting it to a String since Mule's ObjectToString - transformer can't make it");
			return XmlUtil.convertXMLStreamReaderToString((XMLStreamReader)src, encoding);

		// Also take care of BOM-characters in InputStreams if any
		} else if (src instanceof InputStream) {
			if (logger.isDebugEnabled()) logger.debug("InputStream detected, wrap with a BOMStripper since Mule doesn't seem to handle BOM chars......");
			try {
				src = new BOMStripperInputStream((InputStream)src);
			} catch (IOException e) {
                throw new TransformerException(CoreMessages.errorReadingStream(), e);
            }
			return super.doTransform(src, encoding);

		// Otherwise hand over to the base class
		} else {
			return super.doTransform(src, encoding);
		}
	}
}
