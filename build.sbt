import com.typesafe.sbt.packager.docker.Cmd

name := """WeSquirrels"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
  "net.liftweb" %% "lift-json" % "2.6.2",
  "com.github.tyagihas" % "scala_nats_2.10" % "0.1"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += "The New Motion Public Repo" at "http://nexus.thenewmotion.com/content/groups/public/"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

dockerBaseImage := "frolvlad/alpine-oraclejdk8"

dockerCommands := dockerCommands.value.flatMap{
  case cmd@Cmd("FROM",_) => List(cmd,Cmd("RUN", "apk update && apk add bash"))
  case other => List(other)
}