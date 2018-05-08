package my.seedless

import akka.actor.ActorSystem
import akka.discovery.SimpleServiceDiscovery.ResolvedTarget

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.Future
import akka.discovery.{SimpleServiceDiscovery, dns}
import scala.concurrent.ExecutionContext.Implicits.global

class MyServiceDiscovery extends SimpleServiceDiscovery {

  override def lookup(name: String, resolveTimeout: FiniteDuration): Future[SimpleServiceDiscovery.Resolved] = {
    if (name == "nodes.mycluster.local") {
      Future{
        SimpleServiceDiscovery.Resolved(name,collection.immutable.Seq(
          ResolvedTarget("127.0.0.12",None),
          ResolvedTarget("127.0.0.14",None)
        ))
      }
    }
    else
      Future{
        SimpleServiceDiscovery.Resolved(name,collection.immutable.Seq())
      }
  }

  println("init MyDns")
}
