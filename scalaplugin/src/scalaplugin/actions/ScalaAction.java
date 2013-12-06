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
			return getScalaHome() + File.separator + "bin" + File.separator + "scala.bat";
		}else{
			return getScalaHome() + File.separator + "bin" + File.separator + "scala.sh";
		}
	}
	
}
