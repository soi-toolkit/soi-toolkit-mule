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
package soi_toolkit_generator_plugin.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.soitoolkit.tools.generator.util.PreferencesUtil;

import soi_toolkit_generator_plugin.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_DEFAULT_ROOT_FOLDER, PreferencesUtil.getDefaultRootFolder());
		store.setDefault(PreferenceConstants.P_MAVEN_HOME, PreferencesUtil.getMavenHome());
		store.setDefault(PreferenceConstants.P_GROOVY_MODEL, PreferencesUtil.getCustomGroovyModelImpl());
		store.setDefault(PreferenceConstants.P_ECLIPSE_GOAL, "eclipse:eclipse");
		store.setDefault(PreferenceConstants.P_SFTP_ROOT_FOLDER, PreferencesUtil.getDefaultSftpRootFolder());
		
/*
    static public String getDefaultRootFolder() {
    static public String getMavenHome() {
	static public String getCustomGroovyModelImpl() {
    static public String getDefaultSftpRootFolder() {
 */
	}

}
