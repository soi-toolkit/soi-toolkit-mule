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
package org.soitoolkit.tools.generator.plugin.createservice;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.soitoolkit.tools.generator.plugin.model.enums.EnumUtil;
import org.soitoolkit.tools.generator.plugin.model.enums.MepEnum;
import org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum;
import org.soitoolkit.tools.generator.plugin.model.impl.ModelUtil;
import org.soitoolkit.tools.generator.plugin.util.SwtUtil;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */

public class CreateServicePage extends WizardPage {

	public boolean isJavaIdentifier(String str) {
		int n = str.length();
		if (n == 0) return false;
		if (!Character.isJavaIdentifierStart(str.charAt(0))) return false;
		for (int i = 1; i < n; i++)
			if (!Character.isJavaIdentifierPart(str.charAt(i))) return false;
		return true;
	}

	@SuppressWarnings("unused")
	private String getSelectedValue(Combo combo) {
		try {
			return combo.getItem(combo.getSelectionIndex());
		} catch (Exception e) {
			return e + " for index = " + combo.getSelectionIndex();
		}
	}

//	public enum MepReqRespInbTranspEnum implements LabeledEnum { 
//		MEP_REQUEST_RESPONSE("Request/Response"), MEP_ONE_WAY("One Way"), MEP_PUBLISH_SUBSCRIBE("Publish/Subscribe"); 
//
//		public static MepReqRespInbTranspEnum get(int ordinal) {
//			return values()[ordinal];
//		}
//
//		private String label;
//		private MepReqRespInbTranspEnum(String label) {
//			this.label = label;
//		}
//		public String getLabel() {return label;}
//	}
	
	private Combo mepCombo;
	private Combo inboundTransportCombo;
	private Combo outboundTransportCombo;

	private int  selectedMep;	
	private int  selectedInboundTransport;	
	private int  selectedOutboundTransport;	
//	private int  serviceType;	
	private Text projectText;
	private Text serviceText;

	private ISelection selection;

