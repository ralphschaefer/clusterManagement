package my.seedless.simplestore.webserver

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.json4s._
import org.json4s.jackson.JsonMethods._
import my.seedless.api._
import my.seedless.api.webserver.ApiMessage
import my.seedless.simplestore.db.Database

class WebserverRouteProducer[A <: Payload](dbName:String)(implicit m: Manifest[A]) {

  import ApiMessage._

  implicit val formats = org.json4s.DefaultFormats

  import my.seedless.api.webserver._

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
        println(s"QUERY")
        complete(all())
      } ~
      post {
        entity(as[WriteRequest]) { item =>
          println(s"CREATE: $item")
          complete(
            write(item.item.extract[A])
          )
        }
      }
    } ~
    path(Segment) { id =>
      get {
        println(s"GET: $id")
        complete(read(id))
      } ~
      delete {
        println(s"DELETE: $id")
        complete(remove(id))
      }
    }
}