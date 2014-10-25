name := "spark-notebook-core"

version := "1.0"

scalaVersion  := "2.10.4"

val akkaV = "2.2.3-shaded-protobuf"

val sprayV = "1.2.1"

libraryDependencies += "org.apache.spark" %% "spark-repl" % "1.1.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.0" % "test"

libraryDependencies += "io.spray" % "spray-can" % sprayV

libraryDependencies += "io.spray" % "spray-routing" % sprayV

libraryDependencies += "org.spark-project.akka" %% "akka-actor" % akkaV

libraryDependencies += "org.spark-project.akka" %% "akka-remote" % akkaV

libraryDependencies += "org.spark-project.akka" %% "akka-slf4j" % akkaV

libraryDependencies += "org.spark-project.akka" %% "akka-testkit" % akkaV