	/**
	 * Constructor for CreateServicePage.
	 * 
	 * @param pageName
	 */
	public CreateServicePage(ISelection selection) {
		super("wizardPage");
		setTitle("SOI Toolkit - Create a new service");
		setDescription("This code generator creates files for a new service in the selected integration componentens project.");
		setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "service-large.png"));
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		
		/*
		 * valbar kod
		 * - service def
		 * - component ?
		 * - transformer
		 * 
		 * - unit test
		 * - test consumer
		 * - test producer
		 * 
		 * - jmeter loadtest
		 * 
		 * + instruktion
		 * - lŠgg till i service conf + unit test conf
		 * - lŠgg till i properties
		 */
		try {
			Composite container = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			container.setLayout(layout);
			layout.numColumns = 1;
			layout.verticalSpacing = 9;
			
			addRadioButtons(container);
			
//			addTextFields(container);

			initialize();
			dialogChanged();
			setControl(container);
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void addRadioButtons (final Composite parent) {

		final Composite container = SwtUtil.createGridContainer(parent, 2);

//		Listener listener = new Listener() {
//			public void handleEvent (Event e) {
//				serviceType = (Integer)e.widget.getData();
//				System.err.println("### Set selected component type to: " + serviceType);
//
//				Control [] children = container.getChildren ();
//				for (int i=0; i<children.length; i++) {
//					Control child = children [i];
//					if (e.widget != child && child instanceof Button && (child.getStyle () & SWT.TOGGLE) != 0) {
//						((Button) child).setSelection (false);
//					}
//				}
//				((Button) e.widget).setSelection (true);
//			}
//		};

		Label label = new Label(container, SWT.NULL);
		label.setText("Message Exchange Pattern:");

		mepCombo = new Combo (container, SWT.READ_ONLY);
//		patternCategoryCombo.setItems (new String [] {"Request/Response Service", "Fire and Forget Service", "File Transfer"});

		mepCombo.setItems (EnumUtil.getLabels(MepEnum.values()));
		
		label = new Label(container, SWT.NULL);
		label.setText("&Inbound Transport:");
		inboundTransportCombo = new Combo (container, SWT.READ_ONLY);
		inboundTransportCombo.setItems (new String [] {"Please select a MEP first!", ""});
	
		label = new Label(container, SWT.NULL);
		label.setText("&Outbound Transport:");
		outboundTransportCombo = new Combo (container, SWT.READ_ONLY);
		outboundTransportCombo.setItems (new String [] {"Please select inbound transport first!", ""});
		
		/*
		 * Mep-Combo-Listener
		 */
		SelectionListener mepComboListener = new SelectionListener () {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				System.err.println("### Set default selected selectedMep");
				Combo c = (Combo)e.widget;
				selectedMep =  c.getSelectionIndex();
				System.err.println("### Set default selected selectedMep to: " + selectedMep);
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				System.err.println("### Set selected selectedMep");
				Combo c = (Combo)e.widget;
				selectedMep =  c.getSelectionIndex();
				System.err.println("### Set selected selectedMep to: " + selectedMep);

				switch (MepEnum.get(c.getSelectionIndex())) {
				case MEP_REQUEST_RESPONSE:
//					patternTypeCombo.setItems (new String [] {"SOAP/HTTPS(S) with SOAP/HTTP(S) adapter",
//							"SOAP/HTTPS(S) with JMS adapter",
//							"SOAP/HTTPS(S) with REST/HTTP(S) adapter"});
					inboundTransportCombo.setItems  (new String [] {"SOAP"});
					outboundTransportCombo.setItems (new String [] {"SOAP", "REST", "JMS", "JDBC"});
					break;
				case MEP_ONE_WAY:
//					patternTypeCombo.setItems (new String [] {"JMS with JMS adapter",
//							"JMS with JDBC adapter",
//							"JMS with file adapter"});
					inboundTransportCombo.setItems  (new String [] {"JMS", "SFTP", "HTTP (Multipart POST)"}); // , "JDBC"
					outboundTransportCombo.setItems (new String [] {"JMS", "SFTP"}); // , "JDBC"
					break;
				case MEP_PUBLISH_SUBSCRIBE:
					inboundTransportCombo.setItems  (new String [] {"JMS"});
					outboundTransportCombo.setItems (new String [] {"JMS"});
					break;
//				case 3:
//					inboundTransportCombo.setItems (new String [] {"SFTP to SFTP",
//							"SFTP to JDBC",
//							"SFTP to JMS"});
//					break;
				}

				dialogChanged();

			}
		};
		mepCombo.addSelectionListener(mepComboListener);

		/*
		 * Inbound-Transport-Combo-Listener
		 */
		SelectionListener inboundTransportComboListener = new SelectionListener () {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				System.err.println("### Set default selected selectedInboundTransport");
				Combo c = (Combo)e.widget;
				selectedInboundTransport = c.getSelectionIndex();
				System.err.println("### Set default selected selectedInboundTransport to: " + selectedInboundTransport);
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				System.err.println("### Set selected selectedInboundTransport");
				Combo c = (Combo)e.widget;
				selectedInboundTransport = c.getSelectionIndex();
				System.err.println("### Set selected selectedInboundTransport type to: " + selectedInboundTransport);

//				int inbTransp = c.getSelectionIndex();
//				switch (MepEnum.get(mepCombo.getSelectionIndex())) {
//				case MEP_REQUEST_RESPONSE:
//					break;
//				case MEP_ONE_WAY:
//					outboundTransportCombo.setItems (new String [] {c.getItem(inbTransp)});
//					break;
//				case MEP_PUBLISH_SUBSCRIBE:
//					break;
//				}

				dialogChanged();

			}
		};
		inboundTransportCombo.addSelectionListener(inboundTransportComboListener);

		/*
		 * Outbound-Transport-Combo-Listener
		 */
		SelectionListener outboundTransportComboListener = new SelectionListener () {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				System.err.println("### Set default selected selectedOutboundTransport");
				Combo c = (Combo)e.widget;
				selectedOutboundTransport = c.getSelectionIndex();
				System.err.println("### Set default selected selectedOutboundTransport to: " + selectedOutboundTransport);
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				System.err.println("### Set selected selectedOutboundTransport");
				Combo c = (Combo)e.widget;
				selectedOutboundTransport = c.getSelectionIndex();
				System.err.println("### Set selected selectedOutboundTransport type to: " + selectedOutboundTransport);

				dialogChanged();

			}
		};
		outboundTransportCombo.addSelectionListener(outboundTransportComboListener);

