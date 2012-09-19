package org.soitoolkit.tools.generator.util

import groovy.io.FileType
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil

import org.soitoolkit.tools.generator.model.IModel;
import org.soitoolkit.tools.generator.model.ServiceDescriptorXmlSchemaModel;
import org.soitoolkit.tools.generator.model.ServiceDescriptorModel;

public class ServiceDescriptorModelUtilImpl implements org.soitoolkit.tools.generator.util.ServiceDescriptorModelUtil {
	
	public ServiceDescriptorModel createServiceDescriptorModel(IModel model, File wsdlFile, File targetDir) {
		
		def wsdl = new XmlSlurper().parseText(wsdlFile.text)
		
		def name = wsdl.attributes().get("name")
		def targetNamespace = wsdl.attributes().get("targetNamespace")
		
		// Relative file path to be copied to target folder.
		String relativePath = FileUtil.getRelativePath(wsdlFile,
						targetDir);
		
		ServiceDescriptorModel serviceDescriptorModel = new ServiceDescriptorModel(model, name, relativePath);
		
		List<ServiceDescriptorXmlSchemaModel> schemasImports = createWsdlXmlSchemaModel(wsdlFile);
		serviceDescriptorModel.setSchemaImports(schemasImports);
		serviceDescriptorModel.setTargetDir(targetDir);
		serviceDescriptorModel.setTargetNamespace(new URI(targetNamespace));
		
		return serviceDescriptorModel;
	}
	
	public List<ServiceDescriptorXmlSchemaModel> createWsdlXmlSchemaModel(File wsdlFile) {
		
		def models = []
	
		def wsdl = new XmlSlurper().parseText(wsdlFile.text)
		
		def types = wsdl?.depthFirst().find { it.name() == 'types' }
		
		def importElements = types?.depthFirst().findAll { it.name() == 'import' }
		
		importElements.each { item -> 
		
			def schemaLocation = item.@schemaLocation.text()
			def namespace = item.@namespace.text()
			
			def URI schemaLocationURI = (schemaLocation== null) ? null : new URI(schemaLocation);
			def URI namespaceURI = (namespace== null) ? null : new URI(namespace);
			
			def tmpXmlFile = null;
			
			if (schemaLocation != null) {
				tmpXmlFile = wsdlFile.parent + "/" + schemaLocation;
			}
			
			if (tmpXmlFile != null && new File(tmpXmlFile).exists()) {
				def model = new ServiceDescriptorXmlSchemaModel(schemaLocationURI, namespaceURI)
			
				List<ServiceDescriptorXmlSchemaModel> xmlSchemas = createXmlSchemaModel(new File(tmpXmlFile))
				model.setSchemaImports(xmlSchemas);
				
				models << model
			}
		}
		
		def includeElements = types?.depthFirst().findAll { it.name() == 'include' }
		
		includeElements.each { item -> 
		
			def schemaLocation = item.@schemaLocation.text()
			
			def URI schemaLocationURI = (schemaLocation== null) ? null : new URI(schemaLocation);
			
			def tmpXmlFile = null;
			
			if (schemaLocation != null) {
				tmpXmlFile = wsdlFile.parent + "/" + schemaLocation;
			}
			
			if (tmpXmlFile != null && new File(tmpXmlFile).exists()) {
				def model = new ServiceDescriptorXmlSchemaModel(schemaLocationURI, null)
			
				List<ServiceDescriptorXmlSchemaModel> xmlSchemas = createXmlSchemaModel(new File(tmpXmlFile))
				model.setSchemaImports(xmlSchemas);
				
				models << model
			}
		}
		
		models
	}
	
	public List<ServiceDescriptorXmlSchemaModel> createXmlSchemaModel(File xmlFile) {
		
		def models = []
	
		def xml = new XmlSlurper().parseText(xmlFile.text)
		
		def importElements = xml.depthFirst().findAll { it.name() == 'import' }
		
		importElements.each { item -> 
		
			def schemaLocation = item.@schemaLocation.text()
			def namespace = item.@namespace.text()
			
			def URI schemaLocationURI = (schemaLocation== null) ? null : new URI(schemaLocation);
			def URI namespaceURI = (namespace== null) ? null : new URI(namespace);
			
			def tmpXmlFile = null;
			
			if (schemaLocation != null) {
				tmpXmlFile = xmlFile.parent + "/" + schemaLocation;
			}
			
			if (tmpXmlFile != null && new File(tmpXmlFile).exists()) {
				def model = new ServiceDescriptorXmlSchemaModel(schemaLocationURI, namespaceURI)
			
				List<ServiceDescriptorXmlSchemaModel> xmlSchemas = createXmlSchemaModel(new File(tmpXmlFile))
				model.setSchemaImports(xmlSchemas);
				
				models << model
			}
		
		}
		
		def includeElements = xml.depthFirst().findAll { it.name() == 'include' }
		
		includeElements.each { item -> 
		
			def schemaLocation = item.@schemaLocation.text()
			def namespace = item.@namespace.text()
			
			def URI schemaLocationURI = (schemaLocation== null) ? null : new URI(schemaLocation);
			
			def tmpXmlFile = null;
			
			if (schemaLocation != null) {
				tmpXmlFile = wsdlFile.parent + "/" + schemaLocation;
			}
			
			if (tmpXmlFile != null && new File(tmpXmlFile).exists()) {
				def model = new ServiceDescriptorXmlSchemaModel(schemaLocationURI, null)
			
				List<ServiceDescriptorXmlSchemaModel> xmlSchemas = createXmlSchemaModel(new File(tmpXmlFile))
				model.setSchemaImports(xmlSchemas);
				
				models << model
			}
		
		}
		
		models
	}
	
	/**
	 * 
	 * 
	 * */
	public List<File> getAllFilesMatching(File directory, String pattern) {
		
		def filesFound = []
		
		directory?.traverse(type:FileType.FILES, nameFilter: ~pattern) { fileFound -> 
			filesFound << fileFound
		}
		filesFound
	}
}