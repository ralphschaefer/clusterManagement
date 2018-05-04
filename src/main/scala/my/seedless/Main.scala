package my.seedless

import akka.discovery.SimpleServiceDiscovery.Resolved

import scala.util.Success

object Main extends App {
  println("startup ...")

  val nodeInfo = NodeInfo("managementTest")

  println("running ...")


  import scala.concurrent.ExecutionContext.Implicits.global
  nodeInfo.servicelookup.onComplete{
    case Success(Resolved(serviceName,list)) =>
      println(s"$serviceName -> ${list.mkString(",")}")
    case _ =>
      println("no service found")
  }

  println("ANY Key?")
  scala.io.StdIn.readLine()

  nodeInfo.shutdown()

  println("stopped ...")

}