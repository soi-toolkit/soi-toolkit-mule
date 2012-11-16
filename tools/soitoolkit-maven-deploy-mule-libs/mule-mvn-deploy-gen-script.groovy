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


/*
 * Groovy-script to generate a Maven-deploy script for deploying
 * Mule ESB jars to an Maven-repository manager.
 * Tested with Mule 3.2.1 EE.
 *
 * Supposed to be run after the script: $MULE_HOME/bin/populate_m2_repo
 * Usage example:
 *   1. Run the populate_m2_repo script like: %MULE_HOME%\bin\populate_m2_repo.cmd %MULE_HOME%\repo
 *   2. Put *this* script in %MULE_HOME%\repo
 *   3. Update variable "mavenRepoUrl" below with the url to your maven-repo-manager and repo
 *   4. Run this script: groovy mule-mvn-deploy-gen-script.groovy > mule-mvn-deploy-script.cmd
 *      NOTE: Groovy must be installed, see: http://groovy.codehaus.org/Installing+Groovy
 *   5. Run the generated script mule-mvn-deploy-script.cmd to deploy jars to the Maven repo manager
 *
 * 2012-01-19 Hakan Dahl
 */

def mavenRepoUrl="http://MY_MAVEN_REPO_MANAGER/repo/content/repositories/thirdparty"
def mavenRepositoryId="thirdparty"
def rootDirsToProcess=[ "com/mulesoft", "org/mule" ]


scriptGenerator = new GenDeployScript(mavenRepoUrl, mavenRepositoryId)
scriptGenerator.processRootDirs(rootDirsToProcess)


public class GenDeployScript {
  String mavenRepoUrl
  String mavenRepositoryId
  
  
  public GenDeployScript(String mavenRepoUrl, String mavenRepositoryId) {
    this.mavenRepoUrl = mavenRepoUrl
    this.mavenRepositoryId = mavenRepositoryId
  }

  
  def processRootDirs(dirs) {
    dirs.each {
      def files = new File(it).listFiles()
      processFiles files
    }
  }


  // recurse through directories
  def processFiles(files) {
    files.each {
      if (it.isDirectory()) {
        def filesInDir = it.listFiles()
        
        if(foundLeafDirectory(filesInDir)) {
          //println "********** LEAF: " + it
          prepareMavenDeployCommand(it)
        }
        else {
          // recurse
          processFiles filesInDir      
        }      
      }
    }
  }

  
  boolean foundLeafDirectory(files) {
    boolean foundLeaf = true
    files.each {
      if (it.isDirectory()) {
        foundLeaf = false
      }
    }
    return foundLeaf
  }


  def prepareMavenDeployCommand(dir) {
    def files = dir.listFiles()
    def jarFile;
    def jarTestFile;
    def pomFile;
    files.each {
      if (it.getName().endsWith("tests.jar")) {
        jarTestFile = it
      }
      else if (it.getName().endsWith(".jar")) {
        jarFile = it
      }
      else if (it.getName().endsWith(".pom")) {
        pomFile = it
      }
    }
    
    printMavenDeployCommand(jarFile, jarTestFile, pomFile)
  }


  def printMavenDeployCommand(jarFile, jarTestFile, pomFile) {
    def mvnCmd = "call mvn deploy:deploy-file -Durl=" + mavenRepoUrl + " -DrepositoryId=" + mavenRepositoryId
    mvnCmd += " -DpomFile=" + pomFile
    
    
    if (jarFile != null) {
      println mvnCmd + " -Dfile=" + jarFile
    }
    else {
      println mvnCmd + " -Dfile=" + pomFile
    }
    
    // add any test-jars
    if (jarTestFile != null) {
      println mvnCmd + " -Dfile=" + jarTestFile + " -Dclassifier=tests"
    }
  }
  
}
