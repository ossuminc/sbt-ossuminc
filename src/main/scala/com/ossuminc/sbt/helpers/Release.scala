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

import com.jsuereth.sbtpgp.PgpKeys
import com.ossuminc.sbt.helpers.Release.Keys.{
  additionalCheckSteps,
  artifactKinds,
  checkHeadersOnRelease,
  checkScalaFormattingOnRelease,
  privateNexusResolver,
  runTestsOnRelease
}
import sbt.KeyRanks.APlusTask
import sbt.Keys.publish
import sbt.*
import sbtrelease.{ReleasePlugin, Version}
import sbtrelease.ReleasePlugin.autoImport.{
  ReleaseStep,
  releaseProcess,
  releasePublishArtifactsAction,
  releaseStepCommand,
  releaseUseGlobalVersion,
  releaseVersionBump
}
import sbtrelease.ReleaseStateTransformations.{
  checkSnapshotDependencies,
  commitNextVersion,
  commitReleaseVersion,
  inquireVersions,
  publishArtifacts,
  pushChanges,
  runClean,
  setNextVersion,
  setReleaseVersion,
  tagRelease
}

object Release extends AutoPluginHelper {

  /** The AutoPlugins that we depend upon */
  override def autoPlugins: Seq[AutoPlugin] = Seq(ReleasePlugin)

  object Keys {
    sealed trait ArtifactKind

    case object ZipFileArtifact extends ArtifactKind

    case object DebianServerArtifact extends ArtifactKind

    case object RPMScalaServerArtifact extends ArtifactKind

    case object DockerServerArtifact extends ArtifactKind

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

  private def projectSettings: Seq[sbt.Setting[_]] = {
    Seq[sbt.Setting[_]](
      Keys.additionalCheckSteps := Seq.empty[ReleaseStep],
      Keys.checkScalaFormattingOnRelease := false,
      Keys.runTestsOnRelease := true,
      Keys.checkScalaFormattingOnRelease := true,
      Keys.checkHeadersOnRelease := true
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

  private val choosePublishTask = TaskKey[Unit](
    "choose-publish-signed",
    "Choose which publishing task to use",
    APlusTask
  )

  def releasePublishTask(
    isOSS: Boolean
  ): Def.Initialize[Task[Unit]] = Def.taskDyn[Unit] {
    if (isOSS) { // releasing open source
      PgpKeys.publishSigned
    } else {
      publish
    }
  }

  def configure(project: Project): Project = {
    project
      .enablePlugins(ReleasePlugin)
      .settings(projectSettings)
      .settings(
        releaseUseGlobalVersion := true,
        releaseVersionBump := Version.Bump.Bugfix,
        choosePublishTask := {
          val isOSS: Boolean = Keys.privateNexusResolver.value.isEmpty
          releasePublishTask(isOSS)
        },
        releasePublishArtifactsAction := choosePublishTask.value,
        releaseProcess := {
          initialSteps ++
            checkingSteps(
              checkHeadersOnRelease.value,
              checkScalaFormattingOnRelease.value,
              runTestsOnRelease.value,
              additionalCheckSteps.value
            ) ++
            taggingSteps(checkScalaFormattingOnRelease.value) ++
            packagingSteps(artifactKinds.value) ++
            finalSteps(privateNexusResolver.value.isEmpty)
        }
      )
  }
}
