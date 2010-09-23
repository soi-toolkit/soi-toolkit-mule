package org.soitoolkit.tools.generator.plugin.model;

public class GroovyModelImpl extends DefaultModelImpl implements IModel {

	public GroovyModelImpl() {
		System.err.println("Groovy at your service :-)");
	
	}
	
	@Override
	public String getServiceProject() {
		return getArtifactId() + "-svc";
	}

	@Override
	public String getServiceProjectFilepath() {
		return "composites/" + getServiceProject();
	}

	@Override
	public String getWebProjectFilepath() {
		return "modules/" + getWebProject();
	}

	public String getTeststubWebProjectFilepath() {
		return "modules/" + getTeststubWebProject();
	}
	
}
