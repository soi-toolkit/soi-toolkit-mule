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
package org.soitoolkit.tools.generator.plugin.model;

import static org.soitoolkit.tools.generator.plugin.model.impl.ModelUtil.makeJavaName;

import java.util.ArrayList;
import java.util.List;

public class ServiceDescriptorModel {

	private IModel model;
	private String serviceDescriptor;
	private List<ServiceDescriptorOperationModel> operations = new ArrayList<ServiceDescriptorOperationModel>();

	public ServiceDescriptorModel(IModel model, String serviceDescriptor, List<String> operations) {
		this.model = model;
		this.serviceDescriptor = serviceDescriptor;

		initOperations(model, serviceDescriptor, operations);
	}

	public String getSchema() {
		return serviceDescriptor;
	}
	public String getSchemaFilepath() {
		return model.getJavaPackageFilepath();
	}
	public String getWsdl() {
		return serviceDescriptor + "Service";
	}
	public String getWsdlNamespace() {
		String namespace = "urn:" + getPathPrefix() + ".wsdl:v1";
		return namespace.toLowerCase();
	}

	public String getSchemaNamespace() {
		String namespace = "urn:" + getPathPrefix() + ".schema:v1";
		return namespace.toLowerCase();
	}
	public String getWsdlJavaPackage() {
		String javaPackage = getPathPrefix() + ".wsdl.v1";
		return javaPackage.toLowerCase();
	}
	public String getSchemaJavaPackage() {
		String javaPackage = getPathPrefix() + ".schema.v1";
		return javaPackage.toLowerCase();
	}
	public String getWsdlPortType() {
		return serviceDescriptor + "Interface";
	}
	public String getWsdlBinding() {
		return serviceDescriptor + "Binding";
	}
	public String getWsdlPort() {
		return serviceDescriptor + "Port";
	}
	public String getWsdlService() {
		return getWsdl();
	}
	
	public List<ServiceDescriptorOperationModel> getOperations() {
		return operations;
	}

	// -----------------

	private void initOperations(IModel model, String serviceDescriptor, List<String> operations) {

		// If no operations are supplied then use the serviceDescriptor-name as the name of the single operation
		if ((operations == null) || operations.size() == 0) {
			this.operations.add(new ServiceDescriptorOperationModel(model, this, serviceDescriptor));
		} else {
			for (String operation : operations) {
				this.operations.add(new ServiceDescriptorOperationModel(model, this, operation));
			}
		}
	}

	private String getPathPrefix() {
		return model.getJavaPackage() + "." + makeJavaName(getSchema());
	}
	
}
