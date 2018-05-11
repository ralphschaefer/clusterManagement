package my.seedless.httpclient

import akka.actor.{Actor, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString
import my.seedless.api.entities.ClusterNode
import my.seedless.api.webserver
import my.seedless.api.webserver.ApiMessage
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.concurrent.Future

class Register(collection:String) extends Actor {

  import akka.pattern.pipe
  import context.dispatcher

  implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))
  implicit val formats = org.json4s.DefaultFormats

  val http = Http(context.system)

  val baseUri = context.system.settings.config.getString("settings.registration")

  val uri = baseUri + "/" + collection

  private def convert[A<:ApiMessage](entity:ResponseEntity)(implicit m: Manifest[A]):Future[ApiMessage] =
    entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(bytes =>
      parse(bytes.utf8String).extract[A]
    )

  class ConvertActor(cnv: ResponseEntity => Future[ApiMessage]) extends Actor {
    def receive = {
      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        cnv(entity).foreach{msg =>
          println(msg)
          sender ! msg
        }
        context.stop(self)
      case HttpResponse(code,_,_,_) =>
        println(s"ERROR from server : $code")
        context.stop(self)
    }
  }


  def receive = {
    case Register.List =>
      println("LIST :"+uri)
      http.singleRequest(
       HttpRequest(
         method = HttpMethods.GET,
         uri = uri,
         entity = HttpEntity.empty(ContentTypes.`application/json`)
       )
      ).pipeTo(
        context.system.actorOf(
          Props(new ConvertActor(convert[webserver.All]))
        )
      )
    case Register.Create(node) =>
      http.singleRequest(
       HttpRequest(
         method = HttpMethods.POST,
         uri = uri,
         entity = HttpEntity(
           ContentTypes.`application/json`,
           compact(render(Extraction.decompose(
             webserver.WriteRequest(node.toJson)
           )))
         )
       )
      ).pipeTo(
        context.system.actorOf(
          Props(new ConvertActor(convert[webserver.WriteResult]))
        )
      )
    case Register.Query(id) =>
      http.singleRequest(
       HttpRequest(
         method = HttpMethods.GET,
         uri = uri+"/"+id,
         entity = HttpEntity.empty(ContentTypes.`application/json`)
       )
      ).pipeTo(
        context.system.actorOf(
          Props(new ConvertActor(convert[webserver.Item]))
        )
      )
    case Register.Delete(id) =>
      http.singleRequest(
       HttpRequest(
         method = HttpMethods.DELETE,
         uri = uri+"/"+id,
         entity = HttpEntity.empty(ContentTypes.`application/json`)
       )
      ).pipeTo(
        context.system.actorOf(
          Props(new ConvertActor(convert[webserver.DeleteResult]))
        )
      )
    case m:ApiMessage =>
      sender ! m
    case anyMessage =>
      println(anyMessage)
  }
}


object Register {
  case object List
  case class Create(node:ClusterNode)
  case class Query(id:String)
  case class Delete(id:String)

  def props(collection:String) = Props(new Register(collection))
}