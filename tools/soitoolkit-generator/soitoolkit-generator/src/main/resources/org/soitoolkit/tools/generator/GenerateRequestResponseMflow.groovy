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
					endpoint(name:getInboundEndpointName(model), 'message-exchange-pattern':"RequestResponse", direction:"Inbound", type:getInboundEndpointType(model), 'entity-id':uuid()) {
      					properties {
      						property(name:getAddressAttribute('in', model.getInboundTransport()),  value:getInboundEndpointAddress(model))
      						property(name:"exchange-pattern", value:"request-response")
                            property(name:"transformer-refs", value:getInboundRequestTransformerRefs(model))
                            property(name:"responseTransformer-refs", value:getInboundResponseTransformerRefs(model))
                            property(name:"org.mule.tooling.ui.modules.core.widgets.meta.ModeAttribute", value:"http://www.mulesoft.org/schema/mule/http/endpoint")
	      				}
      				}
      				pattern (name:"transform-request", type:"org.mule.tooling.ui.modules.core.pattern.customTransformer", 'entity-id':uuid()) {
      					properties {
                            property(name:"class", value:getRequestTransformerClass(model))
                            property(name:"returnClass")
                            property(name:"ignoreBadInput")
                            property(name:"encoding")
                            property(name:"mimeType")
      					}
      					description('Transformer that delegates to a Java class.')
      				}
                    response('entity-id':uuid()) {
                        compartment('entity-id':uuid()) {
                            lane('entity-id':uuid()) {
                                pattern (name:"transform-response", type:"org.mule.tooling.ui.modules.core.pattern.customTransformer", 'entity-id':uuid()) {
                                    properties {
                                        property(name:"class", value:getResponseTransformerClass(model))
                                        property(name:"returnClass")
                                        property(name:"ignoreBadInput")
                                        property(name:"encoding")
                                        property(name:"mimeType")
                                    }
                                    description('Transformer that delegates to a Java class.')
                                }
                            }
                        }
                    }
					endpoint(name:getOutboundEndpointName(model), 'message-exchange-pattern':"RequestResponse", direction:"Outbound", type:getOutboundEndpointType(model), 'entity-id':uuid()) {
      					properties {
      						property(name:getAddressAttribute('out', model.getOutboundTransport()),  value:getOutboundEndpointAddress(model))
      						property(name:"exchange-pattern", value:"request-response")
      						property(name:"transformer-refs", value:getOutboundRequestTransformerRefs(model))
                            property(name:"responseTimeout", value:"${dollarSymbol()}{SERVICE_TIMEOUT_MS}")
                            property(name:"method", value:"GET")
                            property(name:"org.mule.tooling.ui.modules.core.widgets.meta.ModeAttribute", value:"http://www.mulesoft.org/schema/mule/http/endpoint")
      					}
      				}
      				unknown('entity-id':uuid()) {
      				    content("&lt;custom-exception-strategy xmlns=&quot;http://www.mulesoft.org/schema/mule/core&quot; class=&quot;org.soitoolkit.commons.mule.error.ServiceExceptionStrategy&quot;/&gt;")
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

    private String getInboundRequestEndpointAddress(IModel model) {
        String t = model.getInboundTransport()
        if (t == "SOAPHTTP") {
            "${dollarSymbol()}{${model.getUppercaseService()}_INBOUND_URL}"
        } else if (t == "RESTHTTP") {
            "${dollarSymbol()}{${model.getUppercaseService()}_INBOUND_URL}"
        } else {
            "UNKNOWN-ENDPOINT-TYPE"
        }
    }

    private String getInboundRequestTransformerRefs(IModel model) {
        "objToStr logReqIn"
    }

    private String getInboundResponseTransformerRefs(IModel model) {
        String t = model.getInboundTransport()
        
        if (t == "SOAPHTTP") {
            "createSoapFaultIfException logRespOut"
        } else if (t == "RESTHTTP") {
             "logRespOut"
        } else {
             "logRespOut"
        }
    }

    private String getOutboundRequestTransformerRefs(IModel model) {
        "objToStr logReqOut"
    }

    private String getOutboundResponseTransformerRefs(IModel model) {
        String t = model.getInboundTransport()
        
        if (t == "SOAPHTTP") {
            "logRespIn"
        } else if (t == "RESTHTTP") {
             "objToStr logRespIn"
        } else {
             "logRespIn"
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
    private String getRequestTransformerClass(IModel model) {
        "${model.getJavaPackage()}.${model.getLowercaseJavaService()}.${model.getCapitalizedJavaService()}RequestTransformer"
    }
    private String getResponseTransformerClass(IModel model) {
        "${model.getJavaPackage()}.${model.getLowercaseJavaService()}.${model.getCapitalizedJavaService()}ResponseTransformer"
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
    
    private String getOutboundResponseEndpointAddress(IModel model) {
        String t = model.getOutboundTransport()
        if (t == "SOAPHTTP") {
            "${dollarSymbol()}{${model.getUppercaseService()}_OUTBOUND_URL}"
        } else if (t == "RESTHTTP") {
            "${dollarSymbol()}{${model.getUppercaseService()}_OUTBOUND_URL}/sample/#[xpath:/ns:sample/ns:id]"
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
			"http://www.mulesoft.org/schema/mule/https/endpoint"
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