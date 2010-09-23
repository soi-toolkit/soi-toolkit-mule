package org.soitoolkit.tools.generator.plugin.createcomponent;

import static org.soitoolkit.tools.generator.plugin.createcomponent.CreateComponentUtil.IM_SCHEMA_COMPONENT;
import static org.soitoolkit.tools.generator.plugin.createcomponent.CreateComponentUtil.INTEGRATION_COMPONENT;
import static org.soitoolkit.tools.generator.plugin.createcomponent.CreateComponentUtil.SD_SCHEMA_COMPONENT;
import static org.soitoolkit.tools.generator.plugin.createcomponent.CreateComponentUtil.UTILITY_COMPONENT;
import static org.soitoolkit.tools.generator.plugin.createcomponent.CreateComponentUtil.getComponentProjectName;

import java.io.File;
import java.net.URL;

import org.eclipse.jface.dialogs.IDialogPage;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.soitoolkit.tools.generator.plugin.model.ModelFactory;
import org.soitoolkit.tools.generator.plugin.util.PreferencesUtil;
import org.soitoolkit.tools.generator.plugin.util.SwtUtil;

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
		p = (componentType != INTEGRATION_COMPONENT) ? null : p;	
//		System.err.println("getNextPage() returns: " + ((p == null)? "NULL" : p.getTitle()));
		return p;
	}

//	@Override
//	public IWizardPage getPreviousPage() {
//		IWizardPage p = super.getPreviousPage();
//		System.err.println("getPreviousPage() returns: " + ((p == null)? "NULL" : p.getTitle()));
//		return p;
//	}
	
	private int componentType = INTEGRATION_COMPONENT;
	private Text artifactIdText;
	private Text groupIdText;
	private Text versionText;
	private Text rootFolderText;
	private Text mavenHomeText;
	private Text customGroovyModelImplText;

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
		
//		Label label = new Label(container, SWT.NULL);
//		label.setText("&Container:");
//
//		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
//		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
//		containerText.setLayoutData(gd);
//		containerText.addModifyListener(new ModifyListener() {
//			public void modifyText(ModifyEvent e) {
//				dialogChanged();
//			}
//		});

		addRadioButtons(container);

		addTextFields(container);
		
		initialize();
		dialogChanged();
		setControl(container);
	}

	public void addRadioButtons (final Composite parent) {

		final Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
//		layout.verticalSpacing = 9;

		Listener listener = new Listener () {
			public void handleEvent (Event e) {
				componentType = (Integer)e.widget.getData();
//				System.err.println("### Set selected component type to: " + componentType);

				Control [] children = container.getChildren ();
				for (int i=0; i<children.length; i++) {
					Control child = children [i];
					if (e.widget != child && child instanceof Button && (child.getStyle () & SWT.RADIO) != 0) {
						((Button) child).setSelection (false);
					}
				}
				((Button) e.widget).setSelection (true);
				
				dialogChanged();
			}
		};
		Label label = new Label(container, SWT.NULL);
		label.setText("&Type of component:");

		Button b = SwtUtil.createRadioButton(container, listener, INTEGRATION_COMPONENT, "Integration Component");
		b = SwtUtil.createRadioButton(container, listener, UTILITY_COMPONENT,     "Utility Component");
		b.setEnabled(false);
		b = SwtUtil.createRadioButton(container, listener, SD_SCHEMA_COMPONENT,   "Service Description Component");
		b = SwtUtil.createRadioButton(container, listener, IM_SCHEMA_COMPONENT,   "Information Model Component");
		b.setEnabled(false);
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

		// FIXME. Looks like crap :-)
		Label shadow_sep_1 = new Label(container, SWT.SEPARATOR | SWT.SHADOW_OUT | SWT.HORIZONTAL);
		shadow_sep_1.setBounds(50,80,100,50);
		Label shadow_sep_2 = new Label(container, SWT.SEPARATOR | SWT.SHADOW_OUT | SWT.HORIZONTAL);
		shadow_sep_2.setBounds(50,80,100,50);
		Label shadow_sep_3 = new Label(container, SWT.SEPARATOR | SWT.SHADOW_OUT | SWT.HORIZONTAL);
		shadow_sep_3.setBounds(50,80,200,50);
		
		// Maven home folder (label, text, browse-button)
		label = new Label(container, SWT.NULL);
		label.setText("&Maven home Folder:");

		mavenHomeText = new Text(container, SWT.BORDER | SWT.SINGLE);
		mavenHomeText.setEditable(false);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		mavenHomeText.setLayoutData(gd);
		mavenHomeText.addModifyListener(modifyListener);

		button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowseMavenHome();
			}
		});
		
		// Groowy model impl (label, text)		
		label = new Label(container, SWT.NULL);
		label.setText("&Groovy IModel impl.:");

		customGroovyModelImplText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		customGroovyModelImplText.setLayoutData(gd);
		customGroovyModelImplText.addModifyListener(modifyListener);
	}

	
	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
