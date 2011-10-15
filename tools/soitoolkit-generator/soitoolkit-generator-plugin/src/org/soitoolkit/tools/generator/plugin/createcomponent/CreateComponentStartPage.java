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
package org.soitoolkit.tools.generator.plugin.createcomponent;

import static org.soitoolkit.tools.generator.model.enums.ComponentEnum.INTEGRATION_COMPONENT;
import static org.soitoolkit.tools.generator.model.enums.ComponentEnum.UTILITY_COMPONENT;
import static org.soitoolkit.tools.generator.model.enums.MavenEclipseGoalEnum.ECLIPSE_M2ECLIPSE;
import static org.soitoolkit.tools.generator.plugin.createcomponent.CreateComponentUtil.getComponentProjectName;
import static org.soitoolkit.tools.generator.plugin.util.SwtUtil.addRadioButtons;
import static soi_toolkit_generator_plugin.preferences.PreferenceConstants.P_DEFAULT_ROOT_FOLDER;
import static soi_toolkit_generator_plugin.preferences.PreferenceConstants.P_ECLIPSE_GOAL;
import static soi_toolkit_generator_plugin.preferences.PreferenceConstants.P_GROOVY_MODEL;
import static soi_toolkit_generator_plugin.preferences.PreferenceConstants.P_MAVEN_HOME;

import java.io.File;
import java.net.URL;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.soitoolkit.tools.generator.model.ModelFactory;
import org.soitoolkit.tools.generator.model.enums.ComponentEnum;
import org.soitoolkit.tools.generator.model.enums.EnumUtil;
import org.soitoolkit.tools.generator.model.enums.MavenEclipseGoalEnum;
import org.soitoolkit.tools.generator.plugin.util.SwtUtil;
import org.soitoolkit.tools.generator.plugin.util.ValueHolder;

import soi_toolkit_generator_plugin.Activator;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */

public class CreateComponentStartPage extends WizardPage {

	@Override
	public IWizardPage getNextPage() {
		// TODO: Needs to be cleaned up, this design is tied to a two page design and can't handle a third page, e.g. used for some other component type...
		WizardPage p = (WizardPage)super.getNextPage();
		ComponentEnum compEnum = ComponentEnum.get(componentType.value);
		p = (compEnum != INTEGRATION_COMPONENT) ? null : p;	
		return p;
	}

	private ValueHolder<Integer> componentType = new ValueHolder<Integer>(INTEGRATION_COMPONENT.ordinal());
	private ValueHolder<Integer> mavenEclipseGoalType = new ValueHolder<Integer>(ECLIPSE_M2ECLIPSE.ordinal());
	
	private Text artifactIdText;
	private Text groupIdText;
	private Text versionText;
	private Text rootFolderText;
	private Text mavenHomeText;
	private Text customGroovyModelImplText;
//	private Text mavenEclipseGoalText;

	@SuppressWarnings("unused")
	private ISelection selection;

