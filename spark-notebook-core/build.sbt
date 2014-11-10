name := "spark-notebook"

version := "1.0"

scalaVersion := "2.10.3"

val akkaV = "2.2.3-shaded-protobuf"

val sprayV = "1.2.2"

val ScalatraVersion = "2.3.0"

val jettyVersion = "8.1.16.v20140903"

fork in(Test, run) := true

javaOptions in(Test, run) += "-Djava.net.preferIPv4Stack=true"


resolvers += "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies += "org.apache.spark" %% "spark-repl" % "1.1.0" exclude("org.eclipse.jetty", "*")

libraryDependencies += "org.scalatest" %% "scalatest" % "2.0" % "test"

libraryDependencies += "org.scalatra" %% "scalatra" % ScalatraVersion

libraryDependencies += "org.scalatra" %% "scalatra-json" % ScalatraVersion

libraryDependencies += "org.json4s" %% "json4s-native" % "3.2.9"

libraryDependencies += "org.scalatra" %% "scalatra-scalate" % ScalatraVersion

libraryDependencies += "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test"

libraryDependencies += "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0"

libraryDependencies += "org.eclipse.jetty" % "jetty-server" % "8.1.16.v20140903"

libraryDependencies += "org.eclipse.jetty" % "jetty-webapp" % "8.1.16.v20140903"

libraryDependencies += "org.spark-project.akka" %% "akka-actor" % akkaV

libraryDependencies += "org.spark-project.akka" %% "akka-remote" % akkaV

libraryDependencies += "org.spark-project.akka" %% "akka-slf4j" % akkaV

libraryDependencies += "org.spark-project.akka" %% "akka-testkit" % akkaV

logLevel := Level.Info