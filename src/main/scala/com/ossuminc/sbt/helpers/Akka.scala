package com.ossuminc.sbt.helpers
import sbt.*
import sbt.Keys.*

object Akka extends AutoPluginHelper {

  sealed trait AkkaVersion {
    def akka_modules: Seq[ModuleID]
    def akka_test: Seq[ModuleID]
  }

  // Akka 24.05 consists of the following module versions:
  //
  // Akka (core) 2.9.3
  // Akka HTTP 10.6.3
  // Akka gRPC 2.4.3
  // Akka Management 1.5.2
  // Alpakka Kafka 6.0.0
  // Alpakka 8.0.0
  // Akka Persistence R2DBC 1.2.4
  // Akka Persistence JDBC 5.4.1
  // Akka Persistence Cassandra 1.2.1
  // Akka Projections 1.5.4
  // Akka Diagnostics 2.1.1
  // Akka Insights 2.20.0

  case object akka_2024_05 extends AkkaVersion {
    object V {
      val akka_core = "2.9.3"
      val akka_grpc = "2.4.3"
      val akka_http = "10.6.3"
      val akka_persistence_r2dbc = "1.2.4"
      val akka_persistence_cassandra = "1.2.1"
      val akka_management = "1.5.2"
      val akka_projections = "1.5.4"
      val akka_diagnostics = "2.1.1"
      val slf4j = "2.0.13"
    }

    def akka_modules: Seq[ModuleID] = Seq(
      "com.typesafe.akka" %% "akka-slf4j" % V.akka_core,
      "com.typesafe.akka" %% "akka-protobuf-v3" % V.akka_core,
      "com.typesafe.akka" %% "akka-serialization-jackson" % V.akka_core,
      "com.typesafe.akka" %% "akka-actor-typed" % V.akka_core,
      "com.typesafe.akka" %% "akka-cluster-typed" % V.akka_core,
      "com.typesafe.akka" %% "akka-cluster-sharding" % V.akka_core,
      "com.typesafe.akka" %% "akka-cluster-sharding-typed" % V.akka_core,
      "com.typesafe.akka" %% "akka-distributed-data" % V.akka_core,
      "com.typesafe.akka" %% "akka-cluster-tools" % V.akka_core,
      "com.typesafe.akka" %% "akka-persistence-typed" % V.akka_core,
      "com.typesafe.akka" %% "akka-persistence-query" % V.akka_core,
      "com.typesafe.akka" %% "akka-persistence-cassandra" % V.akka_persistence_cassandra,
      "com.typesafe.akka" %% "akka-persistence" % V.akka_core,
      "com.typesafe.akka" %% "akka-persistence-query" % V.akka_core,
      "com.typesafe.akka" %% "akka-cluster-tools" % V.akka_core,
      "com.typesafe.akka" %% "akka-stream" % V.akka_core,
      "com.typesafe.akka" %% "akka-http" % V.akka_http,
      "com.lightbend.akka" %% "akka-persistence-r2dbc" % V.akka_persistence_r2dbc,
      "com.lightbend.akka" %% "akka-projection-core" % V.akka_projections,
      "com.lightbend.akka.grpc" %% "akka-grpc-runtime" % V.akka_grpc,
      "com.lightbend.akka.management" %% "akka-management" % V.akka_management,
      "com.lightbend.akka" %% "akka-diagnostics" % V.akka_diagnostics
    )
    def akka_test: Seq[ModuleID] = Seq(
      "com.typesafe.akka" %% "akka-testkit" % V.akka_core % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % V.akka_core % Test,
      "com.typesafe.akka" %% "akka-persistence-testkit" % V.akka_core % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % V.akka_core % Test,
      "com.typesafe.akka" %% "akka-http-testkit" % V.akka_http % Test,
      "org.slf4j" % "slf4j-simple" % V.slf4j
    )
  }

  def configure(version: AkkaVersion = akka_2024_05)(project: Project): Project = {
    project.settings(
      resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
      libraryDependencies ++= akka_2024_05.akka_modules ++ akka_2024_05.akka_test
    )
  }

  override def configure(project: Project): Project = {
    configure(akka_2024_05)(project)
  }

}
