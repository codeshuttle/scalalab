/**
 * 
 */
package scalaplugin.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

	private IWorkbenchWindow activeWindow = null;
	private Shell shell = null;
	private Process sbtProcess = null;
	private ScheduledExecutorService thread = null;
	private InputStream errorStream = null;
	private InputStream inputStream = null;
	private OutputStream outputStream = null;
	private ScheduledFuture<?> scheduleAtFixedRate = null;
	
	private Collection<Character> charEntered = new ArrayList<>();
	private Stack<String> commands = new Stack<>();
	
	private final KeyListener listener = new KeyListener() {
		
		@Override
		public void keyReleased(KeyEvent e) {
			if(e.keyCode==13){
				Character[] arr = (Character[])charEntered.toArray();
				String command = new String(convert(arr));
				commands.push(command);
				charEntered.clear();
				runCommand();
			}else{
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
		try {
			if (activeWindow == null) {
				sbtProcess = Runtime.getRuntime().exec("sbt");
				errorStream = sbtProcess.getErrorStream();
				inputStream = sbtProcess.getInputStream();
				outputStream = sbtProcess.getOutputStream();
				thread = Executors.newScheduledThreadPool(1);
				scheduleAtFixedRate = thread.scheduleAtFixedRate(new Runnable() {
					@Override
					public void run() {
						try {
							Text error = new Text(shell, SWT.ERROR);
							error.setText(fromStream(errorStream));
							error.pack();
							Text message = new Text(shell, SWT.ALL);
							message.setText(fromStream(inputStream));
							message.pack();
						} catch (IOException e) {
							handleException(e);
						}
					}
				}, 5, 5, TimeUnit.SECONDS);
				
				shell.addKeyListener(listener);
			} else {
				shell.setActive();
			}
		} catch (IOException e) {
			handleException(e);
			sbtProcess = null;
		}
	}

	private void runCommand() {
		try {
			String command = commands.peek()+"\n";
			outputStream.write(command.getBytes(Charset.defaultCharset()));
			Console.log("Executed command '"+command+"'");
		} catch (IOException e) {
			handleException(e);
		}
	}

	private char[] convert(Character[] arr){
		char[] carr = new char[arr.length];
		for(int i=0;i<arr.length;i++){
			carr[i] = arr[i];
		}
		return carr;
	}
	
	private void handleException(IOException e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		Text exceptionTrace = new Text(shell, SWT.NONE);
		String string = sw.toString();
		exceptionTrace.setText(string);
		exceptionTrace.pack();
		MessageDialog.openInformation(shell, "Scala Sbt", string);
	}
	
	String fromStream(InputStream in) throws IOException
	{
	    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	    StringBuilder out = new StringBuilder();
	    String line;
	    while ((line = reader.readLine()) != null) {
	        out.append(line);
	    }
	    return out.toString();
	}
	
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		if(thread!=null){
			shell.removeKeyListener(listener);
			scheduleAtFixedRate.cancel(true);
			thread.shutdownNow();
			errorStream = null;
			inputStream = null;
			outputStream = null;
		}
	}

	@Override
	public void init(IWorkbenchWindow window) {
		activeWindow = window;
		shell = window.getShell();
	}

}
