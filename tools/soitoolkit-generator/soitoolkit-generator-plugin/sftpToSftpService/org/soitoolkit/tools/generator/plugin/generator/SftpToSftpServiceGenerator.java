package org.soitoolkit.tools.generator.plugin.generator;

import static org.soitoolkit.tools.generator.plugin.util.PropertyFileUtil.openPropertyFileForAppend;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.soitoolkit.tools.generator.plugin.util.PreferencesUtil;

public class SftpToSftpServiceGenerator implements Generator {

	GeneratorUtil gu;
	
	public SftpToSftpServiceGenerator(PrintStream ps, String groupId, String artifactId, String serviceName, String folderName) {
		gu = new GeneratorUtil(ps, groupId, artifactId, null, serviceName, null, null, "/templates/integrationComponent/sftpToSftpService", folderName);
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
		PrintWriter out = null;
		try {
			out = openPropertyFileForAppend(gu.getOutputFolder(), gu.getModel().getConfigPropertyFile());
			String service = gu.getModel().getUppercaseService();
			String sftpRootFolder = PreferencesUtil.getDefaultSftpRootFolder();
			
		    out.println("");
		    out.println("# Properties for sftp-service \"" + gu.getModel().getService() + "\"");
		    out.println("# TODO: Update to reflect your settings");
		    out.println(service + "_SENDER_SFTP_ADDRESS=" + sftpRootFolder + "/" + gu.getModel().getLowercaseService() + "/sender");
		    out.println(service + "_SENDER_POLLING_MS=1000");
		    out.println(service + "_SENDER_SIZECHECK_MS=500");
		    out.println(service + "_RECEIVER_SFTP_ADDRESS=" + sftpRootFolder + "/" + gu.getModel().getLowercaseService() + "/receiver");
		    out.println(service + "_ARCHIVE_FOLDER=/Users/magnuslarsson/archive");
		    out.println(service + "_ARCHIVE_RESTART_POLLING_MS=1000");

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null) {out.close();}
		}
	}
}