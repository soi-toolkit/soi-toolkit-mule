package org.soitoolkit.commons.mule.jaxb;

import static org.junit.Assert.*;

import javax.xml.bind.Marshaller;

import org.junit.Test;
import org.soitoolkit.commons.logentry.schema.v1.LogEntryType;
import org.soitoolkit.commons.logentry.schema.v1.LogEvent;

public class JaxbUtilTest {

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
		
		System.err.println(actualXml);
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
		
		System.err.println(actualXml);
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
		jaxbUtil.addMarchallProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		String actualXml = jaxbUtil.marshal(logEvent);
		
		System.err.println(actualXml);
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
				
		System.err.println(actualPayload);
		assertEquals(expectedPayload, actualPayload);
	}

}
