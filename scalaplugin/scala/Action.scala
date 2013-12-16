package scalaplugin.actions

/**
 * @author parthipanp
 *
 */
trait Writer {
	def write( content:String):Unit
}

import scala.collection.JavaConversions._
import scala.collection.mutable.Stack

import java.io.File
import java.util.ArrayList
import java.util.Collection

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
	
	def getAction():Array[String]
	
	def getPortNum():Int
	
	var shell:Shell  = null;
	var message:StyledText = null;

	val charEntered:Stack[Char] = new Stack[Char]
	val commands:Stack[String] = new Stack[String]

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
		new CommandRunner(getAction(),new File(getBaseDir()), errorOut,
					messageOut, getPortNum())
	}
	
	def getBaseDir():String={
		val projects:Array[IProject] = getProject()
		if(projects!=null && projects.length>0){
			val project:IProject = projects(0)
			val location:IPath = project.getLocation()
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
				
				if(charEntered.size>0)
					charEntered.pop()
				
			}else if (e.keyCode == 13) {
//				Character[] arr = (Character[]) charEntered
//						.toArray(new Character[] {})
//				String command = new String(convert(arr))
			  val command:String = getCommand()
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

	def getCommand():String={
	   (for(i<-charEntered.reverse.toList) yield(i))(collection.breakOut)
	}

	def run( action:IAction):Unit = {
		if (c==null) {
			c = getCommandRunner()
			Display.getDefault().asyncExec(new Runnable() {
				override def run():Unit= {
					try {
						val display:Display = Display.getCurrent()
						shell = new Shell(display, SWT.CLOSE
								| SWT.TITLE | SWT.MIN | SWT.MAX | SWT.RESIZE
								| SWT.SHELL_TRIM)// SWT.None
						shell.setLayout(new FillLayout())
						shell.setSize(500, 500)
						shell.open()
						shell.setText(title)
						message = new StyledText(shell, SWT.BORDER|SWT.MULTI)
						message.setSize(500, 500)
						val black:Color = new Color(display, 10, 10, 10)
						message.setBackground(black)
						val white:Color = new Color(display, 255, 255, 255)
						message.setForeground(white)
						c.start()
						message.addKeyListener(listener)
					} catch{
					  case e: Throwable  =>
						Console.error(e)
					}
				}
			})
		} else {
			Display.getDefault().asyncExec(new Runnable() {
				override def run():Unit= {
					shell.setActive()
				}
			})
		}
	}

	private def runCommand():Unit= {
		 val command:String = commands.pop()
		 c.run(command)
	}
//
//	private def convert(Character[] arr) {
//		char[] carr = new char[arr.length]
//		for (int i = 0 i < arr.length i++) {
//			carr[i] = arr[i]
//		}
//		return carr
//	}

	def selectionChanged( action:IAction, selection:ISelection):Unit=  {

	}

	private def showMessage( textToAppend:String, textColor:Int):Unit=  {
		Display.getDefault().asyncExec(new Runnable() {
			override def run() :Unit= {
				setMessage(textToAppend, textColor, SWT.NORMAL)
			}
		})
	}

	private def showError( textToAppend:String):Unit=  {
		Display.getDefault().asyncExec(new Runnable() {
			override def run() :Unit= {
				setMessage(textToAppend, SWT.COLOR_RED, SWT.ITALIC)
			}
		})
	}

	def dispose():Unit= {
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
				} catch {
				  case e:Throwable =>
					Console.error(e)
				}
			}
		})
	}

	def init( window:IWorkbenchWindow):Unit=  {
		// shell = window.getShell()
	}

	def getProject():Array[IProject]= {
		val workspace:IWorkspace = ResourcesPlugin.getWorkspace()
        val root:IWorkspaceRoot = workspace.getRoot()
        return root.getProjects()

	}
	
	private def setMessage(textToAppend:String,textColor:Int,fontStyle:Int):Unit= {
		try {
			val styleRange:StyleRange = new StyleRange()
			styleRange.start = message.getCharCount()
			styleRange.length = textToAppend.length()
			styleRange.fontStyle = fontStyle
			styleRange.foreground = Display.getDefault().getSystemColor(textColor)
			message.append(textToAppend)
			message.setStyleRange(styleRange)
//			Console.log(textToAppend)
		} catch {
		  case e:Throwable =>
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
	
	var title:String = "Simple Build Tool";

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

	var title:String = "Scala Interpreter";
	
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
	
	var scheduleAtFixedRate:ScheduledFuture[_] = null
	var service:ExecutorService = null
	var thread:ScheduledExecutorService = null
	
	var sbtProcess:Process = null;
//	private CommandExecutorMXBean commandExecutor = null;
	
	def run( command:String):Unit={
		try {
			sbtProcess.getOutputStream().write(("\n"+command+"\n").getBytes())
			sbtProcess.getOutputStream().flush()
		} catch{
		  case e:IOException =>
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
				val pb:ProcessBuilder = new ProcessBuilder(cmd.toList)
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
								} catch {
								  case e:IOException =>
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
				
			} catch {
			  case  e: Exception =>
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
		val sw:StringWriter = new StringWriter()
		e.printStackTrace(new PrintWriter(sw))
		Console.error(sw.toString())
		errorWriter.write(sw.toString())
	}
	
	val readMap:Map[InputStream,Long] = new HashMap(2)
	
	private def pipe( in:InputStream, out:Writer) :Unit= {
		var skip:Long = readMap.remove(in).longValue()
		val startSkip:Long = skip
		if(skip>0){
			in.skip(skip)
		}
		val content:StringBuffer = new StringBuffer()
		val available:Int = in.available()
		if(available > 0){
			var read:Int = in.read()
			var check:Boolean = ( read == -1 || skip == (startSkip+available-1) );
			while( !check ){
				skip = skip+1
				content.append(read.asInstanceOf[Char])
				
				read = in.read()
				
				check = ( read == -1 || skip == (startSkip+available-1) );
			}
		}
		if(content.length()>0){
			out.write(content.toString())
		}
		readMap.put(in, skip)
	}
	
	def stop():Unit={
		if(sbtProcess!=null){
			if( this.cmd(0).contains("scala") ){// indexOf() != -1 
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
//
//trait CommandExecutorMXBean{
//	def execute( command:String):Unit
//}

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
class CommandExecutorImpl{

	/* (non-Javadoc)
	 * @see scalaplugin.actions.CommandExecutor#execute(java.lang.String)
	 */
	 def execute(command:String){
		CommandExecutorImpl.exequeue.submit(new Runnable() {
			override def run():Unit= {
//				if("scala".equalsIgnoreCase(type)){
					CommandExecutorImpl.execommand = command
					CommandExecutorImpl.signalWatch()
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
	val XSBT_BOOT:String = "xsbt.boot.Boot"

	val SCALA_RUNNER:String = "scalaplugin.repl.IScala"

	val NAME_COMMAND_EXECUTOR:String = "org.scalaplugin:type=CommandExecutor"

	val exequeue:ExecutorService = Executors.newFixedThreadPool(1)

	var execommand:String = null

	var waitLatch:CountDownLatch = null
	
	val emptyArr:Array[String] = Array[String]();
	
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
	
	val reader:BufferedReader = new BufferedReader(new Reader() {
		
		override def read(cbuf:Array[Char], off:Int, len:Int):Int = {
			0
		}
		
		override def close():Unit= {
		}
	}){

		override def readLine():String = {
			waitAndWatch()
			val c:String = execommand
			execommand = null
//			System.out.println("Command "+c);
			c
		}
	};
	
	def signalWatch():Unit= {
		if(waitLatch!=null){
			waitLatch.countDown()
			waitLatch = null
		}
	}
	
	
	def main(args:Array[String]){
//		
//		System.out.println("Started CommandExecutorImpl! Console readLine "+new BufferedReader (new InputStreamReader(System.in)).readLine());
		ManagementFactory.getPlatformMBeanServer().registerMBean(new CommandExecutorImpl, new ObjectName(NAME_COMMAND_EXECUTOR));
		runtype = args(0)
		if("scala".equalsIgnoreCase(runtype)){
			invokeMain(SCALA_RUNNER,emptyArr)
		}else{
//			scalaConsole();
//			System.setIn(in);
			invokeMain(XSBT_BOOT,emptyArr)
		}
	}
	
	var runtype:String = null
	var loadClass:Class[_] = null
	
	private def invokeMain( clazzname:String, params:Array[String]):Unit = {
	/* throws ClassNotFoundException,
			NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {*/
		if(loadClass==null){
			loadClass = ClassLoader.getSystemClassLoader.loadClass(clazzname)
		}
		if(loadClass!=null){
			val main:Method = loadClass.getMethod("main")
			main.invoke(null, params)
		}else{
			System.err.println("Unable to invoke sbt Boot class.")
		}
	}

	def waitAndWatch():Unit =  {
		if(execommand==null){
			waitLatch = new CountDownLatch(1)
			try {
				waitLatch.await()
			} catch {
			  case e:InterruptedException =>
				e.printStackTrace()
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