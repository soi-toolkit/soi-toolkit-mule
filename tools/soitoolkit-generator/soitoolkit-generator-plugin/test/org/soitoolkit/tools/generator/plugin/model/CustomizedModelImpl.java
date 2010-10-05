package org.soitoolkit.tools.generator.plugin.model;

import org.soitoolkit.tools.generator.plugin.model.impl.DefaultModelImpl;

public class CustomizedModelImpl extends DefaultModelImpl implements IModel {

	@Override
	public String getServiceProject() {
		return getArtifactId() + "-intsvc";
	}
	
}
