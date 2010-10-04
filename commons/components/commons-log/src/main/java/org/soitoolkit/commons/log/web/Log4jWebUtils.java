package org.soitoolkit.commons.log.web;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.xml.DOMConfigurator;
import org.soitoolkit.commons.log.Log4jUtils;

/**
 * Utility methods for using log4j in a Java EE Servlet container
 * 
 * @author Tony Dalbrekt
 */
public final class Log4jWebUtils {
    public static Logger logger = Logger.getLogger(Log4jWebUtils.class);

    /**
     * Extension for parameter and property-name for "watch delay"
     */
    public static final String WATCH_DELAY_EXTENSION = ".watch.delay";

    /**
     * Extension for parameter and property-name for "watch disable"
     */
    public static final String WATCH_DISABLE_EXTENSION = ".watch.disable";

    /**
     * Key for {@code context-param} where the value is the name of the JVM-paramter
     * that points out {@code log4j.xml}.
     */
    public static final String CONTEXT_PARAM_LOG_CONFIG_KEY = "log4j.configuration.key";

    /**
     * Key for {@code context-param} used to activate and deactivate automatic relaod of log4j configuration files (default {@code true}). 
     * Valid values are {@code true/false}.
     */
    public static final String CONTEXT_PARAM_LOG_WATCH_DISABLE_KEY = Log4jUtils.DEFUALT_LOG_CONFIG
            + WATCH_DISABLE_EXTENSION;

    /**
     * Key for {@code context-param} to control how often log4j should check the configuration file for changes. (default 60000 ms = 1 minute).
     * Value specified in milliseconds.
     */
    public static final String CONTEXT_PARAM_LOG_WATCH_DELAY_KEY = Log4jUtils.DEFUALT_LOG_CONFIG
            + WATCH_DELAY_EXTENSION;

    /** Protocol prefix */
    public static final String LOG_FILE_PREFIX = "file:/";

    /**
     * Hidden constructor.
     */
    private Log4jWebUtils() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }

    public static boolean isWatchDisabled(ServletContext sc) {

        Boolean systemPropertyDisable = getIsWatchDisabledSystemProperty(getLogConfigurationKey(sc)
                + WATCH_DISABLE_EXTENSION);

        // Override web.xml config
        if (systemPropertyDisable != null) {
            return systemPropertyDisable.booleanValue();
        }

        Boolean contextParamDisable = getIsWatchDisabledContextParam(sc);

        if (contextParamDisable != null) {
            return contextParamDisable.booleanValue();
        }

        return false;
    }

    static Boolean getIsWatchDisabledContextParam(ServletContext sc) {
        try {

            String val = sc.getInitParameter(CONTEXT_PARAM_LOG_WATCH_DISABLE_KEY);

            if (val == null || (!val.equalsIgnoreCase("true") && !val.equalsIgnoreCase("false"))) {
                return null;
            }

            return Boolean.valueOf(val);

        } catch (Exception e) {
            return null;
        }
    }

    static Boolean getIsWatchDisabledSystemProperty(String paramName) {
        try {
            String val = System.getProperty(paramName);

            if (val == null || (!val.equalsIgnoreCase("true") && !val.equalsIgnoreCase("false"))) {
                return null;
            }

            return Boolean.valueOf(val);

        } catch (Exception e) {
            return null;
        }
    }

    public static long getWatchDelay(ServletContext sc) {

        Long systemPropertyDelay = getWatchDelaySystemProperty(getLogConfigurationKey(sc)
                + WATCH_DELAY_EXTENSION);

        // Override web.xml config
        if (systemPropertyDelay != null) {
            return systemPropertyDelay.longValue();

        }

        Long contextParamDelay = getWatchDelayContextParams(sc);

        if (contextParamDelay != null) {
            return contextParamDelay.longValue();

        }

        return Log4jUtils.DEFAULT_WATCH_DELAY;
    }

    static Long getWatchDelayContextParams(ServletContext sc) {
        try {
            Long delay = Long.valueOf(sc.getInitParameter(CONTEXT_PARAM_LOG_WATCH_DELAY_KEY));
            if (delay < 1) {
                return null;
            }
            return Long.valueOf(sc.getInitParameter(CONTEXT_PARAM_LOG_WATCH_DELAY_KEY));

        } catch (Exception e) {
            return null;
        }
    }

    static Long getWatchDelaySystemProperty(String paramName) {
        try {
            Long delay = Long.valueOf(System.getProperty(paramName));

            if (delay != null && delay < 1) {
                delay = null;
            }
            return delay;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isLog4jConfigurationRedifined(ServletContext sc) {
        return !Log4jUtils.DEFUALT_LOG_CONFIG.equals(getLogConfigurationKey(sc));
    }

    public static String getLogConfigurationKey(ServletContext sc) {
        String val = (String) sc.getInitParameter(CONTEXT_PARAM_LOG_CONFIG_KEY);
        if (val == null) {
            val = Log4jUtils.DEFUALT_LOG_CONFIG;
        }        
        LogLog.debug(CONTEXT_PARAM_LOG_CONFIG_KEY + " = " + val);
        return val;
    }

    public static String getLogConfigurationPath(ServletContext sc) {
    	String configFile = getLogConfigurationKey(sc);

        return configFile;
    }

    public static void reload(ServletContext sc) {
        Log4jUtils.reload(getLogConfigurationPath(sc));
    }

    public static void reloadAndWatch(ServletContext sc, long delay) {
        Log4jUtils.reloadAndWatch(getLogConfigurationPath(sc), delay);
    }
}
