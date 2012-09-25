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

import org.soitoolkit.tools.generator.Generator;
import org.soitoolkit.tools.generator.GeneratorUtil;
import org.soitoolkit.tools.generator.model.enums.DeploymentModelEnum;
import org.soitoolkit.tools.generator.model.enums.MuleVersionEnum;

public class IntegrationComponentTeststubGenerator implements Generator {

	GeneratorUtil gu;
	
	public IntegrationComponentTeststubGenerator(PrintStream ps, String groupId, String artifactId, String version, DeploymentModelEnum deploymentModel, String outputFolder) {
		// Test of custom model impl
		// ModelFactory.setModelClass(CustomizedModelImpl.class);
		MuleVersionEnum muleVersionOnlyHereToSatisfyTheUnderlyingModel = MuleVersionEnum.MAIN_MULE_VERSION;
		gu = new GeneratorUtil(ps, groupId, artifactId, version, null, muleVersionOnlyHereToSatisfyTheUnderlyingModel, deploymentModel, null, "/integrationComponentTeststub", outputFolder + "/__teststubStandaloneProject__");
	}
    public void startGenerator() {

		gu.generateContentAndCreateFile("pom.xml.gt");
		gu.generateContentAndCreateFile("mule-project.xml.gt");

		gu.generateContentAndCreateFile("src/main/app/__teststubStandaloneProject__-config.xml.gt");
		gu.generateContentAndCreateFile("src/main/app/mule-deploy.properties.gt");
//		gu.generateContentAndCreateFile("src/main/app/mule-app.properties.gt");

		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__capitalizedJavaTeststubArtifactId__MuleServer.java.gt");
		gu.generateContentAndCreateFile("src/main/resources/log4j.dtd.gt");
		gu.generateContentAndCreateFile("src/main/resources/log4j.xml.gt");
    }
}
