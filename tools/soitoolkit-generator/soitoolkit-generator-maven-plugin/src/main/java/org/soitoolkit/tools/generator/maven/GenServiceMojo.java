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
package org.soitoolkit.tools.generator.maven;

import static org.soitoolkit.commons.xml.XPathUtil.createDocument;
import static org.soitoolkit.commons.xml.XPathUtil.getDocumentComment;
import static org.soitoolkit.commons.xml.XPathUtil.lookupParameterValue;
import static org.soitoolkit.tools.generator.Generator.GEN_METADATA_TYPE_KEY;
import static org.soitoolkit.tools.generator.Generator.GEN_METADATA_TYPE_SERVICE;
import static org.soitoolkit.tools.generator.util.PomUtil.extractGroupIdAndArtifactIdFromPom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.soitoolkit.tools.generator.Generator;
import org.soitoolkit.tools.generator.OnewayServiceGenerator;
import org.soitoolkit.tools.generator.RequestResponseServiceGenerator;
import org.soitoolkit.tools.generator.model.IModel;
import org.soitoolkit.tools.generator.model.enums.MepEnum;
import org.soitoolkit.tools.generator.model.enums.MuleVersionEnum;
import org.soitoolkit.tools.generator.model.enums.TransformerEnum;
import org.soitoolkit.tools.generator.model.enums.TransportEnum;
import org.w3c.dom.Document;

/**
 * Goal for creating an new Integration Component
 *
 * @goal genService
 * @requiresProject false
 * 
 * @author Magnus Larsson
 */
public class GenServiceMojo extends AbstractMojo {

	/**
     * Service.
     * @parameter expression="${service}" default-value="sample"
     * @required
     */
    private String service;

	/**
     * MessageExchangePattern.
     * @parameter expression="${messageExchangePattern}" default-value="Request/Response"
     * @required
     */
    private String messageExchangePattern;

	/**
     * InboundTransport.
     * @parameter expression="${inboundTransport}" default-value="SOAPHTTP"
     * @required
     */
    private String inboundTransport;

	/**
     * InboundTransport.
     * @parameter expression="${outboundTransport}" default-value="SOAPHTTP"
     * @required
     */
    private String outboundTransport;

	/**
     * Location of the output folder.
     * @parameter expression="${outDir}" default-value="."
     * @required
     */
    private File outDir;

    private static String[] allowedRequestReplyInboundTransport  = new String[] {"SOAPHTTP","SOAPSERVLET"};
    private static String[] allowedRequestReplyOutboundTransport = new String[] {"SOAPHTTP","RESTHTTP","JMS"};
    private static String[] allowedOneWayInboundTransport        = new String[] {"VM", "JMS", "JDBC", "FILE", "FTP", "SFTP", "HTTP", "SERVLET", "POP3", "IMAP"};
    private static String[] allowedOneWayOutboundTransport       = new String[] {"VM", "JMS", "JDBC", "FILE", "FTP", "SFTP", "SMTP"};
    
    public void execute() throws MojoExecutionException {
    	
        getLog().info("");
        getLog().info("==========================");
        getLog().info("= Creating a new Service =");
        getLog().info("==========================");
        getLog().info("");
        getLog().info("ARGUMENTS:");
        getLog().info("(change an arg by suppling: -Darg=value):");
        getLog().info("");
        getLog().info("outDir=" + outDir.getPath());
//        getLog().info("outDir=" + outDir.getAbsolutePath());
//        try {
//			getLog().info("outDir=" + outDir.getCanonicalPath());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
        getLog().info("service=" + service);
        getLog().info("messageExchangePattern=" + messageExchangePattern);
        getLog().info("inboundTransport=" + inboundTransport);
        getLog().info("outboundTransport=" + outboundTransport);
        getLog().info("");

        String pomFile = outDir.getPath() + "/pom.xml";

        IModel m = initPomInfo(pomFile);

        String artifactId = m.getArtifactId();
        String groupId = m.getGroupId();

        getLog().info("EXTRACTED POM-INFO:");
        getLog().info("(from " + pomFile + ")");
        getLog().info("artifactId=" + artifactId);
        getLog().info("groupId=" + groupId);
        getLog().info("");

        MepEnum mepEnum = initMep(messageExchangePattern);
        TransportEnum inboundTransportEnum  = initInboundTransport (mepEnum, inboundTransport);
        TransportEnum outboundTransportEnum = initOutboundTransport(mepEnum, outboundTransport);

        Generator g = null;
        switch (mepEnum) {
		case MEP_REQUEST_RESPONSE:
			g = new RequestResponseServiceGenerator(System.out, groupId, artifactId, service, inboundTransportEnum, outboundTransportEnum, TransformerEnum.JAVA, outDir.getPath());
			break;

		case MEP_ONE_WAY:
			g = new OnewayServiceGenerator(System.out, groupId, artifactId, service, inboundTransportEnum, outboundTransportEnum, TransformerEnum.JAVA, outDir.getPath());
			break;

		case MEP_PUBLISH_SUBSCRIBE:
			throw new MojoExecutionException("Message Exchange Pattern [" + messageExchangePattern + "] not yet supported");

		default:
			break;
		}
		g.startGenerator();
    }

