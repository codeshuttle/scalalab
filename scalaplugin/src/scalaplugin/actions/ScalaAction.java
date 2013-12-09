/**
 * 
 */
package scalaplugin.actions;

import java.io.File;

/**
 * @author parthipanp
 *
 */
public class ScalaAction extends AbstractAction {

	/**
	 * 
	 */
	public ScalaAction() {
		super("Scala Interpreter");
	}
//	"D:\soft\Java\jdk1.7.0_45\bin\java.exe" -Xmx256M -Xms32M -Dscala.home="D:\soft\SCALA-~1.0-M\bin\.." -Denv.emacs="" -Dscala.usejavacp=true  
//	-cp "D:\soft\SCALA-~1.0-M\bin\..\lib\akka-actors.jar;D:\soft\SCALA-~1.0-M\bin\..\lib\jline.jar;D:\soft\SCALA-~1.0-M\bin\..\lib\scala-actors-migration.jar;D:\soft\SCALA-~1.0-M\bin\..\lib\scala-actors.jar;D:\soft\SCALA-~1.0-M\bin\..\lib\scala-compiler.jar;D:\soft\SCALA-~1.0-M\bin\..\lib\scala-library.jar;D:\soft\SCALA-~1.0-M\bin\..\lib\scala-parser-combinators.jar;D:\soft\SCALA-~1.0-M\bin\..\lib\scala-reflect.jar;D:\soft\SCALA-~1.0-M\bin\..\lib\scala-swing.jar;D:\soft\SCALA-~1.0-M\bin\..\lib\scala-xml.jar;D:\soft\SCALA-~1.0-M\bin\..\lib\scalap.jar;D:\soft\SCALA-~1.0-M\bin\..\lib\typesafe-config.jar" 
//	scala.tools.nsc.MainGenericRunner
	final String [] cmd = new String[]{
			AbstractAction.getJavaHome() + File.separator + "bin" +  File.separator + "java.exe",
			"-Xmx256M", 
			"-Xms32M",
			"-Dscala.home=\""+AbstractAction.getScalaHome()+"\"",
			"-Dscala.usejavacp=true",
			"-cp",
			""+AbstractAction.getScalaHome() + File.separator + "lib" + File.separator + "*" +File.pathSeparatorChar+System.getProperty("java.class.path"),
			"scala.tools.nsc.MainGenericRunner"
		};
	
	@Override
	String[] getAction() {
		return cmd;
	}
	
}
