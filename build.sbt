import Dependencies._

scalaVersion := "2.12.4"

lazy val commonSettings = Seq(
  organization := "my.seedless",
  version := "0.1",
  parallelExecution := false
)

lazy val assemblySettings = Seq(
  test in assembly := {},
  aggregate in assembly := false
)

lazy val common = (project in file("common"))
  .settings(
    inThisBuild(
      commonSettings
    ),
    assemblyJarName in assembly := "common.jar",
    assemblySettings,
    name := "common",
    libraryDependencies ++= commonLibs
  )

lazy val register = (project in file("register"))
  .settings(
    inThisBuild(
      commonSettings
    ),
    assemblyJarName in assembly := "register.jar",
    assemblySettings,
    name := "register",
    libraryDependencies ++= commonLibs ++ Seq(
      "org.mapdb" % "mapdb" % "3.0.5"
    )
  )
  .dependsOn(common)

lazy val root = (project in file("."))
  .settings(
    inThisBuild(
      commonSettings
    ),
    assemblyJarName in assembly := "seedless.jar",
    assemblySettings,
    name := "seedless",
    libraryDependencies ++= commonLibs
  )
  .dependsOn(common)


lazy val allProjects=Seq(
  common, register, root
).map(_.id)

addCommandAlias("cleanAll", allProjects.map(name => s"; $name/clean").mkString)

addCommandAlias("updateAll", allProjects.map(name => s"; $name/update").mkString)

addCommandAlias("compileAll", allProjects.map(name => s"; $name/compile").mkString)

addCommandAlias("testCompileAll", allProjects.map(name => s"; $name/test:compile").mkString)

addCommandAlias("testAll", allProjects.map(name => s"; $name/test").mkString)

addCommandAlias("assemblyAll", allProjects.map(name => s"; $name/assembly").mkString)

addCommandAlias("downloadSourcesAll", allProjects.map(name => s"; $name/updateClassifiers").mkString)
