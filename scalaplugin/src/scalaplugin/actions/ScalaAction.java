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

	@Override
	String getAction() {
		if(isWindows()){
			return getScalaHome() + File.pathSeparator + "scala.bat";
		}else{
			return getScalaHome() + File.pathSeparator + "scala.sh";
		}
	}
	
}
