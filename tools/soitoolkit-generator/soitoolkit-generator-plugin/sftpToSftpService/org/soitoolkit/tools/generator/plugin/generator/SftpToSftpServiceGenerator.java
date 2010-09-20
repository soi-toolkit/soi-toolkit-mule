package org.soitoolkit.tools.generator.plugin.generator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;

public class SftpToSftpServiceGenerator implements Generator {

	GeneratorUtil gu;
	
	public SftpToSftpServiceGenerator(PrintStream ps, String groupId, String artifactId, String serviceName, String folderName) {
		gu = new GeneratorUtil(ps, groupId, artifactId, null, serviceName, null, "/templates/integrationComponent/sftpToSftpService", folderName, "serviceProject");
	}
		
    public void startGenerator() {

		gu.generateContentAndCreateFile("src/main/resources/services/__service__-service.xml.gt");
		gu.generateContentAndCreateFile("src/main/java/__javaPackageFilepath__/__lowercaseService__/__capitalizedService__Transformer.java.gt");

		gu.generateContentAndCreateFile("src/test/resources/teststub-services/__service__-teststub-service.xml.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseService__/__capitalizedService__IntegrationTest.java.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseService__/__capitalizedService__TestReceiver.java.gt");

		updatePropertyFile();
		
		/*

		TODO: Lägg på följande i propertfil:
		
		
		
		xxx-common.xml:
		
			<spring:bean name="sftpTransportNotificationLogger" class="org.soitoolkit.commons.mule.sftp.SftpTransportNotificationListenerImpl"/>
   	<notifications dynamic="true">
		<notification event="COMPONENT-MESSAGE"/>
		<notification event="ENDPOINT-MESSAGE"/>
		<notification event="CUSTOM"/>
		<notification-listener ref="sftpTransportNotificationLogger"/>
	</notifications>

		
		*/

		
    }

	private void updatePropertyFile() {
		Writer out = null;
		try {
			String propFile = gu.getOutputFolder() + "/" + gu.getOutputRootFolder() + "/src/environment/" + gu.getModel().getArtifactId() + ".properties";
			String service = gu.getModel().getUppercaseService();
			
			// TODO: Replace with sl4j!
			System.err.println("UPDATE FILE: " + propFile);
			
		    out = new BufferedWriter(new FileWriter(propFile, true));
		    out.write("\n");
		    out.write("# Properties for sftp-service " + gu.getModel().getService() + "\n");
		    out.write("# TODO: Update to reflect your settings\n");
		    out.write(service + "_SENDER_SFTP_ADDRESS=muletest1@localhost/~/sftp/" + gu.getModel().getLowercaseService() + "/sender\n");
		    out.write(service + "_SENDER_POLLING_MS=1000\n");
		    out.write(service + "_SENDER_SIZECHECK_MS=500\n");
		    out.write(service + "_RECEIVER_SFTP_ADDRESS=muletest1@localhost/~/sftp/" + gu.getModel().getLowercaseService() + "/receiver\n");
		    out.write(service + "_ARCHIVE_FOLDER=/Users/magnuslarsson/archive\n");
		    out.write(service + "_ARCHIVE_RESTART_POLLING_MS=1000\n");

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null) try {out.close();} catch (IOException e) {}
		}
	}
}