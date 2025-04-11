val scala3Version = "3.6.4"

lazy val root = project
  .in(file("."))
  .settings(
    name := "readsgsheet",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "org.scalatest" %% "scalatest" % "latest.integration" % Test,
    libraryDependencies += "com.softwaremill.sttp.client4" %% "core" % "4.0.0-RC3",
    libraryDependencies += "com.github.jwt-scala" %% "jwt-core" % "10.0.4",
    libraryDependencies += "com.lihaoyi" %% "upickle" % "4.1.0",
    libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "3.0.0"
  )