	private IModel initPomInfo(String pomFile) throws MojoExecutionException {
		IModel m = null;
		try {
			InputStream pomIs = new FileInputStream(pomFile);
			m = extractGroupIdAndArtifactIdFromPom(pomIs);

			// Validate the presence of GENERATOR_TYPE_SERVICE in pom.xml
			Document doc = createDocument(new FileInputStream(pomFile));
			String docComment = getDocumentComment(doc);
			String type = lookupParameterValue(GEN_METADATA_TYPE_KEY, docComment);
			
			if (!type.equals(GEN_METADATA_TYPE_SERVICE)) {
				throw new MojoExecutionException("Invalid type of maven pom.xml-file (" + pomFile + "), missing the metadata: " +  GEN_METADATA_TYPE_KEY + "=" + GEN_METADATA_TYPE_SERVICE + ", found unexpected metadata: " +  GEN_METADATA_TYPE_KEY + "=" + type);
			}
			
		} catch (FileNotFoundException e) {
			throw new MojoExecutionException("Maven pom.xml-file not found at: " + pomFile, e);
		}
		return m;
	}

	private MepEnum initMep(String mep) throws MojoExecutionException {
		MepEnum mepEnum = null;
		try {
			mepEnum = MepEnum.getByLabel(mep);
		} catch (Exception e) {
			throw new MojoExecutionException("Invalid Message Exchange Pattern: " + mep + ", allowed values: " + MepEnum.allowedLabelValues(), e);
		}
		if (mepEnum == null) {
			throw new MojoExecutionException("Invalid Message Exchange Pattern: " + mep + ", allowed values: " + MepEnum.allowedLabelValues());
		}
		return mepEnum;
	}

	private TransportEnum initInboundTransport(MepEnum mepEnum, String inboundTransport2) throws MojoExecutionException {
		return getTransport(inboundTransport, "inbound", (mepEnum == MepEnum.MEP_ONE_WAY) ? allowedOneWayInboundTransport : allowedRequestReplyInboundTransport);
	}
	
	private TransportEnum initOutboundTransport(MepEnum mepEnum, String outboundTransport) throws MojoExecutionException {
		return getTransport(outboundTransport, "outbound", (mepEnum == MepEnum.MEP_ONE_WAY) ? allowedOneWayOutboundTransport : allowedRequestReplyOutboundTransport);
	}

	private TransportEnum getTransport(String transport, String direction, String[] allowedTransports) throws MojoExecutionException {
		TransportEnum transportEnum = null;

		for (String allowedTransport : allowedTransports) {
			if (allowedTransport.equals(transport)) {
				transportEnum = TransportEnum.valueOf(transport);
			}
		}
		if (transportEnum == null) {
			throw new MojoExecutionException("Invalid " + direction + " transport: " + transport + ", allowed values: " + getAllowedTransports(allowedTransports));
		}
		return transportEnum;
	}
	
	private String getAllowedTransports(String[] allowedTransports) {
		String allowedTransportsStr = "";
		for (String allowedTransport : allowedTransports) {
			allowedTransportsStr += allowedTransport + " ";
		}
	    return allowedTransportsStr;
	}
	
}
