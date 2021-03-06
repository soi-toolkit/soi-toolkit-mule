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
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.soitoolkit.tools.generator.Generator;
import org.soitoolkit.tools.generator.IntegrationComponentGenerator;
import org.soitoolkit.tools.generator.IntegrationComponentTeststubGenerator;
import org.soitoolkit.tools.generator.model.ModelFactory;
import org.soitoolkit.tools.generator.model.enums.DeploymentModelEnum;
import org.soitoolkit.tools.generator.model.enums.MuleVersionEnum;
import org.soitoolkit.tools.generator.model.enums.TransportEnum;

/**
 * Goal for creating an new Integration Component
 *
 * @goal genICTS
 * @requiresProject false
 * 
 * @author Magnus Larsson
 */
public class GenIntegrationComponentTeststubMojo extends AbstractMojo {

	/**
     * ArtifactId.
     * @parameter expression="${artifactId}" default-value="sample1"
     * @required
     */
    private String artifactId;

	/**
     * GroupId.
     * @parameter expression="${groupId}" default-value="org.sample"
     * @required
     */
    private String groupId;

	/**
     * Version.
     * @parameter expression="${version}" default-value="1.0.0-SNAPSHOT"
     * @required
     */
    private String version;

	/**
     * Deploy model.
     * @parameter expression="${deployModel}" default-value="Standalone"
     * @required
     */
    private String deployModel;

	/**
     * Groovy Model.
     * @parameter expression="${groovyModel}"
     * @optional
     */
    private URL groovyModel;

	/**
     * Location of the output folder.
     * @parameter expression="${outDir}" default-value="."
     * @required
     */
    private File outDir;
    
    public void execute() throws MojoExecutionException {

    	
        getLog().info("");
        getLog().info("==================================================");
        getLog().info("= Creating an new Integration Component Teststub =");
        getLog().info("==================================================");
        getLog().info("");
        getLog().info("ARGUMENTS:");
        getLog().info("(change an arg by suppling: -Darg=value):");
        getLog().info("");
        getLog().info("outDir=" + outDir.getPath());
//        getLog().info("outDir=" + outDir.getAbsolutePath());
//        try {
//			getLog().info("outDir=" + outDir.getCanonicalPath());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
        getLog().info("artifactId=" + artifactId);
        getLog().info("groupId=" + groupId);
        getLog().info("version=" + version);
        getLog().info("deployModel=" + deployModel);
        getLog().info("groovyModel=" + groovyModel);
        getLog().info("");

        initGroovyModel();

        DeploymentModelEnum deployModelEnum = initDeployModel(deployModel);

		//Generator g = new IntegrationComponentGenerator(System.out, groupId, artifactId, version, muleVersionEnum, deployModelEnum, connectorsEnum, outDir.getPath());
        Generator g = new IntegrationComponentTeststubGenerator(System.out, groupId, artifactId, version, deployModelEnum, outDir.getPath());
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

	private DeploymentModelEnum initDeployModel(String deployModel) throws MojoExecutionException {
		DeploymentModelEnum deployModelEnum = null;
		try {
			deployModelEnum = DeploymentModelEnum.getByLabel(deployModel);
		} catch (Exception e) {
			throw new MojoExecutionException("Invalid deploy model: " + deployModel + ", allowed values: " + DeploymentModelEnum.allowedLabelValues(), e);
		}
		if (deployModelEnum == null) {
			throw new MojoExecutionException("Invalid deploy model: " + deployModel + ", allowed values: " + DeploymentModelEnum.allowedLabelValues());
		}
		return deployModelEnum;
	}

}
