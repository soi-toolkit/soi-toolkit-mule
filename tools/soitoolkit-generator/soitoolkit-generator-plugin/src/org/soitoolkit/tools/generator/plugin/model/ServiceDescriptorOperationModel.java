package org.soitoolkit.tools.generator.plugin.model;

public class ServiceDescriptorOperationModel {

	@SuppressWarnings("unused")
	private IModel model;
	private ServiceDescriptorModel sdModel;
	private String operation;

	public ServiceDescriptorOperationModel(IModel model, ServiceDescriptorModel sdModel, String operation) {
		this.model = model;
		this.sdModel = sdModel;
		this.operation = operation;
	}
	
	public String getOperation() {
		return operation;
	}

	public String getSchemaRequestElement() {
		return getOperation();
	}
	public String getSchemaResponseElement() {
		return getOperation() + "Response";
	}
	public String getWsdlRequestMessage() {
		return getOperation() + "Request";
	}
	public String getWsdlResponseMessage() {
		return getOperation() + "Response";
	}
	public String getWsdlOperation() {
		return getOperation();
	}
	public String getWsdlSoapAction() {
		return sdModel.getWsdlNamespace() + ":" + getWsdlOperation();
	}
	
}
