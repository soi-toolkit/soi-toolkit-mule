package soi_toolkit_generator_plugin.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import soi_toolkit_generator_plugin.Activator;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class SoiToolkiGeneratorPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public SoiToolkiGeneratorPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("A demonstration of a preference page implementation");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		addField(new DirectoryFieldEditor(PreferenceConstants.P_MAVEN_HOME, "&Maven home folder:", getFieldEditorParent()));

		addField(new DirectoryFieldEditor(PreferenceConstants.P_DEFAULT_ROOT_FOLDER,   "Default root folder:", getFieldEditorParent()));

		addField(new RadioGroupFieldEditor(PreferenceConstants.P_ECLIPSE_GOAL, "Maven Eclipse goal", 1,
			new String[][] { { "eclipse:eclipse", "eclipse:eclipse" }, {"eclipse:m2eclipse", "eclipse:m2eclipse" }
		}, getFieldEditorParent()));

		addField(new StringFieldEditor(PreferenceConstants.P_GROOVY_MODEL, "Custom Groovy model:", getFieldEditorParent()));

		addField(new StringFieldEditor(PreferenceConstants.P_SFTP_ROOT_FOLDER,         "Default SFTP root folder:", getFieldEditorParent()));
		addField(new FileFieldEditor(PreferenceConstants.P_SFTP_IDENTITY_FILE,         "SFTP private key file:", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.P_SFTP_IDENTITY_PASSPHRASE, "SFTP private key passphrase:", getFieldEditorParent()));
		
//		addField(
//			new BooleanFieldEditor(
//				PreferenceConstants.P_BOOLEAN,
//				"&An example of a boolean preference",
//				getFieldEditorParent()));

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}