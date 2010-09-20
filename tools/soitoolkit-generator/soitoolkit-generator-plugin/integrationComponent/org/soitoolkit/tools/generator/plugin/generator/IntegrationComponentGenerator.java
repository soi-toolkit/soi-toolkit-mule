package org.soitoolkit.tools.generator.plugin.generator;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import org.soitoolkit.tools.generator.plugin.model.CustomizedModelImpl;
import org.soitoolkit.tools.generator.plugin.model.ModelFactory;
import org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum;

public class IntegrationComponentGenerator implements Generator {

	GeneratorUtil gu;
	
	public IntegrationComponentGenerator(PrintStream ps, String groupId, String artifactId, String version, List<TransportEnum> transports, String folderName) {
		// Test of custom model impl
		// ModelFactory.setModelClass(CustomizedModelImpl.class);
		gu = new GeneratorUtil(ps, groupId, artifactId, version, null, transports, "/templates/integrationComponent/newProject", folderName, "integrationComponentProject");
	}
		
    public void startGenerator() {

		gu.generateFolder("branches");
		gu.generateFolder("tags");
		gu.generateContentAndCreateFile("trunk/pom.xml.gt");
		
		// TODO: Refactor to a reusable java-project generator + some mule-stuff?
		gu.generateContentAndCreateFile("trunk/__serviceProject__/pom.xml.gt");
		// TODO: Add code for standard monitorService!
		gu.generateFolder("trunk/__serviceProject__/src/main/java/__javaPackageFilepath__");
		gu.generateContentAndCreateFile("trunk/__serviceProject__/src/main/resources/__artifactId__-common.xml.gt");
		gu.generateContentAndCreateFile("trunk/__serviceProject__/src/main/resources/__artifactId__-config.xml.gt");
		gu.generateFolder("trunk/__serviceProject__/src/main/resources/services");
		gu.generateContentAndCreateFile("trunk/__serviceProject__/src/test/java/__javaPackageFilepath__/__capitalizedArtifactId__MuleServer.java.gt");
		gu.generateContentAndCreateFile("trunk/__serviceProject__/src/test/resources/__artifactId__-teststubs-and-services-config.xml.gt");
		gu.generateContentAndCreateFile("trunk/__serviceProject__/src/test/resources/__artifactId__-teststubs-only-config.xml.gt");
		gu.generateFolder("trunk/__serviceProject__/src/test/resources/teststub-services");
		gu.generateContentAndCreateFile("trunk/__serviceProject__/src/environment/log4j.dtd.gt");
		gu.generateContentAndCreateFile("trunk/__serviceProject__/src/environment/log4j.xml.gt");
		gu.generateContentAndCreateFile("trunk/__serviceProject__/src/environment/__artifactId__.properties.gt");

		// TODO: Refactor to reusable schema-project?
		
		// TODO: Refactor to reusable war-project + some mule-stuff?
		gu.generateContentAndCreateFile("trunk/__webProject__/pom.xml.gt");
		gu.generateContentAndCreateFile("trunk/__webProject__/src/main/webapp/META-INF/MANIFEST.MF.gt");
		gu.generateContentAndCreateFile("trunk/__webProject__/src/main/webapp/WEB-INF/web.xml.gt");
		gu.generateFolder("trunk/__webProject__/src/main/webapp/WEB-INF/classes");
		gu.generateFolder("trunk/__webProject__/src/main/webapp/WEB-INF/lib");
		gu.generateContentAndCreateFile("trunk/__webProject__/src/main/webapp/index.jsp.gt");

		gu.generateContentAndCreateFile("trunk/__teststubWebProject__/pom.xml.gt");
		gu.generateContentAndCreateFile("trunk/__teststubWebProject__/src/main/webapp/META-INF/MANIFEST.MF.gt");
		gu.generateContentAndCreateFile("trunk/__teststubWebProject__/src/main/webapp/WEB-INF/web.xml.gt");
		gu.generateFolder("trunk/__teststubWebProject__/src/main/webapp/WEB-INF/classes");
		gu.generateFolder("trunk/__teststubWebProject__/src/main/webapp/WEB-INF/lib");
		gu.generateContentAndCreateFile("trunk/__teststubWebProject__/src/main/webapp/index.jsp.gt");

    }
}
