/* 
 * Licensed to the soi-toolkit project under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The soi-toolkit project licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.soitoolkit.tools.generator;

import java.io.PrintStream;
import java.util.List;

import org.soitoolkit.tools.generator.Generator;
import org.soitoolkit.tools.generator.GeneratorUtil;
import org.soitoolkit.tools.generator.model.enums.DeploymentModelEnum;
import org.soitoolkit.tools.generator.model.enums.MuleVersionEnum;
import org.soitoolkit.tools.generator.model.enums.TransportEnum;

public class IntegrationComponentGenerator implements Generator {

	GeneratorUtil gu;
	
	public IntegrationComponentGenerator(PrintStream ps, String groupId, String artifactId, String version, MuleVersionEnum muleVersion, DeploymentModelEnum deploymentModel, List<TransportEnum> transports, String folderName) {
		// Test of custom model impl
		// ModelFactory.setModelClass(CustomizedModelImpl.class);
		gu = new GeneratorUtil(ps, groupId, artifactId, version, null, muleVersion, deploymentModel, transports, "/integrationComponent", folderName + "/__integrationComponentProject__");
	}
    public void startGenerator() {

		gu.generateFolder("branches");
		gu.generateFolder("tags");

		gu.generateContentAndCreateFile("trunk/pom.xml.gt");
		gu.generateContentAndCreateFile("trunk/mule-project.xml.gt");
		gu.generateContentAndCreateFile("trunk/application/mule-deploy.properties.gt");

		gu.generateFolder("trunk/src/main/java/__javaPackageFilepath__");
		gu.generateFolder("trunk/src/main/resources/flow");
		gu.generateContentAndCreateFile("trunk/src/main/app/mule-config.xml.gt");
		gu.generateContentAndCreateFile("trunk/src/main/resources/__artifactId__-common.xml.gt");
		gu.generateContentAndCreateFile("trunk/src/main/resources/__artifactId__-config.xml.gt");

	    if (gu.getModel().isJdbc()) {
			gu.generateContentAndCreateFile("trunk/src/main/resources/__artifactId__-jdbc-connector.xml.gt");
			gu.generateContentAndCreateFile("trunk/src/environment/setup/__artifactId__-db-create-tables.sql.gt");
			gu.generateContentAndCreateFile("trunk/src/environment/setup/__artifactId__-db-drop-tables.sql.gt");
			gu.generateContentAndCreateFile("trunk/src/environment/setup/__artifactId__-db-insert-testdata.sql.gt");
	    }

		gu.generateContentAndCreateFile("trunk/src/test/java/__javaPackageFilepath__/__capitalizedJavaArtifactId__MuleServer.java.gt");
		gu.generateContentAndCreateFile("trunk/src/test/resources/__artifactId__-integrationtests-common.xml.gt");
		gu.generateContentAndCreateFile("trunk/src/test/resources/__artifactId__-teststubs-and-services-config.xml.gt");
		gu.generateContentAndCreateFile("trunk/src/test/resources/__artifactId__-teststubs-only-config.xml.gt");
		gu.generateFolder("trunk/src/test/resources/testfiles");
		gu.generateFolder("trunk/src/test/resources/teststub-services");
		gu.generateContentAndCreateFile("trunk/src/environment/log4j.dtd.gt");
		gu.generateContentAndCreateFile("trunk/src/environment/log4j.xml.gt");
		gu.generateContentAndCreateFile("trunk/src/environment/__securityPropertyFile__.properties.gt");
		gu.generateContentAndCreateFile("trunk/src/environment/__configPropertyFile__.properties.gt");
    }
		
    public void startGeneratorMultiProjectStyle() {

		gu.generateFolder("branches");
		gu.generateFolder("tags");
		gu.generateContentAndCreateFile("trunk/pom.xml.gt");

		// TODO: Refactor to a reusable java-project generator + some mule-stuff?
		gu.generateContentAndCreateFile("trunk/__serviceProjectFilepath__/pom.xml.gt");
		gu.generateContentAndCreateFile("trunk/__serviceProjectFilepath__/.project.gt");
		gu.generateContentAndCreateFile("trunk/__serviceProjectFilepath__/mule-project.xml.gt");
		gu.generateContentAndCreateFile("trunk/__serviceProjectFilepath__/application/mule-deploy.properties.gt");

		// TODO: Add code for standard monitorService!
		gu.generateFolder("trunk/__serviceProjectFilepath__/src/main/java/__javaPackageFilepath__");
		gu.generateFolder("trunk/__serviceProjectFilepath__/src/main/app");
		gu.generateFolder("trunk/__serviceProjectFilepath__/src/main/resources/flow");
		gu.generateContentAndCreateFile("trunk/__serviceProjectFilepath__/src/main/resources/__artifactId__-common.xml.gt");
		gu.generateContentAndCreateFile("trunk/__serviceProjectFilepath__/src/main/resources/__artifactId__-config.xml.gt");

	    if (gu.getModel().isJdbc()) {
			gu.generateContentAndCreateFile("trunk/__serviceProjectFilepath__/src/main/resources/__artifactId__-jdbc-connector.xml.gt");
			gu.generateContentAndCreateFile("trunk/__serviceProjectFilepath__/src/environment/setup/__artifactId__-db-create-tables.sql.gt");
			gu.generateContentAndCreateFile("trunk/__serviceProjectFilepath__/src/environment/setup/__artifactId__-db-drop-tables.sql.gt");
			gu.generateContentAndCreateFile("trunk/__serviceProjectFilepath__/src/environment/setup/__artifactId__-db-insert-testdata.sql.gt");
	    }

		gu.generateFolder("trunk/__serviceProjectFilepath__/src/main/resources/services");
		gu.generateContentAndCreateFile("trunk/__serviceProjectFilepath__/src/test/java/__javaPackageFilepath__/__capitalizedJavaArtifactId__MuleServer.java.gt");
		gu.generateContentAndCreateFile("trunk/__serviceProjectFilepath__/src/test/resources/__artifactId__-teststubs-and-services-config.xml.gt");
		gu.generateContentAndCreateFile("trunk/__serviceProjectFilepath__/src/test/resources/__artifactId__-teststubs-only-config.xml.gt");
		gu.generateFolder("trunk/__serviceProjectFilepath__/src/test/resources/testfiles");
		gu.generateFolder("trunk/__serviceProjectFilepath__/src/test/resources/teststub-services");
		gu.generateContentAndCreateFile("trunk/__serviceProjectFilepath__/src/environment/log4j.dtd.gt");
		gu.generateContentAndCreateFile("trunk/__serviceProjectFilepath__/src/environment/log4j.xml.gt");
		gu.generateContentAndCreateFile("trunk/__serviceProjectFilepath__/src/environment/__securityPropertyFile__.properties.gt");
		gu.generateContentAndCreateFile("trunk/__serviceProjectFilepath__/src/environment/__configPropertyFile__.properties.gt");

		// Support for the mule deployment model
	    if (gu.getModel().isStandaloneDeployModel()) {
	    	gu.generateContentAndCreateFile("trunk/__standaloneProjectFilepath__/pom.xml.gt");
			gu.generateContentAndCreateFile("trunk/__standaloneProjectFilepath__/src/main/app/mule-config.xml.gt");
			gu.generateContentAndCreateFile("trunk/__teststubStandaloneProjectFilepath__/pom.xml.gt");
			gu.generateContentAndCreateFile("trunk/__teststubStandaloneProjectFilepath__/src/main/app/mule-config.xml.gt");
	    }
		
		// Support for the war deployment model
	    if (gu.getModel().isWarDeployModel()) {
			gu.generateContentAndCreateFile("trunk/__webProjectFilepath__/pom.xml.gt");
			gu.generateContentAndCreateFile("trunk/__webProjectFilepath__/src/main/webapp/META-INF/MANIFEST.MF.gt");
			gu.generateContentAndCreateFile("trunk/__webProjectFilepath__/src/main/webapp/WEB-INF/web.xml.gt");
			gu.generateFolder("trunk/__webProjectFilepath__/src/main/webapp/WEB-INF/classes");
			gu.generateFolder("trunk/__webProjectFilepath__/src/main/webapp/WEB-INF/lib");
			gu.generateContentAndCreateFile("trunk/__webProjectFilepath__/src/main/webapp/index.jsp.gt");
	
			gu.generateContentAndCreateFile("trunk/__teststubWebProjectFilepath__/pom.xml.gt");
			gu.generateContentAndCreateFile("trunk/__teststubWebProjectFilepath__/src/main/webapp/META-INF/MANIFEST.MF.gt");
			gu.generateContentAndCreateFile("trunk/__teststubWebProjectFilepath__/src/main/webapp/WEB-INF/web.xml.gt");
			gu.generateFolder("trunk/__teststubWebProjectFilepath__/src/main/webapp/WEB-INF/classes");
			gu.generateFolder("trunk/__teststubWebProjectFilepath__/src/main/webapp/WEB-INF/lib");
			gu.generateContentAndCreateFile("trunk/__teststubWebProjectFilepath__/src/main/webapp/index.jsp.gt");
	    }

    }
}
