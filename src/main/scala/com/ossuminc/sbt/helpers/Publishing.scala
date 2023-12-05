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

import sbt.Keys.*
import sbt.*
import xerial.sbt.Sonatype as SonatypePlugin
import xerial.sbt.Sonatype.autoImport.{sonatypeProfileName, sonatypeSessionName}
//import scala.xml.*

import com.ossuminc.sbt.helpers.Release.Keys.{publishReleasesTo, publishSnapshotsTo}
import com.ossuminc.sbt.helpers.RootProjectInfo.Keys._

/** Settings For SonatypePublishing Plugin */
object Publishing extends AutoPluginHelper {

  object Keys {
    val sonatypeServer: SettingKey[String] = settingKey[String](
      "The host name of the sonatype server to publish artifacts to. Defaults to s01.oss.sonatype.org"
    )
  }

  private val defaultSonatypeServer = "s01.oss.sonatype.org"

  def configure(project: Project): Project = {
    project
      .enablePlugins(SonatypePlugin)
      .settings(
        sonatypeProfileName := "ossum",
        Keys.sonatypeServer := defaultSonatypeServer,
        sonatypeSessionName := organization.value + "/" + name.value,
        publishSnapshotsTo := {
          val sonatypeOssSnapshots = s"https://${Keys.sonatypeServer}/content/repositories/snapshots"
          MavenRepository("Sonatype OSS Snapshots", sonatypeOssSnapshots)
        },
        publishReleasesTo := {
          val sonatypeOssStaging = s"https://${Keys.sonatypeServer}/service/local/staging/deploy/maven2"
          MavenRepository("Sonatype Maven Release Staging", sonatypeOssStaging)
        },
        homepage := Some(
          url(s"https://github.com/${gitHubOrganization.value}/${gitHubRepository.value}")
        ),
        publishMavenStyle := true,
        pomIncludeRepository := { _ => false },
        publishTo := {
          if (isSnapshot.value) {
            Some(publishSnapshotsTo.value)
          } else {
            Some(publishReleasesTo.value)
          }
        },
        Test / publishArtifact := false,
        scmInfo := {
          val gitUrl =
            s"//github.com/${gitHubOrganization.value}/${gitHubRepository.value}"
          Some(
            ScmInfo(
              url("https:" + gitUrl),
              "scm:git:" + gitUrl + ".git",
              Some("https:" + gitUrl)
            )
          )
        }
      )
  }
}
