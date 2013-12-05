package scalaplugin.actions;

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

public class SbtCommand {

	private final OutputStream errorStream;
	private final OutputStream inputStream;
	
	private ScheduledFuture<?> scheduleAtFixedRate = null;
	private final ExecutorService service = Executors.newSingleThreadExecutor();
	
	private ScheduledExecutorService thread = null;
	private Process sbtProcess = null;
	
	public SbtCommand(OutputStream errorStream, OutputStream inputStream) {
		super();
		this.errorStream = errorStream;
		this.inputStream = inputStream;
	}
	
	public OutputStream getOutputStream() {
		return sbtProcess.getOutputStream();
	}

	private final Runnable initializer = new Runnable() {
		
		@Override
		public void run() {
			try {
				// refresh(errorFile,messageFile);

				ProcessBuilder pb = new ProcessBuilder("D:\\soft\\sbt\\bin\\sbt.bat");

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
								try {
//									System.out.println("Write response from Thread!");
									pipe(sbtProcess.getErrorStream(),errorStream);
									pipe(sbtProcess.getInputStream(),inputStream);
								} catch (IOException e) {
									handleException(e);
								}
							}
						}, 5, 5, TimeUnit.SECONDS);

			} catch (IOException e) {
				handleException(e);
				sbtProcess = null;
			}finally{
				service.shutdownNow();
			}
		}
	};
	
	
	private boolean started = false;
	public void start(){
		started = true;
		service.submit(initializer);
	}
	
	public boolean isStarted(){
		return started;
	}

	public void handleException(IOException e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		try {
			errorStream.write(sw.toString().getBytes());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private final Map<InputStream,Long> readMap = new HashMap<>(2);
	
	private void pipe(InputStream in,OutputStream out) throws IOException {
		long skip = readMap.remove(in).longValue();
		//System.out.println("pipe skip "+skip);
		if(skip>0){
			in.skip(skip);
		}
		
		if(in.available() > 0){
			while( true ){
				int read = in.read();
				//System.out.println("read "+read);
				if(read==-1) break;
				
				skip =+ 1;
				out.write(read);
			}
		}
		readMap.put(in, skip);
	}
	
	public void stop(){
		scheduleAtFixedRate.cancel(true);
		thread.shutdownNow();
	}
	
	public static void main(String[] args) throws InterruptedException{
		final OutputStream errorOut = new OutputStream() {
			
			@Override
			public void write(int b) throws IOException {
				System.err.append((char)b);
			}
		};
		final OutputStream messageOut = new OutputStream() {
			
			@Override
			public void write(int b) throws IOException {
				System.out.append((char)b);
			}
		};
		final SbtCommand c = new SbtCommand(errorOut, messageOut);
		c.start();
		new CountDownLatch(1).await();
	}
	
}
