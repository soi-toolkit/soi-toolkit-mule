package org.soitoolkit.tools.generator.model;

import org.soitoolkit.tools.generator.model.impl.DefaultModelImpl;
import org.soitoolkit.tools.generator.model.IModel;

public class GroovyModelImpl extends DefaultModelImpl implements IModel {

	public String getServiceProject() {
		return getArtifactId() + "-module-intsvc";
	}

	public String getServiceProjectFilepath() {
		return "modules/intsvc";
	}

	public String getWebProject() {
		return getArtifactId() + "-app-integrations";
	}

	public String getWebProjectFilepath() {
		return "applications/integrations";
	}

	public String getTeststubWebProject() {
		return getArtifactId() + "-app-integration-teststubs";
	}

	public String getTeststubWebProjectFilepath() {
		return "applications/integration-teststubs";
	}
}
