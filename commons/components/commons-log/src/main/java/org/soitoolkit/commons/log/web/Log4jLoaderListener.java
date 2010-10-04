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
