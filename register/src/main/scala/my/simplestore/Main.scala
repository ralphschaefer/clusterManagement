package my.simplestore

import db._

object Main extends App {
  println("simple store")

  val akkaArtifacts=AkkaArtifacts()
  val webserver = new Webserver(akkaArtifacts)

  println("ANY Key?")
  scala.io.StdIn.readLine()


  webserver.unbind()
  println("unbind")
  akkaArtifacts.shutdown
  println("akka shutdoen")
  Database.cleanAll
  println("clean all collections")
  DatabaseEngine.close()
  println("shutdown db")


}
