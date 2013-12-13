package scalaplugin.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.management.MXBean;
import javax.management.ObjectName;

@MXBean
public class CommandExecutorImpl implements CommandExecutorMXBean{

	private static final String XSBT_BOOT = "xsbt.boot.Boot";

	private static final String SCALA_RUNNER = "scalaplugin.repl.IScala";

	public static final String NAME_COMMAND_EXECUTOR = "org.scalaplugin:type=CommandExecutor";

	private static final CommandExecutorMXBean exe = new CommandExecutorImpl();
	
	private static final ExecutorService exequeue = Executors.newFixedThreadPool(1);

	private static volatile String execommand = null;

	private static CountDownLatch waitLatch = null;
	
//	private static volatile int cursor = 0; 	
//	private static final InputStream in =  new InputStream() {
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
	
	public static final BufferedReader reader = new BufferedReader(new Reader() {
		
		@Override
		public int read(char[] cbuf, int off, int len) throws IOException {
			return 0;
		}
		
		@Override
		public void close() throws IOException {
		}
	}){

		@Override
		public String readLine() throws IOException {
			waitAndWatch();
			String c = execommand;
			execommand = null;
//			System.out.println("Command "+c);
			return c;
		}
		
	};
	
	
	/* (non-Javadoc)
	 * @see scalaplugin.actions.CommandExecutor#execute(java.lang.String)
	 */
	@Override
	public void execute(final String command){
		exequeue.submit(new Runnable() {
			@Override
			public void run() {
//				if("scala".equalsIgnoreCase(type)){
					execommand = command;
					signalWatch();
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
	
	static void signalWatch() {
		if(waitLatch!=null){
			waitLatch.countDown();
			waitLatch = null;
		}
	}

	public CommandExecutorImpl() {
	}

	public static void main(String[] args) throws Throwable{
//		
//		System.out.println("Started CommandExecutorImpl! Console readLine "+new BufferedReader (new InputStreamReader(System.in)).readLine());
		ManagementFactory.getPlatformMBeanServer().registerMBean(exe, new ObjectName(NAME_COMMAND_EXECUTOR));
		type = args[0];
		if("scala".equalsIgnoreCase(type)){
			invokeMain(SCALA_RUNNER,new String[]{});
		}else{
//			scalaConsole();
//			System.setIn(in);
			invokeMain(XSBT_BOOT,new String[]{});
		}
	}
	private static String type = null;
	private static Class<?> loadClass = null;
	
	private static void invokeMain(final String clazzname,final String[] params) throws ClassNotFoundException,
			NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		if(loadClass==null){
			loadClass = CommandExecutorImpl.class.getClassLoader().loadClass(clazzname);
		}
		if(loadClass!=null){
			Method main = loadClass.getMethod("main", String[].class);
			main.invoke(null, (Object)params);
		}else{
			System.err.println("Unable to invoke sbt Boot class.");
		}
	}

	static void waitAndWatch() {
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
