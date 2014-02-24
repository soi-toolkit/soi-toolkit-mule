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

import static org.soitoolkit.tools.generator.util.PomUtil.extractGroupIdAndArtifactIdFromPom;
import static soi_toolkit_generator_plugin.preferences.PreferenceConstants.P_MAVEN_HOME;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.ui.actions.OrganizeImportsAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.soitoolkit.tools.generator.model.IModel;
import org.soitoolkit.tools.generator.model.enums.MavenEclipseGoalEnum;
import org.soitoolkit.tools.generator.model.enums.MuleVersionEnum;
import org.soitoolkit.tools.generator.model.enums.TransformerEnum;
import org.soitoolkit.tools.generator.model.enums.TransportEnum;
import org.soitoolkit.tools.generator.plugin.util.SwtUtil;
import org.soitoolkit.tools.generator.util.SystemUtil;
import org.soitoolkit.tools.generator.OnewayRobustServiceGenerator;
import org.soitoolkit.tools.generator.OnewayServiceGenerator;
import org.soitoolkit.tools.generator.RequestResponseServiceGenerator;

import soi_toolkit_generator_plugin.Activator;


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

public class CreateServiceWizard extends Wizard implements INewWizard {
	private CreateServicePage page;
	private ISelection selection;

	/**
	 * Constructor for CreateServiceWizard.
	 */
	public CreateServiceWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new CreateServicePage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final int    mep = page.getSelectedMep();
		final TransportEnum inboundTransport = page.getSelectedInboundTransport();
		final TransportEnum outboundTransport = page.getSelectedOutboundTransport();
		final TransformerEnum transformerType = page.getTransformerType(); 

		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		final String mavenHome = prefs.getString(P_MAVEN_HOME);
		final int mavenEclipseGoalType = MavenEclipseGoalEnum.ECLIPSE_ECLIPSE.ordinal();
		
		final String serviceName = page.getServiceName();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(mavenHome, mavenEclipseGoalType, containerName, mep, inboundTransport, outboundTransport, transformerType, serviceName, monitor);
				} catch (CoreException e) {
					e.printStackTrace();
					throw new InvocationTargetException(e);
				} catch (Throwable e) {
					e.printStackTrace();
					if (e instanceof RuntimeException) {
						throw (RuntimeException)e;
					} else {
						throw new InvocationTargetException(e);
					}
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
	
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 * @param inboundTransport 
	 * @param outboundTransport 
	 * @param mep 
	 */
	private void doFinish(String mavenHome, int mavenEclipseGoalType, String containerName, int mep, TransportEnum inboundTransport, TransportEnum outboundTransport, TransformerEnum transformerType, String serviceName, IProgressMonitor monitor) throws CoreException {

		monitor.beginTask("Creating " + serviceName, 5);
		
		final PrintStream ps = new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
//				page3.writeStatusText(b);
				System.err.print(Character.toString((char) b));
			}
		});

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		
		IModel m = extractGroupIdAndArtifactIdFromPom(getPomAsInputStream(resource));
			
		// TODO: Get gid and aid from the pom.xml...
		String groupId = m.getGroupId();
		String artifactId = m.getArtifactId();
		String projectFolderName = resource.getLocation().toOSString();
		File projectFolder = new File(projectFolderName);
		String rootFolderName = projectFolder.toString();
		
//		IContainer container = (IContainer) resource;
//		final IFile file = container.getFile(new Path(serviceName));
//		try {
//			InputStream stream = openContentStream();
//			if (file.exists()) {
//				file.setContents(stream, true, true, monitor);
//			} else {
//				file.create(stream, true, monitor);
//			}
//			stream.close();
//		} catch (IOException e) {
//		}
		
		monitor.worked(1);
		monitor.setTaskName("Generating files...");

		// TODO: Get rid of these hardcoded integers, use the enum ordinal values!
		switch (mep) {
		case 0: // Req Resp
			
			new RequestResponseServiceGenerator(ps, groupId, artifactId, serviceName, MuleVersionEnum.MAIN_MULE_VERSION, inboundTransport, outboundTransport, transformerType , rootFolderName).startGenerator();
			break;

		case 1: // One Way
			
			new OnewayServiceGenerator(ps, groupId, artifactId, serviceName, MuleVersionEnum.MAIN_MULE_VERSION, inboundTransport, outboundTransport, transformerType, rootFolderName).startGenerator();
			break;

		case 2: // One Way Robust
			
			new OnewayRobustServiceGenerator(ps, groupId, artifactId, serviceName, MuleVersionEnum.MAIN_MULE_VERSION, inboundTransport, outboundTransport, transformerType, rootFolderName).startGenerator();
			break;

//		case 2: // Pub Sub
//			

		default:
			break;
		}
		
//		monitor.setTaskName("Opening file for editing...");
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

		// TODO issue #345: DataMapper support - we need to run the maven goal "studio:studio" here
		// hardcoded for now
		//if (transformerType == TransformerEnum.EE_DATAMAPPER) {
			//System.err.println("issue #345: HARDCODED maven eclipse goal to studio:stduio for Mule-EE");
			//mavenEclipseGoalType = MavenEclipseGoalEnum.MULESTUDIO_MULESTUDIO.ordinal();
		//}
				
		String buildCommand = "mvn" + (SwtUtil.isWindows() ? ".bat" : "") + " " + MavenEclipseGoalEnum.get(mavenEclipseGoalType).getLabel();

		monitor.setTaskName("Execute command: " + buildCommand);
		try {
			System.err.println("Execute: " + buildCommand + " in folder: " + rootFolderName);
			SystemUtil.executeCommand(mavenHome, buildCommand, rootFolderName, System.out, System.err);
		} catch (IOException ioe) {
			throwCoreException("Failed to create a new component", ioe);
		}
		
		monitor.worked(1);
		monitor.setTaskName("Organize Imports...");
//		doOrganizeImports(resource);

		monitor.worked(1);
		monitor.setTaskName("Refresh workspace...");
		ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);

		monitor.worked(1);
	}

	private void doOrganizeImports(IResource project) {
		System.err.println("CreateServiceWizard.doOrganizeImports() START");
		boolean skip = false;
		if (!skip) {
//			Shell s = getShell();
//			PlatformUI.getWorkbench();
//			IWizard w = page.getWizard();
//			IObjectActionDelegate od;
//			IWorkbenchPart wp;
//			PlatformUI.getWorkbench().getDisplay();

			try {
				IWorkbench wb = PlatformUI.getWorkbench();
				IWorkbenchWindow awba = wb.getActiveWorkbenchWindow();
				IWorkbenchPage apage = awba.getActivePage();
				IWorkbenchPart apart = apage.getActivePart();
				IWorkbenchSite wbs = apart.getSite();
				OrganizeImportsAction oia = new OrganizeImportsAction(wbs);
				oia.run(new StructuredSelection(project));
			} catch (Throwable e) {
				System.err.println("CreateServiceWizard.doOrganizeImports() ERROR");
				e.printStackTrace();
			}
		}
		System.err.println("CreateServiceWizard.doOrganizeImports() DONE");
	}
	
	private InputStream getPomAsInputStream(IResource resource)
			throws CoreException {
		IContainer container = (IContainer)resource;
		IFile file = container.getFile(new Path("pom.xml"));
		InputStream content = file.getContents();
		return content;
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