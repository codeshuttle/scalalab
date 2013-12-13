package scalaplugin.actions

/**
 * @author parthipanp
 *
 */
trait Writer {
	def write( content:String):Unit
}

import java.io.File
import java.util.ArrayList
import java.util.Collection
import java.util.Stack

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.resources.IWorkspaceRoot
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.IPath
import org.eclipse.jface.action.IAction
import org.eclipse.jface.viewers.ISelection
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.StyleRange
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.events.KeyEvent
import org.eclipse.swt.events.KeyListener
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import org.eclipse.ui.IWorkbenchWindow
import org.eclipse.ui.IWorkbenchWindowActionDelegate

import scalaplugin.Console

/**
 * @author parthipanp
 * 
 */
trait AbstractAction {

	var title:String
//	public static final Collection<AbstractAction> actions = new ArrayList<>(2)
	
	abstract def getAction():Array[String]
	
	abstract def getPortNum():Int
	
	var shell:Shell  = null;
	var message:StyledText = null;

	val charEntered:Stack = new Stack;
	val commands:Stack = new Stack;

	val errorOut:Writer = new Writer() {

		override def write(b:String):Unit= {
			showError(b)
		}
	}
	
	val messageOut:Writer = new Writer() {

		override def write(b:String):Unit= {
			showMessage(b, SWT.COLOR_BLUE)
		}
	}
	
	private def getCommandRunner():CommandRunner = {
		new CommandRunner(getAction(),getBaseDir(), errorOut,
					messageOut, getPortNum())
	}
	
	def getBaseDir():String={
		Array[IProject] projects = getProject()
		if(projects!=null && projects.length>0){
			IProject project = projects(0)
			IPath location = project.getLocation()
			location.toFile().getAbsolutePath()
		}else{
			"."
		}
	}
	
	var c:CommandRunner = null;
	
	val  listener:KeyListener = new KeyListener() {

		override def keyReleased( e:KeyEvent):Unit = {
//			Console.log("key code '" + e.keyCode + "' character '"+e.character+"' ")
//			showMessage(""+e.character, SWT.COLOR_WHITE)
			if(e.keyCode==131072&&e.character!=' '){
				// continue...
			}else if(e.keyCode==16777224){
				// continue...
			}else if (e.keyCode == 8) {
				
				if(!charEntered.isEmpty())
					charEntered.pop()
				
			}else if (e.keyCode == 13) {
				Character[] arr = (Character[]) charEntered
						.toArray(new Character[] {})
				String command = new String(convert(arr))
				commands.push(command)
				charEntered.clear()
				Console.log("runcommand "+command)
				runCommand()
			} else {
				charEntered.push(e.character)
			}
		}

		override def keyPressed( e:KeyEvent) :Unit ={

		}
	}


