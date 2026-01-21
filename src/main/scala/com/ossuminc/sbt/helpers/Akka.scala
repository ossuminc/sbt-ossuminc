package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.*

/** A helper that can be used to configure the complex dependencies in the Akka Platform.
  *
  * IMPORTANT: Akka is licensed under the Business Source License (BSL) 1.1 as of 2024.
  * A repository token is required to access Akka artifacts. Set the AKKA_REPO_TOKEN
  * environment variable with your token from https://akka.io/key
  *
  * This helper automatically:
  * - Adds the Akka repository resolver (https://repo.akka.io/maven)
  * - Configures credentials from AKKA_REPO_TOKEN environment variable
  * - Configures CrossVersion.for3Use2_13 for Scala 3 projects (Akka is Scala 2.13 only)
  */
object Akka extends AutoPluginHelper {

  /** Akka repository URL */
  val akkaRepoUrl = "https://repo.akka.io/maven"

  /** Akka repository resolver */
  val akkaResolver: MavenRepository = "Akka library repository".at(akkaRepoUrl)

  /** Akka repository credentials from AKKA_LICENSE_KEY environment variable */
  def akkaCredentials: Seq[Credentials] = {
    sys.env.get("AKKA_LICENSE_KEY").map { token =>
      Credentials("Akka library repository", "repo.akka.io", "token", token)
    }.toSeq
  }

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
    * Automatically adds:
    * - Akka repository resolver (https://repo.akka.io/maven)
    * - Credentials from AKKA_REPO_TOKEN environment variable
    * - Core Akka modules for the specified release
    *
    * @param release The Akka release version ("25.10" or "24.10"). Default is latest (25.10).
    * @param project The project to configure
    * @return The configured project with Akka resolver, credentials, and core modules
    */
  def forRelease(release: String = "")(project: Project): Project = {
    val coreVersion = release match {
      case "2025.10" | "25.10" | "latest" | "" => V_25_10.akka_core
      case "2024.10" | "24.10"                 => V_24_10.akka_core
      case other: String => throw new IllegalArgumentException(
        s"Unknown Akka release: $other. Supported releases: 25.10 (latest), 24.10"
      )
    }

    project.settings(
      resolvers += akkaResolver,
      credentials ++= akkaCredentials,
      libraryDependencies ++= coreModules(coreVersion)
    )
  }

  /** Configure Akka with latest release */
  def apply(project: Project): Project = forRelease("")(project)
}
