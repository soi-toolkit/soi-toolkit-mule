package org.soitoolkit.tools.generator.plugin.util;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */

public class StatusPage extends WizardPage {


	private StyledText statusTextArea;

	private ISelection selection;

	/**
	 * Constructor for StatusPage.
	 * 
	 * @param pageName
	 */
	public StatusPage(ISelection selection) {
		super("wizardPage");
		setTitle("SOI Toolkit - Create a new component, work in progress...");
		setDescription("Output from creating the new component");
		setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "component-large.png"));
		this.selection = selection;
	}

	public void writeLine(final String line) {
		getContainer().getShell().getDisplay().syncExec(
			new Runnable() {
				public void run() {
					if (statusTextArea != null) {
						statusTextArea.append(line + '\n'); 
						// TODO: Display the last line, the following did not help :-)
						// statusTextArea.setCaretOffset(statusTextArea.getCharCount());
					}
				}
			});
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

		statusTextArea = new StyledText(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL
				| SWT.H_SCROLL);
		statusTextArea.setLayoutData(new GridData(GridData.FILL_BOTH
				| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
		statusTextArea.setEditable(false);
		
		// TODO: How to pack?
//		container.pack();
		
		initialize();
		dialogChanged();
		setControl(container);
	}


	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
	}


	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete((message == null));
	}
	
}