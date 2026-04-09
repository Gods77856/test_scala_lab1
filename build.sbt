name := "RedditScalaLab"
version := "0.1.0"
scalaVersion := "2.13.14"

libraryDependencies += "org.json4s" %% "json4s-jackson" % "4.0.6"
libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.4.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.17" % Test

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked"
)
