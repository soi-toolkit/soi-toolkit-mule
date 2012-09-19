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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.soitoolkit.tools.generator.util.FileUtil;

public class ServiceDescriptorXmlSchemaModel {
	private URI namespace;
	private URI schemaLocation;
	
	private List<ServiceDescriptorXmlSchemaModel> schemaImports = new ArrayList<ServiceDescriptorXmlSchemaModel>();
	
	public ServiceDescriptorXmlSchemaModel(URI schemaLocation, URI namespace) {
		this.namespace = namespace;
		this.schemaLocation = schemaLocation;
	}
	
	public List<ServiceDescriptorXmlSchemaModel> getSchemaImports() {
		return schemaImports;
	}
	
	public void setSchemaImports(List<ServiceDescriptorXmlSchemaModel> schemaImports) {
		this.schemaImports = schemaImports;
	}
	
	public URI getNamespace() {
		return namespace;
	}
	
	public void setNamespace(URI namespace) {
		this.namespace = namespace;
	}
	
	public URI getSchemaLocation() {
		return schemaLocation;
	}
	
	public void setSchemaLocation(URI schemaLocation) {
		this.schemaLocation = schemaLocation;
	}
	
	public List<URI> getNamespaces() {
		List<URI> namespaces = new ArrayList<URI>();
		
		if (namespace != null) {
			namespaces.add(namespace);
		}
		
		for (ServiceDescriptorXmlSchemaModel schemaImport : schemaImports) {
			namespaces.addAll(schemaImport.getNamespaces());
		}
		return namespaces;
	}
	
	public List<URI> getSchemaLocations() {
		List<URI> schemaLocations = new ArrayList<URI>();
		schemaLocations.add(schemaLocation);
		
		for (ServiceDescriptorXmlSchemaModel schemaImport : schemaImports) {
			schemaLocations.addAll(schemaImport.getSchemaLocations());
		}
		return schemaLocations;
	}
	
	public List<File> getXmlSchemaFiles(File parent, File root) throws MalformedURLException, IOException {
		
		List<File> files = new ArrayList<File>();
		
		for (ServiceDescriptorXmlSchemaModel schemaImport : schemaImports) {
			
			if (schemaImport.getSchemaLocation() != null) {
			
				File file = new File(parent + "/" + schemaImport.getSchemaLocation().toString());
				
				String relativePath = FileUtil.getRelativePath(file.getCanonicalFile(), root);
				
				if (file.exists()) {
					files.add(new File(relativePath));
				}
				
				files.addAll(schemaImport.getXmlSchemaFiles(file.getParentFile(), root));
			}
		}
		return files;
	}
}
