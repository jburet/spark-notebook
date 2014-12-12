
name := "spark-notebook-core"

version := "1.0"

scalaVersion := "2.10.3"

val akkaV = "2.2.3-shaded-protobuf"

val sprayV = "1.2.2"

val ScalatraVersion = "2.3.0"

val jettyVersion = "8.1.16.v20140903"

fork in(Test, run) := true

javaOptions in(Test, run) += "-Djava.net.preferIPv4Stack=true"

resolvers += "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies += "org.apache.spark" %% "spark-repl" % "1.1.0" exclude("org.eclipse.jetty", "*") exclude("log4j", "*") exclude("org.slf4j", "*")

libraryDependencies += "org.apache.spark" %% "spark-sql" % "1.1.0" exclude("org.eclipse.jetty", "*") exclude("log4j", "*") exclude("org.slf4j", "*")

libraryDependencies += "org.scalatest" %% "scalatest" % "2.0" % "test"

libraryDependencies += "org.scalatra" %% "scalatra" % ScalatraVersion exclude("com.typesafe.akka", "*")

libraryDependencies += "org.scalatra" %% "scalatra-json" % ScalatraVersion

libraryDependencies += "org.json4s" %% "json4s-native" % "3.2.9"

libraryDependencies += "org.scalatra" %% "scalatra-scalate" % ScalatraVersion

libraryDependencies += "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test"

libraryDependencies += "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test"

libraryDependencies += "org.scalatra" %% "scalatra-atmosphere" % "2.3.0" exclude("com.typesafe.akka", "*")

libraryDependencies += "org.atmosphere" % "atmosphere-runtime" % "2.2.3"

libraryDependencies += "org.eclipse.jetty.websocket" % "websocket-server" % "9.2.1.v20140609"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0"

libraryDependencies += "org.eclipse.jetty" % "jetty-server" % "9.2.1.v20140609"

libraryDependencies += "org.eclipse.jetty" % "jetty-webapp" % "9.2.1.v20140609"

libraryDependencies += "org.eclipse.jetty" % "jetty-plus" % "9.2.1.v20140609"

libraryDependencies += "org.spark-project.akka" %% "akka-actor" % akkaV

libraryDependencies += "org.spark-project.akka" %% "akka-remote" % akkaV

libraryDependencies += "org.spark-project.akka" %% "akka-slf4j" % akkaV

libraryDependencies += "org.spark-project.akka" %% "akka-testkit" % akkaV

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.13"

libraryDependencies += "org.slf4j" % "log4j-over-slf4j" % "1.7.7"

libraryDependencies += "org.parboiled" %% "parboiled" % "2.0.1"