/**
 * 
 */
package scalaplugin.actions;

/**
 * @author parthipanp
 *
 */
public class ScalaAction extends AbstractAction {

	/**
	 * 
	 */
	public ScalaAction() {
		super();
	}

	@Override
	String getAction() {
		return CommandRunner.SCALA_BAT;
	}
	
}