	/**
	 * Constructor for CreateComponentStartPage.
	 * 
	 * @param pageName
	 */
	public CreateComponentStartPage(ISelection selection) {
		super("wizardPage");

		setTitle("SOI Toolkit - Create a new component");
		setDescription("This code generator creates a new component");
		setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "component-large.png"));
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		layout.verticalSpacing = 9;
		
		addRadioButtons(EnumUtil.getLabels(ComponentEnum.values()), "&Type of component:", componentType, container, new Listener () {
			public void handleEvent (Event e) {
				dialogChanged();
			}
		});

		addTextFields(container);
		
		initialize();
		dialogChanged();
		setControl(container);
	}

	private void addTextFields(Composite parent) {

		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		
		ModifyListener modifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		};

		artifactIdText = SwtUtil.createTextField(container, "&Artifact Id:", modifyListener);
		groupIdText = SwtUtil.createTextField(container, "&Group Id:", modifyListener);
		versionText = SwtUtil.createTextField(container, "&Version:", modifyListener);
				

		// Root folder (label, text, browse-button)
		Label label = new Label(container, SWT.NULL);
		label.setText("&Root Folder:");

		rootFolderText = new Text(container, SWT.BORDER | SWT.SINGLE);
		rootFolderText.setEditable(false);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		rootFolderText.setLayoutData(gd);
		rootFolderText.addModifyListener(modifyListener);

		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowseRootFolder();
			}
		});

		label = new Label(container, SWT.NULL);
		label.setText("Preferences:");
		label = new Label(container, SWT.NULL);
		label.setText("(read-only here, updated in the Preferences-page)");
		new Label(container, SWT.NULL); // FIXME Stupid filler...
		
		// Maven home folder (label, text, browse-button)
		label = new Label(container, SWT.NULL);
		label.setText("&Maven home Folder:");

		mavenHomeText = new Text(container, SWT.BORDER | SWT.SINGLE);
		mavenHomeText.setEnabled(false);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		mavenHomeText.setLayoutData(gd);
		new Label(container, SWT.NULL); // FIXME Stupid filler...
		
		// Groovy model impl (label, text)		
		label = new Label(container, SWT.NULL);
		label.setText("Custom Groovy model:");

		customGroovyModelImplText = new Text(container, SWT.BORDER | SWT.SINGLE);
		customGroovyModelImplText.setEnabled(false);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		customGroovyModelImplText.setLayoutData(gd);
		new Label(container, SWT.NULL); // FIXME Stupid filler...
		
//		// Maven eclispe goal (label, text)		
//		label = new Label(container, SWT.NULL);
//		label.setText("Maven Eclipse goal:");
//
//		mavenEclipseGoalText = new Text(container, SWT.BORDER | SWT.SINGLE);
//		mavenEclipseGoalText.setEnabled(false);
//		gd = new GridData(GridData.FILL_HORIZONTAL);
//		mavenEclipseGoalText.setLayoutData(gd);
////		mavenEclipseGoalText(modifyListener);
//		new Label(container, SWT.NULL); // FIXME Stupid filler...
	}

	
	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
		// TODO: Add preferences for a+g+v + parent-a+g+v!
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		artifactIdText.setText("sample1");
		groupIdText.setText("org.sample");
		versionText.setText("1.0.0-SNAPSHOT");
		rootFolderText.setText(prefs.getString(P_DEFAULT_ROOT_FOLDER));
		mavenHomeText.setText(prefs.getString(P_MAVEN_HOME));
		customGroovyModelImplText.setText(prefs.getString(P_GROOVY_MODEL));

		// Moving away from eclipse:m2eclipse
