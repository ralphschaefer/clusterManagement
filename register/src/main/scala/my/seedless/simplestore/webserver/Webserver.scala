package my.seedless.simplestore.webserver

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import my.seedless.api._
import entities._
import my.seedless.simplestore.AkkaArtifacts

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
      print("(demo) ")
      new WebserverRouteProducer[Demo]("demo").route
    } ~
    pathPrefix("clusternodes") {
      print("(clusternodes) ")
      new WebserverRouteProducer[ClusterNode]("clusterRegistration").route
    }

  private val bindingFuture = Http().bindAndHandle(route,akkaArtifacts.httpServerBind, akkaArtifacts.httpServerPort)

  println(s"*** Start webserver on ${akkaArtifacts.httpServerBind}:${akkaArtifacts.httpServerPort}")

  def unbind() = bindingFuture.flatMap(_.unbind())

}
