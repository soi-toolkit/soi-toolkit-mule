package org.soitoolkit.tools.generator.plugin.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;


public class PreferencesUtil {

	/**
     * Hidden constructor.
     */
    private PreferencesUtil() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }

    static public String getDefaultRootFolder() {
    	return get("root_folder", System.getProperty("user.home"));
    }
    
    static public String getMavenHome() {
    	return get("maven_home", System.getProperty("user.home"));
    }
    
	static public String getCustomGroovyModelImpl() {
    	return get("custom_groovy_model_impl", "");
	}

    static public String getDefaultSftpRootFolder() {
    	return get("sftp_root_folder", "muletest1@localhost/~/sftp");
    }
    
    static public String getDefaultSftpIdentityFile() {
    	return get("sftp_identity_file", System.getProperty("user.home") + "/.ssh/id_dsa");
    }
    
    static public String getDefaultSftpIdentityPassphrase() {
    	return get("sftp_identity_passphrase", System.getProperty("user.home") + "nnn");
    }

	// -----------------
	
    static private ResourceBundle defaultPreferences = null;
    
    static private ResourceBundle getDefaultPreferences() {
    	if (defaultPreferences == null) {
    		defaultPreferences = ResourceBundle.getBundle("soi_toolkit_generator_plugin_default_preferences");
    	}
    	return defaultPreferences;
    }

    static private String get(String key, String defaultValue) {
    	try {
			String value = getDefaultPreferences().getString(key);
			System.err.println("PREFUTIL: " + key + " = [" + value + "]");
			return (value.trim().length() > 0) ? value : defaultValue;
		} catch (MissingResourceException mre) {
			return defaultValue;
		}
    }
}