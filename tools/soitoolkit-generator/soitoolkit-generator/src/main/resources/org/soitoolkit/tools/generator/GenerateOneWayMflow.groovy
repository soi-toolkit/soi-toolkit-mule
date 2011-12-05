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
package org.soitoolkit.tools.generator.util

import org.soitoolkit.tools.generator.FileGenerator
import org.soitoolkit.tools.generator.model.IModel
import java.util.UUID

public class GenerateMflow implements FileGenerator {
	public String generateFileContent(IModel model) {
		def inTr   = model.getInboundTransport()
		def outTr  = model.getOutboundTransport()

    	def writer = new StringWriter()
    	def xml = new groovy.xml.MarkupBuilder(writer)

    	xml.'mule-configuration'(name:model.service, xmlns:'http://www.mulesoft.com/tooling/messageflow') {
      		flow(name:getFlowName(model)) {
      			lane('entity-id':uuid()) {
					endpoint(name:getInboundEndpointName(model), 'message-exchange-pattern':"OneWay", direction:"Inbound", type:getInboundEndpointType(model), 'entity-id':uuid()) {
      					properties {
      						property(name:getAddressAttribute('in', model.getInboundTransport()),  value:getInboundEndpointAddress(model))
      						property(name:"exchange-pattern", value:"one-way")
      						property(name:"transformer-refs", value:getInboundTransformerRefs(model))
//JMS SPECIFIC!!!
//      						property(name:"disableTransportTransformer",         value:"false")
//      						property(name:"disableTemporaryReplyToDestinations", value:"false")

//      						property(name:"endpoint.connector.ref",    value:getInboundConnectorRef(model))
//      						property(name:"endpoint.address",      value:getInboundEndpointAddress(model))

							if (inTr == "FILE") {
	      						property(name:"pollingFrequency", value:"${dollarSymbol()}{${model.getUppercaseService()}_INBOUND_POLLING_MS}")
	      						property(name:"fileAge",          value:"${dollarSymbol()}{${model.getUppercaseService()}_INBOUND_FILE_AGE_MS}")
	      						property(name:"moveToDirectory",  value:"${dollarSymbol()}{${model.getUppercaseService()}_ARCHIVE_FOLDER}")
	      						property(name:"moveToPattern",    value:"#[header:originalFilename]")
	      					}
	      				}
      				}
      				pattern (name:"transform-message", type:"org.mule.tooling.ui.modules.core.pattern.customTransformer", 'entity-id':uuid()) {
      					properties {
      						property(name:"custom.transformer.classname", value:getTransformerClass(model))
      					}
      					description('Transformer that delegates to a Java class.')
      				}
					endpoint(name:getOutboundEndpointName(model), 'message-exchange-pattern':"OneWay", direction:"Outbound", type:getOutboundEndpointType(model), 'entity-id':uuid()) {
      					properties {
      						property(name:getAddressAttribute('out', model.getOutboundTransport()),  value:getOutboundEndpointAddress(model))
      						property(name:"exchange-pattern",                                 value:"one-way")
      						property(name:"transformer-refs",                                value:getOutboundTransformerRefs(model))
//      						property(name:"endpoint.connector.ref",    value:getOutboundConnectorRef(model))
//      						property(name:"endpoint.address",      value:getOutboundEndpointAddress(model))

							if (outTr == "FILE") {
								if (model.isInboundEndpointFilebased()) {
		      						property(name:"outputPattern", value:"#[header:originalFilename]")
		      					} else {
		      						property(name:"outputPattern", value:"${dollarSymbol()}{${model.getUppercaseService()}_OUTBOUND_FILE}")
		      					}
		      				}
      					}
      				}
      			}
      		}
    	}

    	writer.toString()
	}
	
	private String dollarSymbol() {
		"\$"
	}

	private String uuid() {
		UUID.randomUUID().toString()
	}

	private String getFlowName(IModel model) {
		model.getService() + '-service'
	}

	private String getInboundEndpointName(IModel model) {
		model.getInboundTransport() + '-IN'
	}
	
	private String getInboundEndpointType(IModel model) {
		getEndpointType(model.getInboundTransport())
	}

	private String getInboundEndpointAddress(IModel model) {
		String t = model.getInboundTransport()
		if (t == "VM") {
			"${dollarSymbol()}{${model.getUppercaseService()}_IN_VM_QUEUE}"
		} else if (t == "JMS") {
			"${dollarSymbol()}{${model.getUppercaseService()}_IN_QUEUE}"
		} else if (t == "JDBC") {
			"${dollarSymbol()}{${model.getLowercaseJavaService()}-export-query}"
		} else if (t == "FILE") {
			"${dollarSymbol()}{${model.getUppercaseService()}_INBOUND_FOLDER}"
		} else if (t == "FTP") {
			"${dollarSymbol()}{${model.getUppercaseService()}_INBOUND_FOLDER}"
		} else if (t == "SFTP") {
			"${dollarSymbol()}{${model.getUppercaseService()}_INBOUND_SFTP_FOLDER}"
		} else if (t == "HTTP") {
			"${dollarSymbol()}{${model.getUppercaseService()}_INBOUND_URL}"
		} else if (t == "SERVLET") {
			"${dollarSymbol()}{${model.getUppercaseService()}_INBOUND_URL}"
		} else {
			"UNKNOWN-ENDPOINT-TYPE"
		}
	}

