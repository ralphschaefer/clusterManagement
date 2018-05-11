package my.seedless.simplestore

import db._
import my.seedless.simplestore.webserver.Webserver

object Main extends App {
  println("run 'simple store'")

  val akkaArtifacts=AkkaArtifacts()
  val webserver = new Webserver(akkaArtifacts)

  println("ANY Key?")
  scala.io.StdIn.readLine()

  webserver.unbind()
  akkaArtifacts.shutdown()
  Database.cleanAll()
  DatabaseEngine.close()
}
