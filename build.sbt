scalaVersion := "2.12.4"

val akkaVersion = "2.5.11"
val akkaHttpVersion = "10.1.1"
val scalaTestVersion = "3.0.5"

lazy val commonSettings = Seq(
  organization := "com.tpalanga",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.12.4",
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
)

val commonDependencies = Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.20",
  "com.softwaremill.sttp" %% "core" % "1.1.12",
  "com.softwaremill.sttp" %% "akka-http-backend" % "1.1.12",
  "com.softwaremill.sttp" %% "json4s" % "1.1.12",
  "io.spray" %%  "spray-json" % "1.3.4",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
)

val testingDependencies = Seq(
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",
  "org.scalactic" %% "scalactic" % scalaTestVersion % "test",
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
)

lazy val aMonolithService = project.
  settings(
    name := "aMonolithService",
    commonSettings,
    libraryDependencies ++= commonDependencies ++ testingDependencies
  )
  .dependsOn(zCommon)

lazy val bActorsService = project.
  settings(
    name := "bActorsService",
    commonSettings,
    libraryDependencies ++= commonDependencies ++ testingDependencies
  )
  .dependsOn(zCommon)

lazy val eReactiveService = project.
  settings(
    name := "eReactiveService",
    commonSettings,
    libraryDependencies ++= commonDependencies ++ testingDependencies
  )
  .dependsOn(zCommon)

lazy val zCommon = project.
  settings(
    name := "zCommon",
    commonSettings,
    libraryDependencies ++=  commonDependencies ++ testingDependencies
  )
lazy val zTests = project.
  settings(
    name := "zTests",
    commonSettings,
    libraryDependencies ++=  commonDependencies ++ testingDependencies
  )
