package soi_toolkit_generator_plugin.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.soitoolkit.tools.generator.plugin.util.PreferencesUtil;

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
		store.setDefault(PreferenceConstants.P_ECLIPSE_GOAL, "eclipse:m2eclipse");
		store.setDefault(PreferenceConstants.P_SFTP_ROOT_FOLDER, PreferencesUtil.getDefaultSftpRootFolder());
		store.setDefault(PreferenceConstants.P_SFTP_IDENTITY_FILE, PreferencesUtil.getDefaultSftpIdentityFile());
		store.setDefault(PreferenceConstants.P_SFTP_IDENTITY_PASSPHRASE, PreferencesUtil.getDefaultSftpIdentityPassphrase());
		
/*
    static public String getDefaultRootFolder() {
    static public String getMavenHome() {
	static public String getCustomGroovyModelImpl() {
    static public String getDefaultSftpRootFolder() {
    static public String getDefaultSftpIdentityFile() {
    static public String getDefaultSftpIdentityPassphrase() {
 */
	}

}
