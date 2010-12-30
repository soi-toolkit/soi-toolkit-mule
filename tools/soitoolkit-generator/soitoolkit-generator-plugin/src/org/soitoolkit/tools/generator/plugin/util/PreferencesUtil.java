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
    	return get("root_folder", getUserHome());
    }
    
    static public String getMavenHome() {
    	return get("maven_home", getUserHome());
    }
    
	static public String getCustomGroovyModelImpl() {
    	return get("custom_groovy_model_impl", "");
	}

    static public String getDefaultFileRootFolder() {
    	return get("file_root_folder", getUserHome() + "/soitoolkit/file-transport");
    }
    
    static public String getDefaultFtpRootFolder() {
    	return get("ftp_root_folder", "muletest1:muletest1@localhost/~/ftp");
    }
    
    static public String getDefaultSftpRootFolder() {
    	return get("sftp_root_folder", "muletest1@localhost/~/sftp");
    }
    
    static public String getDefaultSftpIdentityFile() {
    	return get("sftp_identity_file", getUserHome() + "/.ssh/id_dsa");
    }
    
    static public String getDefaultSftpIdentityPassphrase() {
    	return get("sftp_identity_passphrase", "nnn");
    }

    static public String getDefaultArchiveFolder() {
    	return get("archive_folder", getUserHome() + "/soitoolkit/archive");
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
			return (value.trim().length() > 0) ? value : defaultValue;
		} catch (MissingResourceException mre) {
			return defaultValue;
		}
    }
    
    static private String getUserHome() {
		return System.getProperty("user.home").replace('\\', '/');
    }
}