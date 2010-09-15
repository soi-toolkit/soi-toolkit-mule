package org.soitoolkit.commons.log;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Utility methods for Log4J
 * 
 * @author Tony Dalbrekt
 */
public class Log4jUtils {
    public static Logger logger = Logger.getLogger(Log4jUtils.class);

    /**
     * Default interval (in ms) between checks of updates in the log4j config file.
     * Defaults to 60 secs.
     */
    public static final long DEFAULT_WATCH_DELAY = 60000;

    /** 
     * Default name of the variable that points to the log4j config file
     */
    public static final String DEFUALT_LOG_CONFIG = "log4j.configuration";

    public static final String XML_FILE_EXENSION = ".xml";

    /**
     * Hidden constructor.
     */
    private Log4jUtils() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }

    /**
     * (Re)Loads the config file using {@link DOMConfigurator} for {@code .xml} files and {@link PropertyConfigurator} for {@code .proeprties} files.
     * 
     * @param configFile log4j configuration file, {@code .properties} or {@code .xml}
     */
    public static void reload(String configFile) {
    	LogLog.debug("Will reload log4j based on config-file " + configFile);
        if (configFile != null) {
            if (configFile.endsWith(XML_FILE_EXENSION)) {
            	LogLog.debug("...using DOMConfigurator.configure()");
            	DOMConfigurator.configure(configFile);

            } else if (configFile.endsWith(".properties")) {
            	LogLog.debug("...using PropertyConfigurator.configure()");
                PropertyConfigurator.configure(configFile);

            }
            if (logger.isInfoEnabled()) {
                logger.info("Reloads log4j configuration: " + configFile);
            }
        }
    }

    /**
     * (Re)Loads the config file using {@link DOMConfigurator} for {@code .xml} files and {@link PropertyConfigurator} for {@code .proeprties} files.
     * Reloads the file automatically when changed.
     * 
     * @param configFile log4j configuration file, {@code .properties} or {@code .xml}
     * @param delay number of milliseconds between checks of changes in the config file, defaults to {@link Log4jUtils#DEFAULT_WATCH_DELAY} if set to 0
     */
    public static void reloadAndWatch(final String configFile, final long delay) {
        long watchDelay = delay == 0 ? Log4jUtils.DEFAULT_WATCH_DELAY : delay;
        if (configFile != null) {

            if (configFile.endsWith(XML_FILE_EXENSION)) {
                DOMConfigurator.configureAndWatch(configFile, watchDelay);
            } else
                PropertyConfigurator.configureAndWatch(configFile, watchDelay);

        }
        if (logger.isInfoEnabled()) {
            logger.info("Reloads and watches log4j configuration: " + configFile);
        }
    }
}