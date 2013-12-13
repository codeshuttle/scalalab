package scalaplugin.wizards


import org.eclipse.jface.viewers.IStructuredSelection
import org.eclipse.jface.wizard.Wizard
import org.eclipse.ui.INewWizard
import org.eclipse.ui.IWorkbench
import org.eclipse.core.runtime._
import org.eclipse.jface.operation._
import java.lang.reflect.InvocationTargetException
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.jface.viewers.ISelection
import org.eclipse.core.resources._
import org.eclipse.core.runtime.CoreException
import java.io._
import org.eclipse.ui._
import org.eclipse.ui.ide.IDE


/**
 * This is a sample new wizard. Its role is to create a new file 
 * resource in the provided container. If the container resource
 * (a folder or a project) is selected in the workspace 
 * when the wizard is opened, it will accept it as the target
 * container. The wizard creates one file with the extension
 * "scala". If a sample multi-page editor (also available
 * as a template) is registered for the same extension, it will
 * be able to open it.
 */

class ScalaNewWizard extends Wizard with INewWizard {
	var page:ScalaNewWizardPage ;
	var selection:ISelection ;

	/**
	 * Constructor for ScalaNewWizard.
	 */
	 /*
	def this() {
		//setNeedsProgressMonitor(true)
	}
	*/
	
	/**
	 * Adding the page to the wizard.
	 */
	def addPages():Unit = {
		page = new ScalaNewWizardPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	def performFinish():Boolean = {
		val containerName:String = page.getContainerName()
		val fileName:String = page.getFileName()
		var op:IRunnableWithProgress = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, fileName, monitor)
				} catch (CoreException e) {
					throw new InvocationTargetException(e)
				} finally {
					monitor.done()
				}
			}
		};
		try {
			getContainer().run(true, false, op)
		} catch{
			case e:	InterruptedException => false
			case e:	InvocationTargetException => {
				Throwable realException = e.getTargetException()
				MessageDialog.openError(getShell(), "Error", realException.getMessage())
				false
			}
		}
		true
	}
	
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 */

	private def doFinish(
		 containerName:String,
		 fileName:String,
		 monitor:IProgressMonitor):Unit = {
//		throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + fileName, 2)
		var root:IWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot()
		var resource:IResource = root.findMember(new Path(containerName))
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.")
		}
		var container:IContainer = (IContainer) resource
		val file:IFile = container.getFile(new Path(fileName))
		try {
			val stream:InputStream = openContentStream()
			if (file.exists()) {
				file.setContents(stream, true, true, monitor)
			} else {
				file.create(stream, true, monitor)
			}
			stream.close()
		} catch (IOException e) {
		}
		monitor.worked(1)
		monitor.setTaskName("Opening file for editing...")
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				try {
					IDE.openEditor(page, file, true)
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1)
	}
	
	/**
	 * We will initialize file contents with a sample text.
	 */

	private def openContentStream():InputStream = {
		val contents:String =
			"This is the initial file contents for *.scala file that should be word-sorted in the Preview page of the multi-page editor"
		return new ByteArrayInputStream(contents.getBytes())
	}

	private def throwCoreException( message:String):Unit = {// throws CoreException {
		val status:IStatus =
			new Status(IStatus.ERROR, "scalaplugin", IStatus.OK, message, null)
		throw new CoreException(status)
	}

	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	def init( workbench:IWorkbench, selection:IStructuredSelection):Unit = {
		this.selection = selection
	}
}


import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (scala).
 */

class ScalaNewWizardPage( selection:ISelection) extends WizardPage {
	var  containerText:Text = null;

	var fileText:Text = null;

	//var selection:ISelection = null;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	 /*
	def this( selection:ISelection) {
		//super("wizardPage")
		//setTitle("Multi-page Editor File")
		//setDescription("This wizard creates a new file with *.scala extension that can be opened by a multi-page editor.")
		this.selection = selection
	}
	*/

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	def createControl( parent:Composite):Unit = {
		val container:Composite = new Composite(parent, SWT.NULL)
		val layout:GridLayout = new GridLayout()
		container.setLayout(layout)
		layout.numColumns = 3
		layout.verticalSpacing = 9
		Label label = new Label(container, SWT.NULL)
		label.setText("&Container:")

		containerText = new Text(container, SWT.BORDER | SWT.SINGLE)
		val gd:GridData = new GridData(GridData.FILL_HORIZONTAL)
		containerText.setLayoutData(gd)
		containerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged()
			}
		});

		val button:Button = new Button(container, SWT.PUSH)
		button.setText("Browse...")
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse()
			}
		});
		label = new Label(container, SWT.NULL)
		label.setText("&File name:")

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE)
		gd = new GridData(GridData.FILL_HORIZONTAL)
		fileText.setLayoutData(gd)
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged()
			}
		});
		initialize()
		dialogChanged()
		setControl(container)
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private def initialize():Unit = {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			
			val ssel:IStructuredSelection = (IStructuredSelection) selection
			
			if (ssel.size() > 1){
				return
			}
			
			val obj:Object = ssel.getFirstElement()
			if (obj instanceof IResource) {
				var container:IContainer
				if (obj instanceof IContainer)
					container = (IContainer) obj
				else
					container = ((IResource) obj).getParent()
				containerText.setText(container.getFullPath().toString())
			}
		}
		fileText.setText("new_file.scala")
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private def handleBrowse():Unit =  {
		var dialog:ContainerSelectionDialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Select new file container")
		if (dialog.open() == ContainerSelectionDialog.OK) {
			var result:Array[Object] = dialog.getResult()
			if (result.length == 1) {
				containerText.setText(((Path) result(0)).toString())
			}
		}
	}

	/**
	 * Ensures that both text fields are set.
	 */

	private def dialogChanged() :Unit = {
		var container:IResource = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(getContainerName()))
		val fileName:String = getFileName()

		if (getContainerName().length() == 0) {
			updateStatus("File container must be specified")
			return
		}
		if (container == null
				|| (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
			updateStatus("File container must exist")
			return
		}
		if (!container.isAccessible()) {
			updateStatus("Project must be writable")
			return
		}
		if (fileName.length() == 0) {
			updateStatus("File name must be specified")
			return
		}
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("File name must be valid")
			return
		}
		val dotLoc:Int = fileName.lastIndexOf('.');
		if (dotLoc != -1) {
			val ext:String = fileName.substring(dotLoc + 1)
			if (ext.equalsIgnoreCase("scala") == false) {
				updateStatus("File extension must be \"scala\"")
				return
			}
		}
		updateStatus(null)
	}

	private def updateStatus( message:String):Unit =  {
		setErrorMessage(message)
		setPageComplete(message == null)
	}

	def getContainerName():String = {
		containerText.getText()
	}

	def getFileName():String = {
		fileText.getText()
	}
}