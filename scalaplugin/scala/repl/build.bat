## Build file for Scala custom Repl.
scalac -cp "%SCALA_HOME%\lib;C:\\Users\\Parthi\\Documents\\GitHub\\scalalab\\scalaplugin\\bin;" Repl.scala

jar -cvf scalaplugin_repl.jar .
