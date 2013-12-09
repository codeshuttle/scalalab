package scalaplugin;

public class Console {

	public static void log(String message){
		System.out.println(" NewPlugin - "+message);
	}

	public static void error(String message) {
		System.err.println(" NewPlugin - "+message);
	}

	public static void error(Throwable e) {
		e.printStackTrace(System.err);
	}
	
}
