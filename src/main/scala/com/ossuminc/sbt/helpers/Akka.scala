package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.*

/** A helper that can be used to configure the complex dependencies in the Akka Platform */
object Akka extends AutoPluginHelper {

  sealed trait AkkaVersion {
    def akka_modules: Seq[ModuleID]
    def akka_test: Seq[ModuleID]
  }

  /** An object to define the components of the Akka 2024.05 release of Akka Platform */
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
      "com.typesafe.akka" %% "akka-actor" % V.akka_core,
      "com.typesafe.akka" %% "akka-actor-typed" % V.akka_core,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % V.akka_core % Test,
      "com.typesafe.akka" %% "akka-coordination" % V.akka_core,
      "com.typesafe.akka" %% "akka-cluster-typed" % V.akka_core,
      "com.typesafe.akka" %% "akka-cluster" % V.akka_core,
      "com.typesafe.akka" %% "akka-cluster-metrics" % V.akka_core,
      "com.typesafe.akka" %% "akka-cluster-sharding" % V.akka_core,
      "com.typesafe.akka" %% "akka-cluster-sharding-typed" % V.akka_core,
      "com.typesafe.akka" %% "akka-cluster-tools" % V.akka_core,
      "com.typesafe.akka" %% "akka-discovery" % V.akka_core,
      "com.typesafe.akka" %% "akka-distributed-data" % V.akka_core,
      "com.typesafe.akka" %% "akka-persistence" % V.akka_core,
      "com.typesafe.akka" %% "akka-persistence-tck" % V.akka_core,
      "com.typesafe.akka" %% "akka-persistence-typed" % V.akka_core,
      "com.typesafe.akka" %% "akka-persistence-query" % V.akka_core,
      "com.typesafe.akka" %% "akka-protobuf-v3" % V.akka_core,
      "com.typesafe.akka" %% "akka-remote" % V.akka_core,
      "com.typesafe.akka" %% "akka-slf4j" % V.akka_core,
      "com.typesafe.akka" %% "akka-stream" % V.akka_core,
      "com.typesafe.akka" %% "akka-stream-typed" % V.akka_core,
      "org.slf4j" % "slf4j-simple" % V.slf4j
    )
    def akka_test: Seq[ModuleID] = Seq(
      "com.typesafe.akka" %% "akka-testkit" % V.akka_core % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % V.akka_core % Test,
      "com.typesafe.akka" %% "akka-persistence-testkit" % V.akka_core % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % V.akka_core % Test,
      "com.typesafe.akka" %% "akka-http-testkit" % V.akka_http % Test,
      "com.typesafe.akka" %% "akka-multi-node-testkit" % V.akka_core % Test
    )
  }

  case object akka_2024_10 extends AkkaVersion {
    object V {
      val akka_core = "2.10.0"
      val akka_http = "10.7.0"
      val akka_grpc = "2.5.0"
      val akka_persistence_r2dbc = "1.2.4"
      val akka_management = "1.6.0"
      val akka_projections = "1.5.4"
      val akka_diagnostics = "2.1.1"
      val akka_kafka = "7.0.1"
      val slf4j = "2.0.13"
    }
    def akka_core: Seq[ModuleID] = Seq(
      "com.typesafe.akka" %% "akka-actor" % V.akka_core,
      "com.typesafe.akka" %% "akka-actor-typed" % V.akka_core,
      "com.typesafe.akka" %% "akka-cluster-typed" % V.akka_core,
      "com.typesafe.akka" %% "akka-cluster-sharding" % V.akka_core,
      "com.typesafe.akka" %% "akka-cluster-sharding-typed" % V.akka_core,
      "com.typesafe.akka" %% "akka-distributed-data" % V.akka_core,
      "com.typesafe.akka" %% "akka-cluster-tools" % V.akka_core,
      "com.typesafe.akka" %% "akka-persistence-typed" % V.akka_core,
      "com.typesafe.akka" %% "akka-persistence-query" % V.akka_core,
      "com.typesafe.akka" %% "akka-persistence" % V.akka_core,
      "com.typesafe.akka" %% "akka-persistence-query" % V.akka_core,
      "com.typesafe.akka" %% "akka-protobuf-v3" % V.akka_core,
      "com.typesafe.akka" %% "akka-remote" % V.akka_core,
      "com.typesafe.akka" %% "akka-slf4j" % V.akka_core,
      "com.typesafe.akka" %% "akka-stream" % V.akka_core,
      "com.typesafe.akka" %% "akka-serialization-jackson" % V.akka_core
    )

    def akka_modules: Seq[ModuleID] = akka_core ++ Seq(
      "com.lightbend.akka.grpc" %% "akka-grpc-runtime" % V.akka_grpc,
      "com.typesafe.akka" %% "akka-http" % V.akka_http,
      "com.lightbend.akka" %% "akka-persistence-r2dbc" % V.akka_persistence_r2dbc,
      "com.lightbend.akka" %% "akka-projection-core" % V.akka_projections,
      "com.lightbend.akka.management" %% "akka-management" % V.akka_management,
      "com.lightbend.akka" %% "akka-diagnostics" % V.akka_diagnostics,
      "com.typesafe.akka" %% "akka-stream-kafka" % V.akka_kafka,
      "org.slf4j" % "slf4j-simple" % V.slf4j
    )
    def akka_test: Seq[ModuleID] = Seq(
      "com.typesafe.akka" %% "akka-testkit" % V.akka_core % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % V.akka_core % Test,
      "com.typesafe.akka" %% "akka-persistence-testkit" % V.akka_core % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % V.akka_core % Test,
      "com.typesafe.akka" %% "akka-http-testkit" % V.akka_http % Test
    )
  }

  def configure(version: AkkaVersion = akka_2024_10)(project: Project): Project = {
    project.settings(
      resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
      libraryDependencies ++= version.akka_modules ++ version.akka_test
    )
  }

  override def configure(project: Project): Project = {
    configure(akka_2024_05)(project)
  }
}
