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
    	def writer = new StringWriter()
    	def xml = new groovy.xml.MarkupBuilder(writer)

    	xml.'mule-configuration'(xmlns:'http://www.mulesoft.com/tooling/messageflow') {
      		flow('entity-id':uuid()) {
      			lane('entity-id':uuid()) {
					endpoint(name:getInboundEndpointName(model), 'message-exchange-pattern':"OneWay", direction:"Inbound", type:getInboundEndpointType(model), 'entity-id':uuid()) {
      					properties {
      						property(name:"endpoint.connector.ref",    value:getInboundConnectorRef(model))
//      						property(name:"endpoint.address",      value:getInboundEndpointAddress(model))
      						property(name:"endpoint.exchange.pattern", value:"")
      						property(name:getAddressAttribute(model.getInboundTransport()),  value:getInboundEndpointAddress(model))
      					}
      				}
      				pattern (name:"Java Transformer", type:"org.mule.tooling.ui.modules.core.pattern.customTransformer", 'entity-id':uuid()) {
      					properties {
      						property(name:"custom.transformer.classname", value:getTransformerClass(model))
      					}
      					description('Transformer that delegates to a Java class.')
      				}
					endpoint(name:getOutboundEndpointName(model), 'message-exchange-pattern':"OneWay", direction:"Outbound", type:getOutboundEndpointType(model), 'entity-id':uuid()) {
      					properties {
      						property(name:"endpoint.connector.ref",    value:getOutboundConnectorRef(model))
//      						property(name:"endpoint.address",      value:getOutboundEndpointAddress(model))
      						property(name:"endpoint.exchange.pattern", value:"")
      						property(name:getAddressAttribute(model.getOutboundTransport()),  value:getOutboundEndpointAddress(model))
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

	private String getInboundEndpointName(IModel model) {
		model.getInboundTransport() + '-IN'
	}
	
	private String getInboundEndpointType(IModel model) {
		getEndpointType(model.getInboundTransport())
	}

	private String getInboundConnectorRef(IModel model) {
		getConnectorRef(model.getInboundTransport())
	}

	private String getInboundEndpointAddress(IModel model) {
		String t = model.getInboundTransport()
		if (t == "VM") {
			"${dollarSymbol()}{${model.getUppercaseService()}_IN_VM_QUEUE}"
		} else if (t == "JMS") {
			"${dollarSymbol()}{${model.getUppercaseService()}_IN_QUEUE}"
		} else {
			"UNKNOWN-ENDPOINT-TYPE"
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
	
	private String getOutboundConnectorRef(IModel model) {
		getConnectorRef(model.getOutboundTransport())
	}

	private String getOutboundEndpointAddress(IModel model) {
		String t = model.getInboundTransport()
		if (t == "VM") {
			"${dollarSymbol()}{${model.getUppercaseService()}_OUT_VM_QUEUE}"
		} else if (t == "JMS") {
			"${dollarSymbol()}{${model.getUppercaseService()}_OUT_QUEUE}"
		} else {
			"UNKNOWN-ENDPOINT-TYPE"
		}
	}
	
    private String getAddressAttribute(String t) {
		if (t == "VM") {
			"vm.path"
		} else if (t == "JMS") {
			"endpoint.queue"
		} else {
			"UNKNOWN-ADDRESS_ATTRIBUTE"
		}
	}

	private String getEndpointType(String t) {
		if (t == "VM") {
			"org.mule.tooling.ui.modules.core.endpoint.vmEndpoint"
		} else if (t == "JMS") {
			"org.mule.tooling.ui.modules.core.endpoint.jms"
		} else {
			"UNKNOWN-ENDPOINT-TYPE"
		}
	}
	
	private String getConnectorRef(String t) {
		if (t == "JMS") {
			"soitoolkit-jms-connector"
		} else {
			""
		}
	}
	
}