//		if (selection != null && selection.isEmpty() == false
//				&& selection instanceof IStructuredSelection) {
//			IStructuredSelection ssel = (IStructuredSelection) selection;
//			if (ssel.size() > 1)
//				return;
//			Object obj = ssel.getFirstElement();
//			if (obj instanceof IResource) {
//				IContainer container;
//				if (obj instanceof IContainer)
//					container = (IContainer) obj;
//				else
//					container = ((IResource) obj).getParent();
//				containerText.setText(container.getFullPath().toString());
//			}
//		}
		artifactIdText.setText("sample1");
		groupIdText.setText("org.sample");
		versionText.setText("1.0-SNAPSHOT");
		rootFolderText.setText(PreferencesUtil.getDefaultRootFolder());
		mavenHomeText.setText(PreferencesUtil.getMavenHome());
		customGroovyModelImplText.setText(PreferencesUtil.getCustomGroovyModelImpl());
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	public void handleBrowseRootFolder() {
		DirectoryDialog dialog = new DirectoryDialog (getShell());
		String platform = SWT.getPlatform();
		dialog.setFilterPath (platform.equals("win32") || platform.equals("wpf") ? "c:\\" : "/");

//		FileDialog dialog = new FileDialog (getShell(), SWT.SAVE);
//		FileSelectionDialog dialog = 
//			new FileSelectionDialog(getShell(), null, "Select ML-folder");
//		dialog.setInitialSelections();
		String foldername = dialog.open();
		System.err.println("RESULT ROOT_FOLDER = " + foldername);
		rootFolderText.setText(foldername);		
		
/*		
		Display display = new Display ();
		Shell shell = new Shell (display);
		shell.open ();
		FileDialog dialog = new FileDialog (shell, SWT.SAVE);
		String [] filterNames = new String [] {"Image Files", "All Files (*)"};
		String [] filterExtensions = new String [] {"*.gif;*.png;*.xpm;*.jpg;*.jpeg;*.tiff", "*"};
		String filterPath = "/";
		String platform = SWT.getPlatform();
		if (platform.equals("win32") || platform.equals("wpf")) {
			filterNames = new String [] {"Image Files", "All Files (*.*)"};
			filterExtensions = new String [] {"*.gif;*.png;*.bmp;*.jpg;*.jpeg;*.tiff", "*.*"};
			filterPath = "c:\\";
		}
		dialog.setFilterNames (filterNames);
		dialog.setFilterExtensions (filterExtensions);
		dialog.setFilterPath (filterPath);
		dialog.setFileName ("myfile");
		String filename = dialog.open ();
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();
		containerText.setText(filename);
*/
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
		
//		System.err.println("### UPDATE BUTTONS!!!");
		// TODO: Needs to be cleaned up, this design is tied to a two page design and can't handle a third page, e.g. used for some other component type...

		// If creating an integration component then force viewing page #2 otherwise mark it as completed
		CreateIntegrationComponentPage p = ((CreateComponentWizard)getWizard()).getCreateIntegrationComponentPage();
		p.setMustBeDisplayed(componentType == INTEGRATION_COMPONENT);
		getContainer().updateButtons();

		
//		IResource container = ResourcesPlugin.getWorkspace().getRoot()
//				.findMember(new Path(getContainerName()));
		String rootFolderName = getRootFolder();

//		if (getContainerName().length() == 0) {
//			updateStatus("File container must be specified");
//			return;
//		}
//		if (container == null
//				|| (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
//			updateStatus("File container must exist");
//			return;
//		}
//		if (!container.isAccessible()) {
//			updateStatus("Project must be writable");
//			return;
//		}
		if (rootFolderName.length() == 0) {
			updateStatus("The root folder must be specified");
			return;
		}

		File rootFolder = new File(rootFolderName);
		if (!rootFolder.isDirectory()) {
			updateStatus("The root folder must be an existing folder");
			return;
		}
		
		String projectFolderName = getComponentProjectName(componentType, getGroupId(), getArtifactId());
		
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
			updateStatus("The maven home folder must be an existing folder");
			return;
		}
		
		File mvn = new File(getMavenHome() + "/bin/mvn" + (SwtUtil.isWindows() ? ".bat" : ""));
		if (!mvn.isFile()) {
			updateStatus("The maven executable can't be found at: " + mvn.getAbsolutePath());
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
				updateStatus("Invalid Groovy class for a custom model, error: " + ex);
				return;
			}
		}

		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

//	public String getContainerName() {
//		return containerText.getText();
//	}

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
		return componentType;
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