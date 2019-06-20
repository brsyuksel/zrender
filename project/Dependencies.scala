import sbt._

object Dependencies {
  lazy val scalazCore = "org.scalaz" %% "scalaz-core" % "7.2.27"
  lazy val scalazZIO = "org.scalaz" %% "scalaz-zio" % "1.0-RC5"
  lazy val scalazZIOInterop7x = "org.scalaz" %% "scalaz-zio-interop-scalaz7x" % "1.0-RC5"
  lazy val scalazZIOInteropCats = "org.scalaz" %% "scalaz-zio-interop-cats" % "1.0-RC5"
  lazy val fs2Core = "co.fs2" %% "fs2-core" % "1.0.5"
  lazy val fs2Http = "com.spinoco" %% "fs2-http" % "0.4.1"
  lazy val circeCore = "io.circe" %% "circe-core" % "0.11.0"
  lazy val circeGeneric = "io.circe" %% "circe-generic" % "0.11.0"
  lazy val circeParser = "io.circe" %% "circe-parser" % "0.11.0"
  lazy val circeFs2 = "io.circe" %% "circe-fs2" % "0.11.0"
  lazy val pureconfig = "com.github.pureconfig" %% "pureconfig" % "0.11.0"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8"
}
