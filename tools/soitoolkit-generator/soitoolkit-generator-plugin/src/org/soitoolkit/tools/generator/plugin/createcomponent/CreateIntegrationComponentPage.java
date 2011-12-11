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

import static org.soitoolkit.tools.generator.model.enums.DeploymentModelEnum.STANDALONE_DEPLOY;
import static org.soitoolkit.tools.generator.model.enums.MuleVersionEnum.MULE_3_2_1;
import static org.soitoolkit.tools.generator.plugin.util.SwtUtil.addRadioButtons;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.soitoolkit.tools.generator.model.enums.DeploymentModelEnum;
import org.soitoolkit.tools.generator.model.enums.EnumUtil;
import org.soitoolkit.tools.generator.model.enums.MuleVersionEnum;
import org.soitoolkit.tools.generator.model.enums.TransportEnum;
import org.soitoolkit.tools.generator.plugin.util.SwtUtil;
import org.soitoolkit.tools.generator.plugin.util.ValueHolder;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */

public class CreateIntegrationComponentPage extends WizardPage {

//	@Override
//	public boolean isPageComplete() {
//		boolean isPageComplete = super.isPageComplete();
//		System.err.println("CreateIntegrationComponentPage.isPageComplete() returns: " + isPageComplete);
//		return super.isPageComplete();
//	}
//
//	@Override
//	public void setPageComplete(boolean complete) {
//		System.err.println("CreateIntegrationComponentPage.setPageComplete() sets complete  to: " + complete);
//		super.setPageComplete(complete);
//	}
//
//	@Override
//	public IWizardPage getPreviousPage() {
//		IWizardPage p = super.getPreviousPage();
//		setMustBeDisplayed(false);	
//		System.err.println("CreateIntegrationComponentPage.getPreviousPage() returns: " + ((p == null)? "NULL" : p.getTitle()));
//		return p;
//	}

	public void setMustBeDisplayed(boolean mustBeDisplayed) {
		this.mustBeDisplayed = mustBeDisplayed;
		dialogChanged();
//		System.err.println("CreateIntegrationComponentPage.setMustBeDisplayed() sets complete  to: " + mustBeDisplayed);
	}

	private boolean mustBeDisplayed = false;
	private MuleVersionEnum muleVersion = MULE_3_2_1;
	
	private Combo muleVersionCombo;
	private ValueHolder<Integer> deploymentModelType = new ValueHolder<Integer>(STANDALONE_DEPLOY.ordinal());
	private Button vmButton;
	private Button jmsButton;
	private Button jdbcButton;
	private Button servletButton;
	private Button fileButton;
	private Button ftpButton;
	private Button sftpButton;
	private Button pop3Button;
	private Button imapButton;
	private Button smtpButton;

	private ISelection selection;

	/**
	 * Constructor for CreateIntegrationComponentPage.
	 * 
	 * @param pageName
	 */
	public CreateIntegrationComponentPage(ISelection selection) {
		super("wizardPage");
		setTitle("SOI Toolkit - Create a new component, page 2");
		setDescription("Configuration the integration component");
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

		FocusListener focusListener = new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				System.err.println("CreateIntegrationComponentPage.focusLost() called");				
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				System.err.println("CreateIntegrationComponentPage.focusGained() called");				
				setMustBeDisplayed(false);	
			}
		};
		
		container.addFocusListener(focusListener);
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

		// Combo for Mule Version
		Label label = new Label(container, SWT.NULL);
		label.setText("Mule version:");

		muleVersionCombo = new Combo (container, SWT.READ_ONLY);
		muleVersionCombo.setItems (EnumUtil.getLabels(MuleVersionEnum.values()));

		
		/*
		 * Mule Version-Combo-Listener
		 */
		SelectionListener muleVersionComboListener = new SelectionListener () {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				System.err.println("### Set default selected MuleVersion");
				Combo c = (Combo)e.widget;
				muleVersion =  MuleVersionEnum.get(c.getSelectionIndex());
				System.err.println("### Set default selected MuleVersion to: " + muleVersion);
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				System.err.println("### Set selected MuleVersion");
				Combo c = (Combo)e.widget;
				muleVersion =  MuleVersionEnum.get(c.getSelectionIndex());
				System.err.println("### Set selected MuleVersion to: " + muleVersion);

				dialogChanged();

			}
		};
		muleVersionCombo.addSelectionListener(muleVersionComboListener);


		// Select deployment model
		addRadioButtons(
			EnumUtil.getLabels(DeploymentModelEnum.values()), 
			"Deployment model:", 
			deploymentModelType, container, new Listener () {
				public void handleEvent (Event e) {
					dialogChanged();
				}
			},
			false
		);

		
		// CheckBoxes for transports
		label = new Label(container, SWT.NULL);
		label.setText("Connectors:");

		int i = 0;
		// Jms transport is mandatory for logging, should not be selectable
