name := "23-actorstest"

version := "0.1"

scalaVersion := "2.12.3"

val akkaHttpVersion = "10.0.9"
val akkaVersion = "2.5.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
)

libraryDependencies += "io.spray" %% "spray-json" % "1.3.3"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)
libraryDependencies ++= Seq(
  "org.iq80.leveldb" % "leveldb" % "0.7",
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"
)
val catsversion = "1.0.0-MF"
libraryDependencies += "org.typelevel" %% "cats-core" % catsversion
libraryDependencies += "org.typelevel" %% "cats-free" % catsversion