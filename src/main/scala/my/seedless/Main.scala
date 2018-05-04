package my.seedless

object Main extends App {
  println("startup ...")

  val nodeInfo = NodeInfo("mangementTest")

  println("running ...")
  println("ANY Key?")
  scala.io.StdIn.readLine()

  nodeInfo.shutdown()

  println("stopped ...")

}