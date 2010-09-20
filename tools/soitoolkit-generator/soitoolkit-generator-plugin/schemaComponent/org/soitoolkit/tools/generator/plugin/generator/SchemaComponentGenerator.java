package org.soitoolkit.tools.generator.plugin.generator;

import java.io.PrintStream;
import java.util.List;

public class SchemaComponentGenerator implements Generator {
	
	GeneratorUtil gu;
	
	public SchemaComponentGenerator(PrintStream ps, String groupId, String artifactId, String version, String schemaName, List<String> operations, String folderName) {
		gu = new GeneratorUtil(ps, groupId, artifactId, version, null, null, schemaName, operations, "/templates/schemaComponent/newProject", folderName, "schemaProject");
	}
		
    public void startGenerator() {
		
		gu.generateFolder("branches");
		gu.generateFolder("tags");		
		gu.generateContentAndCreateFile("trunk/pom.xml.gt");
		gu.generateContentAndCreateFile("trunk/src/main/resources/schemas/__sd.schemaFilepath__/__sd.schema__.xsd.gt");
		gu.generateContentAndCreateFile("trunk/src/main/resources/schemas/__sd.schemaFilepath__/__sd.wsdl__.wsdl.gt");

    }
}
