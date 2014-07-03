package org.soitoolkit.tools.generator.plugin.createcomponent;

import static org.soitoolkit.tools.generator.plugin.util.SwtUtil.addRadioButtons;

import java.io.File;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.soitoolkit.tools.generator.plugin.util.ValueHolder;

public class CreateServiceDescriptionComponentPage extends WizardPage {

	private ISelection selection;
	
	private Text wsdlResourceText;
//	private Button wsdlFileButton;
	private Button wsdlFolderButton;
	
	private ValueHolder<Integer> wsdlSelectionType = new ValueHolder<Integer>(0);
	
	public void setMustBeDisplayed(boolean mustBeDisplayed) {
		this.mustBeDisplayed = mustBeDisplayed;
		dialogChanged();
	}

	private boolean mustBeDisplayed = false;
	
	public CreateServiceDescriptionComponentPage(ISelection selection) {
		super("wizardPage");
		setTitle("SOI Toolkit - Create a new component, page 2");
		setDescription("Configuration the service description component");
		setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "component-large.png"));
		
		this.selection = selection;
	}
	
	@Override
	public void setPageComplete(boolean complete) {
		super.setPageComplete(complete);
	}
	
	@Override
	public void createControl(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 4;
		layout.verticalSpacing = 9;
		
		// WSDL Options - Existing/Dummy
		addRadioButtons(new String[] {"Existing WSDL(s)", "Generate a sample WSDL"}, "&Use existing WSDL(s) or generate a sample WSDL:", wsdlSelectionType, container, new Listener () {
			public void handleEvent (Event e) {
				
				if (wsdlSelectionType.value.intValue() == 0) {
					wsdlResourceText.setEditable(true);
					wsdlResourceText.setEnabled(true);
					wsdlFolderButton.setEnabled(true);
					//wsdlFileButton.setEnabled(true);
					
				} else {
					wsdlResourceText.setEditable(false);
					wsdlResourceText.setEnabled(false);
					wsdlFolderButton.setEnabled(false);
					//wsdlFileButton.setEnabled(false);
					wsdlResourceText.setText("");
				}
				dialogChanged();
			}
		});

		ModifyListener modifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		};
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);	
		wsdlResourceText = new Text(container, SWT.BORDER | SWT.SINGLE);
		wsdlResourceText.setEditable(true);
		wsdlResourceText.setLayoutData(gd);
		wsdlResourceText.addModifyListener(modifyListener);
		
		wsdlFolderButton = new Button(container, SWT.PUSH);
		wsdlFolderButton.setText("Folder...");
		wsdlFolderButton.setEnabled(true);
		wsdlFolderButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowseFolder();
			}
		});
		
//		wsdlFileButton = new Button(container, SWT.PUSH);
//		wsdlFileButton.setText("File...");
//		wsdlFileButton.setEnabled(true);
//		wsdlFileButton.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				handleBrowseFile();
//			}
//		});
		
		wsdlResourceText.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				dialogChanged();
			}
			
			@Override
			public void focusGained(FocusEvent e) {
			}
		});
		
		dialogChanged();
		setControl(container);
	}
	
	public void handleBrowseFolder() {
		
		DirectoryDialog dialog = new DirectoryDialog (getShell());
		String platform = SWT.getPlatform();
		dialog.setFilterPath (platform.equals("win32") || platform.equals("wpf") ? "c:\\" : "/");

		String foldername = dialog.open();
		
		if (foldername == null) {
			wsdlResourceText.setText("");
		} else {
			wsdlResourceText.setText(foldername);
		}
	}
	
//	public void handleBrowseFile() {
//		FileDialog dialog = new FileDialog(getShell());
//		
//		//DirectoryDialog dialog = new DirectoryDialog (getShell());
//		String platform = SWT.getPlatform();
//		dialog.setFilterPath (platform.equals("win32") || platform.equals("wpf") ? "c:\\" : "/");
//		dialog.setFilterExtensions(new String[] {"*.wsdl"});
//		
//		String filemane = dialog.open();
//		
//		if (filemane == null) {
//			wsdlResourceText.setText("");
//		} else {
//			wsdlResourceText.setText(filemane);
//		}
//	}
	
	public String getWsdlResource() {
		if (wsdlResourceText == null)
			return "";
		return wsdlResourceText.getText();
	}
	
	private void dialogChanged() {
		
		IWizardPage current = getWizard().getContainer().getCurrentPage();
		
		if (current.equals(this) || (current.getNextPage() != null && current.getNextPage().equals(this))) {
			if (wsdlSelectionType.value.intValue() == 0) {
				String resource = getWsdlResource();
				
				if (resource.length() == 0) {
					updateStatus("No WSDL resource(s) specified, specify either a folder or file.");
					return;
				}

				File file = new File(resource);
				if (!file.exists()) {
					updateStatus("The resource does not exist.");
					return;
				}
			}
		}
		updateStatus(null);
	}
	
	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete((message == null) && !mustBeDisplayed);
	}

}
