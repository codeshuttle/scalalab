/**
 * 
 */
package scalaplugin.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import scalaplugin.Console;

/**
 * @author parthipanp
 * 
 */
public abstract class AbstractAction implements IWorkbenchWindowActionDelegate {

	public static final Collection<AbstractAction> actions = new ArrayList<>(2);
	
	abstract String[] getAction();

	private Shell shell = null;
	private StyledText message = null;

	private final Stack<Character> charEntered = new Stack<>();
	private final Stack<String> commands = new Stack<>();

	private final Writer errorOut = new Writer() {

		@Override
		public void write(String b) {
			showError(b);
		}
	};
	private final Writer messageOut = new Writer() {

		@Override
		public void write(String b) {
			showMessage(b, SWT.COLOR_BLUE);
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
//			Console.log("key code '" + e.keyCode + "'");
			showMessage(""+e.character, SWT.COLOR_WHITE);
			if (e.keyCode == 8 && !charEntered.isEmpty()) {
				charEntered.pop();
			}else if (e.keyCode == 13) {
				Character[] arr = (Character[]) charEntered
						.toArray(new Character[] {});
				String command = new String(convert(arr));
				commands.push(command);
				charEntered.clear();
				runCommand();
			} else {
				charEntered.push(e.character);
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
		actions.add(this);
	}

	@Override
	public void run(IAction action) {
		if (c==null) {
			c = getCommandRunner();
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					try {
						Display display = Display.getCurrent();
						shell = new Shell(display, SWT.CLOSE
								| SWT.TITLE | SWT.MIN | SWT.MAX | SWT.RESIZE
								| SWT.SHELL_TRIM);// SWT.None
						shell.setLayout(new FillLayout());
						shell.setSize(500, 500);
						shell.open();
						shell.setText(title);
						message = new StyledText(shell, SWT.BORDER|SWT.MULTI);
						message.setSize(500, 500);
						Color black = new Color(display, 10, 10, 10);
						message.setBackground(black);
						c.start();
						message.addKeyListener(listener);
					} catch (Throwable e) {
						Console.error(e);
					}
				}
			});
		} else {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					shell.setActive();
				}
			});
		}
	}

	private void runCommand() {
		 String command = commands.peek() + "\n";
		 c.run(command);
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

	private void showMessage(final String textToAppend,final int textColor) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				setMessage(textToAppend, textColor, SWT.NORMAL);
			}
		});
	}

	private void showError(final String textToAppend) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				setMessage(textToAppend, SWT.COLOR_RED, SWT.ITALIC);
			}
		});
	}

	@Override
	public void dispose() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					if (c!=null) {
						c.stop();
					}
					message.removeKeyListener(listener);
					message = null;
					shell.dispose();
					charEntered.clear();
					commands.clear();
					c = null;
				} catch (Throwable e) {
					Console.error(e);
				}
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

	static String getJavaHome(){
		return getEnvOrSys("JAVA_HOME");
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

	private void setMessage(final String textToAppend, final int textColor, int fontStyle) {
		try {
			StyleRange styleRange = new StyleRange();
			styleRange.start = message.getCharCount();
			styleRange.length = textToAppend.length();
			styleRange.fontStyle = fontStyle;
			styleRange.foreground = Display.getDefault().getSystemColor(textColor);
			message.append(textToAppend);
			message.setStyleRange(styleRange);
//			Console.log(textToAppend);
		} catch (Throwable e) {
			Console.error(e);
		}
	}
}
