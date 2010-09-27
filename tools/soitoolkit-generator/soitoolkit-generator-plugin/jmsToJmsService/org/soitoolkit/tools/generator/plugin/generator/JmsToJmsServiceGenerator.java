package org.soitoolkit.tools.generator.plugin.generator;

import static org.soitoolkit.tools.generator.plugin.util.PropertyFileUtil.openPropertyFileForAppend;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

public class JmsToJmsServiceGenerator implements Generator {

	GeneratorUtil gu;
	
	public JmsToJmsServiceGenerator(PrintStream ps, String groupId, String artifactId, String serviceName, String folderName) {
		gu = new GeneratorUtil(ps, groupId, artifactId, null, serviceName, null, "/templates/integrationComponent/jmsToJmsService", folderName);
	}
		
    public void startGenerator() {

		gu.generateContentAndCreateFile("src/main/resources/services/__service__-service.xml.gt");
		gu.generateContentAndCreateFile("src/main/java/__javaPackageFilepath__/__lowercaseService__/__capitalizedService__Transformer.java.gt");

		gu.generateContentAndCreateFile("src/test/resources/teststub-services/__service__-teststub-service.xml.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseService__/__capitalizedService__IntegrationTest.java.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseService__/__capitalizedService__TestReceiver.java.gt");

		updatePropertyFile();
    }

	private void updatePropertyFile() {
		PrintWriter out = null;
		try {
			out = openPropertyFileForAppend(gu.getOutputFolder(), gu.getModel().getArtifactId());
			String service = gu.getModel().getUppercaseService();
			
		    out.println("");
		    out.println("# Properties for jms-service " + gu.getModel().getService());
		    out.println("# TODO: Update to reflect your settings");
		    out.println(service + "_IN_QUEUE="  + gu.getModel().getJmsInQueue());
		    out.println(service + "_OUT_QUEUE=" + gu.getModel().getJmsOutQueue());
		    out.println(service + "_DL_QUEUE="  + gu.getModel().getJmsDLQueue());

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null) {out.close();}
		}
	}
}