	override def run(IAction action):Unit = {
		if (c==null) {
			c = getCommandRunner()
			Display.getDefault().asyncExec(new Runnable() {
				override def run():Unit= {
					try {
						Display display = Display.getCurrent()
						shell = new Shell(display, SWT.CLOSE
								| SWT.TITLE | SWT.MIN | SWT.MAX | SWT.RESIZE
								| SWT.SHELL_TRIM)// SWT.None
						shell.setLayout(new FillLayout())
						shell.setSize(500, 500)
						shell.open()
						shell.setText(title)
						message = new StyledText(shell, SWT.BORDER|SWT.MULTI)
						message.setSize(500, 500)
						Color black = new Color(display, 10, 10, 10)
						message.setBackground(black)
						Color white = new Color(display, 255, 255, 255)
						message.setForeground(white)
						c.start()
						message.addKeyListener(listener)
					} catch (Throwable e) {
						Console.error(e)
					}
				}
			})
		} else {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					shell.setActive()
				}
			})
		}
	}

	private def runCommand():Unit= {
		 String command = commands.peek()
		 c.run(command)
	}

	private def convert(Character[] arr) {
		char[] carr = new char[arr.length]
		for (int i = 0 i < arr.length i++) {
			carr[i] = arr[i]
		}
		return carr
	}

	override def selectionChanged( action:IAction, selection:ISelection):Unit=  {

	}

	private def showMessage( textToAppend:String, textColor:Int):Unit=  {
		Display.getDefault().asyncExec(new Runnable() {
			override def run() :Unit= {
				setMessage(textToAppend, textColor, SWT.NORMAL)
			}
		})
	}

	private def showError(final String textToAppend):Unit=  {
		Display.getDefault().asyncExec(new Runnable() {
			override def run() :Unit= {
				setMessage(textToAppend, SWT.COLOR_RED, SWT.ITALIC)
			}
		})
	}

	override def dispose():Unit= {
		Display.getDefault().asyncExec(new Runnable() {
			override def run() :Unit= {
				try {
					if (c!=null) {
						c.stop()
					}
					message.removeKeyListener(listener)
					message = null
					shell.dispose()
					charEntered.clear()
					commands.clear()
					c = null
				} catch (Throwable e) {
					Console.error(e)
				}
			}
		})
	}

	override def init( window:IWorkbenchWindow):Unit=  {
		// shell = window.getShell()
	}

	def getProject():Array[IProject]= {
		IWorkspace workspace = ResourcesPlugin.getWorkspace()
        IWorkspaceRoot root = workspace.getRoot()
        return root.getProjects()

	}
	
	private def setMessage(textToAppend:String,textColor:Int,fontStyle:Int):Unit= {
		try {
			StyleRange styleRange = new StyleRange()
			styleRange.start = message.getCharCount()
			styleRange.length = textToAppend.length()
			styleRange.fontStyle = fontStyle
			styleRange.foreground = Display.getDefault().getSystemColor(textColor)
			message.append(textToAppend)
			message.setStyleRange(styleRange)
//			Console.log(textToAppend)
		} catch (Throwable e) {
			Console.error(e)
		}
	}
}
object AbstractAction{

	var OS:String = null;

	def getOsName():String= {
		if (OS == null) {
			OS = System.getProperty("os.name")
		}
		OS
	}

	def isWindows():Boolean = {
		getOsName().startsWith("Windows")
	}

	def getEclipseHome():String= {
		 new File(".").getAbsolutePath()
	}
	
	def getJavaHome():String= {
		 getEnvOrSys("JAVA_HOME")
	}
	
	def getSbtHome():String= {
		 getEnvOrSys("SBT_HOME")
	}

	def getScalaHome():String= {
		 getEnvOrSys("SCALA_HOME")
	}
	
	def getEnvOrSys(v:String):String= {
		if(System.getProperty(v)!=null){
			 System.getProperty(v)
		}else{
			 System.getenv(v)
		}
	}
}

class SbtAction extends AbstractAction with IWorkbenchWindowActionDelegate {
	
	this(){
		title = "Simple Build Tool";
	}
	

