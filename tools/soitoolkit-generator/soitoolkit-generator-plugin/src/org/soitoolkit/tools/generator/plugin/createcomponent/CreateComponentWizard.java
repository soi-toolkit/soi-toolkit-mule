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
import static org.soitoolkit.tools.generator.plugin.createcomponent.CreateComponentUtil.getComponentProjectName;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.soitoolkit.tools.generator.model.IModel;
import org.soitoolkit.tools.generator.model.ModelFactory;
import org.soitoolkit.tools.generator.model.enums.ComponentEnum;
import org.soitoolkit.tools.generator.model.enums.DeploymentModelEnum;
import org.soitoolkit.tools.generator.model.enums.MavenEclipseGoalEnum;
import org.soitoolkit.tools.generator.model.enums.MuleVersionEnum;
import org.soitoolkit.tools.generator.model.enums.TransportEnum;
import org.soitoolkit.tools.generator.IntegrationComponentGenerator;
import org.soitoolkit.tools.generator.SchemaComponentGenerator;
import org.soitoolkit.tools.generator.plugin.util.StatusPage;
import org.soitoolkit.tools.generator.plugin.util.SwtUtil;
import org.soitoolkit.tools.generator.util.SystemUtil;

/**
 * This is a sample new wizard. Its role is to create a new file 
 * resource in the provided container. If the container resource
 * (a folder or a project) is selected in the workspace 
 * when the wizard is opened, it will accept it as the target
 * container. The wizard creates one file with the extension
 * "mpe". If a sample multi-page editor (also available
 * as a template) is registered for the same extension, it will
 * be able to open it.
 */

public class CreateComponentWizard extends Wizard implements INewWizard {
	private CreateComponentStartPage  page;
	private CreateIntegrationComponentPage page2;
	private StatusPage page3;
	private ISelection selection;

	/**
	 * Helper method for inter-page communication
	 * TODO: Is this the right way to go???
	 * 
	 * @return
	 */
	CreateIntegrationComponentPage getCreateIntegrationComponentPage() {
		return page2;
	}
	
