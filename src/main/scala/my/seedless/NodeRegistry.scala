package my.seedless

import akka.pattern.ask
import akka.util.Timeout
import my.seedless.api.entities.ClusterNode

import scala.concurrent.{ExecutionContext, Future}

class NodeRegistry(nodeInfo: NodeInfo)(implicit val ec: ExecutionContext, implicit val timeout:Timeout) {
  def register(c: ClusterNode):Future[Any] = nodeInfo.clusterNodes ? httpclient.Register.Create(c)
  def list():Future[Any] = nodeInfo.clusterNodes ? httpclient.Register.List
  def delete(id:String):Future[Any] = nodeInfo.clusterNodes ? httpclient.Register.Delete(id)
}


// make NodeRegistry global for it can be used in MyServiceDiscovery
object NodeRegistry {

  private var nodeRegistry:Option[NodeRegistry] = None

  private var executionContext:Option[ExecutionContext] = None

  def init(nodeInfo: NodeInfo)(implicit ec: ExecutionContext = nodeInfo.system.dispatcher, timeout: Timeout) = {
    this.nodeRegistry = Some(new NodeRegistry(nodeInfo))
    this.executionContext = Some(ec)
  }

  def getEc = executionContext.getOrElse(throw NoRegistry)

  def register(c: ClusterNode) = nodeRegistry.getOrElse(throw NoRegistry).register(c)

  def list() = nodeRegistry.getOrElse(throw NoRegistry).list()

  def delete(id: String) = nodeRegistry.getOrElse(throw NoRegistry).delete(id)

  case object NoRegistry extends Exception("No Registry found")

}
