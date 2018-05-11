package my.seedless.simplestore

import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.stream.ActorMaterializer

trait AkkaArtifacts {
  val system        : ActorSystem
  val materializer  : ActorMaterializer
  val httpServerPort: Int
  val httpServerBind: String
  def shutdown():Unit = {
    CoordinatedShutdown.get(system).run(CoordinatedShutdown.unknownReason)
    println("Shutdown actor system")
  }
}

object AkkaArtifacts extends FromConfig {
  def apply() = new AkkaArtifacts {
    val system: ActorSystem = ActorSystem(settings.getString("actorSystemName"))
    val materializer: ActorMaterializer = ActorMaterializer()(system)
    val httpServerPort: Int = settings.getInt("serverPort")
    val httpServerBind: String = settings.getString("serverHost")
  }
}