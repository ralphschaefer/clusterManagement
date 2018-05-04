package my.seedless

import akka.actor.{ActorRef, ActorSystem, CoordinatedShutdown}
import akka.discovery.SimpleServiceDiscovery.Resolved
import akka.discovery.{ServiceDiscovery, SimpleServiceDiscovery}
import akka.management.AkkaManagement
import com.typesafe.config.Config
import scala.concurrent.duration._

import scala.concurrent.Future

trait NodeInfo {
  val system: ActorSystem
  val config: Config
  val httpClusterManagement: AkkaManagement
  val discovery: SimpleServiceDiscovery
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
    lazy val discovery = ServiceDiscovery(system).discovery
    lazy val servicelookup: Future[Resolved] = discovery.lookup("nodes.mycluser.local", resolveTimeout = 500 milliseconds)
  }
}