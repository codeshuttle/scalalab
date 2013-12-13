package scalaplugin.actions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import scalaplugin.Console;

public class CommandRunner {

	private final String[] cmd;
	private final File baseDir;
	private final int port;
	private final Writer errorWriter;
	private final Writer inputWriter;
	
	private ScheduledFuture<?> scheduleAtFixedRate = null;
	private ExecutorService service = null;
	private ScheduledExecutorService thread = null;
	
	private Process sbtProcess = null;
//	private CommandExecutorMXBean commandExecutor = null;
	
	public CommandRunner(String[] command,String baseDir, Writer errorWriter, Writer inputWriter, int port) {
		super();
		this.cmd = command;
		this.baseDir = new File(baseDir);
		this.errorWriter = errorWriter;
		this.inputWriter = inputWriter;
		this.port = port;
	}
	
	public void run(final String command){
		try {
			sbtProcess.getOutputStream().write(("\n"+command+"\n").getBytes());
			sbtProcess.getOutputStream().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
//		if(commandExecutor!=null ){
//			commandExecutor.execute(command);
//		}else{
//			Console.error("Unable to execute command '" + command + "'");
//		}
	}
	
	private volatile boolean running = false;
	
	private final Runnable initializer = new Runnable() {
		
		@Override
		public void run() {
			try {
				ProcessBuilder pb = new ProcessBuilder(cmd);
				pb.directory(baseDir);
				sbtProcess = pb.start();
				
				thread = Executors.newScheduledThreadPool(1);
				
				readMap.put(sbtProcess.getErrorStream(), 0l);
				readMap.put(sbtProcess.getInputStream(), 0l);
				
				scheduleAtFixedRate = thread.scheduleAtFixedRate(
						new Runnable() {
							@Override
							public void run() {
								if(running) return;
								try {
									running = true;
//									Console.log("Write response from Thread error!");
									pipe(sbtProcess.getErrorStream(),errorWriter);
//									Console.log("Write response from Thread message!");
									pipe(sbtProcess.getInputStream(),inputWriter);
								} catch (IOException e) {
									handleException(e);
								}finally{
									running = false;
								}
							}
						}, 1, 1, TimeUnit.SECONDS);
				
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
				
				sbtProcess.waitFor();
				
			} catch (Exception e) {
				handleException(e);
			}finally{
			}
		}
	};
	
	
	public void start(){
		service = Executors.newSingleThreadExecutor();
		service.submit(initializer);
	}
	
	public boolean isStarted(){
		return service!=null;
	}

	public void handleException(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		Console.error(sw.toString());
		errorWriter.write(sw.toString());
	}
	
	private final Map<InputStream,Long> readMap = new HashMap<>(2);
	
	private void pipe(InputStream in,Writer out) throws IOException {
		long skip = readMap.remove(in).longValue();
		final long startSkip = skip;
		if(skip>0){
			in.skip(skip);
		}
		final StringBuffer content = new StringBuffer();
		final int available = in.available();
		if(available > 0){
			while( true ){
				int read = in.read();
				if(read==-1||skip==startSkip+available-1)  break;	

				skip++;
				content.append((char)read);
			}
		}
		if(content.length()>0){
			out.write(content.toString());
		}
		readMap.put(in, skip);
	}
	
	public void stop(){
		if(sbtProcess!=null){
			if(this.cmd[0].indexOf("scala")!=-1){
				run(":q\n");
			}else{
				run("exit\n");
			}
			sbtProcess.destroy();
			sbtProcess = null;
		}
		if (scheduleAtFixedRate!=null) {
			scheduleAtFixedRate.cancel(true);
		}
		if (thread!=null) {
			thread.shutdownNow();
			thread = null;
		}
		if (service!=null) {
			service.shutdownNow();
			service = null;
		}
	}
	
	private static final int portNum = 9099;
	private static final Writer errorOut = new Writer() {
		
		@Override
		public void write(String b) {
			System.err.append(b);
		}
	};
	private static final Writer messageOut = new Writer() {
		
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
	
}