//	}
//
//	private void addTextFields(Composite parent) {
//
//		Composite container = SwtUtil.createGridContainer(parent, 2);
////		Composite container = new Composite(parent, SWT.NULL);
////		GridLayout layout = new GridLayout();
////		container.setLayout(layout);
////		layout.numColumns = 3;
////		layout.verticalSpacing = 9;

		label = new Label(container, SWT.NULL);
		label.setText("&Project:");

		Composite projectContainer = SwtUtil.createGridContainer(container, 2);

		projectText = new Text(projectContainer, SWT.BORDER | SWT.SINGLE);
		projectText.setText("                  "); // TODO: Otherwise it is shrinked to mimimum width...
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		projectText.setLayoutData(gd);
		projectText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		Button button = new Button(projectContainer, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		label = new Label(container, SWT.NULL);
		label.setText("&Service name:");

		serviceText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		serviceText.setLayoutData(gd);
		serviceText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
	}


	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource) {
				IContainer container;
				if (obj instanceof IContainer)
					container = (IContainer) obj;
				else
					container = ((IResource) obj).getParent();
				projectText.setText(container.getFullPath().toString());
			}
		}
		serviceText.setText("mySample");
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Select new file container");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				projectText.setText(((Path) result[0]).toString());
			}
		}
	}

	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {

		// Validate MEP
		int mepIdx = mepCombo.getSelectionIndex();
		if (mepIdx == -1) {
			updateStatus("Select a message exchenge pattern");
			return;
		}
		
		// Validate Inbound Transport
		int ibtIdx = inboundTransportCombo.getSelectionIndex();
		if (ibtIdx == -1) {
			updateStatus("Select an inbound transport");
			return;
		}
		
		// Validate Outbound Transport
		int obtIdx = outboundTransportCombo.getSelectionIndex();
		if (obtIdx == -1) {
			updateStatus("Select an outbound transport");
			return;
		}
		
		String containerName = getContainerName().trim();
		IResource container = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(containerName));
		String serviceName = getServiceName();

		if (containerName.length() == 0) {
			updateStatus("Select a project");
			return;
		}
		if (container == null || (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
			updateStatus("Selected project must exist");
			return;
		}
		if (!container.isAccessible()) {
			updateStatus("Selected project must be writable");
			return;
		}
		if (serviceName.length() == 0) {
			updateStatus("Name of the service must be specified");
			return;
		}
		if (!isJavaIdentifier(ModelUtil.makeJavaName(serviceName))) {
			updateStatus("Service name must be a valid Java identifier");
			return;
		}
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerName() {
		return projectText.getText();
	}

	public int getSelectedMep() {
		return selectedMep;
	}

	public TransportEnum getSelectedInboundTransport() {
		
		TransportEnum t = null;
		switch (selectedInboundTransport) {
			case 0: 
				t = TransportEnum.JMS;
				break;
			case 1: 
				t = TransportEnum.SFTP;
				break;
			case 2: 
				t = TransportEnum.SERVLET;
				break;
		}
		return t;
	}
	
	public TransportEnum getSelectedOutboundTransport() {

		TransportEnum t = null;
		switch (selectedOutboundTransport) {
			case 0: 
				t = TransportEnum.JMS;
				break;
			case 1: 
				t = TransportEnum.SFTP;
				break;
		}
		return t;
	}

	public String getServiceName() {
		return serviceText.getText();
	}
	
	
/*
	private void addRadioButtons (final Composite parent) {

		final Composite container = SwtUtil.createGridContainer(parent, 1);
//		final Composite tabCont   = SwtUtil.createGridContainer(container, 1);
//		final Composite tabCont1  = SwtUtil.createGridContainer(tabCont, 1);
//		final Composite tabCont2  = SwtUtil.createGridContainer(tabCont, 1);
//		final Composite tabCont3  = SwtUtil.createGridContainer(tabCont, 1);



		Listener listener = new Listener() {
			public void handleEvent (Event e) {
				serviceType = (Integer)e.widget.getData();
				System.err.println("### Set selected component type to: " + serviceType);

				Control [] children = container.getChildren ();
				for (int i=0; i<children.length; i++) {
					Control child = children [i];
					if (e.widget != child && child instanceof Button && (child.getStyle () & SWT.TOGGLE) != 0) {
						((Button) child).setSelection (false);
					}
				}
				((Button) e.widget).setSelection (true);
			}
		};
		Label label = new Label(container, SWT.NULL);
		label.setText("Pattern &Category:");

		System.err.println("### Creates a combo");
		Combo patternCategoryCombo = new Combo (container, SWT.READ_ONLY);
		patternCategoryCombo.setItems (new String [] {"Request/Response Service", "Fire and Forget Service", "File Transfer"});
//		patternCombo.setSize (200, 200);

		label = new Label(container, SWT.NULL);
		label.setText("&Type of Pattern:");
		final Combo patternTypeCombo = new Combo (container, SWT.READ_ONLY);
		patternTypeCombo.setItems (new String [] {"Please select a pattern category first!", ""});
		
		SelectionListener patternComboListener = new SelectionListener () {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				System.err.println("### Set default selected");
				patternType = e.widget.getData();
				System.err.println("### Set default selected component type to: " + patternType + " for wideget of type " + e.widget.getClass().getName());
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				System.err.println("### Set selected");
				Combo c = (Combo)e.widget;
				patternType = e.widget.getData();
				System.err.println("### Set selected component type to: " + patternType + " for wideget of type " + c.getSelectionIndex());

				switch (c.getSelectionIndex()) {
				case 0:
					patternTypeCombo.setItems (new String [] {"SOAP/HTTPS(S) with SOAP/HTTP(S) adapter",
							"SOAP/HTTPS(S) with JMS adapter",
							"SOAP/HTTPS(S) with REST/HTTP(S) adapter"});
					break;
				case 1:
					patternTypeCombo.setItems (new String [] {"JMS with JMS adapter",
							"JMS with JDBC adapter",
							"JMS with file adapter"});
					break;
				case 2:
					patternTypeCombo.setItems (new String [] {"SFTP to SFTP",
							"SFTP to JDBC",
							"SFTP to JMS"});
					break;

				}
				

				
				
				switch (c.getSelectionIndex()) {
				case 0:
					tabCont1.setVisible(true);
					tabCont2.setVisible(false);
					tabCont3.setVisible(false);
					break;
				case 1:
					tabCont1.setVisible(false);
					tabCont2.setVisible(true);
					tabCont3.setVisible(false);
					break;
				case 2:
					tabCont1.setVisible(false);
					tabCont2.setVisible(false);
					tabCont3.setVisible(true);
					break;

				}
			}
		};
		patternCategoryCombo.addSelectionListener(patternComboListener);

		Composite tabCont = SwtUtil.createGridContainer(container, 1);

//		CTabFolder tabFolder = new CTabFolder (container, SWT.BORDER);
//		tabFolder.setSimple(false);

//		CTabItem item = new CTabItem (tabFolder, SWT.NULL);
//		item.setText ("Request/Response");
		
		Composite tabCont1 = SwtUtil.createGridContainer(tabCont, 1);
		tabCont1.setVisible(false);
//		item.setControl(tabCont1);
		
		int i = 0;
		SwtUtil.createRadioButton(tabCont1, listener, i++, "SOAP/HTTPS(S) with SOAP/HTTP(S) adapter");
		SwtUtil.createRadioButton(tabCont1, listener, i++, "SOAP/HTTPS(S) with JMS adapter");
		SwtUtil.createRadioButton(tabCont1, listener, i++, "SOAP/HTTPS(S) with REST/HTTP(S) adapter");
		
//		item = new CTabItem (tabFolder, SWT.NULL);
//		item.setText ("Fire & Forget");
		
		Composite tabCont2 = SwtUtil.createGridContainer(tabCont, 1);
		tabCont2.setVisible(false);
//		item.setControl(tabCont2);

		SwtUtil.createRadioButton(tabCont2, listener, i++, "JMS with JMS adapter");
		SwtUtil.createRadioButton(tabCont2, listener, i++, "JMS with JDBC adapter");
		SwtUtil.createRadioButton(tabCont2, listener, i++, "JMS with file adapter");

//		item = new CTabItem (tabFolder, SWT.NULL);
//		item.setText ("File transfer");
		
		Composite tabCont3 = SwtUtil.createGridContainer(tabCont, 1);
		tabCont3.setVisible(false);
//		item.setControl(tabCont3);

		SwtUtil.createRadioButton(tabCont3, listener, i++, "SFTP to SFTP");
		SwtUtil.createRadioButton(tabCont3, listener, i++, "SFTP to JDBC");
		SwtUtil.createRadioButton(tabCont3, listener, i++, "SFTP to JMS");
		
//		tabCont.setSize (400, 400);

	}


 */
	
	
}