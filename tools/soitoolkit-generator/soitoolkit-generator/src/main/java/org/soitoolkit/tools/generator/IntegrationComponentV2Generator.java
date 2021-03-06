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

/**
 * Integration Component (IC) generator.
 */
public class IntegrationComponentV2Generator implements Generator {

	GeneratorUtil gu;
	MuleVersionEnum muleVersion;
	
	public IntegrationComponentV2Generator(PrintStream ps, String groupId, String artifactId, String version, MuleVersionEnum muleVersion, DeploymentModelEnum deploymentModel, List<TransportEnum> transports, String outputFolder) {
		gu = new GeneratorUtil(ps, groupId, artifactId, version, null, muleVersion, deploymentModel, transports, "/integrationComponentV2", outputFolder + "/__integrationComponentProject__");
		this.muleVersion = muleVersion;
	}
    public void startGenerator() {

		gu.generateContentAndCreateFile("pom.xml.gt");
		gu.generateContentAndCreateFile("mule-project.xml.gt");

		gu.generateFolder("src/main/api");
		gu.generateFolder("src/main/java/__javaPackageFilepath__");
		gu.generateFolder("src/environment");
		gu.generateFolder("mappings");
		
		gu.generateContentAndCreateFile("src/main/app/__artifactId__-common.xml.gt");
		gu.generateContentAndCreateFile("src/main/app/mule-deploy.properties.gt");

		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__capitalizedJavaArtifactId__MuleServer.java.gt");
		gu.generateFolder("src/test/resources/testfiles");
		gu.generateFolder("src/test/resources/teststub-services");
		
		gu.generateContentAndCreateFile("src/test/resources/mule-standalone-with-teststubs-config.xml.gt");
		
		// Mule 3.6 changed from log4j-1.2 to log4j-2 which uses different config files 
		if (muleVersion.isVersionEqualOrGreater(MuleVersionEnum.MULE_3_6_1)) {
			gu.generateContentAndCreateFile("src/main/resources/log4j2.xml.gt");
			gu.generateContentAndCreateFile("src/environment/log4j2.xml.gt");
		}
		else {
			gu.generateContentAndCreateFile("src/main/resources/log4j.dtd.gt");
			gu.generateContentAndCreateFile("src/main/resources/log4j.xml.gt");
			gu.generateContentAndCreateFile("src/environment/log4j.dtd.gt");
			gu.generateContentAndCreateFile("src/environment/log4j.xml.gt");
		}
		
		gu.generateContentAndCreateFile("src/main/resources/__configPropertyFile__.properties.gt");
    }
}
