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

public class MyTestGroovyGenerator implements FileGenerator {
	public String generateFileContent(IModel model) {
    	def writer = new StringWriter()
    	def xml = new groovy.xml.MarkupBuilder(writer)

    	xml.'mule-configuration'(xmlns:'http://www.mulesoft.com/tooling/messageflow') {
      		flow('entity-id':uuid()) {
      			lane('entity-id':uuid()) {
					endpoint('message-exchange-pattern':"OneWay", direction:"Inbound", type:"org.mule.tooling.ui.modules.core.endpoint.vmEndpoint", name:"VM", 'entity-id':uuid()) {
      					properties {
      						property(value:"", name:"endpoint.address")
      						property(value:"", name:"endpoint.exchange.pattern")
      						property(value:"in2", name:"vm.path")
      						property(value:"", name:"endpoint.connector.ref")
      					}
      				}
      				pattern (type:"org.mule.tooling.ui.modules.core.pattern.customTransformer", name:"Custom Transformer", 'entity-id':uuid()) {
      					properties {
      						property(value:"org.ce.Ml2", name:"custom.transformer.classname")
      					}
      					description('Transformer that delegates to a Java class.')
      				}
					endpoint('message-exchange-pattern':"OneWay", direction:"Outbound", type:"org.mule.tooling.ui.modules.core.endpoint.vmEndpoint", name:"VM", 'entity-id':uuid()) {
      					properties {
      						property(value:"", name:"endpoint.address")
      						property(value:"", name:"endpoint.exchange.pattern")
      						property(value:"out2", name:"vm.path")
      						property(value:"", name:"endpoint.connector.ref")
      					}
      				}
      			}
      		}
    	}

    	writer.toString()
	}
	
	private String uuid() {
		UUID.randomUUID().toString()
	}

}