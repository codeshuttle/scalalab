## Build file for Scala custom Repl.
scalac -cp "%SCALA_HOME%\lib;.\\..\\bin;" *.scala

jar -cvf scalaplugin_repl.jar .
