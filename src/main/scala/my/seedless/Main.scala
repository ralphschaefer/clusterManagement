package my.seedless

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.ClusterEvent.ClusterDomainEvent
import akka.cluster.{Cluster, ClusterEvent}
import akka.util.Timeout
import my.seedless.api.entities.ClusterNode
import my.seedless.api.webserver.WriteResult

import scala.concurrent.Await
import scala.concurrent.duration._

object Main extends App {
  println("startup ...")

  val nodeInfo = NodeInfo("managementTest")
  implicit val ec = nodeInfo.system.dispatcher
  implicit val timeout = Timeout(5 seconds)
  NodeRegistry.init(nodeInfo)

  println("register self")
  val reg:WriteResult = Await.result(
    NodeRegistry.register(
      ClusterNode(
        host = nodeInfo.config.getString("akka.management.http.hostname"),
        port = nodeInfo.config.getInt("akka.management.http.port")
      )
    ),
    timeout.duration
  ).asInstanceOf[WriteResult]
  println(reg)
  println("-----------------")

  nodeInfo.startClusterBootstrap()

  nodeInfo.cluster.subscribe(nodeInfo.system.actorOf(Props[ClusterWatcher]), ClusterEvent.InitialStateAsEvents, classOf[ClusterDomainEvent])

  println("running ...")
  println("ANY Key?")
  scala.io.StdIn.readLine()

  println("deregister self")
  Await.result(
    NodeRegistry.delete(reg.id),
    timeout.duration
  )

  nodeInfo.shutdown()

  println("stopped ...")

}


class ClusterWatcher extends Actor with ActorLogging {
  implicit val cluster = Cluster(context.system)

  override def receive = {
    case msg =>
      println(s"Cluster $msg -> ${cluster.selfAddress}")

  }
}