	/**
	 * Constructor for CreateComponentWizard.
	 */
	public CreateComponentWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	
	@Override
	public void setContainer(IWizardContainer wizardContainer) {
		super.setContainer(wizardContainer);
		
		registerChangePageListener();		
	}

	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new CreateComponentStartPage(selection);
		addPage(page);
		page2 = new CreateIntegrationComponentPage(selection);
		addPage(page2);
		page3 = new StatusPage(selection);
		addPage(page3);
	}

	private void registerChangePageListener() {
		WizardDialog wd = (WizardDialog)getContainer();
		
		if (wd == null) return;

		IPageChangingListener pageChaninglistener = new IPageChangingListener() {
			@Override
			public void handlePageChanging(PageChangingEvent event) {
				Object p = (event == null) ? null : event.getTargetPage();
				boolean isPage2 = (p != null && p == page2);
				if (isPage2) page2.setMustBeDisplayed(false);
			}
		};
		((WizardDialog)getContainer()).addPageChangingListener(pageChaninglistener);
		
		IPageChangedListener pageChanedlistener = new IPageChangedListener() {
			@Override
			public void pageChanged(PageChangedEvent event) {
				Object p = (event == null) ? null : event.getSelectedPage();
				boolean isPage3 = (p != null && p == page3);
				if (isPage3) {
					System.err.println("### Displaying PAGE3, start code generation");
				}
			}
		};
		((WizardDialog)getContainer()).addPageChangedListener(pageChanedlistener);
		
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
//		final String containerName = page.getContainerName();
		final int componentType = page.getComponentType();
		final String artifactId = page.getArtifactId();
		final String groupId = page.getGroupId();
		final String version = page.getVersion();
		final String folderName = page.getRootFolder();
		final String mavenHome = page.getMavenHome();
		final int mavenEclipseGoalType = page.getMavenEclipseGoalType();
		
		
		ComponentEnum compTypeEnum = ComponentEnum.get(componentType);
		final List<TransportEnum> transports = (compTypeEnum == INTEGRATION_COMPONENT) ? page2.getTransports() : null;

		final MuleVersionEnum muleVersion = page2.getMuleVersion();

		final DeploymentModelEnum deploymentModel = page2.getDeploymentModel();
		
		if (compTypeEnum == INTEGRATION_COMPONENT) {
			raiseSecurityNotice(transports);
		}
		
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(mavenHome, mavenEclipseGoalType, componentType, artifactId, groupId, version, muleVersion, transports, deploymentModel, folderName, monitor);
				} catch (CoreException e) {
					e.printStackTrace();
					throw new InvocationTargetException(e);
				} catch (Throwable e) {
					e.printStackTrace();
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}

	private void raiseSecurityNotice(final List<TransportEnum> transports) {
		boolean ftpSelected  = false;
		boolean sftpSelected = false;
		boolean jdbcSelected = false;
		String transportString = null;
		for (TransportEnum transportEnum : transports) {
			if (transportEnum == TransportEnum.JDBC) {
				jdbcSelected = true;
				if (transportString == null) {
					transportString = "JDBC";
				} else {
					transportString += " and JDBC";					
				}
			}
			if (transportEnum == TransportEnum.FTP) {
				sftpSelected = true;
				if (transportString == null) {
					transportString = "FTP";
				} else {
					transportString += " and FTP";					
				}
			}
			if (transportEnum == TransportEnum.SFTP) {
				sftpSelected = true;
				if (transportString == null) {
					transportString = "SFTP";
				} else {
					transportString += " and SFTP";					
				}
			}
		}

		if (jdbcSelected || ftpSelected || sftpSelected) {
			MessageDialog.openInformation(getShell(), "Security Notice", "Security related properties for " + transportString + " will be created in the security-property-file in the src/environment folder. Please update these properties to reflect your environment and protect the information accordingly, e.g. don't commit your credential information to subversion!");
		}
	}
	
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 * @param folderName2 
	 * @param componentType 
	 * @param transports 
	 * @param deploymentModel 
	 */

	private void doFinish(String mavenHome, int mavenEclipseGoalType, int componentType, String artifactId, String groupId, String version, MuleVersionEnum muleVersion, List<TransportEnum> transports, DeploymentModelEnum deploymentModel, String folderName, IProgressMonitor monitor) throws CoreException {

		// create a sample folder
		monitor.beginTask("Starting the generator...", 3);

		// Create two print-streams one for each out and err (is written to in separated threads so we better create one stream per thread...)
		PrintStream out = page3.createStatusPrintStream();
		PrintStream err = page3.createStatusPrintStream();
		
		// Show status page
		getContainer().getShell().getDisplay().syncExec(
			new Runnable() {
				public void run() {
					getContainer().showPage(page3);
				}
			});
		
		monitor.worked(1);
		monitor.setTaskName("Generating files to folder: " + folderName);

		ComponentEnum compEnum = ComponentEnum.get(componentType);
		try {
			
			switch (compEnum) {
			case INTEGRATION_COMPONENT:
				new IntegrationComponentGenerator(out, groupId, artifactId, version, muleVersion, deploymentModel, transports, folderName).startGenerator();
				break;

			case SD_SCHEMA_COMPONENT:
				
				String schemaName = artifactId;
				List<String> operations = null;
				new SchemaComponentGenerator(out, groupId, artifactId, version, schemaName, operations, folderName).startGenerator();								
				break;

			default:
				throw new RuntimeException("Unsupported component type: " + componentType);
			}

			String componentProjectName = getComponentProjectName(componentType, groupId, artifactId);
			final String path = folderName + "/" + componentProjectName;
			
			int noOfFilesAndFoldersCreated = SystemUtil.countFiles(path);
			
			monitor.worked(1);
			String buildCommand = "mvn" + (SwtUtil.isWindows() ? ".bat" : "") + " install " + MavenEclipseGoalEnum.get(mavenEclipseGoalType).getLabel();

			monitor.setTaskName("Execute command: " + buildCommand);
			SystemUtil.executeCommand(mavenHome + "/bin/" + buildCommand, path + "/trunk", out, err);
			
			monitor.worked(1);
			monitor.setTaskName("Open project(s) in " + path + "/trunk");

			
			switch (compEnum) {
			case INTEGRATION_COMPONENT:
				IModel m = ModelFactory.newModel(groupId, artifactId, null, null, null, null, null);
				openProject(path + "/trunk/" + m.getServiceProjectFilepath() + "/.project");
				
				if (deploymentModel == DeploymentModelEnum.STANDALONE_DEPLOY) {
					openProject(path + "/trunk/" + m.getStandaloneProjectFilepath() + "/.project");
					openProject(path + "/trunk/" + m.getTeststubStandaloneProjectFilepath() + "/.project");
				}

				if (deploymentModel == DeploymentModelEnum.WAR_DEPLOY) {
					openProject(path + "/trunk/" + m.getWebProjectFilepath() + "/.project");
					openProject(path + "/trunk/" + m.getTeststubWebProjectFilepath() + "/.project");
				}
				break;

			case SD_SCHEMA_COMPONENT:
				openProject(path + "/trunk/.project");
				break;

			default:
				break;
			}


		} catch (IOException ioe) {
			throwCoreException("Failed to create a new component", ioe);
		}
		
		
//		getShell().getDisplay().asyncExec(new Runnable() {
//			public void run() {
//				IWorkbenchPage page =
//					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//				try {
//					IDE.openEditor(page, file, true);
//				} catch (PartInitException e) {
//				}
//			}
//		});
		monitor.worked(1);
	}

	private void openProject(String path) throws CoreException {
		IProjectDescription description = ResourcesPlugin.getWorkspace().loadProjectDescription(new Path(path));
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
		project.create(description, null);
		project.open(null);
	}

	
	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "soi_toolkit_generator_plugin", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	private void throwCoreException(String message, Throwable exception) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "soi_toolkit_generator_plugin", message, exception);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}