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

import static org.junit.Assert.*;

import javax.xml.bind.Marshaller;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.logentry.schema.v1.LogEntryType;
import org.soitoolkit.commons.logentry.schema.v1.LogEvent;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

public class JaxbUtilTest {

	private final static Logger log = LoggerFactory.getLogger(JaxbUtilTest.class);
	
	@Test
	public void testMarshal() {
		
		String expectedXml = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
		"<logEvent xmlns=\"urn:org.soitoolkit.commons.logentry.schema:v1\">" + 
		"<logEntry>" +
		"<payload>MyPayload</payload>" +
		"</logEntry>" +
		"</logEvent>";
		
		LogEvent logEvent = new LogEvent();
		LogEntryType logEntry = new LogEntryType();
		logEvent.setLogEntry(logEntry);
		logEntry.setPayload("MyPayload");
		
		JaxbUtil jaxbUtil = new JaxbUtil(LogEvent.class);
		String actualXml = jaxbUtil.marshal(logEvent);
		
		log.info(actualXml);
		assertEquals(expectedXml, actualXml);
	}

	@Test
	public void testMarshalWithoutXmlRootElement() {
		
		String expectedXml = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
		"<logEntry xmlns=\"urn:org.soitoolkit.commons.logentry.schema:v1\">" + 
		"<payload>MyPayload</payload>" +
		"</logEntry>";
		
		LogEntryType logEntry = new LogEntryType();
		logEntry.setPayload("MyPayload");
		
		JaxbUtil jaxbUtil = new JaxbUtil(LogEvent.class);
		String actualXml = jaxbUtil.marshal(logEntry, "urn:org.soitoolkit.commons.logentry.schema:v1", "logEntry");
		
		log.info(actualXml);
		assertEquals(expectedXml, actualXml);
	}

	@Test
	public void testMarshalWithFormatting() {
		
		String expectedXml = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
		"<logEvent xmlns=\"urn:org.soitoolkit.commons.logentry.schema:v1\">\n" + 
		"    <logEntry>\n" +
		"        <payload>MyPayload</payload>\n" +
		"    </logEntry>\n" +
		"</logEvent>\n";
		
		LogEvent logEvent = new LogEvent();
		LogEntryType logEntry = new LogEntryType();
		logEvent.setLogEntry(logEntry);
		logEntry.setPayload("MyPayload");
		
		JaxbUtil jaxbUtil = new JaxbUtil(LogEvent.class);
		jaxbUtil.addMarshallProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		String actualXml = jaxbUtil.marshal(logEvent);
		
		log.info(actualXml);
		assertEquals(expectedXml, actualXml);
	}

	@Test
	public void testUnmarshal() {
		
		String expectedPayload = "MyPayload"; 
		
		String xml = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
		"<logEvent xmlns=\"urn:org.soitoolkit.commons.logentry.schema:v1\">" + 
		"<logEntry>" +
		"<payload>MyPayload</payload>" +
		"</logEntry>" +
		"</logEvent>";
		
		JaxbUtil jaxbUtil = new JaxbUtil(LogEvent.class);
		LogEvent logEvent = (LogEvent)jaxbUtil.unmarshal(xml);

		String actualPayload = logEvent.getLogEntry().getPayload();
				
		log.info(actualPayload);
		assertEquals(expectedPayload, actualPayload);
	}

}
