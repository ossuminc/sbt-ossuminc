package com.ossuminc.sbt.helpers

import com.typesafe.sbt.SbtNativePackager
import com.typesafe.sbt.SbtNativePackager.{Docker, Universal}
import com.typesafe.sbt.packager.docker.DockerPlugin
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._
import com.typesafe.sbt.packager.linux.LinuxPlugin.autoImport.daemonUser
import com.typesafe.sbt.packager.graalvmnativeimage.GraalVMNativeImagePlugin
import com.typesafe.sbt.packager.Keys.{maintainer, packageDescription, packageName, packageSummary, stage}
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.graalvmnativeimage.GraalVMNativeImagePlugin.autoImport.graalVMNativeImageCommand
import sbt._
import sbt.Keys._

import java.io.File

object Packaging extends AutoPluginHelper {

  /** Keys for docker-dual configuration */
  object Keys {
    val dockerPublishProd = taskKey[Unit](
      "Build and publish production Docker image (distroless, amd64)"
    )
    val dockerStageProd = taskKey[Unit](
      "Stage production Docker image files"
    )
    val dockerProdBaseImage = settingKey[String](
      "Base image for production Docker builds"
    )
    val dockerDevBaseImage = settingKey[String](
      "Base image for development Docker builds"
    )
    val dockerMainClass = settingKey[String](
      "Main class for Docker entrypoint"
    )
  }

  /** Default base images for dual Docker builds */
  object Defaults {
    val devBaseImage = "eclipse-temurin:25-jdk-noble"
    val prodBaseImage = "gcr.io/distroless/java25-debian13:nonroot"
    val repository = "ghcr.io"
    val username = "ossuminc"
  }

  override def apply(project: Project) = none(project)

  def none(project: Project): Project = project

  def universal(
    maintainerEmail: String,
    pkgName: String,
    pkgSummary: String,
    pkgDescription: String
  )(project: Project): Project =
    project
      .enablePlugins(JavaAppPackaging)
      .settings(
        maintainer := maintainerEmail,
        ThisBuild / maintainer := maintainerEmail,
        Universal / maintainer := maintainerEmail,
        Universal / packageName := pkgName,
        Universal / packageSummary := pkgSummary,
        Universal / packageDescription := pkgDescription
      )

  def docker(
    maintainerEmail: String,
    pkgName: String = "",
    pkgSummary: String = "",
    pkgDescription: String = "",
    dockerFile: File = file("")
  )(project: Project): Project = {
    project
      .enablePlugins(SbtNativePackager, DockerPlugin)
      .settings(
        maintainer := maintainerEmail,
        ThisBuild / maintainer := maintainerEmail,
        Docker / maintainer := maintainerEmail,
        Docker / packageName := pkgName,
        Docker / packageSummary := pkgSummary,
        Docker / packageDescription := pkgDescription
      )
  }

