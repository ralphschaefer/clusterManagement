package my.seedless

import akka.actor.{ActorRef, ActorSystem, CoordinatedShutdown}
import akka.management.AkkaManagement
import com.typesafe.config.Config

trait NodeInfo {
  val system: ActorSystem
  val config: Config
  val httpClusterManagement: AkkaManagement
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
  }
}