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
			return getSbtHome() + File.separator + "bin" + File.separator + "sbt.bat";
		}else{
			return getSbtHome() + File.separator + "bin" + File.separator + "sbt.sh";
		}
	}

}
