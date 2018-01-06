name := "kafka_streams_demo"

version := "0.1"

scalaVersion := "2.11.11"

val kafka_streams_scala_version = "0.1.0"

val kryo = Seq(
  "com.twitter" %% "chill-bijection" % "0.8.1"
)

libraryDependencies ++= Seq("com.lightbend" %%
  "kafka-streams-scala" % kafka_streams_scala_version)
libraryDependencies ++= kryo
        