package org.soitoolkit.tools.generator.plugin.generator;

import static org.soitoolkit.tools.generator.plugin.util.PropertyFileUtil.openPropertyFileForAppend;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.soitoolkit.tools.generator.plugin.util.PreferencesUtil;

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
			String sftpRootFolder = PreferencesUtil.getDefaultSftpRootFolder();
			
		    out.println("");
		    out.println("# Properties for jms-service " + gu.getModel().getService());
		    out.println("# TODO: Update to reflect your settings");
		    out.println(service + "_IN_QUEUE="  + service + "_IN_QUEUE");
		    out.println(service + "_OUT_QUEUE=" + service + "_OUT_QUEUE");
		    out.println(service + "_DL_QUEUE="  + service + "_DL_QUEUE");

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null) {out.close();}
		}
	}

}