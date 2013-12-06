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

	@Override
	String getAction(){
		if(isWindows()){
			return getSbtHome() + File.pathSeparator + "sbt.bat";
		}else{
			return getSbtHome() + File.pathSeparator + "sbt.sh";
		}
	}

}
