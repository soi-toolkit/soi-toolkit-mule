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
package org.soitoolkit.tools.generator;

import static org.soitoolkit.tools.generator.util.MiscUtil.convertStreamToString;
import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.List;

import org.soitoolkit.tools.generator.model.ServiceDescriptorModel;
import org.soitoolkit.tools.generator.model.enums.MuleVersionEnum;
import org.soitoolkit.tools.generator.util.FileUtil;
import org.soitoolkit.tools.generator.util.ServiceDescriptorModelUtil;

public class SchemaComponentGenerator implements Generator {

	GeneratorUtil gu;

	File targetFile;
	File sourceFile;

	ServiceDescriptorModelUtil serviceDescriptorModelUtil;
	
	public SchemaComponentGenerator(PrintStream ps, String groupId,
			String artifactId, String version, String schemaName,
			List<String> operations, String outputFolder, String sourceFolder) {
		gu = new GeneratorUtil(ps, groupId, artifactId, version, null,
				MuleVersionEnum.MAIN_MULE_VERSION, null, schemaName,
				operations, "/schemaComponent/newProject", outputFolder
						+ "/__schemaProject__");
		
		serviceDescriptorModelUtil = getServiceDescriptorModelUtilInstance();

		if (sourceFolder!=null) {
			sourceFile = new File(sourceFolder);
		} 
		
		targetFile = new File(gu.getOutputFolder() + "/"
				+ "src/main/resources/schemas");
	}

	public void startGenerator() {

		if (sourceFile == null) {
			// Copy Sample WSDL + Sample XML Schema
			gu.logInfo("Generating WSDL + XML schema.");
			gu.copyContentAndCreateFile("src/main/resources/schemas/sample.xsd.gt");
			gu.logInfo("Created file: " + "sample.xsd.");
			gu.copyContentAndCreateFile("src/main/resources/schemas/sampleService.wsdl.gt");
			gu.logInfo("Created file: " + "sampleService.wsdl.");
		} else {
			FileUtil.copyFiles(sourceFile, targetFile);
		}
		
		initServiceDescriptorModel();
		
		gu.generateContentAndCreateFile("pom.xml.gt");
		gu.generateContentAndCreateFile("wcf.bat.gt");
	}

	/*
	 * Initialization of ServiceDescriptorModel based on WSDL(s)
	 * 
	 * */
	private void initServiceDescriptorModel() {

		List<ServiceDescriptorModel> sds = gu.getModel().getSds();

		// Extract all WSDL files from source directory.
		List<File> wsdlFiles = FileUtil.getAllFilesMatching(targetFile, ".*\\.wsdl");
		
		for (File wsdlFile : wsdlFiles) {
			ServiceDescriptorModel serviceDescriptorModel = serviceDescriptorModelUtil
					.createServiceDescriptorModel(gu.getModel(), wsdlFile,
							targetFile);
			sds.add(serviceDescriptorModel);
		}
	}

	private ServiceDescriptorModelUtil getServiceDescriptorModelUtilInstance() {
		Class<ServiceDescriptorModelUtil> clazz;
		Object aScript = null;
		try {
			URL url = ServiceDescriptorModelUtil.class.getResource("ServiceDescriptorModelUtilImpl.groovy");
			clazz = getGroovyClass(url);
			aScript = clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return (ServiceDescriptorModelUtil) aScript;
	}

	private Class getGroovyClass(URL url) throws IllegalArgumentException,
			IOException {
		String groovyCode = convertStreamToString(url.openStream());
		ClassLoader parent = SchemaComponentGenerator.class.getClassLoader();
		GroovyClassLoader loader = new GroovyClassLoader(parent);
		Class groovyClass = loader.parseClass(groovyCode);

		return groovyClass;
	}
}
