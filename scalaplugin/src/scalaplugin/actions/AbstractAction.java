/**
 * 
 */
package scalaplugin.actions;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import scalaplugin.Console;

/**
 * @author parthipanp
 * 
 */
public abstract class AbstractAction implements IWorkbenchWindowActionDelegate {

	abstract String getAction();

	private Shell shell = null;
	private Text error = null;
	private Text message = null;

	private final Collection<Character> charEntered = new ArrayList<>();
	private final Stack<String> commands = new Stack<>();

	private final OutputStream errorOut = new OutputStream() {

		@Override
		public void write(int b) throws IOException {
			showError("" + (char) b);
		}
	};
	private final OutputStream messageOut = new OutputStream() {

		@Override
		public void write(int b) throws IOException {
			showMessage("" + (char) b);
		}
	};
	
	private CommandRunner getCommandRunner(){
		IProject[] projects = getProject();
		if(projects!=null && projects.length>0){
			IProject project = projects[0];
			IPath location = project.getLocation();
			return new CommandRunner(getAction(),location.toFile().getAbsolutePath(), errorOut,
					messageOut);
		}else{
			return new CommandRunner(getAction(),".", errorOut,
					messageOut);
		}

	}
	
	private CommandRunner c = null;
	
	private final KeyListener listener = new KeyListener() {

		@Override
		public void keyReleased(KeyEvent e) {
			try {
				c.getOutputStream().write(e.keyCode);
				Console.log("key code '" + e.keyCode + "'");
			} catch (IOException e1) {
				c.handleException(e1);
			}
			if (e.keyCode == 13) {
				Character[] arr = (Character[]) charEntered
						.toArray(new Character[] {});
				String command = new String(convert(arr));
				commands.push(command);
				charEntered.clear();
				runCommand();
			} else {
				charEntered.add(e.character);
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {

		}
	};

	private final String title;

	/**
	 * 
	 */
	public AbstractAction(String title) {
		this.title = title;
	}

	@Override
	public void run(IAction action) {
		if (c==null) {
			c = getCommandRunner();
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					shell = new Shell(Display.getCurrent(), SWT.CLOSE
							| SWT.TITLE | SWT.MIN | SWT.MAX | SWT.RESIZE
							| SWT.SHELL_TRIM);// SWT.None
					shell.open();
					shell.setText(title);
					// shell.setBackground(org.eclipse.swt.graphics.Color.);
					// MessageDialog.openInformation(shell, "Scala Sbt",
					// "start thread");
					error = new Text(shell, SWT.MULTI | SWT.ERROR);
					message = new Text(shell, SWT.MULTI | SWT.None);
					// error = message;
					error.pack();
					message.pack();
					// showMessage("sbt started!");
					c.start();
					message.addKeyListener(listener);
					// MessageDialog.openInformation(shell, "Scala Sbt",
					// "end thread");
				}
			});
		} else {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(shell, "Scala Sbt",
							"setActive");
					shell.setActive();
				}
			});
		}
	}

	private void runCommand() {
		// try {
		String command = commands.peek() + "\n";
		// c.getOutputStream().write(
		// command.getBytes(Charset.defaultCharset()));
		Console.log("Executed command '" + command + "'");
		// } catch (IOException e) {
		// c.handleException(e);
		// }
	}

	private char[] convert(Character[] arr) {
		char[] carr = new char[arr.length];
		for (int i = 0; i < arr.length; i++) {
			carr[i] = arr[i];
		}
		return carr;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	private void showMessage(final String m) {
		// Update the user interface asynchronously
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				message.append(m);
				message.pack();
			}
		});
	}

	private void showError(final String e) {
		// Update the user interface asynchronously
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				error.append(e);
				error.pack();
			}
		});
	}

	@Override
	public void dispose() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (c.isStarted()) {
					c.stop();
				}
				message.removeKeyListener(listener);
				error = null;
				message = null;
				shell.dispose();
				charEntered.clear();
				commands.clear();
				c = null;
			}
		});
	}

	@Override
	public void init(IWorkbenchWindow window) {
		// shell = window.getShell();
	}

	private static String OS = null;

	static String getOsName() {
		if (OS == null) {
			OS = System.getProperty("os.name");
		}
		return OS;
	}

	static boolean isWindows() {
		return getOsName().startsWith("Windows");
	}
	
	static String getSbtHome(){
		return getEnvOrSys("SBT_HOME");
	}

	static String getScalaHome(){
		return getEnvOrSys("SCALA_HOME");
	}
	
	static String getEnvOrSys(String var){
		if(System.getProperty(var)!=null){
			return System.getProperty(var);
		}else{
			return System.getenv(var);
		}
	}
	
	IProject[] getProject(){
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        return root.getProjects();

	}
}
