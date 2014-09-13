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

import static org.soitoolkit.tools.generator.model.impl.ModelUtil.capitalize;
import static org.soitoolkit.tools.generator.model.impl.ModelUtil.initialLowerCase;

import java.io.PrintStream;

import org.soitoolkit.tools.generator.model.IModel;
import org.soitoolkit.tools.generator.model.enums.MuleVersionEnum;
import org.soitoolkit.tools.generator.model.enums.TransformerEnum;
import org.soitoolkit.tools.generator.model.enums.TransportEnum;

public class AggregatingServiceGenerator implements Generator {

    GeneratorUtil guParent;
    GeneratorUtil guIc;
	GeneratorUtil guIcTestStub;
	GeneratorUtil guService;
	
	public AggregatingServiceGenerator(PrintStream ps, String domainId, String artifactId, String version, MuleVersionEnum muleVersion, String outputFolder, boolean genSchema) {
        guParent     = createGeneratorUtil(ps, domainId, artifactId, version, muleVersion, "/aggregatingService/parent", outputFolder, null, genSchema);
        guIc         = createGeneratorUtil(ps, domainId, artifactId, version, muleVersion, "/aggregatingService/ic", outputFolder, artifactId, genSchema);
		guIcTestStub = createGeneratorUtil(ps, domainId, artifactId, version, muleVersion, "/aggregatingService/icTestStub", outputFolder, artifactId + "-teststub", genSchema);
		guService    = createGeneratorUtil(ps, domainId, artifactId, version, muleVersion, "/aggregatingService/service", outputFolder, artifactId, genSchema);
	}

	public GeneratorUtil createGeneratorUtil(PrintStream ps, String domainId, String artifactId, String version, MuleVersionEnum muleVersion, String templateFolder, String outputFolder, String outputFolderSuffix, boolean genSchema) {
		String groupId = "se.skltp.aggregatingservices." + domainId;
		String serviceName = artifactId;
		
		outputFolder = outputFolder + "/" + domainId + "/" + artifactId + "/trunk";
		if (outputFolderSuffix != null ) {
			outputFolder += "/" + outputFolderSuffix;
		}

        GeneratorUtil gu = new GeneratorUtil(ps, groupId, artifactId, version, serviceName, muleVersion, TransportEnum.HTTP, TransportEnum.HTTP, TransformerEnum.JAVA, templateFolder, outputFolder);
        IModel m = gu.getModel();
        m.getExt().put("domainId", domainId);
        m.getExt().put("genSchema", genSchema);

        String schemaTopFolder = "TD_REQUESTSTATUS_1_0_1_R";
        String schemaDomainId = "riv.crm.requeststatus";
        String schemaArtifactId = "GetRequestActivities";
        m.getExt().put("schemaTopFolder",                  schemaTopFolder);
        m.getExt().put("schemaDomainId",                   schemaDomainId.replace('.', ':'));
        m.getExt().put("schemaGroupId",                    "se." + schemaDomainId);
        m.getExt().put("schemaArtifactId",                 schemaArtifactId);
        m.getExt().put("schemaLowercaseArtifactId",        schemaArtifactId.toLowerCase());
        m.getExt().put("schemaInitialLowercaseArtifactId", initialLowerCase(schemaArtifactId));
        return gu;
	}

    public void startGenerator() {
        startParentGenerator();
    	startProjectGenerator();
    	startTeststubProjectGenerator();
		startServiceGenerator();
    }

    private void startParentGenerator() {
        IModel m = guParent.getModel();
        guParent.logInfo("\n\nCreates a AggregatingService-parent-pom: " + m.getGroupId() + " - " + m.getArtifactId() + "\n");

        guParent.generateContentAndCreateFile("pom.xml.gt");
    }

