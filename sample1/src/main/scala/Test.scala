import scala.io.Source
import java.util.concurrent.TimeUnit

object Test extends App {

  val fname = "/usr/share/dict/words"
  
  val start = System.currentTimeMillis();
//  val counters = Source.fromFile(fname).getLines().
//  						grouped(3).map(_(2).split("\\s+")(0))

  val counters = ((Source fromFile fname).getLines grouped 3) map { _.last takeWhile (!_.isWhitespace) }
//  System.out.println(" Time taken to process : "+(System.currentTimeMillis()-start));
//  val content = new StringBuffer()
//  counters.foreach(c => content.append(c).append("\n"))
  //counters.reduce(_ + "\n" + _)
  
//  System.out.println(" Time taken to process+reduce : "+(System.currentTimeMillis()-start));
//  println( "counters \n"+ counters.reduceLeft(_ + "\n" + _) )
//  scala.io.Source.fromFile("output1.txt").write( content )(Codec.UTF*)
  val f = new java.io.PrintWriter(new java.io.File("output1.txt"))
  counters.foreach(c => f.println(c))
//  f.write(content.toString())
//  f.write(counters.reduceLeft(_ + "\n" + _))
//  f.write(counters.head.toString)
//  counters.tail.foreach(c => f.write("\n"+c))
  f.close()				
  
  System.out.println(" Time taken process+reduce+write : "+TimeUnit.SECONDS.convert((System.currentTimeMillis()-start), TimeUnit.MILLISECONDS)+" secs.");
  
}