	override def getAction():Array[String]={
		 Array(
				AbstractAction.getJavaHome() + File.separator + "bin" +  File.separator + "java.exe",
				"-Xmx512m", 
				"-XX:MaxPermSize=256m",
				"-XX:ReservedCodeCacheSize=128m",
				"-Dsbt.log.format=true",
//				"-Djline.terminal=jline.UnsupportedTerminal",
				
//				"-Dcom.sun.management.jmxremote",
//				"-Dcom.sun.management.jmxremote.port="+portNum,
//				"-Dcom.sun.management.jmxremote.local.only=false",
//				"-Dcom.sun.management.jmxremote.authenticate=false",
//				"-Dcom.sun.management.jmxremote.ssl=false",
				
				"-cp",
				""+AbstractAction.getSbtHome() + File.separator + "bin" + File.separator + "sbt-launch.jar"+File.pathSeparatorChar+System.getProperty("java.class.path")+File.pathSeparatorChar+"D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui_3.105.0.v20130522-1122.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.swt_3.102.1.v20130827-2021.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.swt.win32.win32.x86_64_3.102.1.v20130827-2048.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.jface_3.9.1.v20130725-1141.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.commands_3.6.100.v20130515-1857.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.workbench_3.105.1.v20130821-1411.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.e4.ui.workbench3_0.12.0.v20130515-1857.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.runtime_3.9.0.v20130326-1255.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.osgi_3.9.1.v20130814-1242.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.common_3.6.200.v20130402-1505.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.jobs_3.5.300.v20130429-1813.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.runtime.compatibility.registry_3.5.200.v20130514-1256\\runtime_registry_compatibility.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.registry_3.5.301.v20130717-1549.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.preferences_3.5.100.v20130422-1538.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.contenttype_3.4.200.v20130326-1255.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.app_1.3.100.v20130327-1442.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.jface.text_3.8.101.v20130802-1147.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.text_3.5.300.v20130515-1451.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.resources_3.8.101.v20130717-0806.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.editors_3.8.100.v20130513-1637.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.filebuffers_3.5.300.v20130225-1821.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.ide_3.9.1.v20130704-1828.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.views_3.6.100.v20130326-1250.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.workbench.texteditor_3.8.101.v20130729-1318.jar;C:\\Users\\Parthi\\Documents\\GitHub\\scalalab\\scalaplugin\\bin",
				"xsbt.boot.Boot"
//				"scalaplugin.actions.CommandExecutorImpl",
//				"sbt"
			)
//		Array(
//				AbstractAction.getJavaHome() + File.separator + "bin" +  File.separator + "java.exe",
//				"-Xmx512m", 
//				"-XX:MaxPermSize=256m",
//				"-XX:ReservedCodeCacheSize=128m",
//				"-Dsbt.log.format=true",
//
//				"-Dcom.sun.management.jmxremote",
//				"-Dcom.sun.management.jmxremote.port="+getPortNum(),
//				"-Dcom.sun.management.jmxremote.local.only=false",
//				"-Dcom.sun.management.jmxremote.authenticate=false",
//				"-Dcom.sun.management.jmxremote.ssl=false",
//				
//				"-cp",
//				""+AbstractAction.getSbtHome() + File.separator + "bin" + File.separator + "sbt-launch.jar"+File.pathSeparatorChar+System.getProperty("java.class.path")+File.pathSeparatorChar+"D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui_3.105.0.v20130522-1122.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.swt_3.102.1.v20130827-2021.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.swt.win32.win32.x86_64_3.102.1.v20130827-2048.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.jface_3.9.1.v20130725-1141.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.commands_3.6.100.v20130515-1857.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.workbench_3.105.1.v20130821-1411.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.e4.ui.workbench3_0.12.0.v20130515-1857.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.runtime_3.9.0.v20130326-1255.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.osgi_3.9.1.v20130814-1242.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.common_3.6.200.v20130402-1505.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.jobs_3.5.300.v20130429-1813.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.runtime.compatibility.registry_3.5.200.v20130514-1256\\runtime_registry_compatibility.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.registry_3.5.301.v20130717-1549.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.preferences_3.5.100.v20130422-1538.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.contenttype_3.4.200.v20130326-1255.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.app_1.3.100.v20130327-1442.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.jface.text_3.8.101.v20130802-1147.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.text_3.5.300.v20130515-1451.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.resources_3.8.101.v20130717-0806.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.editors_3.8.100.v20130513-1637.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.filebuffers_3.5.300.v20130225-1821.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.ide_3.9.1.v20130704-1828.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.views_3.6.100.v20130326-1250.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.workbench.texteditor_3.8.101.v20130729-1318.jar;C:\\Users\\Parthi\\Documents\\GitHub\\scalalab\\scalaplugin\\bin",
//				
//				"scalaplugin.actions.CommandExecutorImpl",
//				"sbt"
//			)
	}

	override def getPortNum():Int = {
		8808
	}

}

class ScalaAction extends AbstractAction with IWorkbenchWindowActionDelegate{

	/**
	 * 
	 */
	this() {
		title = "Scala Interpreter";
	}
	
