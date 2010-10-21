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
package org.soitoolkit.tools.generator.plugin.generator;

import java.io.PrintStream;
import java.util.List;

public class SchemaComponentGenerator implements Generator {
	
	GeneratorUtil gu;
	
	public SchemaComponentGenerator(PrintStream ps, String groupId, String artifactId, String version, String schemaName, List<String> operations, String folderName) {
		gu = new GeneratorUtil(ps, groupId, artifactId, version, null, null, null, schemaName, operations, "/templates/schemaComponent/newProject", folderName + "/__schemaProject__");
	}
		
    public void startGenerator() {
		
		gu.generateFolder("branches");
		gu.generateFolder("tags");		
		gu.generateContentAndCreateFile("trunk/pom.xml.gt");
		gu.generateContentAndCreateFile("trunk/src/main/resources/schemas/__sd.schemaFilepath__/__sd.schema__.xsd.gt");
		gu.generateContentAndCreateFile("trunk/src/main/resources/schemas/__sd.schemaFilepath__/__sd.wsdl__.wsdl.gt");

    }
}