	private String getInboundTransformerRefs(IModel model) {
		String t = model.getInboundTransport()
		
		if (t == "JMS") {
			"logMsgIn jmsToStr"
		} else if (t == "SERVLET" || t == "HTTP") {
			"createCorrId logMsgIn mimeToStr"
		} else if (t == "FILE" || t == "FTP" || t == "SFTP") {
			"objToStr logMsgIn"
		} else {
			 "logMsgIn"
		}
	}

	private String getOutboundTransformerRefs(IModel model) {
		String t = model.getOutboundTransport()
		
		if (t == "JMS") {
			"strToJms logMsgOut"
		} else {
			 "logMsgOut"
		}
	}

	private String getTransformerClass(IModel model) {
		"${model.getJavaPackage()}.${model.getLowercaseJavaService()}.${model.getCapitalizedJavaService()}Transformer"
	}
	private String getOutboundEndpointName(IModel model) {
		model.getOutboundTransport() + '-OUT'
	}

	private String getOutboundEndpointType(IModel model) {
		getEndpointType(model.getInboundTransport())
	}
	
	private String getOutboundEndpointAddress(IModel model) {
		String t = model.getInboundTransport()
		if (t == "VM") {
			"${dollarSymbol()}{${model.getUppercaseService()}_OUT_VM_QUEUE}"
		} else if (t == "JMS") {
			"${dollarSymbol()}{${model.getUppercaseService()}_OUT_QUEUE}"
		} else if (t == "JDBC") {
			"${dollarSymbol()}{${model.getLowercaseJavaService()}-import-query}"
		} else if (t == "FILE") {
			"${dollarSymbol()}{${model.getUppercaseService()}_OUTBOUND_FOLDER}"
		} else if (t == "FTP") {
			"${dollarSymbol()}{${model.getUppercaseService()}_OUTBOUND_FOLDER}"
		} else if (t == "SFTP") {
			"${dollarSymbol()}{${model.getService()}-service}"
		} else if (t == "SMTP") {
			"${dollarSymbol()}{${model.getUppercaseService()}_OUT_XXX}"
		} else {
			"UNKNOWN-ENDPOINT-TYPE"
		}
	}
	
    private String getAddressAttribute(String direction, String t) {
		if (t == "VM") {
			"path"
		} else if (t == "JMS") {
			"queue"
		} else if (t == "JDBC") {
			"queryKey"
		} else if (t == "FILE") {
			"path"
		} else if (t == "FTP") {
			"address"
		} else if (t == "SFTP") {
			(direction == 'in') ? "address" : "path"
		} else if (t == "HTTP") {
			"address"
		} else if (t == "SERVLET") {
			"address"
		} else if (t == "SMTP") {
			"address"
		} else {
			"UNKNOWN-ADDRESS_ATTRIBUTE"
		}
	}

	private String getEndpointType(String t) {
		if (t == "VM") {
			"http://www.mulesoft.org/schema/mule/vm/endpoint"
		} else if (t == "JMS") {
			"http://www.mulesoft.org/schema/mule/jms/endpoint"
		} else if (t == "JDBC") {
			"http://www.mulesoft.org/schema/mule/jdbc/endpoint"
		} else if (t == "FILE") {
			"http://www.mulesoft.org/schema/mule/file/endpoint"
		} else if (t == "FTP") {
			"http://www.mulesoft.org/schema/mule/ftp/endpoint"
		} else if (t == "SFTP") {
			"http://www.mulesoft.org/schema/mule/sftp/endpoint"
		} else if (t == "HTTP") {
			"http://www.mulesoft.org/schema/mule/http/endpoint"
		} else if (t == "SERVLET") {
			"http://www.mulesoft.org/schema/mule/servlet/endpoint"
		} else if (t == "SMTP") {
			"http://www.mulesoft.org/schema/mule/smtp/endpoint"
		} else {
			"UNKNOWN-ENDPOINT-TYPE"
		}
	}
	
/*
	private String getInboundConnectorRef(IModel model) {
		getConnectorRef(model.getInboundTransport())
	}

	private String getOutboundConnectorRef(IModel model) {
		getConnectorRef(model.getOutboundTransport())
	}

	private String getConnectorRef(String t) {
		if (t == "JMS") {
			"soitoolkit-jms-connector"
		} else {
			""
		}
	}
*/
}