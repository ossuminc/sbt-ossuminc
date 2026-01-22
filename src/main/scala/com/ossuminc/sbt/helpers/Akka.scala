package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.*

/** A helper that can be used to configure the complex dependencies in the Akka Platform.
  *
  * IMPORTANT: Akka is licensed under the Business Source License (BSL) 1.1 as of 2024. Two separate
  * credentials are required:
  *
  *   1. AKKA_REPO_TOKEN - Repository access token for downloading artifacts. Set this environment
  *      variable with your token from https://account.akka.io Used in repository URL:
  *      https://repo.akka.io/{token}/secure
  *
  * 2. akka.license-key - Runtime license key for your application. Configure this in your
  * application.conf, NOT as an environment variable for sbt.
  *
  * This helper automatically:
  *   - Adds the Akka repository resolver with tokenized URL (requires AKKA_REPO_TOKEN)
  *   - Configures CrossVersion.for3Use2_13 for Scala 3 projects (Akka is Scala 2.13 only)
  */
object Akka extends AutoPluginHelper {

  /** Akka repository URL with embedded token. Akka uses tokenized URLs for authentication - the
    * token is part of the URL path. See https://doc.akka.io/libraries/akka-dependencies/current/
    */
  private def akkaRepoUrl: String = {
    sys.env.get("AKKA_REPO_TOKEN") match {
      case Some(token) => s"https://repo.akka.io/$token/secure"
      case None =>
        System.err.println(
          "WARNING: AKKA_REPO_TOKEN environment variable not set. " +
            "Get your repository access token at https://account.akka.io"
        )
        // Return a URL that will fail with a clear error
        "https://repo.akka.io/MISSING_TOKEN/secure"
    }
  }

  /** Akka Maven-style repository resolver */
  private def akkaMavenResolver: MavenRepository = "akka-secure-mvn".at(akkaRepoUrl)

  /** Akka Ivy-style repository resolver. Some Akka artifacts are published in Ivy format and
    * require Ivy-style patterns.
    */
  private def akkaIvyResolver: URLRepository =
    Resolver.url("akka-secure-ivy", url(akkaRepoUrl))(Resolver.ivyStylePatterns)

  /** Both Akka resolvers (Maven and Ivy style) */
  private def akkaResolvers: Seq[Resolver] = Seq(akkaMavenResolver, akkaIvyResolver)

  /** Helper to create Akka dependency with Scala 2.13 cross-version for Scala 3 compatibility */
  private def akkaModule(org: String, name: String, version: String): ModuleID =
    (org %% name % version).cross(CrossVersion.for3Use2_13)

  /** Helper to create Akka test dependency with Scala 2.13 cross-version */
  private def akkaTestModule(org: String, name: String, version: String): ModuleID =
    (org %% name % version % Test).cross(CrossVersion.for3Use2_13)

  sealed trait ReleaseVersions {
    def akka_core: String
    def akka_http: String
    def akka_grpc: String
    def akka_persistence_r2dbc: String
    def akka_management: String
    def akka_projections: String
    def akka_kafka: String
  }

  /** Version numbers for Akka 2024.10 release */
  private object V_24_10 extends ReleaseVersions {
    val akka_core = "2.10.0"
    val akka_http = "10.7.0"
    val akka_grpc = "2.5.0"
    val akka_persistence_r2dbc = "1.3.0"
    val akka_management = "1.6.0"
    val akka_projections = "1.6.0"
    val akka_kafka = "7.0.0"
  }

  /** Version numbers for Akka 25.10 release (latest as of Jan 2026) Version numbers from:
    * https://doc.akka.io/libraries/akka-dependencies/current/
    */
  private object V_25_10 extends ReleaseVersions {
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
    *   - Akka repository resolvers (Maven and Ivy style) with tokenized URL from AKKA_LICENSE_KEY
    *   - Core Akka modules for the specified release
    *
    * @param release
    *   The Akka release version ("25.10" or "24.10"). Default is latest (25.10).
    * @param project
    *   The project to configure
    * @return
    *   The configured project with Akka resolvers and core modules
    */
  def forRelease(
    release: String = "",
    withHTTP: Boolean = false,
    withGrpc: Boolean = false,
    withPersistence: Boolean = false,
    withProjections: Boolean = false,
    withManagement: Boolean = false,
    withKafka: Boolean = false
  )(project: Project): Project = {
    val versions: ReleaseVersions = release match {
      case "2025.10" | "25.10" | "latest" | "" => V_25_10
      case "2024.10" | "24.10"                 => V_24_10
      case other: String =>
        throw new IllegalArgumentException(
          s"Unknown Akka release: $other. Supported releases: 25.10 (latest), 24.10"
        )
    }

    project.settings(
      resolvers ++= akkaResolvers,
      libraryDependencies ++= coreModules(versions.akka_core),
      libraryDependencies ++= coreTestModules(versions.akka_core),
      libraryDependencies ++= {
        Seq.empty[ModuleID] ++ {
          if (withHTTP) httpModules(versions.akka_http) ++ httpTestModules(versions.akka_http)
          else Seq.empty[ModuleID]
        } ++ {
          if (withGrpc) grpcModules(versions.akka_grpc) else Seq.empty[ModuleID]
        } ++ {
          if (withPersistence) persistenceR2dbcModules(versions.akka_persistence_r2dbc)
          else Seq.empty[ModuleID]
        } ++ {
          if (withProjections) projectionsModules(versions.akka_projections)
          else Seq.empty[ModuleID]
        } ++ {
          if (withManagement) managementModules(versions.akka_management) else Seq.empty[ModuleID]
        } ++ {
          if (withKafka) kafkaModules(versions.akka_kafka) else Seq.empty[ModuleID]
        }
      }
    )
  }

  /** Configure Akka with the latest release */
  def apply(project: Project): Project = forRelease("")(project)
}
