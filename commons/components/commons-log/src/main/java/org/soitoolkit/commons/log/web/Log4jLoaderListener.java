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
package org.soitoolkit.commons.log.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;

/**
 * @author Tony Dalbrekt
 */
public class Log4jLoaderListener implements ServletContextListener {
    private Logger logger = Logger.getLogger(Log4jLoaderListener.class);

    /**
     * {@inheritdoc}
     * 
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Shutdown Log4j LogManager...");
        LogManager.shutdown();

    }

    /**
     * {@inheritdoc}
     * 
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent sce) {
    	LogLog.debug("Start to initialize log4j");

    	LogLog.debug("Confige CXF to use log4j!");

    	ServletContext sc = sce.getServletContext();
        if (Log4jWebUtils.isWatchDisabled(sc)) {
            Log4jWebUtils.reload(sc);
        } else {
            Log4jWebUtils.reloadAndWatch(sc, Log4jWebUtils.getWatchDelay(sc));
        }
        logger.info("Log4j initialized...");
    }
}
