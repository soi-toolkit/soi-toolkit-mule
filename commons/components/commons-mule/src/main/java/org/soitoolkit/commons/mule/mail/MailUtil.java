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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various helper methods used for mail related processing
 * 
 * @author Magnus Larsson
 *
 */
public class MailUtil {

	private final static Logger logger = LoggerFactory.getLogger(MailUtil.class);

	/**
     * Hidden constructor.
     */
    private MailUtil() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }
 
	public static List<MailMessage> receiveMessagesUsingImap (
		String host,
		String username,
		String password,
		boolean deleteReadMessages,
		boolean debug) {

		return receiveMessages("imap", host, username, password, deleteReadMessages, debug);
	}

	public static List<MailMessage> receiveMessages (
		String protocol,
		String host,
		String username,
		String password,
		boolean deleteReadMessages,
		boolean debug) {
		
		try {
			// Create empty properties 
			Properties props = new Properties();
			props.put("mail.debug", (debug) ? "true" : "false");  
			
			// Get session 
			Session session = Session.getDefaultInstance(props, new UsrPwdAuthenticator(username, password));
			session.setDebug(debug);  
			
			// Get the store 
			Store store = session.getStore(protocol); 
			store.connect(host, username, password);
			
			// Get folder 
			Folder folder = store.getFolder("INBOX"); 
			folder.open(deleteReadMessages? Folder.READ_WRITE : Folder.READ_ONLY);
			
			// Get messages 
			Message[] messages = folder.getMessages();

			// Extract message info and mark messages for delete if requested
			List<MailMessage> msgs = new ArrayList<MailMessage>();
			for (int i=0, n=messages.length; i<n; i++) { 
				msgs.add(new MailMessage(messages[i]));
				if (deleteReadMessages) {
					messages[i].setFlag(Flags.Flag.DELETED, true);
				}
			}

			// Close connection 
			folder.close(deleteReadMessages); 
			store.close();
			
			return msgs;
		} catch (NoSuchProviderException e) {
			throw new RuntimeException(e);
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}		
	}
	
	public static void sendSmtpSslMessage(  
		String smtpHost,
		String smtpSslPort,
		String username,
		String password,
		String from,
		String to,
		String subject,
		String content,
		List<String> filenames,
		boolean debug
	) {

		try {
			logger.info("Send mail to {}, content: {}.", to, content);
			
			Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());  

			Properties props = new Properties();  
			props.put("mail.transport.protocol", "smtp");  
			props.put("mail.host", smtpHost);  
			props.put("mail.smtp.auth", "true");  
			props.put("mail.smtp.port", smtpSslPort);  
			props.put("mail.smtp.socketFactory.port", smtpSslPort);  
			props.put("mail.smtp.socketFactory.class",  "javax.net.ssl.SSLSocketFactory");  
			props.put("mail.smtp.socketFactory.fallback", "false");  
			props.put("mail.debug", debug? "true" : "false");  
			
			Session session = Session.getDefaultInstance(props, new UsrPwdAuthenticator(username, password)); 
			session.setDebug(debug);  
			  
			sendMessage(from, to, subject, content, filenames, session);
		} catch (AddressException e) {
			throw new RuntimeException(e);
		} catch (NoSuchProviderException e) {
			throw new RuntimeException(e);
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}  
		logger.info("Mail sent");

	}

	public static void sendSmtpMessageNoAuth(  
			String smtpHost,
			String smtpPort,
			String from,
			String to,
			String subject,
			String content,
			List<String> filenames,
			boolean debug
		) {

			try {
				logger.info("Send mail to {}, content: {}.", to, content);
				
				Properties props = new Properties();  
				props.put("mail.transport.protocol", "smtp");  
				props.put("mail.host", smtpHost);  
				props.put("mail.smtp.auth", "false");  
				props.put("mail.smtp.port", smtpPort);  
				props.put("mail.debug", debug? "true" : "false");  
				
				Session session = Session.getDefaultInstance(props); 
				session.setDebug(debug);  
				  
				sendMessage(from, to, subject, content, filenames, session);
			} catch (AddressException e) {
				throw new RuntimeException(e);
			} catch (NoSuchProviderException e) {
				throw new RuntimeException(e);
			} catch (MessagingException e) {
				throw new RuntimeException(e);
			}  
			logger.info("Mail sent");

		}

	private static void sendMessage(String from, String to, String subject,
			String content, List<String> filenames, Session session)
			throws MessagingException, AddressException,
			NoSuchProviderException {
		MimeMessage message = new MimeMessage(session);  
		message.setSender(new InternetAddress(from));  
		message.setSubject(subject, "UTF-8");  
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));  

		if (filenames == null || filenames.size() == 0) {
			message.setContent(content, "text/plain; charset=UTF-8");			
			
		} else {
			// Setup a multipart message 
			Multipart multipart = new MimeMultipart(); 
			message.setContent(multipart);

			// Create the message part 
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(content, "text/plain; charset=UTF-8");			
			multipart.addBodyPart(messageBodyPart);
			
			// Add attachments, if any 
			if (filenames != null) {
				for (String filename : filenames) {
					BodyPart attatchmentBodyPart = new MimeBodyPart(); 
					DataSource source = new FileDataSource(filename); 
					attatchmentBodyPart.setDataHandler(new DataHandler(source)); 
					// Only use the final part fo the filename in the mail, i.e. not the path of folders if any 
					File f = new File(filename);
					attatchmentBodyPart.setFileName(f.getName()); 
					multipart.addBodyPart(attatchmentBodyPart);
				}
			}
		}
		
		message.saveChanges(); 
		   
		Transport transport = session.getTransport();  
		try {
			transport.connect();  
			transport.sendMessage(message, message.getAllRecipients());  
		} finally {
			transport.close();
		}
	}  	
    
    public static String getText(Part part) throws ParseException, MessagingException, IOException {

		ContentType contentType = new ContentType(part.getContentType());
		System.err.println("contentType: " + part.getContentType() + ", class: " + part.getContent().getClass().getName());
		
		if (part.isMimeType("text/*")) {
			String charset = contentType.getParameter("charset");
			System.err.println("Charset: " + charset);

			return (String)part.getContent();

	    } else if (part.isMimeType("multipart/*")) {
	        Multipart mp = (Multipart)part.getContent();
	        for (int i = 0; i < mp.getCount(); i++) {
                String text = getText(mp.getBodyPart(i));

                if (text != null) {
                	return text;
                }
	        }
	    }
		return null;
	}

	public static void fillAddresses(Message mail, RecipientType addrType, List<String> addrList) throws MessagingException {
		Address[] receipients = mail.getRecipients(addrType);
		
		if (receipients == null) return;
		
		for (Address address : receipients) {
			addrList.add(address.toString());
		}
	}

	/*
	 * TODO: Methods below should be moved to som io-util class!
	 */

	public static void saveFile(String filename, byte[] content) throws IOException {
		saveFile(filename, new ByteArrayInputStream(content));
	}

	public static void saveFile(String filename, InputStream inputStream) throws IOException {
		File file = createFileWithUniqueName(filename);
		copy(inputStream, file);
		System.err.println("### Wrote attachement to file: " + file.getAbsolutePath());
	}

	
	public static File createFileWithUniqueName(String orgFilename) throws IOException	{

		File file = new File(orgFilename);

		// Bail out if no conflict, the a file with this filename doesn't exist already
		if (!file.exists()) return file;

		// Find out a unique name...
		int fileTypeIdx = orgFilename.lastIndexOf('.');
		String filename = orgFilename;
		String fileType = "";
		if (fileTypeIdx >= 0) {
			fileType = filename.substring(fileTypeIdx); // Let the fileType include the leading '.'
			filename = filename.substring(0, fileTypeIdx);
		}
		for (int i=0; file.exists(); i++) {

			file = new File(filename + '-' + i + fileType);
		}
		
		return file;
	}	
	
	public static byte[] copy(InputStream src) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		copy(src, baos);
		return baos.toByteArray();
	}	

	public static void copy(InputStream src, File dst) throws IOException {
		copy(src, new FileOutputStream(dst));
	}	
	
	public static void copy(InputStream src, OutputStream dst) throws IOException {

	    try {
			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = src.read(buf)) > 0) {
			    dst.write(buf, 0, len);
			}
		} finally {
		    if (src != null) {
		    	src.close();
		    }
		    if (dst != null) {
			    dst.flush();
			    dst.close();
		    }
		}
	}
}
