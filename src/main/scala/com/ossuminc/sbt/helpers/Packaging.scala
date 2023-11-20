/*
 * Copyright 2015-2017 Reactific Software LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.*
import com.typesafe.sbt.SbtNativePackager.{Universal, UniversalDocs, UniversalSrc}
import com.typesafe.sbt.packager.SettingsHelper
import com.typesafe.sbt.packager.archetypes.JavaServerAppPackaging
import com.typesafe.sbt.packager.archetypes.jar.ClasspathJarPlugin
import com.typesafe.sbt.packager.archetypes.systemloader.SystemVPlugin
import com.typesafe.sbt.packager.debian.DebianPlugin
import com.typesafe.sbt.packager.docker.DockerSpotifyClientPlugin
import com.typesafe.sbt.packager.rpm.RpmPlugin
import com.typesafe.sbt.packager.universal.UniversalPlugin
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.*
import sbtrelease.ReleasePlugin.autoImport.{ReleaseStep, releaseStepCommand, releaseStepTask}

/** Enable PublishUniversalPlugin to make universal artifacts publishable */
object Packaging extends AutoPluginHelper {

  private val zip: String = "zip"

  private[sbt] val packagingReleaseSteps: SettingKey[Seq[ReleaseStep]] =
    settingKey[Seq[ReleaseStep]](
      "internal: the release steps to package the artifacts"
    )

  val packageZip: TaskKey[File] = taskKey[File]("package-zip")

  override def projectSettings: Seq[sbt.Setting[_]] = {
    Seq[Setting[_]](packagingReleaseSteps := Seq.empty[ReleaseStep])
  }

  def sbtPlugin(project: Project): Project = {
    project
      .enablePlugins(ClasspathJarPlugin)
      .settings(
        _root_.sbt.Keys.sbtPlugin := true
      )
  }

  def universalCode(project: Project): Project = {
    project
      .settings(
        packagingReleaseSteps +=
          releaseStepTask(UniversalPlugin.autoImport.dist),
        SettingsHelper
          .makeDeploymentSettings(Universal, Universal / dist, zip) ++ SettingsHelper
          .makeDeploymentSettings(UniversalSrc, UniversalSrc / dist, zip) ++ SettingsHelper
          .makeDeploymentSettings(UniversalDocs, UniversalDocs / dist, zip) ++
          // --add the artifact so it is included in the publishing tasks
          addArtifact(
            Universal / packageZip / artifact,
            Universal / packageZip
          ),
        // FIXME: Are these dependencies needed any more?
        publish := { publish.dependsOn(Universal / dist).value },
        publishM2 := { publishM2.dependsOn(Universal / dist).value },
        publishLocal := { publishLocal.dependsOn(Universal / dist).value }
      )
  }

  def universalLibrary(project: Project): Project = {
    project
      .enablePlugins(ClasspathJarPlugin, UniversalPlugin)
      .configure(universalCode)
  }

  def universalServer(project: Project): Project = {
    project
      .enablePlugins(JavaServerAppPackaging, UniversalPlugin)
      .configure(universalCode)
      .settings(
        packageZip :=
          (baseDirectory in Compile).value / "target" / "universal" /
            s"${name.value}-${version.value}.$zip",
        artifact in (Universal, packageZip) := {
          val art = (artifact in (Universal, packageZip)).value
          art.withType(zip).withExtension(zip)
        }
      )
  }

  def linuxServer(project: Project): Project = {
    project
      .configure(universalServer)
      .enablePlugins(SystemVPlugin, RpmPlugin, DebianPlugin)
      .settings(packagingReleaseSteps += releaseStepCommand("rpm:packageBin"))
  }

  def dockerServer(project: Project): Project = {
    import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._
    project
      .configure(linuxServer)
      .enablePlugins(DockerSpotifyClientPlugin)
      .settings(
        packagingReleaseSteps ++= Seq[ReleaseStep](
          releaseStepTask(UniversalPlugin.autoImport.dist),
          releaseStepCommand("docker:publishLocal")
        ),
        // packageName in Docker := packageName.value
        version in Docker := version.value,
        dockerBaseImage := "openjdk:8-jre-alpine",
        dockerRepository := Some("dockerUser"), // ???
        dockerExposedPorts := Seq(9000),
        dockerExposedVolumes := Seq(),
        dockerLabels in Docker := Map(
          "latest" -> version.value,
          version.value -> version.value
        )
        // , defaultLinuxInstallLocation in Docker := "/app"
      )
  }
}

/*
enablePlugins(UniversalPlugin)
enablePlugins(JavaAppPackaging)
mainClass in Compile :=
Some("com.robothive.assimilation.server.RobotHiveAssimilationServer")
topLevelDirectory := None
mappings in Universal += {
val jar = (packageBin in Compile).value
jar -> ("lib/" + jar.getName)
}


enablePlugins(com.typesafe.sbt.packager.docker.DockerPlugin)

mappings in Docker := (mappings in Universal).value
packageName in Docker := packageName.value
version in Docker := version.value
maintainer in Docker := "Yoppworks Inc."
daemonUser in Docker := "root"
defaultLinuxInstallLocation in Docker := "/app"
dockerCommands := {
val destDir = (defaultLinuxInstallLocation in Docker).value
val user = (daemonUser in Docker).value
val group = (daemonGroup in Docker).value
val cmds = dockerCommands.value.map {
case Cmd("ADD", _) =>
Cmd("ADD", s"--chown=$user:$group . $destDir")
case x: CmdLike =>
x
}
println("DockerCommands:\n" + cmds.map(_.toString).mkString("\n"))
cmds
}

dockerBaseImage := "openjdk:8-jre-slim"
dockerExposedPorts := Seq[Int](8080)
dockerExposedUdpPorts := Seq.empty[Int]
dockerExposedVolumes := Seq()
dockerLabels in Docker := Map(
"latest" -> version.value,
version.value -> version.value
)
dockerRepository := Some("nexus.yoppworks.com:18082")

 */