	override def getAction():Array[String]={
		 Array(
				AbstractAction.getJavaHome() + File.separator + "bin" +  File.separator + "java.exe",
				"-Xmx256M", 
				"-Xms32M",
				"-Dscala.home=\""+AbstractAction.getScalaHome()+"\"",
				"-Dscala.usejavacp=true",

				"-Dcom.sun.management.jmxremote",
				"-Dcom.sun.management.jmxremote.port="+getPortNum(),
				"-Dcom.sun.management.jmxremote.local.only=false",
				"-Dcom.sun.management.jmxremote.authenticate=false",
				"-Dcom.sun.management.jmxremote.ssl=false",
				
				"-cp",
				""+AbstractAction.getScalaHome() + File.separator + "lib" + File.separator + "*" +File.pathSeparatorChar+System.getProperty("java.class.path")+File.pathSeparatorChar+"D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui_3.105.0.v20130522-1122.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.swt_3.102.1.v20130827-2021.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.swt.win32.win32.x86_64_3.102.1.v20130827-2048.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.jface_3.9.1.v20130725-1141.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.commands_3.6.100.v20130515-1857.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.workbench_3.105.1.v20130821-1411.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.e4.ui.workbench3_0.12.0.v20130515-1857.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.runtime_3.9.0.v20130326-1255.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.osgi_3.9.1.v20130814-1242.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.common_3.6.200.v20130402-1505.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.jobs_3.5.300.v20130429-1813.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.runtime.compatibility.registry_3.5.200.v20130514-1256\\runtime_registry_compatibility.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.registry_3.5.301.v20130717-1549.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.preferences_3.5.100.v20130422-1538.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.contenttype_3.4.200.v20130326-1255.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.app_1.3.100.v20130327-1442.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.jface.text_3.8.101.v20130802-1147.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.text_3.5.300.v20130515-1451.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.resources_3.8.101.v20130717-0806.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.editors_3.8.100.v20130513-1637.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.filebuffers_3.5.300.v20130225-1821.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.ide_3.9.1.v20130704-1828.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.views_3.6.100.v20130326-1250.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.workbench.texteditor_3.8.101.v20130729-1318.jar;C:\\Users\\Parthi\\Documents\\GitHub\\scalalab\\scalaplugin\\bin;C:\\Users\\Parthi\\Documents\\GitHub\\scalalab\\scalaplugin\\scala",
				
				"scalaplugin.actions.CommandExecutorImpl",
				"scala"
			)
	}
	
	override def getPortNum():Int = {
		8809
	}
}

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.util.HashMap
import java.util.Map
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

import javax.management.JMX
import javax.management.MBeanServerConnection
import javax.management.MalformedObjectNameException
import javax.management.ObjectName
import javax.management.remote.JMXConnector
import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXServiceURL

import scalaplugin.Console

