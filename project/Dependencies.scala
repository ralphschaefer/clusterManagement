import sbt._

object Dependencies {
  lazy val akkaVersion = "2.5.11"
  lazy val commonLibs = Seq (
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe" % "config" % "1.3.2",
    "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
    "com.typesafe.akka" %% "akka-http"   % "10.1.0",
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % "3.0.1",
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.github.jknack" % "handlebars" % "4.0.6",
    "com.github.jknack" % "handlebars-jackson2" % "4.0.6",
    "org.json4s" %% "json4s-jackson" % "3.5.3",
    "com.twitter" %% "chill-akka" % "0.9.2",
    "com.lightbend.akka.management" %% "akka-management" % "0.12.0",
    "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % "0.12.0",
    "com.lightbend.akka.discovery" %% "akka-discovery-dns" % "0.12.0",
    "com.lightbend.akka.management" %% "akka-management-cluster-http" % "0.12.0"
  )
}
