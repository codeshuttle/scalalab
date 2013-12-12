## Build file for Scala custom Repl.
scalac -cp "%SCALA_HOME%\lib;.\\..\\..\\..\\scalaplugin\\bin;" Repl.scala

jar -cvf scalaplugin_repl.jar .