class CommandRunner(cmd:Array[String],baseDir:File,errorWriter:Writer,
			inputWriter:Writer,port:Int) {
	
	private ScheduledFuture<?> scheduleAtFixedRate = null;
	private ExecutorService service = null;
	private ScheduledExecutorService thread = null;
	
	private Process sbtProcess = null;
//	private CommandExecutorMXBean commandExecutor = null;
	
	def run( command:String):Unit={
		try {
			sbtProcess.getOutputStream().write(("\n"+command+"\n").getBytes())
			sbtProcess.getOutputStream().flush()
		} catch (IOException e) {
			e.printStackTrace()
		}
//		if(commandExecutor!=null ){
//			commandExecutor.execute(command);
//		}else{
//			Console.error("Unable to execute command '" + command + "'");
//		}
	}
	
	var running:Boolean = false
	
	val initializer:Runnable = new Runnable() {
		
		override def run():Unit= {
			try {
				ProcessBuilder pb = new ProcessBuilder(cmd)
				pb.directory(baseDir)
				sbtProcess = pb.start()
				
				thread = Executors.newScheduledThreadPool(1)
				
				readMap.put(sbtProcess.getErrorStream(), 0l)
				readMap.put(sbtProcess.getInputStream(), 0l)
				
				scheduleAtFixedRate = thread.scheduleAtFixedRate(
						new Runnable() {
							override def run():Unit= {
								if(running) return
								try {
									running = true
//									Console.log("Write response from Thread error!")
									pipe(sbtProcess.getErrorStream(),errorWriter)
//									Console.log("Write response from Thread message!")
									pipe(sbtProcess.getInputStream(),inputWriter)
								} catch (IOException e) {
									handleException(e)
								}finally{
									running = false
								}
							}
						}, 1, 1, TimeUnit.SECONDS)
				
//				Thread.sleep(10000);
				
//				JMXServiceURL u = new JMXServiceURL(
//						  "service:jmx:rmi:///jndi/rmi://localhost:" + port +  "/jmxrmi");
//				JMXConnector jmxc = JMXConnectorFactory.connect(u);
//				MBeanServerConnection mbsc = 
//					    jmxc.getMBeanServerConnection();
//				ObjectName mbeanName = new ObjectName(CommandExecutorImpl.NAME_COMMAND_EXECUTOR);
//				commandExecutor = JMX.newMBeanProxy(mbsc, mbeanName, 
//						CommandExecutorMXBean.class, true);
				
//				Console.log("connected to the remote process!");
				
				sbtProcess.waitFor()
				
			} catch (Exception e) {
				handleException(e)
			}finally{
			}
		}
	};
	
	def start():Unit={
		service = Executors.newSingleThreadExecutor();
		service.submit(initializer);
	}
	
	def isStarted():Boolean={
		service!=null
	}

	def handleException( e:Exception):Unit= {
		StringWriter sw = new StringWriter()
		e.printStackTrace(new PrintWriter(sw))
		Console.error(sw.toString())
		errorWriter.write(sw.toString())
	}
	
	val readMap:Map = new HashMap(2)
	
	private def pipe( in:InputStream, out:Writer) :Unit= {
		long skip = readMap.remove(in).longValue()
		final long startSkip = skip
		if(skip>0){
			in.skip(skip)
		}
		final StringBuffer content = new StringBuffer()
		final int available = in.available()
		if(available > 0){
			while( true ){
				int read = in.read()
				if(read==-1||skip==startSkip+available-1)  break

				skip++
				content.append((char)read)
			}
		}
		if(content.length()>0){
			out.write(content.toString())
		}
		readMap.put(in, skip)
	}
	
	def stop():Unit={
		if(sbtProcess!=null){
			if(this.cmd[0].indexOf("scala")!=-1){
				run(":q\n")
			}else{
				run("exit\n")
			}
			sbtProcess.destroy()
			sbtProcess = null
		}
		if (scheduleAtFixedRate!=null) {
			scheduleAtFixedRate.cancel(true)
		}
		if (thread!=null) {
			thread.shutdownNow()
			thread = null
		}
		if (service!=null) {
			service.shutdownNow()
			service = null
		}
	}
	/*
	val int portNum = 9099;
	val Writer errorOut = new Writer() {
		
		@Override
		public void write(String b) {
			System.err.append(b);
		}
	};
	val Writer messageOut = new Writer() {
		
		@Override
		public void write(String b){
			System.out.append(b);
		}
	};
	private static String baseBir = "C:\\Users\\Parthi\\Documents\\GitHub\\scalalab\\sample1";
	
	public static void main(String[] args) throws InterruptedException, IOException, MalformedObjectNameException{
		String [] cmd =  
				
//			new String[]{
//				AbstractAction.getJavaHome() + File.separator + "bin" +  File.separator + "java.exe",
//				"-Xmx256M", 
//				"-Xms32M",
//				"-Dscala.home=\""+AbstractAction.getScalaHome()+"\"",
//				"-Dscala.usejavacp=true",
//				
//				"-Dcom.sun.management.jmxremote",
//				"-Dcom.sun.management.jmxremote.port="+portNum,
//				"-Dcom.sun.management.jmxremote.local.only=false",
//				"-Dcom.sun.management.jmxremote.authenticate=false",
//				"-Dcom.sun.management.jmxremote.ssl=false",
//				
//				"-cp",
//				""+AbstractAction.getScalaHome() + File.separator + "lib" + File.separator + "*" +File.pathSeparatorChar+System.getProperty("java.class.path")+File.pathSeparatorChar+"D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui_3.105.0.v20130522-1122.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.swt_3.102.1.v20130827-2021.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.swt.win32.win32.x86_64_3.102.1.v20130827-2048.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.jface_3.9.1.v20130725-1141.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.commands_3.6.100.v20130515-1857.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.workbench_3.105.1.v20130821-1411.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.e4.ui.workbench3_0.12.0.v20130515-1857.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.runtime_3.9.0.v20130326-1255.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.osgi_3.9.1.v20130814-1242.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.common_3.6.200.v20130402-1505.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.jobs_3.5.300.v20130429-1813.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.runtime.compatibility.registry_3.5.200.v20130514-1256\\runtime_registry_compatibility.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.registry_3.5.301.v20130717-1549.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.preferences_3.5.100.v20130422-1538.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.contenttype_3.4.200.v20130326-1255.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.app_1.3.100.v20130327-1442.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.jface.text_3.8.101.v20130802-1147.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.text_3.5.300.v20130515-1451.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.resources_3.8.101.v20130717-0806.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.editors_3.8.100.v20130513-1637.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.filebuffers_3.5.300.v20130225-1821.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.ide_3.9.1.v20130704-1828.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.views_3.6.100.v20130326-1250.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.workbench.texteditor_3.8.101.v20130729-1318.jar;C:\\Users\\Parthi\\Documents\\GitHub\\scalalab\\scalaplugin\\bin;C:\\Users\\Parthi\\Documents\\GitHub\\scalalab\\scalaplugin\\scala;",
//				
//				"scalaplugin.actions.CommandExecutorImpl",
//				"scala"
//			};
				
			new String[]{
			AbstractAction.getJavaHome() + File.separator + "bin" +  File.separator + "java.exe",
			"-Xmx512m", 
			"-XX:MaxPermSize=256m",
			"-XX:ReservedCodeCacheSize=128m",
			"-Dsbt.log.format=true",
//			"-Djline.terminal=jline.UnsupportedTerminal",
			
//			"-Dcom.sun.management.jmxremote",
//			"-Dcom.sun.management.jmxremote.port="+portNum,
//			"-Dcom.sun.management.jmxremote.local.only=false",
//			"-Dcom.sun.management.jmxremote.authenticate=false",
//			"-Dcom.sun.management.jmxremote.ssl=false",
			
			"-cp",
			""+AbstractAction.getSbtHome() + File.separator + "bin" + File.separator + "sbt-launch.jar"+File.pathSeparatorChar+System.getProperty("java.class.path")+File.pathSeparatorChar+"D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui_3.105.0.v20130522-1122.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.swt_3.102.1.v20130827-2021.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.swt.win32.win32.x86_64_3.102.1.v20130827-2048.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.jface_3.9.1.v20130725-1141.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.commands_3.6.100.v20130515-1857.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.workbench_3.105.1.v20130821-1411.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.e4.ui.workbench3_0.12.0.v20130515-1857.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.runtime_3.9.0.v20130326-1255.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.osgi_3.9.1.v20130814-1242.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.common_3.6.200.v20130402-1505.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.jobs_3.5.300.v20130429-1813.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.runtime.compatibility.registry_3.5.200.v20130514-1256\\runtime_registry_compatibility.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.registry_3.5.301.v20130717-1549.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.preferences_3.5.100.v20130422-1538.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.contenttype_3.4.200.v20130326-1255.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.app_1.3.100.v20130327-1442.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.jface.text_3.8.101.v20130802-1147.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.text_3.5.300.v20130515-1451.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.resources_3.8.101.v20130717-0806.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.editors_3.8.100.v20130513-1637.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.filebuffers_3.5.300.v20130225-1821.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.ide_3.9.1.v20130704-1828.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.views_3.6.100.v20130326-1250.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.workbench.texteditor_3.8.101.v20130729-1318.jar;C:\\Users\\Parthi\\Documents\\GitHub\\scalalab\\scalaplugin\\bin",
			"xsbt.boot.Boot"
//			"scalaplugin.actions.CommandExecutorImpl",
//			"sbt"
		};
		
		new CommandRunner(cmd,baseBir, errorOut, messageOut, portNum).start();
	}
	*/
}

