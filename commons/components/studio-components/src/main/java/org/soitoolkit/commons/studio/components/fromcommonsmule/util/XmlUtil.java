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
package org.soitoolkit.commons.studio.components.fromcommonsmule.util;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.mule.module.xml.stax.ReversibleXMLStreamReader;
import org.mule.module.xml.util.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * General utility methods for working with XML.

 * @author Magnus Larsson
 */
public class XmlUtil extends org.mule.util.XMLUtils {

	private static Logger logger = LoggerFactory.getLogger(XmlUtil.class);

	private static XMLInputFactory  xmlInputFactory  = XMLInputFactory.newInstance();
    private static XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

    /*

	public static XMLStreamReader toXmlStreamReader(Object src) throws XMLStreamException {
		return XMLUtils.toXMLStreamReader(xmlInputFactory, src);
	}

    public static QName getRootElementQName(Object src) throws XMLStreamException {
		QName name;
		XMLStreamReader reader = null;
		try {
			reader = toXmlStreamReader(src);
			reader.nextTag();
			name = reader.getName();
		} finally {
			reader.close();
		}
		return name;
	}

    /**
     * Converts a javax.xml.datatype.XMLGregorianCalendar to a java.util.Date.
     * 
     * @param a XMLGregorianCalendar date object
     * @return the corresponding Date object
     * /
    public static Date convertXmlDateToDate(XMLGregorianCalendar date) {
    	return date.toGregorianCalendar().getTime();
	}
*/
    /**
     * Converts a java.util.Date to a javax.xml.datatype.XMLGregorianCalendar, uses current time if specified date is null;
     * 
     * @param date, uses current time if specified date is null
     * @return a corresponding XMLGregorianCalendar object
     */
    public static XMLGregorianCalendar convertDateToXmlDate(Date date) {
		try {
			GregorianCalendar fromDate = new GregorianCalendar();
			if (date != null) {
				fromDate.setTime(date);
			}
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(fromDate);
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

    public static String convertReversibleXMLStreamReaderToString(ReversibleXMLStreamReader reader, String encoding) {

		if (logger.isDebugEnabled()) logger.debug("Start read ReversibleXMLStreamReader, T={}, R={}", Thread.currentThread().getId(), reader.toString());
		boolean wasTrackingEnabled = reader.isTracking();

		// Turn on tracking if not already enabled
		if (!wasTrackingEnabled) {
			reader.setTracking(true);
		}

		// Now transform the the stream to a string 
		String xml = convertXMLStreamReaderToString(reader, encoding);

		// Turn off tracking if it was not already enabled
		if (!wasTrackingEnabled) {
			reader.setTracking(false);
		}

		// Rest the stream so other consumers can read it
		reader.reset();
		if (logger.isDebugEnabled()) logger.debug("Stop read ReversibleXMLStreamReader, now reset");

		return xml;
	}

    public static String convertXMLStreamReaderToString(XMLStreamReader reader, String encoding) {
		try {

			ByteArrayOutputStream os = new ByteArrayOutputStream(2048);			

			// Unfortunately, the StAX source doesn't copy/serialize correctly so 
			// we have to do this little hack.
			XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(os, encoding);

			try {
			    writer.writeStartDocument();
			    XMLUtils.copy(reader, writer);
			    writer.writeEndDocument();
	
			    String result = os.toString();
			    return result;
	
			} finally {
			    writer.close();
			    os.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
