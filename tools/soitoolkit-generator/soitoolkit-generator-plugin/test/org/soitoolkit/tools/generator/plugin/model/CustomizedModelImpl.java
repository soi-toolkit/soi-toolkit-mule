package org.soitoolkit.tools.generator.plugin.model;

public class CustomizedModelImpl extends DefaultModelImpl implements IModel {

	@Override
	public String getServiceProject() {
		return getArtifactId() + "-intsvc";
	}
	
}
