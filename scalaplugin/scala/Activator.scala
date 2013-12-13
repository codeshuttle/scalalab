package scalaplugin

import org.eclipse.jface.resource.ImageDescriptor
import org.eclipse.ui.plugin.AbstractUIPlugin
import org.osgi.framework.BundleContext

//import scalaplugin.actions.AbstractAction

/**
 * The activator class controls the plug-in life cycle
 */
class Activator extends AbstractUIPlugin {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	override def start(context:BundleContext):Unit = {
		Console log("started "+context)
		super.start(context)
		Activator.setActivator(this)
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	override def stop( context:BundleContext):Unit = {
		Console.log("stopped "+context)
		Activator.setActivator(null)
		/*
		for(AbstractAction a:AbstractAction.actions){
			try {
				a.dispose();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		*/
		super.stop(context)
	}


}

object Activator{
	// The plug-in ID
	val PLUGIN_ID:String = "scalaplugin"; //$NON-NLS-1$

	// The shared instance
	var plugin :Activator = null;
	
	def setActivator(p:Activator):Unit={
		plugin = p;
	}
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	 def getDefault() :Activator = {
		plugin
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	def getImageDescriptor( path:String):ImageDescriptor= {
		AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path)
	}
}

object Console {

	def log( message:String):Unit={
		System.out.println(" NewPlugin - "+message)
	}

	def error( message:String):Unit= {
		System.err.println(" NewPlugin - "+message);
	}

	def error( e:Throwable):Unit= {
		e.printStackTrace(System.err);
	}
	
}