trait CommandExecutorMXBean{
	def execute( command:String):Unit
}

import java.io.BufferedReader
import java.io.IOException
import java.io.Reader
import java.lang.management.ManagementFactory
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import javax.management.MXBean
import javax.management.ObjectName

@MXBean
class CommandExecutorImpl with CommandExecutorMXBean{

	this() {
	}

	/* (non-Javadoc)
	 * @see scalaplugin.actions.CommandExecutor#execute(java.lang.String)
	 */
	@Override
	public void execute(final String command){
		exequeue.submit(new Runnable() {
			@Override
			public void run() {
//				if("scala".equalsIgnoreCase(type)){
					CommandExecutorImpl.execommand = command;
					CommandExecutorImpl.signalWatch();
//				}else{
//					try {
//						invokeMain(XSBT_BOOT,new String[]{command});
//					} catch (ClassNotFoundException e) {
//						e.printStackTrace();
//					} catch (NoSuchMethodException e) {
//						e.printStackTrace();
//					} catch (IllegalAccessException e) {
//						e.printStackTrace();
//					} catch (InvocationTargetException e) {
//						e.printStackTrace();
//					}
//				}
			}
		});
	} 
	
}

object CommandExecutorImpl{
	val XSBT_BOOT:String = "xsbt.boot.Boot";

