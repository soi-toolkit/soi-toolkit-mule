package org.soitoolkit.tools.generator.plugin.createservice;

import static org.soitoolkit.tools.generator.plugin.util.XmlUtil.createDocument;
import static org.soitoolkit.tools.generator.plugin.util.XmlUtil.getFirstValue;
import static org.soitoolkit.tools.generator.plugin.util.XmlUtil.getXPathResult;

import java.io.ByteArrayInputStream;
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.soitoolkit.tools.generator.plugin.generator.JmsToJmsServiceGenerator;
import org.soitoolkit.tools.generator.plugin.generator.SftpToSftpServiceGenerator;
import org.soitoolkit.tools.generator.plugin.model.DefaultModelImpl;
import org.soitoolkit.tools.generator.plugin.model.IModel;
import org.soitoolkit.tools.generator.plugin.model.ModelFactory;
import org.w3c.dom.Document;


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
		final int    inboundTransport = page.getSelectedInboundTransport();
		final int    outboundTransport = page.getSelectedOutboundTransport();
		final String serviceName = page.getServiceName();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, mep, inboundTransport, outboundTransport, serviceName, monitor);
				} catch (CoreException e) {
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
	
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 * @param inboundTransport 
	 * @param outboundTransport 
	 * @param mep 
	 */
	private void doFinish(String containerName, int mep, int inboundTransport, int outboundTransport, String serviceName, IProgressMonitor monitor) throws CoreException {

		monitor.beginTask("Creating " + serviceName, 3);
		
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

		// TODO: Get rid of these hardcoded integrers, use the enum ordinal values!
		switch (mep) {
		case 0: // Req Resp
			
			break;

		case 1: // One Way
			
			switch (inboundTransport) {
			case 0: // JMS --> JMS
				new JmsToJmsServiceGenerator(ps, groupId, artifactId, serviceName, rootFolderName).startGenerator();
				
				break;

			case 1: // SFTP --> SFTP
				new SftpToSftpServiceGenerator(ps, groupId, artifactId, serviceName, rootFolderName).startGenerator();
				
				break;

			default:
				break;
			}
			
			break;

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
		monitor.setTaskName("Refresh workspace...");
		ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		monitor.worked(1);
	}

	private InputStream getPomAsInputStream(IResource resource)
			throws CoreException {
		IContainer container = (IContainer)resource;
		IFile file = container.getFile(new Path("pom.xml"));
		InputStream content = file.getContents();
		return content;
	}

	private IModel extractGroupIdAndArtifactIdFromPom(InputStream content) {
		String nsPrefix = "ns";
		String nsURI = "http://maven.apache.org/POM/4.0.0";
		Document doc = createDocument(content);

		// XPath Query for showing all nodes value

		String parentGroupId    = getFirstValue(getXPathResult(doc, nsPrefix, nsURI, "/ns:project/ns:parent/ns:groupId/text()"));
		String artifactId = getFirstValue(getXPathResult(doc, nsPrefix, nsURI, "/ns:project/ns:artifactId/text()"));
		String groupId    = getFirstValue(getXPathResult(doc, nsPrefix, nsURI, "/ns:project/ns:groupId/text()"));

		if (groupId == null) groupId = parentGroupId;
		
		return ModelFactory.newModel(groupId, artifactId, null, null, null);
	}

	

	/**
	 * We will initialize file contents with a sample text.
	 */

	private InputStream openContentStream() {
		String contents =
			"This is the initial file contents for *.mpe file that should be word-sorted in the Preview page of the multi-page editor";
		return new ByteArrayInputStream(contents.getBytes());
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "soi_toolkit_generator_plugin", IStatus.OK, message, null);
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