import scala.io.Source

object Main extends App {
  /*
  Console.println("Hello World!")
  
  val c1 = new Complex( 3 , 4 )
  
  println( "c1 "+ c1 )

  val c2 = new Complex( 3.0 , 4.0 )
 
  println( "c2 "+ c2 )

  val c3 = c1/c2
 
  println( "c3 "+ c3 )

  val c4 = new Complex( 3 , -4 )
 
  println( "c4 "+ c4 )

  val c5 = c2*c4
 
  println( "c5 "+ c5 )
  */
  
  val fname = "D:\\ws\\sample1\\words.txt"
    
 val word = List( Source.fromFile(fname).getLines().map(f => f.trim()).filter(s=>{
   s.length() > 3  && s.indexOf("-") == -1 &&
    s.indexOf("'") == -1 && s.indexOf(".") == -1 &&
    s.indexOf("1") == -1 && s.indexOf("2") == -1 &&
    s.indexOf("3") == -1 && s.indexOf("4") == -1 &&
    s.indexOf("5") == -1 && s.indexOf("6") == -1 &&
    s.indexOf("7") == -1 && s.indexOf("8") == -1 &&
    s.indexOf("9") == -1 && s.indexOf("0") == -1 &&
    s.indexOf("/") == -1 && s.indexOf("&") == -1 
 }) toList :_*)
 
  println(" Dictionary Length : " + word.length)
  val coder = new Coder( word )
  println(" Enter number : " )
  val encodeNumber = readLine()
  println(" Coder : "+coder.encode(encodeNumber))
  // 617 6205
  
}

