package my.seedless

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.ClusterEvent.ClusterDomainEvent
import akka.cluster.{Cluster, ClusterEvent}
import akka.discovery.SimpleServiceDiscovery.Resolved
import akka.http.scaladsl.Http
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.stream.ActorMaterializer

import scala.util.Success

object Main extends App {
  println("startup ...")

  val nodeInfo = NodeInfo("managementTest")
  implicit val system = nodeInfo.system
  implicit val mat = ActorMaterializer()
  implicit val cluster = Cluster(nodeInfo.system)

  nodeInfo.httpClusterManagement.start()
  ClusterBootstrap(nodeInfo.system).start()

  cluster.subscribe(nodeInfo.system.actorOf(Props[ClusterWatcher]), ClusterEvent.InitialStateAsEvents, classOf[ClusterDomainEvent])

  import akka.http.scaladsl.server.Directives._
  Http().bindAndHandle(complete("Hello world"), "0.0.0.0", 8080)

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



class ClusterWatcher extends Actor with ActorLogging {
  implicit val cluster = Cluster(context.system)

  override def receive = {
    case msg ⇒ log.info("Cluster {} >>> {}", msg, cluster.selfAddress)
  }
}