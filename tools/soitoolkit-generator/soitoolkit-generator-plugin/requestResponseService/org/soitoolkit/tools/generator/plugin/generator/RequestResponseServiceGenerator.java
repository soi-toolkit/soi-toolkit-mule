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

import static org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum.JMS;
import static org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum.SOAP;
import static org.soitoolkit.tools.generator.plugin.util.PropertyFileUtil.openPropertyFileForAppend;
import static org.soitoolkit.tools.generator.plugin.util.XmlUtil.appendXmlFragment;
import static org.soitoolkit.tools.generator.plugin.util.XmlUtil.createDocument;
import static org.soitoolkit.tools.generator.plugin.util.XmlUtil.getXPathResult;
import static org.soitoolkit.tools.generator.plugin.util.XmlUtil.getXml;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.soitoolkit.tools.generator.plugin.model.enums.TransformerEnum;
import org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RequestResponseServiceGenerator implements Generator {

	GeneratorUtil gu;
	
	public RequestResponseServiceGenerator(PrintStream ps, String groupId, String artifactId, String serviceName, TransportEnum inboundTransport, TransportEnum outboundTransport, TransformerEnum transformerType, String folderName) {
		gu = new GeneratorUtil(ps, groupId, artifactId, null, serviceName, null, inboundTransport, outboundTransport, transformerType, "/requestResponseService", folderName);
	}
		
    public void startGenerator() {

    	System.err.println("### A BRAND NEW REQUEST-RESPONSE-SERVICE IS ON ITS WAY..., INB: " + gu.getModel().getInboundTransport() + ", OUTB: " + gu.getModel().getOutboundTransport() + ", TRANSFORMER: " + gu.getModel().getTransformerType());
		TransportEnum inboundTransport  = TransportEnum.valueOf(gu.getModel().getInboundTransport());
		TransportEnum outboundTransport = TransportEnum.valueOf(gu.getModel().getOutboundTransport());
		TransformerEnum transformerType = TransformerEnum.valueOf(gu.getModel().getTransformerType());

    	gu.generateContentAndCreateFile("src/main/resources/services/__service__-service.xml.gt");
    	if (transformerType == TransformerEnum.SMOOKS) {
	    	gu.generateContentAndCreateFile("src/main/resources/transformers/__service__-request-transformer.xml.gt");
	    	gu.generateContentAndCreateFile("src/main/resources/transformers/__service__-response-transformer.xml.gt");
    	} else {
	    	gu.generateContentAndCreateFile("src/main/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__RequestTransformer.java.gt");
			gu.generateContentAndCreateFile("src/main/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__ResponseTransformer.java.gt");
    	}

		gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__-request-input.xml.gt");
		gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__-request-expected-result.csv.gt");
		gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__-response-input.csv.gt");
		gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__-response-expected-result.xml.gt");
		gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__-fault-response-input.csv.gt");
		gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__-fault-response-expected-result.xml.gt");
		gu.generateContentAndCreateFile("src/test/resources/teststub-services/__service__-teststub-service.xml.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__IntegrationTest.java.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__RequestTransformerTest.java.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__ResponseTransformerTest.java.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__TestConsumer.java.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__TestProducer.java.gt");
		
		updatePropertyFiles(inboundTransport, outboundTransport);
		
		String file = gu.getOutputFolder() + "/pom.xml";
		String xmlFragment = 
			"\n" +
			"\n" +
			"		<dependency xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
			"			<groupId>org.soitoolkit.refapps.sd</groupId>\n" +
			"			<artifactId>soitoolkit-refapps-sample-schemas</artifactId>\n" +
			"			<version>${soitoolkit.version}</version>\n" +
			"		</dependency>\n";
		addDependency(file, xmlFragment, "soitoolkit-refapps-sample-schemas");


		xmlFragment =
			"		<dependency xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
			"			<groupId>org.mule.modules.mule-module-smooks</groupId>\n" +
			"			<artifactId>smooks-4-mule-2</artifactId>\n" +
			"			<version>1.2</version>\n" +
			"		</dependency>\n";
		addDependency(file, xmlFragment, "smooks-4-mule-2");

		xmlFragment =
			"		<dependency xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
			"			<groupId>org.milyn</groupId>\n" +
			"			<artifactId>milyn-smooks-templating</artifactId>\n" +
			"			<version>1.3.1</version>\n" +
			"		</dependency>\n";
		addDependency(file, xmlFragment, "milyn-smooks-templating");

		xmlFragment =
			"		<dependency xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
			"			<groupId>org.milyn</groupId>\n" +
			"			<artifactId>milyn-smooks-csv</artifactId>\n" +
			"			<version>1.3.1</version>\n" +
			"		</dependency>\n";
		addDependency(file, xmlFragment, "milyn-smooks-csv");
    }

	private void updatePropertyFiles(TransportEnum inboundTransport, TransportEnum outboundTransport) {
		
		PrintWriter cfg = null;
		PrintWriter sec = null;
		try {
			cfg = openPropertyFileForAppend(gu.getOutputFolder(), gu.getModel().getConfigPropertyFile());
			sec = openPropertyFileForAppend(gu.getOutputFolder(), gu.getModel().getSecurityPropertyFile());

			String service        = gu.getModel().getUppercaseService();
		    String serviceName    = gu.getModel().getLowercaseService();

			// Print header for this service's properties
		    cfg.println("");
		    cfg.println("# Properties for service \"" + gu.getModel().getService() + "\"");
		    cfg.println("# TODO: Update to reflect your settings");

		    if (inboundTransport == SOAP) {
			    cfg.println(service + "_INBOUND_URI=" + serviceName + "/v1");
		    }
		    
		    if (outboundTransport == JMS) {
			    cfg.println(service + "_REQUEST_QUEUE="  + gu.getModel().getJmsRequestQueue());
			    cfg.println(service + "_RESPONSE_QUEUE=" + gu.getModel().getJmsResponseQueue());
		    }
		    
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (cfg != null) {cfg.close();}
			if (sec != null) {sec.close();}
		}
	}
	
	private void addDependency(String file, String xmlFragment, String artifactId) {
		InputStream content = null;
		String xml = null;
		try {
			
//		    System.err.println("### ADD: " + xmlFragment + " to " + file);
			content = new FileInputStream(file);
			
			Document doc = createDocument(content);

			Map<String, String> namespaceMap = new HashMap<String, String>();
			namespaceMap.put("ns", "http://maven.apache.org/POM/4.0.0");

			// First verify that the dependency does not exist already
			NodeList testList = getXPathResult(doc, namespaceMap, "/ns:project/ns:dependencies/ns:dependency/ns:artifactId[.='" + artifactId + "']");
			if (testList.getLength() > 0) {
				// TODO: Replace with sl4j!
				System.err.println("### Fragment already exists, bail out!!!");
				return;
			}
			
			NodeList rootList = getXPathResult(doc, namespaceMap, "/ns:project/ns:dependencies");
			Node root = rootList.item(0);
//		    System.err.println("### ROOT NODE: " + ((root == null) ? " NULL" : root.getLocalName()));		    
		    
		    appendXmlFragment(root, xmlFragment);
			
		    xml = getXml(doc);
			System.err.println(xml);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (content != null) {try {content.close();} catch (IOException e) {}}
		}

		PrintWriter pw = null;
		try {
			pw = openFileForOverwrite(file);
			pw.print(xml);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (pw != null) {pw.close();}
		}
	
//	    System.err.println("### UPDATED: " + file);
	}

	private PrintWriter openFileForOverwrite(String filename) throws IOException {

		// TODO: Replace with sl4j!
		System.err.println("Overwrite file: " + filename);

	    return new PrintWriter(new BufferedWriter(new FileWriter(filename, false)));
	}
}
