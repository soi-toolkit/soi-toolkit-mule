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
package org.soitoolkit.commons.mule.mail;

import static org.junit.Assert.*;

import static org.soitoolkit.commons.mule.mail.MailUtil.sendSmtpSslMessage;
import static org.soitoolkit.commons.mule.mail.MailUtil.receiveMessagesUsingImap;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jdbc.JdbcUtil;

/**
 * Tests for the MailUtil - class
 * 
 * @author Magnus Larsson
 *
 */
public class MailUtilTest {

	private final static Logger log = LoggerFactory.getLogger(JdbcUtil.class);

	String smtpHost     = "smtp.gmail.com";
	String smtpSslPort  = "465";
	String smtpUsername = "soitoolkit1";
	String smtpPassword = "soitoolkit1pwd";

	String imapHost     = "imap.n.mail.yahoo.com";
	String imapUsername = "soitoolkit2@yahoo.se";
	String imapPassword = "soitoolkit2pwd";

	String from         = smtpUsername + "@gmail.com";
	String to           = imapUsername;
	String subject      = "Hello soi-toolkit user (åäöÅÄÖ)";
	String text         = "Welcome to soi-toolkit at " + new Date() + " (åäöÅÄÖ)";

	String attatchment1 = "src/test/resources/testfiles/mail/soi-toolkit-home.pdf";
	String attatchment2 = "src/test/resources/testfiles/mail/soi-toolkit-home.png";
	
	boolean debug       = false;

	int waitForMailToBeDeliveredMs = 10000;
	
	@Before
	public void setup() {
		// Read all messages to ensure that the inbox is empty...
//		boolean deleteReadMessages = true;
//		int size = receiveMessagesUsingImap(imapHost, imapUsername, imapPassword, deleteReadMessages, debug).size();
//		log.info("Removed {} mails from {} during setup of this test", size, imapUsername);
	}
	
	@Test 
	public void dummyTest() throws Exception {
	}
	
//	@Test 
	public void sendAndReceiveMessageWithoutAttachmentsTest() throws Exception {

		log.info("SMTP without attachements...");
		sendSmtpSslMessage(smtpHost, smtpSslPort, smtpUsername, smtpPassword, from, to, subject, text, null, debug);
//		sendSmtpMessageNoAuth(smtpHost, smtpPort, from, to, subject, text, null, debug);

		waitForMailToBeDelivered();

		log.info("IMAP...");
		boolean deleteReadMessages = true;
		List<MailMessage> messages = receiveMessagesUsingImap(imapHost, imapUsername, imapPassword, deleteReadMessages, debug);

		assertEquals(1, messages.size());
		
		MailMessage m = messages.get(0);
		assertEquals(from,    m.getFrom());
		assertEquals(to,      m.getTo().get(0));
		assertEquals(subject, m.getSubject());
		
		assertEquals(text + "\r\n", m.getText());
		assertEquals(0,       m.getAttachments().size());
	}

//	@Test 
	public void sendAndReceiveMessageWithAttachmentsTest() throws Exception {

		log.info("SMTP with attachment...");
		List<String> attachements = new ArrayList<String>();
		attachements.add(attatchment1);
		attachements.add(attatchment2);
		sendSmtpSslMessage(smtpHost, smtpSslPort, smtpUsername, smtpPassword, from, to, subject, text, attachements, debug);
//		sendSmtpMessageNoAuth(smtpHost, smtpPort, from, to, subject, text, attachements, debug);
		
		waitForMailToBeDelivered();
		
		log.info("IMAP...");
		boolean deleteReadMessages = true;
		List<MailMessage> messages = receiveMessagesUsingImap(imapHost, imapUsername, imapPassword, deleteReadMessages, debug);

		assertEquals(1, messages.size());
		
		MailMessage m = messages.get(0);
		assertEquals(from,    m.getFrom());
		assertEquals(to,      m.getTo().get(0));
		assertEquals(subject, m.getSubject());
		assertEquals(text,    m.getText());

		assertEquals(2,       m.getAttachments().size());
		assertEquals("soi-toolkit-home.pdf", m.getAttachments().get(0).getFilename());
		assertEquals("soi-toolkit-home.png", m.getAttachments().get(1).getFilename());

		assertArrayEquals(m.getAttachments().get(0).getContent(), MailUtil.copy(new FileInputStream(attatchment1)));
		assertArrayEquals(m.getAttachments().get(1).getContent(), MailUtil.copy(new FileInputStream(attatchment2)));
	}

	private void waitForMailToBeDelivered() {
		log.info("Wait for a while for the mail to be delivered (" + waitForMailToBeDeliveredMs + ")...");
		try {
			Thread.sleep(waitForMailToBeDeliveredMs);
		} catch (InterruptedException e) {
		}
	}
	
	
}
