package my.seedless

import akka.discovery.SimpleServiceDiscovery.ResolvedTarget
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import akka.discovery.SimpleServiceDiscovery
import my.seedless.api.entities.ClusterNode
import my.seedless.api.webserver.All
import org.json4s._
import org.json4s.jackson.JsonMethods._

class MyServiceDiscovery extends SimpleServiceDiscovery {

  implicit val formats = org.json4s.DefaultFormats

  override def lookup(name: String, resolveTimeout: FiniteDuration): Future[SimpleServiceDiscovery.Resolved] = {

    implicit val ec: ExecutionContext = NodeRegistry.getEc

    // ignore name by now

    NodeRegistry.list().map { res =>
      val JObject(lst:List[JField]) = res.asInstanceOf[All].list
      val targets = lst.map { node =>
        val n = node._2.extract[ClusterNode]
        ResolvedTarget(n.host,Some(n.port))
      }
      SimpleServiceDiscovery.Resolved(name,targets)
    }
  }

}
