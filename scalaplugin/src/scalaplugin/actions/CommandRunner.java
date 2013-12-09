package scalaplugin.actions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import scalaplugin.Console;

public class CommandRunner {

//	public static final String SBT_BAT = "D:\\soft\\sbt\\bin\\sbt.bat";
//	public static final String SCALA_BAT = "D:\\soft\\scala-2.11.0-M5\\bin\\scala.bat";
	
	private final String[] cmd;
	private final File baseDir;
	private final Writer errorWriter;
	private final Writer inputWriter;
	
	private ScheduledFuture<?> scheduleAtFixedRate = null;
	private ExecutorService service = null;
	private ScheduledExecutorService thread = null;
	
	private Process sbtProcess = null;
	
	public CommandRunner(String[] command,String baseDir, Writer errorWriter, Writer inputWriter) {
		super();
		this.cmd = command;
		this.baseDir = new File(baseDir);
		this.errorWriter = errorWriter;
		this.inputWriter = inputWriter;
	}
	
	public void run(String command){
		try {
			OutputStream outputStream = sbtProcess.getOutputStream();
			outputStream.write(command.getBytes("UTF-8"));
			outputStream.flush();
			Console.log("Executed command '" + command + "'");
		} catch (IOException e) {
			handleException(e);
		}
	}
	
	private volatile boolean running = false;
	
	private final Runnable initializer = new Runnable() {
		
		@Override
		public void run() {
			try {
				// refresh(errorFile,messageFile);
				
				ProcessBuilder pb = new ProcessBuilder(cmd);
				pb.directory(baseDir);
//				pb.environment().putAll(System.getenv());
				
//				Map<String, String> environment = pb.environment();
				
//				for(Entry<String, String> e:environment.entrySet()){
//					Console.log(e.getKey()+"="+e.getValue());
//				}
				
//				   env.putAll(environment);
				
//				Console.log("running command at directory "+baseDir.getAbsolutePath());
				// pb.redirectErrorStream(true);
				// pb.redirectError(Redirect.appendTo(errorFile));
				// pb.redirectOutput(Redirect.appendTo(messageFile));
				//
				//
				// WatchService watcher =
				// FileSystems.getDefault().newWatchService();
				// Path errorpath =
				// FileSystems.getDefault().getPath(errorFile.getParent(),
				// errorFile.getName());
				// Path messagepath =
				// FileSystems.getDefault().getPath(messageFile.getParent(),
				// messageFile.getName());
				//
				// WatchKey errorWatchKey = errorpath.register(watcher,
				// StandardWatchEventKinds.ENTRY_MODIFY);
				// WatchKey messageWatchKey = messagepath.register(watcher,
				// StandardWatchEventKinds.ENTRY_MODIFY);

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
				
				sbtProcess.waitFor();
				
			} catch (Exception e) {
				handleException(e);
//				sbtProcess = null;
			}finally{
//				service.shutdownNow();
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
//		try {
			errorWriter.write(sw.toString());
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
	}
	
	private final Map<InputStream,Long> readMap = new HashMap<>(2);
	
	private void pipe(InputStream in,Writer out) throws IOException {
		long skip = readMap.remove(in).longValue();
		final long startSkip = skip;
//		Console.log("pipe skip "+skip);
		if(skip>0){
			in.skip(skip);
		}
		final StringBuffer content = new StringBuffer();
		final int available = in.available();
//		Console.log("read available "+available+" skip "+skip);
		if(available > 0){
			while( true ){
				int read = in.read();
//				Console.log("read int "+read+" skip "+skip);
				if(read==-1||skip==startSkip+available-1)  break;	

				skip++;
				content.append((char)read);
//				Console.log("read content "+content.toString());
//				out.write(read);
			}
		}
		if(content.length()>0){
//			Console.log("write "+content.toString());
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
	
	public static void main(String[] args) throws InterruptedException{
		final Writer errorOut = new Writer() {
			
			@Override
			public void write(String b) {
				System.err.append(b);
			}
		};
		final Writer messageOut = new Writer() {
			
			@Override
			public void write(String b){
				System.out.append(b);
			}
		};
		String separator = File.separator;
		
		String [] cmd = new String[]{
			AbstractAction.getJavaHome() + separator + "bin" +  separator + "java.exe",
			"-Xmx512m", 
			"-XX:MaxPermSize=256m",
			"-XX:ReservedCodeCacheSize=128m",
			"-Dsbt.log.format=true",
			"-cp",
			""+AbstractAction.getSbtHome() + File.separator + "bin" + File.separator + "sbt-launch.jar;"+System.getProperty("java.class.path"),
			"xsbt.boot.Boot"
		};
		
//		String cmd = AbstractAction.getJavaHome() + separator + "bin" +  separator + "java.exe  -Xmx512M -XX:MaxPermSize=256m -XX:ReservedCodeCacheSize=128m -Dsbt.log.format=true  -jar "+AbstractAction.getSbtHome() + separator + "bin" + separator + "sbt-launch.jar";
		final CommandRunner c = new CommandRunner(cmd,".", errorOut, messageOut);
		c.start();
		new CountDownLatch(1).await();
	}
	
}
