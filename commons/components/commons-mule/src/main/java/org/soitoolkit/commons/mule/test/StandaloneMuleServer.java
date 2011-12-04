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
package org.soitoolkit.commons.mule.test;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;


/**
 * Minimal configuration of mule used for manual testing against a stand alone Mule server suitable to be started from within the development environment, e.g. Eclipse.
 * 
 * NOTE: The natural name for this class shoud be MuleServer but since that name already is taken by org.mule.MuleServer we use another name to minimize risk for confusing naming conflicts...
 *
 * @author Magnus Larsson
 *
 */
public class StandaloneMuleServer {

	private static Logger log = LoggerFactory.getLogger(StandaloneMuleServer.class);
	
	// Configuration parameters set by the constructor
	protected String muleServerId = null;
	protected String muleConfig = null;
    
    // The underlying mule server and servlet container
    org.mule.MuleServer muleServer = null;

	/**
	 * Constructor that takes configuration parameters
	 * 
	 * @param muleServerId
	 * @param muleConfig
	 */
    public StandaloneMuleServer(String muleServerId, String muleConfig, boolean loadServices) {
    	this.muleServerId = muleServerId;
    	
    	// Initiate the muleConfig with all config files from mule-deploy.properties, optionally filter out service-config files
    	this.muleConfig = getConfigFileFromMuleDeployPropertyFile(loadServices);	
    	
    	if (muleConfig != null && muleConfig.length() > 0) {
    		this.muleConfig += ", " + muleConfig;
    	}
	}

    /**
     * Convenience method that both starts and stops mule 
     * 
     * @throws Exception
     */
	public void run() throws Exception {

		// Start me up...
        log.info("Startup...");
		start();

        // Run until the return key is hit...
        log.info("Hit the RETURN - key to shutdown");
        System.in.read();

        // Bye, bye...
        log.info("Shutdown...");
        shutdown();
        log.info("Shutdown complete");
	}

	/**
	 * Start up mule...
	 * 
	 * @throws InterruptedException
	 * @throws Exception
	 */
	public void start() throws InterruptedException, Exception {
		
        log.info("Startup Mule...");

        // Before launching Mule ESB set its server id
		System.setProperty("mule.serverId", muleServerId);

		// Before launching Mule ESB alse ensure that CXF use LOG4J for logging
		System.setProperty("org.apache.cxf.Logger", "org.apache.cxf.common.logging.Log4jLogger");

		// Startup Mule ESB in the background
		muleServer = new org.mule.MuleServer(muleConfig);
        muleServer.start(true, true);
	}

	/**
	 * Shutdown mule
	 * 
	 * @throws Exception
	 */
	public void shutdown() throws Exception {
		
        log.info("Shutdown Mule...");

        // Shutdown mule server
        muleServer.shutdown();
	}


	/**
	 * Get mule config-files from the standard mule-deploy.properties file
	 * 
	 * @param loadServices - set to false if no service-config files are to be loaded, e.g. for a teststub-service only configuration
	 * @return
	 */
	protected String getConfigFileFromMuleDeployPropertyFile(boolean loadServices) {

	    // Get all config-files from the mule-deploy.properties - file
	    RecursiveResourceBundle rb = new RecursiveResourceBundle("mule-deploy");
	    String allConfigFiles = rb.getString("config.resources");
	    
	    
	    // If services-config-files are to be used (the normal case) then just return the list
	    if (loadServices) return allConfigFiles;
	    
	    
	    // Ok, so now we need to filter out all service-config files, i.e. files ending with "-service.xml"
	    String[] allConfigFilesArr = allConfigFiles.split(",");

	    // Place all non-service config-files in a list
	    List<String> configFilesList = new ArrayList<String>();
	    for (String configFile : allConfigFilesArr) {
	    	// Only add config files not ending with "-service.xml"
	    	if (!configFile.endsWith("-service.xml")) {
				configFilesList.add(configFile);
			}
		}

	    // Finally convert the list to a comma separated string and return it
	    StringBuffer configFiles = new StringBuffer();
	    for (String configFile : configFilesList) {
	    	if (configFiles.length() == 0) {
	    		configFiles.append(configFile);
	    	} else {
	    		configFiles.append(',').append(configFile);
	    	}
		}
	    return configFiles.toString();
	}

}