	val SCALA_RUNNER:String = "scalaplugin.repl.IScala";

	val NAME_COMMAND_EXECUTOR:String = "org.scalaplugin:type=CommandExecutor";

	val exequeue:ExecutorService = Executors.newFixedThreadPool(1);

	var execommand:String = null;

	var waitLatch:CountDownLatch = null;
	
//	private static volatile int cursor = 0; 	
//	val InputStream in =  new InputStream() {
//		
//		@Override
//		public int read() throws IOException {
//			waitAndWatch();
//			if(execommand!=null && cursor>=execommand.length()){
//				cursor = 0;
//				execommand=null;
//				return 13;
//			}
//			return execommand.charAt(cursor++);
//		}
//	};
	
	val BufferedReader reader = new BufferedReader(new Reader() {
		
		override def read(cbuf:Array[Char], off:Int, len:Int):Int = {
			0
		}
		
		override def close():Unit= {
		}
	}){

		override def readLine():String = {
			waitAndWatch();
			String c = execommand;
			execommand = null;
//			System.out.println("Command "+c);
			c;
		}
	};
	
	def signalWatch():Unit= {
		if(waitLatch!=null){
			waitLatch.countDown()
			waitLatch = null
		}
	}
	
	
	def main(Array[String] args):Unit={
//		
//		System.out.println("Started CommandExecutorImpl! Console readLine "+new BufferedReader (new InputStreamReader(System.in)).readLine());
		ManagementFactory.getPlatformMBeanServer().registerMBean(new CommandExecutorImpl, new ObjectName(NAME_COMMAND_EXECUTOR));
		type = args(0);
		if("scala".equalsIgnoreCase(type)){
			invokeMain(SCALA_RUNNER,new String[]{});
		}else{
//			scalaConsole();
//			System.setIn(in);
			invokeMain(XSBT_BOOT,new String[]{});
		}
	}
	var type:String = null
	var loadClass:Class = null
	
	private def invokeMain( clazzname:String, params:Array[String]):Unit = {
	/* throws ClassNotFoundException,
			NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {*/
		if(loadClass==null){
			loadClass = CommandExecutorImpl.class.getClassLoader().loadClass(clazzname)
		}
		if(loadClass!=null){
			Method main = loadClass.getMethod("main", String[].class)
			main.invoke(null, (Object)params);
		}else{
			System.err.println("Unable to invoke sbt Boot class.")
		}
	}

	def waitAndWatch():Unit =  {
		if(execommand==null){
			waitLatch = new CountDownLatch(1);
			try {
				waitLatch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
//				return ":q";
			}
		}
	}

	
	
//	org.fusesource.jansi.internal.Kernel32
//	STD_INPUT_HANDLE
//	org.fusesource.jansi.internal.WindowsSupport.setConsoleMode(int mode) 
	
	
//	@Override
//	public void write(final String command) {
//		queue.submit(new Runnable() {
//			@Override
//			public void run() {
//				System.out.println(command);
//			}
//		});
//	}
	
//	private static void scalaConsole(){
//		Class<?> consoleClass = null;
//		try {
//			if(consoleClass==null)
//				consoleClass = CommandExecutorImpl.class.getClassLoader().loadClass("scala.Console");
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			Method setIn = consoleClass.getMethod("setIn", Reader.class);
//			setIn.invoke(null, (Object)reader);
//		} catch (NoSuchMethodException e) {
//			e.printStackTrace();
//		} catch (SecurityException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
//		}
//	}
}