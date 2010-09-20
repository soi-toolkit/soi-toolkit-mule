package org.soitoolkit.tools.generator.plugin.model;

public class CustomizedModelImpl extends DefaultModelImpl {

	@Override
	public String getServiceProject() {
		return getArtifactId() + "-intsvc";
	}
	
}
