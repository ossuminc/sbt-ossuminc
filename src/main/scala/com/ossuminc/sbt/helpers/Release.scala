/*
 * Copyright 2015-2017 Ossum Inc.
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

import sbt.Keys.baseDirectory
import sbt.*
import sbtrelease.{ReleasePlugin, Vcs, Version}
import sbtrelease.ReleasePlugin.autoImport.*
import sbtrelease.ReleaseStateTransformations.*

object Release extends AutoPluginHelper {

  /** THe Keys and types pertaining to releasing artifacts */
  object Keys {
    sealed trait ArtifactKind

    case object ZipFileArtifact extends ArtifactKind

    case object DebianServerArtifact extends ArtifactKind

    case object RPMScalaServerArtifact extends ArtifactKind

    case object DockerServerArtifact extends ArtifactKind

    case object WindowsArtifact extends ArtifactKind

    val artifactKinds: SettingKey[Seq[ArtifactKind]] =
      settingKey[Seq[ArtifactKind]](
        "The kinds of artifacts the project should produce. " +
          "Defaults to just to ZipFileArtifact"
      )

    val additionalCheckSteps: SettingKey[Seq[ReleaseStep]] =
      settingKey[Seq[ReleaseStep]](
        "Additional steps to add to the releasing process"
      )

    val privateNexusResolver: SettingKey[Option[Resolver]] =
      settingKey[Option[Resolver]](
        "Set to the (probably internal) resolver you want to search " +
          "first. Defaults to None but if set to Some[Resolver], it will" +
          "replace the Maven Central resolver so needs to include that if " +
          "needed. Also becomes the destination of the release, otherwise" +
          "Sonatype OSS Nexus for releasing to Maven Central "
      )

    val publishSnapshotsTo: SettingKey[Resolver] = settingKey[Resolver](
      "The Sonatype Repository to which snapshot versions are " +
        "published. Defaults to the OSS Sonatype Repository for snapshots."
    )

    val publishReleasesTo: SettingKey[Resolver] = settingKey[Resolver](
      "The Sonatype Repository to which release versions are " +
        "published. Defaults to the Sonatype OSS staging repository. To " +
        "publish releases, you must configure your account and password into " +
        "the SBT credentials file: ~/.sbt/1.0/sonatype.sbt"
    )

    val checkHeadersOnRelease: SettingKey[Boolean] =
      settingKey[Boolean]("Cause headerCheck to be run when releasing. Default is true")

    val runTestsOnRelease: SettingKey[Boolean] = settingKey[Boolean](
      "Cause tests to be run when releasing. Default is true"
    )

    val checkScalaFormattingOnRelease: SettingKey[Boolean] = settingKey[Boolean](
      "Reformat scala source coe with Scalafmt as part of releasing. " +
        "Default is false"
    )
  }

  private def defaultSettings: Seq[sbt.Setting[_]] = {
    Seq[sbt.Setting[_]](
      Keys.artifactKinds := Seq(Keys.ZipFileArtifact),
      Keys.privateNexusResolver := None,
      Keys.additionalCheckSteps := Seq.empty[ReleaseStep],
      Keys.checkScalaFormattingOnRelease := false,
      Keys.runTestsOnRelease := true,
      Keys.checkScalaFormattingOnRelease := true,
      Keys.checkHeadersOnRelease := true,
      releaseVcs := Vcs.detect((ThisBuild / baseDirectory).value),
      releaseUseGlobalVersion := true,
      releaseVersionBump := Version.Bump.Bugfix
    )
  }

  def initialSteps: Seq[ReleaseStep] =
    Seq[ReleaseStep](checkSnapshotDependencies, inquireVersions, runClean)

  def checkingSteps(
    checkHeaders: Boolean,
    checkScalastyle: Boolean,
    checkTests: Boolean,
    additionalCheckSteps: Seq[ReleaseStep]
  ): Seq[ReleaseStep] = {
    additionalCheckSteps ++ {
      if (checkHeaders) {
        Seq[ReleaseStep](releaseStepCommand("headerCheck"))
      } else {
        Seq.empty[ReleaseStep]
      }
    } ++ {
      if (checkScalastyle) {
        Seq[ReleaseStep](releaseStepCommand("scalastyle"))
      } else {
        Seq.empty[ReleaseStep]
      }
    } ++ {
      if (checkTests) {
        Seq[ReleaseStep](releaseStepCommand("test"))
      } else {
        Seq.empty[ReleaseStep]
      }
    }
  }

  def taggingSteps(runScalafmt: Boolean): Seq[ReleaseStep] = {
    {
      if (runScalafmt) {
        Seq[ReleaseStep](releaseStepCommand("scalafmt"))
      } else {
        Seq.empty[ReleaseStep]
      }
    } ++
      Seq[ReleaseStep](
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease
      )
  }

  def packagingSteps(artifactKinds: Seq[Keys.ArtifactKind]): Seq[ReleaseStep] = {
    artifactKinds.map[ReleaseStep, Seq[ReleaseStep]] {
      case Keys.ZipFileArtifact ⇒
        releaseStepCommand("Universal:packageBin")
      case Keys.DebianServerArtifact ⇒
        releaseStepCommand("deb:packageBin")
      case Keys.RPMScalaServerArtifact ⇒
        releaseStepCommand("rpm:packageBin")
      case Keys.DockerServerArtifact ⇒
        releaseStepCommand("docker:stage")
      case Keys.WindowsArtifact =>
        releaseStepCommand("Windows:")
    }
  }

  def finalSteps(releaseOSS: Boolean): Seq[ReleaseStep] = {
    Seq[ReleaseStep](
      publishArtifacts,
      setNextVersion,
      commitNextVersion
    ) ++ {
      if (releaseOSS) {
        Seq[ReleaseStep](releaseStepCommand("sonatypeReleaseAll"))
      } else {
        Seq.empty[ReleaseStep]
      }
    } :+ pushChanges
  }

  def configure(project: Project): Project =
    project
      .enablePlugins(ReleasePlugin)
      .settings(defaultSettings)
      .settings(
        releaseProcess := {
          initialSteps ++
            checkingSteps(
              Keys.checkHeadersOnRelease.value,
              Keys.checkScalaFormattingOnRelease.value,
              Keys.runTestsOnRelease.value,
              Keys.additionalCheckSteps.value
            ) ++
            taggingSteps(Keys.checkScalaFormattingOnRelease.value) ++
            packagingSteps(Keys.artifactKinds.value) ++
            finalSteps(Keys.privateNexusResolver.value.isEmpty)
        }
      )
}
