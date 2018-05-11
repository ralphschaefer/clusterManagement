package my.seedless

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.ClusterEvent.ClusterDomainEvent
import akka.cluster.{Cluster, ClusterEvent}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._

object Main extends App {
  println("startup ...")

  val nodeInfo = NodeInfo("managementTest")

  println("running instances")
  implicit val timeout = Timeout(5 seconds)
  val res = Await.result(
    nodeInfo.clusterNodes ? httpclient.Register.List,
    timeout.duration
  )
  println(res)

  nodeInfo.startClusterBootstrap()

  nodeInfo.cluster.subscribe(nodeInfo.system.actorOf(Props[ClusterWatcher]), ClusterEvent.InitialStateAsEvents, classOf[ClusterDomainEvent])

  println("running ...")
  println("ANY Key?")
  scala.io.StdIn.readLine()

  nodeInfo.shutdown()

  println("stopped ...")

}

object sss {
  import akka.io.{ Dns, IO }

}


class ClusterWatcher extends Actor with ActorLogging {
  implicit val cluster = Cluster(context.system)

  override def receive = {
    case msg =>
      println(s"Cluster $msg -> ${cluster.selfAddress}")

  }
}