    private void startProjectGenerator() {
		IModel m = guIc.getModel();
		guIc.logInfo("\n\nCreates a AggregatingService-project: " + m.getGroupId() + " - " + m.getArtifactId() + "\n");

    	guIc.generateContentAndCreateFile("pom.xml.gt");
		guIc.generateContentAndCreateFile("mule-project.xml.gt");

		guIc.generateFolder("src/main/java/__javaPackageFilepath__");
		guIc.generateFolder("src/environment");
		guIc.generateFolder("mappings");
		guIc.generateFolder("flows");
		guIc.generateFolder("api");
    	guIc.generateContentAndCreateFileUsingGroovyGenerator(getClass().getResource("GenerateMinimalMflow.groovy"), "flows/__artifactId__-common.mflow");

		guIc.generateContentAndCreateFile("src/main/app/__artifactId__-common.xml.gt");
		guIc.generateContentAndCreateFile("src/main/app/mule-deploy.properties.gt");

		guIc.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__capitalizedJavaArtifactId__MuleServer.java.gt");
		guIc.generateFolder("src/test/resources/testfiles");
		guIc.generateFolder("src/test/resources/teststub-services");

		guIc.generateContentAndCreateFile("src/test/resources/log4j.dtd.gt");
		guIc.generateContentAndCreateFile("src/test/resources/log4j.xml.gt");

		guIc.generateContentAndCreateFile("src/environment/log4j.dtd.gt");
		guIc.generateContentAndCreateFile("src/environment/log4j.xml.gt");

		guIc.generateContentAndCreateFile("src/main/resources/__configPropertyFile__.properties.gt");

		guIc.generateContentAndCreateFile("src/main/resources/schemas/TD_REQUESTSTATUS_1_0_1_R/core_components/crm_requeststatus_1.0.xsd.gt");
		guIc.generateContentAndCreateFile("src/main/resources/schemas/TD_REQUESTSTATUS_1_0_1_R/core_components/interoperability_headers_1.0.xsd.gt");
		guIc.generateContentAndCreateFile("src/main/resources/schemas/TD_REQUESTSTATUS_1_0_1_R/core_components/itintegration_registry_1.0.xsd.gt");

		guIc.generateContentAndCreateFile("src/main/resources/schemas/TD_REQUESTSTATUS_1_0_1_R/interactions/GetRequestActivitiesInteraction/GetRequestActivitiesInteraction_1.0_RIVTABP21.wsdl.gt");
		guIc.generateContentAndCreateFile("src/main/resources/schemas/TD_REQUESTSTATUS_1_0_1_R/interactions/GetRequestActivitiesInteraction/GetRequestActivitiesResponder_1.0.xsd.gt");
	}

	private void startTeststubProjectGenerator() {
		IModel m = guIcTestStub.getModel();
		guIc.logInfo("\n\nCreates a AggregatingService-teststub-project: " + m.getGroupId() + " - " + m.getArtifactId() + "\n");
		guIcTestStub.generateContentAndCreateFile("pom.xml.gt");
		guIcTestStub.generateContentAndCreateFile("mule-project.xml.gt");

		guIcTestStub.generateContentAndCreateFile("src/main/app/__teststubStandaloneProject__-config.xml.gt");
		guIcTestStub.generateContentAndCreateFile("src/main/app/mule-deploy.properties.gt");
//		guIcTestStub.generateContentAndCreateFile("src/main/app/mule-app.properties.gt");

		guIcTestStub.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__capitalizedJavaTeststubArtifactId__MuleServer.java.gt");
		guIcTestStub.generateContentAndCreateFile("src/main/resources/log4j.dtd.gt");
		guIcTestStub.generateContentAndCreateFile("src/main/resources/log4j.xml.gt");
	}

	private void startServiceGenerator() {
		IModel m = guService.getModel();
		guService.logInfo("\n\nCreates a AggregatingService-service: " + m.getGroupId() + " - " + m.getArtifactId() + "\n");

		guService.generateContentAndCreateFile("src/main/java/__javaPackageFilepath__/QueryObjectFactoryImpl.java.gt");
		guService.generateContentAndCreateFile("src/main/java/__javaPackageFilepath__/RequestListFactoryImpl.java.gt");
		guService.generateContentAndCreateFile("src/main/java/__javaPackageFilepath__/ResponseListFactoryImpl.java.gt");
		
		guService.copyContentAndCreateFile("src/test/certs/client-truststore.jks.gt");
		guService.copyContentAndCreateFile("src/test/certs/client.jks.gt");
    	guService.copyContentAndCreateFile("src/test/certs/truststore.jks.gt");
    	guService.copyContentAndCreateFile("src/test/certs/server.jks.gt");
    	guService.copyContentAndCreateFile("src/test/certs/readme.txt.gt");

		guService.generateContentAndCreateFile("src/test/resources/teststub-services/__service__-teststub-service.xml.gt");
		guService.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/QueryObjectFactoryTest.java.gt");
		guService.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/RequestListFactoryTest.java.gt");
		guService.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/ResponseListFactoryTest.java.gt");

		guService.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/integrationtest/__capitalizedJavaService__IntegrationTest.java.gt");
		guService.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/integrationtest/__capitalizedJavaService__TestConsumer.java.gt");
		guService.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/integrationtest/__capitalizedJavaService__TestProducer.java.gt");
		guService.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/integrationtest/__capitalizedJavaService__TestProducerDb.java.gt");
	}
}