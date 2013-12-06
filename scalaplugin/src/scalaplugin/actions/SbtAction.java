/**
 * 
 */
package scalaplugin.actions;


/**
 * @author parthipanp
 * 
 */
public class SbtAction extends AbstractAction {

	/**
	 * 
	 */
	public SbtAction() {
		super();
	}

	@Override
	String getAction(){
		return CommandRunner.SBT_BAT;
	}

}
