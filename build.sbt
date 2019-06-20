import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "zrender"
ThisBuild / organizationName := "zrender"

lazy val root = (project in file("."))
  .settings(
    name := "zrender",
    libraryDependencies ++= Seq(
      scalazCore,
      scalazZIO,
      scalazZIOInterop7x,
      scalazZIOInteropCats,
      fs2Core,
      fs2Http,
      circeCore,
      circeGeneric,
      circeParser,
      circeFs2,
      pureconfig,
      scalaTest % Test
    ),
    scalacOptions ++= Seq(
      "-language:higherKinds"
    )
  )

javaOptions in reStart += "-Xmx2g"
