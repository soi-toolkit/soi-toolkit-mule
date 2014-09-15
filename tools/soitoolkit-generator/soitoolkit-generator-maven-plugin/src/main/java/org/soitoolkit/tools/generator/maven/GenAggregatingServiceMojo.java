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
package org.soitoolkit.tools.generator.maven;

import java.io.File;
import java.net.URL;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.soitoolkit.tools.generator.AggregatingServiceGenerator;
import org.soitoolkit.tools.generator.Generator;
import org.soitoolkit.tools.generator.model.ModelFactory;
import org.soitoolkit.tools.generator.model.enums.MuleVersionEnum;

/**
 * Goal for creating an new Integration Component
 *
 * @goal genAS
 * @requiresProject false
 * 
 * @author Magnus Larsson
 */
public class GenAggregatingServiceMojo extends AbstractMojo {

	/**
     * DomainId.
     * @parameter expression="${domainId}" default-value="domain1.sub11.sub111"
     * @required
     */
    private String domainId;

	/**
     * ArtifactId.
     * @parameter expression="${artifactId}" default-value="GetAggregatedSample"
     * @required
     */
    private String artifactId;

	/**
     * Version.
     * @parameter expression="${version}" default-value="1.0.0-SNAPSHOT"
     * @optional
     */
    private String version;

	/**
     * Mule version.
     * @parameter expression="${muleVersion}" default-value="3.4.0"
     * @optional
     */
    private String muleVersion;

	/**
     * Groovy Model.
     * @parameter expression="${groovyModel}"
     * @optional
     */
    private URL groovyModel;

	/**
     * Location of the output folder.
     * @parameter expression="${outDir}" default-value="."
     * @optional
     */
    private File outDir;

    /**
     * Generate Schema.
     * @parameter expression="${genSchema}" default-value="true"
     * @optional
     */
    private boolean genSchema;

    /**
     * If not generate default schema, whet is the artifactId for the Schema?, e.g. "GetRequestActivities".
     * @parameter expression="${schemaArtifactId}"
     * @optional
     */
    private String schemaArtifactId;

    /**
     * If not generate default schema, then under what top-folder will the schema be found, e.g. "TD_REQUESTSTATUS_1_0_1_R".
     * @parameter expression="${schemaTopFolder}"
     * @optional
     */
    private String schemaTopFolder;

    public void execute() throws MojoExecutionException {

    	
        getLog().info("");
        getLog().info("=======================================");
        getLog().info("= Creating an new Aggregating Service =");
        getLog().info("=======================================");
        getLog().info("");
        getLog().info("ARGUMENTS:");
        getLog().info("(change an arg by suppling: -Darg=value):");
        getLog().info("");
        getLog().info("outDir=\"" + outDir.getPath());
        getLog().info("artifactId=\"" + artifactId + "\"");
        getLog().info("domainId=\"" + domainId + "\"");
        getLog().info("version=\"" + version + "\"");
        getLog().info("muleVersion=\"" + muleVersion + "\"");
        getLog().info("groovyModel=\"" + groovyModel + "\"");
        getLog().info("genSchema=\"" + genSchema + "\"");
        getLog().info("schemaArtifactId=\"" + schemaArtifactId + "\"");
        getLog().info("schemaTopFolder=\"" + schemaTopFolder + "\"");
        getLog().info("");

        initGroovyModel();

        MuleVersionEnum muleVersionEnum = initMuleVersion(muleVersion);

		Generator g = new AggregatingServiceGenerator(System.out, domainId, artifactId, version, muleVersionEnum, outDir.getPath(), genSchema, schemaArtifactId, schemaTopFolder);

		g.startGenerator();
    }

	private void initGroovyModel() throws MojoExecutionException {
		if (groovyModel != null) {
	    	try {
				ModelFactory.setModelGroovyClass(groovyModel);
			} catch (Exception e) {
				throw new MojoExecutionException("Invalid Groovy model: " + groovyModel, e);
			}
		}
	}

	private MuleVersionEnum initMuleVersion(String muleVersion) throws MojoExecutionException {
		MuleVersionEnum muleVersionEnum = null;
		try {
			muleVersionEnum = MuleVersionEnum.getByLabel(muleVersion);
		} catch (Exception e) {
			throw new MojoExecutionException("Invalid Mule version: " + muleVersion + ", allowed values: " + MuleVersionEnum.allowedLabelValues(), e);
		}
		if (muleVersionEnum == null) {
			throw new MojoExecutionException("Invalid Mule version: " + muleVersion + ", allowed values: " + MuleVersionEnum.allowedLabelValues());
		}
		return muleVersionEnum;
	}
}
