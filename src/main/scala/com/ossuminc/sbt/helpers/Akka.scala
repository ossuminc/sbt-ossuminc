package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.*

/** A helper that can be used to configure the complex dependencies in the Akka Platform.
  *
  * IMPORTANT: Akka is licensed under the Business Source License (BSL) 1.1 as of 2024.
  * A repository token is required to access Akka artifacts. Configure the Akka repository
  * resolver in ~/.sbt/1.0/ per Akka's instructions at https://akka.io/key
  *
  * Akka artifacts are published for Scala 2.13 only. This helper automatically configures
  * CrossVersion.for3Use2_13 for Scala 3 projects.
  */
object Akka extends AutoPluginHelper {

  /** Helper to create Akka dependency with Scala 2.13 cross-version for Scala 3 compatibility */
  def akkaModule(org: String, name: String, version: String): ModuleID =
    (org %% name % version).cross(CrossVersion.for3Use2_13)

  /** Helper to create Akka test dependency with Scala 2.13 cross-version */
  def akkaTestModule(org: String, name: String, version: String): ModuleID =
    (org %% name % version % Test).cross(CrossVersion.for3Use2_13)

  /** Version numbers for Akka 2024.10 release */
  object V_24_10 {
    val akka_core = "2.10.0"
    val akka_http = "10.7.0"
    val akka_grpc = "2.5.0"
    val akka_persistence_r2dbc = "1.3.0"
    val akka_management = "1.6.0"
    val akka_projections = "1.6.0"
    val akka_kafka = "7.0.0"
  }

  /** Version numbers for Akka 25.10 release (latest as of Jan 2026)
    * Version numbers from: https://doc.akka.io/libraries/akka-dependencies/current/
    */
  object V_25_10 {
    val akka_core = "2.10.14"
    val akka_http = "10.7.3"
    val akka_grpc = "2.5.10"
    val akka_persistence_r2dbc = "1.3.11"
    val akka_management = "1.6.4"
    val akka_projections = "1.6.18"
    val akka_kafka = "8.0.0"
  }

  /** Core Akka modules (actor, cluster, persistence, stream) - available in all releases */
  def coreModules(coreVersion: String): Seq[ModuleID] = Seq(
    akkaModule("com.typesafe.akka", "akka-actor", coreVersion),
    akkaModule("com.typesafe.akka", "akka-actor-typed", coreVersion),
    akkaModule("com.typesafe.akka", "akka-cluster", coreVersion),
    akkaModule("com.typesafe.akka", "akka-cluster-typed", coreVersion),
    akkaModule("com.typesafe.akka", "akka-cluster-sharding", coreVersion),
    akkaModule("com.typesafe.akka", "akka-cluster-sharding-typed", coreVersion),
    akkaModule("com.typesafe.akka", "akka-cluster-tools", coreVersion),
    akkaModule("com.typesafe.akka", "akka-coordination", coreVersion),
    akkaModule("com.typesafe.akka", "akka-discovery", coreVersion),
    akkaModule("com.typesafe.akka", "akka-distributed-data", coreVersion),
    akkaModule("com.typesafe.akka", "akka-persistence", coreVersion),
    akkaModule("com.typesafe.akka", "akka-persistence-typed", coreVersion),
    akkaModule("com.typesafe.akka", "akka-persistence-query", coreVersion),
    akkaModule("com.typesafe.akka", "akka-protobuf-v3", coreVersion),
    akkaModule("com.typesafe.akka", "akka-remote", coreVersion),
    akkaModule("com.typesafe.akka", "akka-serialization-jackson", coreVersion),
    akkaModule("com.typesafe.akka", "akka-slf4j", coreVersion),
    akkaModule("com.typesafe.akka", "akka-stream", coreVersion),
    akkaModule("com.typesafe.akka", "akka-stream-typed", coreVersion)
  )

  /** Core test modules */
  def coreTestModules(coreVersion: String): Seq[ModuleID] = Seq(
    akkaTestModule("com.typesafe.akka", "akka-testkit", coreVersion),
    akkaTestModule("com.typesafe.akka", "akka-actor-testkit-typed", coreVersion),
    akkaTestModule("com.typesafe.akka", "akka-stream-testkit", coreVersion)
  )

  /** Akka HTTP modules */
  def httpModules(httpVersion: String): Seq[ModuleID] = Seq(
    akkaModule("com.typesafe.akka", "akka-http", httpVersion),
    akkaModule("com.typesafe.akka", "akka-http-core", httpVersion),
    akkaModule("com.typesafe.akka", "akka-http-spray-json", httpVersion)
  )

  /** Akka HTTP test modules */
  def httpTestModules(httpVersion: String): Seq[ModuleID] = Seq(
    akkaTestModule("com.typesafe.akka", "akka-http-testkit", httpVersion)
  )

  /** Akka gRPC module */
  def grpcModules(grpcVersion: String): Seq[ModuleID] = Seq(
    akkaModule("com.lightbend.akka.grpc", "akka-grpc-runtime", grpcVersion)
  )

  /** Akka Persistence R2DBC module */
  def persistenceR2dbcModules(version: String): Seq[ModuleID] = Seq(
    akkaModule("com.lightbend.akka", "akka-persistence-r2dbc", version)
  )

  /** Akka Projections modules */
  def projectionsModules(version: String): Seq[ModuleID] = Seq(
    akkaModule("com.lightbend.akka", "akka-projection-core", version),
    akkaModule("com.lightbend.akka", "akka-projection-eventsourced", version),
    akkaModule("com.lightbend.akka", "akka-projection-r2dbc", version)
  )

  /** Akka Management modules */
  def managementModules(version: String): Seq[ModuleID] = Seq(
    akkaModule("com.lightbend.akka.management", "akka-management", version),
    akkaModule("com.lightbend.akka.management", "akka-management-cluster-http", version),
    akkaModule("com.lightbend.akka.management", "akka-management-cluster-bootstrap", version)
  )

  /** Alpakka Kafka (akka-stream-kafka) modules */
  def kafkaModules(version: String): Seq[ModuleID] = Seq(
    akkaModule("com.typesafe.akka", "akka-stream-kafka", version)
  )

  /** Configure Akka dependencies for a specific release.
    *
    * NOTE: The Akka repository resolver must be configured separately in ~/.sbt/1.0/
    * per Akka's instructions at https://akka.io/key
    *
    * @param release The Akka release version ("25.10" or "24.10"). Default is latest (25.10).
    * @param project The project to configure
    * @return The configured project with core Akka modules
    */
  def forRelease(release: String = "")(project: Project): Project = {
    val coreVersion = release match {
      case "2025.10" | "25.10" | "latest" | "" => V_25_10.akka_core
      case "2024.10" | "24.10"                 => V_24_10.akka_core
      case other: String => throw new IllegalArgumentException(
        s"Unknown Akka release: $other. Supported releases: 25.10 (latest), 24.10"
      )
    }

    // Only include core modules by default - users can add HTTP, gRPC etc. as needed
    project.settings(
      libraryDependencies ++= coreModules(coreVersion)
    )
  }

  /** Configure Akka with latest release */
  def apply(project: Project): Project = forRelease("")(project)
}
