/**
 * 
 */
package scalaplugin.actions;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import scalaplugin.Console;

/**
 * @author parthipanp
 * 
 */
public class SbtAction implements IWorkbenchWindowActionDelegate {

	// private static final File errorFile = new
	// File("D:\\ws\\tmp\\log_error.txt");
	// private static final File messageFile = new
	// File("D:\\ws\\tmp\\log_info.txt");
//	private IWorkbenchWindow activeWindow = null;
	private Shell shell = null;
	
	private Text error = null;
	private Text message = null;

	private final Collection<Character> charEntered = new ArrayList<>();
	private final Stack<String> commands = new Stack<>();

	private final OutputStream errorOut = new OutputStream() {
		
		@Override
		public void write(int b) throws IOException {
			error.append(""+(char)b);
		}
	};
	private final OutputStream messageOut = new OutputStream() {
		
		@Override
		public void write(int b) throws IOException {
			message.append(""+(char)b);
		}
	};
	private final SbtCommand c = new SbtCommand(errorOut, messageOut);
	
	private final KeyListener listener = new KeyListener() {

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.keyCode == 13) {
				Character[] arr = (Character[]) charEntered.toArray();
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

	/**
	 * 
	 */
	public SbtAction() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run(IAction action) {
		if (!c.isStarted()) {
			MessageDialog.openInformation(shell, "Scala Sbt", "start thread");
//			shell.open();
			c.start();
			error = new Text(shell, SWT.READ_ONLY | SWT.MULTI | SWT.ERROR);
			message = new Text(shell, SWT.READ_ONLY | SWT.MULTI | SWT.None);
			message.append("sbt started!");
			MessageDialog.openInformation(shell, "Scala Sbt", "end thread");
		} else {
			MessageDialog.openInformation(shell, "Scala Sbt", "setActive");
			shell.setActive();
		}
	}

	private void runCommand() {
		try {
			String command = commands.peek() + "\n";
			c.getOutputStream().write(command.getBytes(Charset.defaultCharset()));
			Console.log("Executed command '" + command + "'");
		} catch (IOException e) {
			c.handleException(e);
		}
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

	@Override
	public void dispose() {
		if (c.isStarted()){
			c.stop();
		}
		shell.removeKeyListener(listener);
		error = null;
		message = null;
		charEntered.clear();
		commands.clear();
	}

	@Override
	public void init(IWorkbenchWindow window) {
		shell = window.getShell();
	}

}
