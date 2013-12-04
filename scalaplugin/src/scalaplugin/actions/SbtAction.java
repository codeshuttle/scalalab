/**
 * 
 */
package scalaplugin.actions;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;

/**
 * @author parthipanp
 *
 */
public class SbtAction implements IWorkbenchWindowActionDelegate {
	IWorkbenchWindow activeWindow = null;
	
	/**
	 * 
	 */
	public SbtAction() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run(IAction action) {
		Shell shell = activeWindow.getShell();
		MessageDialog.openInformation(shell, "Hello World", "Hello World!");
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(IWorkbenchWindow window) {
		activeWindow = window;
	}

}
