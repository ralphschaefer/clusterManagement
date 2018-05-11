package my.seedless

import akka.actor.{ActorRef, ActorSystem, CoordinatedShutdown}
import akka.cluster.Cluster
import akka.management.AkkaManagement
import akka.management.cluster.bootstrap.ClusterBootstrap
import com.typesafe.config.Config


trait NodeInfo {
  val system: ActorSystem
  val config: Config
  val httpClusterManagement: AkkaManagement
  val clusterBootstrap: ClusterBootstrap
  val cluster: Cluster
  val clusterNodes: ActorRef

  def shutdown():Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    httpClusterManagement.stop().onComplete(
       _ => println("cluster Management stopped")
    )
    CoordinatedShutdown.get(system).run(CoordinatedShutdown.unknownReason)
  }

}

object NodeInfo {
  def apply(actorSystemName: String) = new NodeInfo {
    val system: ActorSystem = ActorSystem(actorSystemName)
    lazy val config: Config = system.settings.config
    lazy val httpClusterManagement = AkkaManagement(system)
    lazy val clusterBootstrap = ClusterBootstrap(system)
    lazy val cluster: Cluster = Cluster(system)
    val clusterNodes: ActorRef = system.actorOf(httpclient.Register.props("clusternodes"), "clusternodes")

    def startClusterBootstrap() =
    {
      httpClusterManagement.start()
      clusterBootstrap.start()
    }
  }
}