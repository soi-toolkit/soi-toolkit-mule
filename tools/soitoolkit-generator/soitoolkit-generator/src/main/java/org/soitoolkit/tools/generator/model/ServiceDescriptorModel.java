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
package org.soitoolkit.tools.generator.model;

import static org.soitoolkit.tools.generator.model.impl.ModelUtil.makeJavaName;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.soitoolkit.tools.generator.util.FileUtil;

public class ServiceDescriptorModel {

	private IModel model;
	
	private List<ServiceDescriptorOperationModel> operations = new ArrayList<ServiceDescriptorOperationModel>();
	
	// Includes both xsd:import + xsd:include
	private List<ServiceDescriptorXmlSchemaModel> schemaImports = new ArrayList<ServiceDescriptorXmlSchemaModel>();

	private String wsdlRelativeFilepath;
	private URI targetNamespace;
	
	private String name;

	private File targetDir;

	private String serviceDescriptor;
	
	public ServiceDescriptorModel(IModel model, String name, String wsdlRelativeFilepath) {
		this.model = model;
		this.name = name;
		this.wsdlRelativeFilepath = wsdlRelativeFilepath;
	}
	
	public ServiceDescriptorModel(IModel model, String serviceDescriptor, List<String> operations) {
		this.model = model;
		this.serviceDescriptor = serviceDescriptor;

		initOperations(model, serviceDescriptor, operations);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public List<URI> getNamespaces() {
		
		List<URI> namespaces = new ArrayList<URI>();
		namespaces.add(targetNamespace);
		
		for (ServiceDescriptorXmlSchemaModel schemaImport : schemaImports) {
			namespaces.addAll(schemaImport.getNamespaces());
		}
		return namespaces;
	}
	
	public List<File> getXmlSchemas() throws MalformedURLException, IOException {
		
		List<File> files = new ArrayList<File>();
		
		File wsdlFile = new File(targetDir.getPath() + "/" + wsdlRelativeFilepath);
		
		for (ServiceDescriptorXmlSchemaModel schemaImport : schemaImports) {
			
			if (schemaImport.getSchemaLocation() != null && !schemaImport.getSchemaLocation().toString().startsWith("http:")) {
				
				File file = new File(schemaImport.getSchemaLocation().getPath());

				if (!file.isAbsolute()) {
					file = new File(wsdlFile.getParentFile() + "/" + schemaImport.getSchemaLocation().toString());
					
					String relativePath = FileUtil.getRelativePath(file.getCanonicalFile(), targetDir);
					
					if (file.exists()) {
						files.add(new File(relativePath));
					}
				} 
				files.addAll(schemaImport.getXmlSchemaFiles(wsdlFile.getParentFile(), targetDir));
			}
		}
		return files;
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

	public String getWsdlRelativeFilepath() {
		return wsdlRelativeFilepath;
	}

	public void setWsdlRelativeFilepath(String wsdlRelativeFilepath) {
		this.wsdlRelativeFilepath = wsdlRelativeFilepath;
	}

	public URI getTargetNamespace() {
		return targetNamespace;
	}

	public void setTargetNamespace(URI targetNamespace) {
		this.targetNamespace = targetNamespace;
	}

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
	
	public String getSchemaJavaPackage(String schema) {
		return model.getSchemaJavaPackage(schema);
	}
	
	public List<ServiceDescriptorXmlSchemaModel> getSchemaImports() {
		return schemaImports;
	}
	
	public void setSchemaImports(List<ServiceDescriptorXmlSchemaModel> schemaImports) {
		this.schemaImports = schemaImports;
	}
	
	public File getTargetDir() {
		return targetDir;
	}

	public void setTargetDir(File targetDir) {
		this.targetDir = targetDir;
	}
}