//		vmButton = SwtUtil.createCheckboxButton(container, null, i++, "VM");
//		vmButton.setSelection(true);
		jmsButton = SwtUtil.createCheckboxButton(container, null, i++, "JMS (required)");
		jmsButton.setSelection(true);
		jmsButton.setEnabled(false);
		jdbcButton = SwtUtil.createCheckboxButton(container, null, i++, "JDBC");
		jdbcButton.setSelection(true);
		servletButton = SwtUtil.createCheckboxButton(container, null, i++, "Servlet");
		servletButton.setSelection(true);

//		fileButton = SwtUtil.createCheckboxButton(container, null, i++, "File");
//		fileButton.setSelection(true);

		ftpButton = SwtUtil.createCheckboxButton(container, null, i++, "FTP");
		ftpButton.setSelection(true);
		
		sftpButton = SwtUtil.createCheckboxButton(container, null, i++, "SFTP");
		sftpButton.setSelection(true);

//		pop3Button = SwtUtil.createCheckboxButton(container, null, i++, "POP3");
//		pop3Button.setSelection(true);
//		imapButton = SwtUtil.createCheckboxButton(container, null, i++, "IMAP");
//		imapButton.setSelection(true);
//		smtpButton = SwtUtil.createCheckboxButton(container, null, i++, "SMTP");
//		smtpButton.setSelection(true);
		
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
		muleVersionCombo.select(MULE_3_2_1.ordinal());
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

//	private void handleBrowseOrg() {
//		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
//				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
//				"Select new file container");
//		if (dialog.open() == ContainerSelectionDialog.OK) {
//			Object[] result = dialog.getResult();
//			if (result.length == 1) {
//				containerText.setText(((Path) result[0]).toString());
//			}
//		}
//	}


	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {

		if (servletButton != null) {
			if (deploymentModelType.value == STANDALONE_DEPLOY.ordinal()) {
				servletButton.setSelection(false);
				servletButton.setEnabled(false);
				servletButton.setText("Servlet transport disabled, only supported with war deploy model");
			} else {
				servletButton.setSelection(true);
				servletButton.setEnabled(true);
				servletButton.setText("Servlet");
			}
		}
		
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete((message == null) && !mustBeDisplayed);
	}

//	public String getContainerName() {
//		return containerText.getText();
//	}

//	public boolean isGenSchemaSelected() {
//		return genSchemaButton.getSelection();
//	}

	public MuleVersionEnum getMuleVersion() {
		return muleVersion;
	}

	public DeploymentModelEnum getDeploymentModel() {
		return DeploymentModelEnum.get(deploymentModelType.value);
	}

	public List<TransportEnum> getTransports() {
		List<TransportEnum> transports = new ArrayList<TransportEnum>();
		if (isVmTransportSelected())      transports.add(TransportEnum.VM);
		if (isJmsTransportSelected())     transports.add(TransportEnum.JMS);
		if (isJdbcTransportSelected())    transports.add(TransportEnum.JDBC);
		if (isServletTransportSelected()) transports.add(TransportEnum.SERVLET);
		if (isFileTransportSelected())    transports.add(TransportEnum.FILE);
		if (isFtpTransportSelected())     transports.add(TransportEnum.FTP);
		if (isSftpTransportSelected())    transports.add(TransportEnum.SFTP);
		if (isPop3TransportSelected())    transports.add(TransportEnum.POP3);
		if (isImapTransportSelected())    transports.add(TransportEnum.IMAP);
		if (isSmtpTransportSelected())    transports.add(TransportEnum.SMTP);
		return transports;
	}

	// ---------------
	
	private boolean isVmTransportSelected() {
		return vmButton != null && vmButton.getSelection();
	}

	private boolean isJmsTransportSelected() {
		return jmsButton != null && jmsButton.getSelection();
	}

	private boolean isJdbcTransportSelected() {
		return jdbcButton != null && jdbcButton.getSelection();
	}

	private boolean isServletTransportSelected() {
		return servletButton != null && servletButton.getSelection();
	}

	private boolean isFileTransportSelected() {
		return fileButton != null && fileButton.getSelection();
	}

	private boolean isFtpTransportSelected() {
		return ftpButton != null && ftpButton.getSelection();
	}

	private boolean isSftpTransportSelected() {
		return sftpButton != null && sftpButton.getSelection();
	}

	private boolean isPop3TransportSelected() {
		return pop3Button != null && pop3Button.getSelection();
	}

	private boolean isImapTransportSelected() {
		return imapButton != null && imapButton.getSelection();
	}

	private boolean isSmtpTransportSelected() {
		return smtpButton != null && smtpButton.getSelection();
	}	
}