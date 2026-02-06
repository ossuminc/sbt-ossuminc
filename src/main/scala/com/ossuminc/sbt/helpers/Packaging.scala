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
import scala.scalanative.sbtplugin.ScalaNativePlugin.autoImport._
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
    val linuxPackage = taskKey[File](
      "Create tar.gz archive of native binary for distribution"
    )
    val linuxPackageArch = settingKey[String](
      "Architecture label for the archive filename (auto-detected if empty)"
    )
    val linuxPackageOs = settingKey[String](
      "OS label for the archive filename (auto-detected if empty)"
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

          // sbt-native-packager stages files under numbered directories
          // (e.g., 2/opt/docker/lib/, 4/opt/docker/bin/). Flatten them
          // into opt/docker/ so the distroless Dockerfile can COPY them.
          val flatTarget = stageDir / "opt" / "docker"
          IO.createDirectory(flatTarget)
          val numberedDirs = stageDir.listFiles().filter { f =>
            f.isDirectory && f.getName.forall(_.isDigit)
          }
          for {
            numDir <- numberedDirs
            source = numDir / "opt" / "docker"
            if source.isDirectory
            child <- source.listFiles()
          } {
            val dest = flatTarget / child.getName
            if (child.isDirectory) {
              IO.copyDirectory(child, dest)
            } else {
              IO.copyFile(child, dest)
            }
            log.info(s"Staged: ${numDir.getName}/opt/docker/${child.getName} -> opt/docker/${child.getName}")
          }

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

  /** Configure npm packaging for a Scala.js project.
    *
    * Delegates to [[NpmPackaging.npm]]. See that method for full
    * parameter documentation.
    *
    * Usage:
    * {{{
    * .jsConfigure(
    *   With.Packaging.npm(
    *     scope = "@ossuminc",
    *     pkgName = "riddl-lib",
    *     pkgDescription = "RIDDL Language Library"
    *   )
    * )
    * }}}
    */
  def npm(
    scope: String = "",
    pkgName: String,
    pkgDescription: String = "",
    keywords: Seq[String] = Seq.empty,
    esModule: Boolean = true,
    templateFile: Option[File] = None
  )(project: Project): Project = {
    NpmPackaging.npm(
      scope, pkgName, pkgDescription, keywords, esModule, templateFile
    )(project)
  }

  /** Normalize JVM os.arch to standard archive label. */
  private def detectArch: String = {
    System.getProperty("os.arch") match {
      case "aarch64" => "arm64"
      case "x86_64" | "amd64" => "amd64"
      case other => other
    }
  }

  /** Normalize JVM os.name to standard archive label. */
  private def detectOs: String = {
    val name = System.getProperty("os.name", "").toLowerCase
    if (name.startsWith("mac") || name.startsWith("darwin")) "darwin"
    else if (name.startsWith("linux")) "linux"
    else if (name.startsWith("windows")) "windows"
    else name.replaceAll("\\s+", "-").toLowerCase
  }

  /** Create a tar.gz archive of a Scala Native binary for distribution.
    *
    * The archive includes the native binary and optionally a README and
    * LICENSE file from the project root. The filename follows the pattern:
    * `<pkgName>-<version>-<os>-<arch>.tar.gz`
    *
    * OS and architecture are auto-detected from the build host by default,
    * since Scala Native compiles for the host platform only. For
    * multi-platform distribution, use CI matrix runners for each target
    * platform.
    *
    * @param pkgName
    *   Base name for the archive and binary
    * @param pkgDescription
    *   Package description (included in generated README)
    * @param arch
    *   Architecture label override (empty = auto-detect from host)
    * @param os
    *   OS label override (empty = auto-detect from host)
    * @param includeReadme
    *   Whether to include a README.md in the archive
    * @param includeLicense
    *   Whether to include the LICENSE file from the project root
    */
  def linux(
    pkgName: String,
    pkgDescription: String = "",
    arch: String = "",
    os: String = "",
    includeReadme: Boolean = true,
    includeLicense: Boolean = true
  )(project: Project): Project = {
    project.settings(
      Keys.linuxPackageArch := {
        if (arch.nonEmpty) arch else detectArch
      },
      Keys.linuxPackageOs := {
        if (os.nonEmpty) os else detectOs
      },

      Keys.linuxPackage := {
        val log = streams.value.log
        val binary = (Compile / nativeLink).value
        val ver = version.value
        val archLabel = Keys.linuxPackageArch.value
        val osLabel = Keys.linuxPackageOs.value
        val baseDir = (ThisBuild / baseDirectory).value

        val stagingName = s"$pkgName-$ver"
        val staging = target.value / stagingName
        IO.delete(staging)
        IO.createDirectory(staging)

        // Copy the native binary
        IO.copyFile(binary, staging / pkgName)
        // Ensure the binary is executable
        (staging / pkgName).setExecutable(true)

        log.info(s"Staged binary: ${binary.getName} -> $staging/$pkgName")

        // Optionally include README
        if (includeReadme) {
          val readme = staging / "README.md"
          IO.write(readme,
            s"""# $pkgName
               |
               |$pkgDescription
               |
               |## Usage
               |
               |```bash
               |./$pkgName --help
               |```
               |""".stripMargin
          )
        }

        // Optionally include LICENSE from project root
        if (includeLicense) {
          val licenseFile = baseDir / "LICENSE"
          if (licenseFile.exists()) {
            IO.copyFile(licenseFile, staging / "LICENSE")
          }
        }

        // Create tar.gz archive
        val archiveName =
          s"$pkgName-$ver-$osLabel-$archLabel.tar.gz"
        val tarball = target.value / archiveName

        val cmd = Seq(
          "tar", "czf", tarball.getAbsolutePath,
          "-C", target.value.getAbsolutePath,
          stagingName
        )
        log.info(s"Creating archive: $archiveName")
        val exitCode = sys.process.Process(cmd).!
        if (exitCode != 0) {
          sys.error(s"tar failed with exit code $exitCode")
        }

        log.info(s"Archive created: $tarball")
        tarball
      }
    )
  }

  /** Generate a Homebrew formula for the project.
    *
    * Delegates to [[HomebrewPackaging.homebrew]]. See that method for
    * full parameter documentation.
    *
    * Usage:
    * {{{
    * .jvmConfigure(
    *   With.Packaging.homebrew(
    *     formulaName = "riddlc",
    *     binaryName = "riddlc",
    *     pkgDescription = "Compiler for the RIDDL language",
    *     homepage = "https://ossum.tech/riddl/"
    *   )
    * )
    * }}}
    */
  def homebrew(
    formulaName: String,
    binaryName: String,
    pkgDescription: String = "",
    homepage: String = "",
    javaVersion: String = "25",
    tapRepo: String = "",
    variant: String = "universal"
  )(project: Project): Project = {
    HomebrewPackaging.homebrew(
      formulaName, binaryName, pkgDescription, homepage,
      javaVersion, tapRepo, variant
    )(project)
  }

  /** Placeholder for Windows MSI packaging — not yet implemented.
    *
    * Logs a warning and returns the project unchanged. This reserves
    * the API shape for future implementation.
    */
  def windowsMsi(
    pkgName: String,
    pkgDescription: String = ""
  )(project: Project): Project = {
    println(
      s"[warn] With.Packaging.windowsMsi() is not yet implemented. " +
      s"Package '$pkgName' will not produce an MSI installer."
    )
    project
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