//		String eclipseGoal = prefs.getString(P_ECLIPSE_GOAL);
//		mavenEclipseGoalText.setText(eclipseGoal);
//		if (eclipseGoal.equals("eclipse:m2eclipse")) {
//			mavenEclipseGoalType.value = MavenEclipseGoalEnum.ECLIPSE_M2ECLIPSE.ordinal();
//		} else {
//			mavenEclipseGoalType.value = MavenEclipseGoalEnum.ECLIPSE_ECLIPSE.ordinal();
//		}
		mavenEclipseGoalType.value = MavenEclipseGoalEnum.ECLIPSE_ECLIPSE.ordinal();
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	public void handleBrowseRootFolder() {
		DirectoryDialog dialog = new DirectoryDialog (getShell());
		String platform = SWT.getPlatform();
		dialog.setFilterPath (platform.equals("win32") || platform.equals("wpf") ? "c:\\" : "/");

		String foldername = dialog.open();
		System.err.println("RESULT ROOT_FOLDER = " + foldername);
		rootFolderText.setText(foldername);		
	}

	public void handleBrowseMavenHome() {
		DirectoryDialog dialog = new DirectoryDialog (getShell());
		dialog.setFilterPath (SwtUtil.isWindows() ? "c:\\" : "/");

		String foldername = dialog.open();
		System.err.println("RESULT MAVEN_HOME = " + foldername);
		mavenHomeText.setText(foldername);		
	}

	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {
		
		// TODO: Needs to be cleaned up, this design is tied to a two page design and can't handle a third page, e.g. used for some other component type...

		if (componentType.value == UTILITY_COMPONENT.ordinal()) {
			updateStatus("Utility components are not yet supported");
			return;
		}

		String artifactId = getArtifactId();
		if (artifactId == null) {
			updateStatus("Missing artifact id");
			return;
		}

		if (artifactId.toLowerCase().startsWith("test")) {
			updateStatus("Artifact id should not start with \"test\" since it will confuse JUnit to think that non unit-test classes are unit-test classes");
			return;
		}

		// If creating an integration component then force viewing page #2 otherwise mark it as completed
		ComponentEnum compEnum = ComponentEnum.get(componentType.value);
		CreateIntegrationComponentPage p = ((CreateComponentWizard)getWizard()).getCreateIntegrationComponentPage();
		p.setMustBeDisplayed(compEnum == INTEGRATION_COMPONENT);
		getContainer().updateButtons();

		String rootFolderName = getRootFolder();

		if (rootFolderName.length() == 0) {
			updateStatus("The root folder must be specified");
			return;
		}

		File rootFolder = new File(rootFolderName);
		if (!rootFolder.isDirectory()) {
			updateStatus("The root folder must be an existing folder");
			return;
		}
		
		String projectFolderName = getComponentProjectName(componentType.value, getGroupId(), getArtifactId());
		
		System.err.println("comp-type: " + componentType + ", proj-namn: " + projectFolderName);
		if (projectFolderName != null) {
			File projectFolder = new File(rootFolderName + "/" + projectFolderName);
			if (projectFolder.exists()) {
				updateStatus("Project folder [" + projectFolderName + "] already exists in root folder [" + rootFolderName + "], select a name of a non-existing folder");
				return;
			}
		}
		
		// TODO: Also assert that a project with the selected name doesn't already exist in the workspace

		File mvnHome = new File(getMavenHome());
		if (!mvnHome.isDirectory()) {
			updateStatus("The maven home folder must be an existing folder, update in the soi-toolkit preferences page");
			return;
		}
		
		File mvn = new File(getMavenHome() + "/bin/mvn" + (SwtUtil.isWindows() ? ".bat" : ""));
		if (!mvn.isFile()) {
			updateStatus("The maven executable can't be found at: " + mvn.getAbsolutePath() + ", update in the soi-toolkit preferences page");
			return;
		}
		
		// Validate custom model by setting it on the model-factory-class
		String groovyClass = getCustomGroovyModelImpl();
		if (groovyClass == null || groovyClass.trim().length() == 0) {
			System.err.println("### Empty groovy-classname, reset model");
			ModelFactory.resetModelClass();
		} else {
			try {
				System.err.println("### Setting groovy-classname: " + groovyClass);
				ModelFactory.setModelGroovyClass(new URL(groovyClass));
			} catch (Throwable ex) {
				ModelFactory.resetModelClass();
				updateStatus("Invalid Groovy class for a custom model (update in the soi-toolkit preferences page), error: " + ex);
				return;
			}
		}

		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getRootFolder() {
		return rootFolderText.getText();
	}

	public String getMavenHome() {
		return mavenHomeText.getText();
	}

	public String getCustomGroovyModelImpl() {
		return customGroovyModelImplText.getText();
	}

	public int getComponentType() {
		return componentType.value;
	}

	public int getMavenEclipseGoalType() {
		return mavenEclipseGoalType.value;
	}

	public String getArtifactId() {
		return artifactIdText.getText();
	}
	public String getGroupId() {
		return groupIdText.getText();
	}
	public String getVersion() {
		return versionText.getText();
	}
}