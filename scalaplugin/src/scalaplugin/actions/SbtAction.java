/**
 * 
 */
package scalaplugin.actions;

import java.io.File;


/**
 * @author parthipanp
 * 
 */
public class SbtAction extends AbstractAction {

	/**
	 * 
	 */
	public SbtAction() {
		super("Simple Build Tool");
	}

	final String [] cmd = new String[]{
			AbstractAction.getJavaHome() + File.separator + "bin" +  File.separator + "java.exe",
			"-Xmx512m", 
			"-XX:MaxPermSize=256m",
			"-XX:ReservedCodeCacheSize=128m",
			"-Dsbt.log.format=true",
			"-cp",
			""+AbstractAction.getSbtHome() + File.separator + "bin" + File.separator + "sbt-launch.jar"+File.pathSeparatorChar+System.getProperty("java.class.path"),
			"xsbt.boot.Boot"
		};
	
	@Override
	String[] getAction(){
		return cmd;
	}

}
