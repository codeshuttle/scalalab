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
	String[] getAction(){
//		return new String[]{
//				AbstractAction.getJavaHome() + File.separator + "bin" +  File.separator + "java.exe",
//				"-Xmx512m", 
//				"-XX:MaxPermSize=256m",
//				"-XX:ReservedCodeCacheSize=128m",
//				"-Dsbt.log.format=true",
////				"-Djline.terminal=jline.UnsupportedTerminal",
//				
////				"-Dcom.sun.management.jmxremote",
////				"-Dcom.sun.management.jmxremote.port="+portNum,
////				"-Dcom.sun.management.jmxremote.local.only=false",
////				"-Dcom.sun.management.jmxremote.authenticate=false",
////				"-Dcom.sun.management.jmxremote.ssl=false",
//				
//				"-cp",
//				""+AbstractAction.getSbtHome() + File.separator + "bin" + File.separator + "sbt-launch.jar"+File.pathSeparatorChar+System.getProperty("java.class.path")+File.pathSeparatorChar+"D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui_3.105.0.v20130522-1122.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.swt_3.102.1.v20130827-2021.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.swt.win32.win32.x86_64_3.102.1.v20130827-2048.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.jface_3.9.1.v20130725-1141.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.commands_3.6.100.v20130515-1857.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.workbench_3.105.1.v20130821-1411.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.e4.ui.workbench3_0.12.0.v20130515-1857.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.runtime_3.9.0.v20130326-1255.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.osgi_3.9.1.v20130814-1242.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.common_3.6.200.v20130402-1505.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.jobs_3.5.300.v20130429-1813.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.runtime.compatibility.registry_3.5.200.v20130514-1256\\runtime_registry_compatibility.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.registry_3.5.301.v20130717-1549.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.preferences_3.5.100.v20130422-1538.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.contenttype_3.4.200.v20130326-1255.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.app_1.3.100.v20130327-1442.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.jface.text_3.8.101.v20130802-1147.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.text_3.5.300.v20130515-1451.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.resources_3.8.101.v20130717-0806.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.editors_3.8.100.v20130513-1637.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.filebuffers_3.5.300.v20130225-1821.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.ide_3.9.1.v20130704-1828.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.views_3.6.100.v20130326-1250.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.workbench.texteditor_3.8.101.v20130729-1318.jar;C:\\Users\\Parthi\\Documents\\GitHub\\scalalab\\scalaplugin\\bin",
//				"xsbt.boot.Boot"
////				"scalaplugin.actions.CommandExecutorImpl",
////				"sbt"
//			};
		
		return new String[]{
				AbstractAction.getJavaHome() + File.separator + "bin" +  File.separator + "java.exe",
				"-Xmx512m", 
				"-XX:MaxPermSize=256m",
				"-XX:ReservedCodeCacheSize=128m",
				"-Dsbt.log.format=true",

				"-Dcom.sun.management.jmxremote",
				"-Dcom.sun.management.jmxremote.port="+getPortNum(),
				"-Dcom.sun.management.jmxremote.local.only=false",
				"-Dcom.sun.management.jmxremote.authenticate=false",
				"-Dcom.sun.management.jmxremote.ssl=false",
				
				"-cp",
				""+AbstractAction.getSbtHome() + File.separator + "bin" + File.separator + "sbt-launch.jar"+File.pathSeparatorChar+System.getProperty("java.class.path")+File.pathSeparatorChar+"D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui_3.105.0.v20130522-1122.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.swt_3.102.1.v20130827-2021.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.swt.win32.win32.x86_64_3.102.1.v20130827-2048.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.jface_3.9.1.v20130725-1141.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.commands_3.6.100.v20130515-1857.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.workbench_3.105.1.v20130821-1411.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.e4.ui.workbench3_0.12.0.v20130515-1857.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.runtime_3.9.0.v20130326-1255.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.osgi_3.9.1.v20130814-1242.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.common_3.6.200.v20130402-1505.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.jobs_3.5.300.v20130429-1813.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.runtime.compatibility.registry_3.5.200.v20130514-1256\\runtime_registry_compatibility.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.registry_3.5.301.v20130717-1549.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.preferences_3.5.100.v20130422-1538.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.contenttype_3.4.200.v20130326-1255.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.equinox.app_1.3.100.v20130327-1442.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.jface.text_3.8.101.v20130802-1147.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.text_3.5.300.v20130515-1451.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.resources_3.8.101.v20130717-0806.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.editors_3.8.100.v20130513-1637.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.filebuffers_3.5.300.v20130225-1821.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.ide_3.9.1.v20130704-1828.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.views_3.6.100.v20130326-1250.jar;D:\\soft\\eclipse-standard-kepler-SR1-win32-x86_64\\eclipse\\plugins\\org.eclipse.ui.workbench.texteditor_3.8.101.v20130729-1318.jar;C:\\Users\\Parthi\\Documents\\GitHub\\scalalab\\scalaplugin\\bin",
				
				"scalaplugin.actions.CommandExecutorImpl",
				"sbt"
			};
	}

	@Override
	int getPortNum() {
		return 8808;
	}

}
