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

import static javax.mail.Message.RecipientType.BCC;
import static javax.mail.Message.RecipientType.CC;
import static javax.mail.Message.RecipientType.TO;
import static org.soitoolkit.commons.mule.mail.MailUtil.copy;
import static org.soitoolkit.commons.mule.mail.MailUtil.fillAddresses;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;

/**
 * Helper class for working with javax.mail.Message instances.
 * 
 * @author Magnus Larsson
 *
 */
public class MailMessage {

	private String from;
	private String subject;
	private String text;
	private List<String> to  = new ArrayList<String>();
	private List<String> cc  = new ArrayList<String>();
	private List<String> bcc = new ArrayList<String>();
	private List<Attachment> attachments = new ArrayList<Attachment>();
	
	public MailMessage(Message mail) {
		try {
			from    = mail.getFrom()[0].toString();
			subject = mail.getSubject();
			text    = MailUtil.getText(mail);	

			fillAddresses(mail, TO,  to);
			fillAddresses(mail, CC,  cc);
			fillAddresses(mail, BCC, bcc);
			fillAttachments(mail, attachments);
			
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void fillAttachments(Part part, List<Attachment> attachments) throws IOException, MessagingException {

		if (part.isMimeType("multipart/*")) {
			Multipart multipart = (Multipart) part.getContent();
			for (int i = 0, n = multipart.getCount(); i < n; i++) {
				Part bodyPart = multipart.getBodyPart(i);
				String disposition = bodyPart.getDisposition();
				if ((disposition != null) && ((disposition.equals(Part.ATTACHMENT) || (disposition.equals(Part.INLINE))))) {
					System.err.println("### Writing attachement to byte array: " + bodyPart.getFileName() + ", contentType: " + part.getContentType() + ", class: " + part.getContent().getClass().getName());
					Attachment attachment = new Attachment(bodyPart.getFileName(), copy(bodyPart.getInputStream()));
					attachments.add(attachment);
				}
				
				if (disposition == null) { 
					// Check if plain 
					MimeBodyPart mbp = (MimeBodyPart)bodyPart; 
					if (mbp.isMimeType("text/plain")) {
						// Handle plain 
						System.err.println("### FOUND TEXT-PART IN PART WITH UNKNOWN DISPOSITION");
					} else {
						// Special non-attachment cases here of content-type image/gif, text/html, ...
						System.err.println("### FOUND NON-TEXT-PART IN PART WITH UNKNOWN DISPOSITION");
					} 
				}				
			}
		}
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public List<String> getTo() {
		return to;
	}

	public List<String> getCc() {
		return cc;
	}

	public List<String> getBcc() {
		return bcc;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}


	
}
