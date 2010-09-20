package org.soitoolkit.tools.generator.plugin.model;

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
		String namespace = "urn:" + model.getJavaPackage() + "." + getSchema() + ".wsdl:v1";
		return namespace.toLowerCase();
	}
	public String getSchemaNamespace() {
		String namespace = "urn:" + model.getJavaPackage() + "." + getSchema() + ".schema:v1";
		return namespace.toLowerCase();
	}
	public String getWsdlJavaPackage() {
		String javaPackage = model.getJavaPackage() + "." + getSchema() + ".wsdl.v1";
		return javaPackage.toLowerCase();
	}
	public String getSchemaJavaPackage() {
		String javaPackage = model.getJavaPackage() + "." + getSchema() + ".schema.v1";
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
}
