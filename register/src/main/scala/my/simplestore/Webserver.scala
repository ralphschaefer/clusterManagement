package my.simplestore

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.json4s._
import db._

class Webserver(akkaArtifacts: AkkaArtifacts)
{
  private implicit val system = akkaArtifacts.system
  private implicit val materializer = akkaArtifacts.materializer
  private implicit val executionContext = system.dispatcher

  implicit val formats = org.json4s.DefaultFormats

  val route:Route =
    pathEndOrSingleSlash {
      complete("registration Servive")
    } ~
    pathPrefix("demo") {
      new WebserverProducer[Demo]("demo").route
    } ~
    pathPrefix("clusternodes") {
      new WebserverProducer[ClusterNode]("clusterRegistration").route
    }

  private val bindingFuture = Http().bindAndHandle(route,akkaArtifacts.httpServerBind, akkaArtifacts.httpServerPort)

  println(s"*** Start webserver on ${akkaArtifacts.httpServerBind}:${akkaArtifacts.httpServerPort}")

  def unbind() = bindingFuture.flatMap(_.unbind())

}

object Websever {

  case class All(list:JObject) extends ApiMessage

  case class Item(result: Option[JValue]) extends ApiMessage

  case class WriteResult(id:String) extends ApiMessage

  case class DeleteResult(success:Boolean) extends ApiMessage

  case class WriteRequest(item:JObject) extends ApiMessage
}

class WebserverProducer[A <: Payload](dbName:String)(implicit m: Manifest[A]) {

  implicit val formats = org.json4s.DefaultFormats

  import Websever._

  val db = Database(dbName)

  protected def all():All = All(
    JObject(
        (for ((k,v) <- db.all[Entity[A]]()) yield JField(k,v.payload.toJson)).toList
    )
  )

  protected def read(id:String):Item = Item(
    db.read[Entity[A]](id).map(_.payload.toJson)
  )

  protected def write(item:A):WriteResult = WriteResult({
    val id = item.hash
    db.write(id, item.asEntity)
    id
  })

  protected def remove(id:String):DeleteResult = DeleteResult(
    db.delete(id)
  )

  val route:Route =
    pathEndOrSingleSlash {
      get {
        complete(all())
      } ~
      post { entity(as[WriteRequest]) { item =>
        complete(write(item.item.extract[A]))
      }}
    } ~
    path(Segment) { id =>
      get {
        complete(read(id))
      }
    } ~
    path(Segment) { id =>
      delete {
        complete(remove(id))
      }
    }

}