  /** Configure dual Docker image builds for dev (local) and prod (GKE).
    *
    * Dev image:
    *   - Base: eclipse-temurin:25-jdk-noble (Ubuntu 24.04 with JDK tools)
    *   - Architecture: linux/arm64 (Apple Silicon)
    *   - Tags: :dev-latest, :dev-<version>
    *   - Built with: docker:publishLocal (default)
    *
    * Prod image:
    *   - Base: gcr.io/distroless/java25-debian13:nonroot (minimal, secure)
    *   - Architecture: linux/amd64 (GKE)
    *   - Tags: :latest, :<version>
    *   - Built with: dockerPublishProd
    *
    * @param mainClass
    *   Fully qualified main class name (e.g., "com.ossuminc.riddl.mcp.Main")
    * @param pkgName
    *   Docker image name (e.g., "riddl-mcp-server")
    * @param exposedPorts
    *   Ports to expose in the container
    * @param pkgDescription
    *   Optional description for the package
    */
  def dockerDual(
    mainClass: String,
    pkgName: String,
    exposedPorts: Seq[Int],
    pkgDescription: String = ""
  )(project: Project): Project = {
    project
      .enablePlugins(SbtNativePackager, JavaAppPackaging, DockerPlugin)
      .settings(
        // Store configuration for use in tasks
        Keys.dockerMainClass := mainClass,
        Keys.dockerDevBaseImage := Defaults.devBaseImage,
        Keys.dockerProdBaseImage := Defaults.prodBaseImage,

        // Default Docker settings (dev image - what docker:publishLocal uses)
        dockerBaseImage := Keys.dockerDevBaseImage.value,
        dockerRepository := Some(Defaults.repository),
        dockerUsername := Some(Defaults.username),
        Docker / packageName := pkgName,
        Docker / packageDescription := pkgDescription,
        Docker / daemonUser := "ossum",
        dockerExposedPorts := exposedPorts,

        // Dev image tags: dev-latest, dev-<version>
        dockerAliases := Seq(
          dockerAlias.value.withTag(Some(s"dev-${version.value}")),
          dockerAlias.value.withTag(Some("dev-latest"))
        ),

        // Note: docker:publishLocal uses standard Docker build which defaults to host
        // architecture (arm64 on Apple Silicon, amd64 on Intel/Linux). This is ideal
        // for local development. Production builds use buildx with explicit amd64.

        // Production staging task - creates Dockerfile for distroless
        Keys.dockerStageProd := {
          val log = streams.value.log
          val stageDir = (Docker / stagingDirectory).value
          val mainCls = Keys.dockerMainClass.value
          val prodBase = Keys.dockerProdBaseImage.value
          val name = (Docker / packageName).value
          val ver = version.value
          val ports = dockerExposedPorts.value

          log.info(s"Staging production Docker image for $name:$ver")

          // First run the normal staging to get lib/ directory
          (Docker / stage).value

          // Generate distroless Dockerfile with EXPOSE directives
          val dockerfile = stageDir / "Dockerfile"
          val exposeLines = ports.map(p => s"EXPOSE $p").mkString("\n")
          val dockerfileContent =
            s"""FROM $prodBase
               |WORKDIR /opt/docker
               |COPY --chown=nonroot:nonroot opt/docker/lib lib
               |$exposeLines
               |ENTRYPOINT ["java", "-cp", "/opt/docker/lib/*", "$mainCls"]
               |""".stripMargin

          IO.write(dockerfile, dockerfileContent)
          log.info(s"Generated distroless Dockerfile at $dockerfile")
        },

        // Production publish task
        Keys.dockerPublishProd := {
          val log = streams.value.log
          val stageDir = (Docker / stagingDirectory).value
          val name = (Docker / packageName).value
          val ver = version.value
          val repo = dockerRepository.value.getOrElse(Defaults.repository)
          val user = dockerUsername.value.getOrElse(Defaults.username)

          // Run staging first
          Keys.dockerStageProd.value

          val fullImageName = s"$repo/$user/$name"
          val tags = Seq(ver, "latest")

          log.info(s"Building production image: $fullImageName")

          // Build with buildx for amd64
          val tagArgs = tags.flatMap(t => Seq("-t", s"$fullImageName:$t"))
          val buildCmd = Seq(
            "docker", "buildx", "build",
            "--platform", "linux/amd64",
            "--push"
          ) ++ tagArgs ++ Seq(stageDir.getAbsolutePath)

          log.info(s"Running: ${buildCmd.mkString(" ")}")

          val exitCode = sys.process.Process(buildCmd).!
          if (exitCode != 0) {
            sys.error(s"Docker buildx failed with exit code $exitCode")
          }

          log.info(s"Successfully published $fullImageName:$ver and $fullImageName:latest")
        }
      )
  }

  def graalVM(pkgName: String, pkgSummary: String, native_image_path: File)(
    project: Project
  ): Project = {
    project
      .enablePlugins(SbtNativePackager, GraalVMNativeImagePlugin)
      .settings(
        packageName := pkgName,
        packageSummary := pkgSummary,
        graalVMNativeImageCommand := native_image_path.getAbsolutePath
      )
  }
}
