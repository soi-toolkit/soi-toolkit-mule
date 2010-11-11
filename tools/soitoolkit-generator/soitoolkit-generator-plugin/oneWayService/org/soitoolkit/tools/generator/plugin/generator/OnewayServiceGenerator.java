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
package org.soitoolkit.tools.generator.plugin.generator;


import static org.soitoolkit.tools.generator.plugin.util.PropertyFileUtil.openPropertyFileForAppend;
import static org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum.JMS;
import static org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum.SFTP;
import static org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum.SERVLET;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum;
import org.soitoolkit.tools.generator.plugin.util.PreferencesUtil;

public class OnewayServiceGenerator implements Generator {

	GeneratorUtil gu;
	
	public OnewayServiceGenerator(PrintStream ps, String groupId, String artifactId, String serviceName, TransportEnum inboundTransport, TransportEnum outboundTransport, String folderName) {
		gu = new GeneratorUtil(ps, groupId, artifactId, null, serviceName, null, inboundTransport, outboundTransport, "/oneWayService", folderName);
	}
		
    public void startGenerator() {

    	System.err.println("### A BRAND NEW ONE-WAY-SERVICE IS ON ITS WAY..., INB: " + gu.getModel().getInboundTransport() + ", OUTB: " + gu.getModel().getOutboundTransport());
		TransportEnum inboundTransport  = TransportEnum.valueOf(gu.getModel().getInboundTransport());
		TransportEnum outboundTransport = TransportEnum.valueOf(gu.getModel().getOutboundTransport());

    	gu.generateContentAndCreateFile("src/main/resources/services/__service__-service.xml.gt");
		gu.generateContentAndCreateFile("src/main/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__Transformer.java.gt");

		gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__-input.txt.gt");
		gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__-expected-result.txt.gt");
		gu.generateContentAndCreateFile("src/test/resources/teststub-services/__service__-teststub-service.xml.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__TransformerTest.java.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__IntegrationTest.java.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__TestReceiver.java.gt");
		
	    // Servlet test consumer (performs a mime multipart hppt post)
	    if (inboundTransport == SERVLET) {
			gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__TestConsumer.java.gt");
	    }

		updatePropertyFile(inboundTransport, outboundTransport);
    }

	private void updatePropertyFile(TransportEnum inboundTransport, TransportEnum outboundTransport) {
		
		PrintWriter out = null;
		try {
			out = openPropertyFileForAppend(gu.getOutputFolder(), gu.getModel().getConfigPropertyFile());
			String service        = gu.getModel().getUppercaseService();
		    String serviceName    = gu.getModel().getLowercaseService();
			String sftpRootFolder = PreferencesUtil.getDefaultSftpRootFolder();
			
		    out.println("");
		    out.println("# Properties for service \"" + gu.getModel().getService() + "\"");
		    out.println("# TODO: Update to reflect your settings");

		    // JMS properties
		    if (inboundTransport == JMS) {
			    out.println(service + "_IN_QUEUE="  + gu.getModel().getJmsInQueue());
			    out.println(service + "_DL_QUEUE="  + gu.getModel().getJmsDLQueue());
		    }
		    if (outboundTransport == JMS) {
			    out.println(service + "_OUT_QUEUE=" + gu.getModel().getJmsOutQueue());
		    }

		    // Servlet properties
		    if (inboundTransport == SERVLET) {
			    out.println(service + "_INBOUND_SERVLET_URI=" + serviceName + "/inbound");
		    }
		    
		    // SFTP properties
		    if (inboundTransport == SFTP) {
				out.println(service + "_SENDER_SFTP_ADDRESS=" + sftpRootFolder + "/" + serviceName + "/sender");
			    out.println(service + "_SENDER_POLLING_MS=1000");
			    out.println(service + "_SENDER_SIZECHECK_MS=500");
		    }
		    if (outboundTransport == SFTP) {
			    out.println(service + "_RECEIVER_SFTP_ADDRESS=" + sftpRootFolder + "/" + serviceName + "/receiver");
			    out.println(service + "_ARCHIVE_RESEND_POLLING_MS=1000");
			    out.println(service + "_TESTSTUB_RECEIVER_POLLING_MS=1000");
			    out.println(service + "_TESTSTUB_RECEIVER_SIZECHECK_MS=500");

			    if (!gu.getModel().isInboundEndpointFilebased()) {
			    	// If we don't have a file based inbound endpoint (e.g. transport) we have to specify the name of the out-file ourself...
			    	out.println(service + "_RECEIVER_FILE=outfile.txt");
			    }
		    }

		    if (inboundTransport == SFTP || outboundTransport == SFTP) {
		    	out.println(service + "_ARCHIVE_FOLDER=/Users/magnuslarsson/archive/" + serviceName);
		    }
		    
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null) {out.close();}
		}
	}
}