package org.soitoolkit.tools.generator.plugin.model;

import java.util.Map;

public interface IModel {

	public Object resolveParameter(String parameterName);

	public Object resolveParameter(String parameterName, Object defaultValue);

	public ServiceDescriptorModel getSd();

	public Map<String, Object> getExt();

	public String getDollarSymbol();

	public String getGroupId();

	public String getArtifactId();

	public String getCapitalizedArtifactId();

	public String getLowercaseArtifactId();

	public String getVersion();

	public String getService();

	public String getCapitalizedService();

	public String getLowercaseService();

	public String getUppercaseService();

	public String getSoitoolkitVersion();

	public String getSuperpomGroupId();

	public String getSuperpomArtifactId();

	public String getSuperpomVersion();

	public String getParentPom();

	public String getIntegrationComponentProject();

	public String getServiceProject();

	public String getSchemaProject();

	public String getWebProject();

	public String getTeststubWebProject();

	public String getJavaPackage();

	public String getJavaPackageFilepath();
	
	public boolean isJms();
	public boolean isSftp();
	public boolean isServlet();
}