package my.simplestore

import db._

object Main extends App {
  println("simple store")

  val akkaArtifacts=AkkaArtifacts()
  val webserver = new Webserver(akkaArtifacts)

  println("ANY Key?")
  scala.io.StdIn.readLine()


  webserver.unbind()
  db.Database.cleanAll

}
