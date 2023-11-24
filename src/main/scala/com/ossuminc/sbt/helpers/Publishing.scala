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

import com.ossuminc.sbt.helpers.ProjectInfo.Keys.{gitHubOrganization, gitHubRepository}
import com.ossuminc.sbt.helpers.Release.Keys.{publishReleasesTo, publishSnapshotsTo}
import sbt.Keys.*
import sbt.*
import xerial.sbt.Sonatype.*
import xerial.sbt.Sonatype as SonatypePlugin

import scala.xml.*

/** Settings For SonatypePublishing Plugin */
object Publishing extends AutoPluginHelper {

  object Keys {}

  /** The AutoPlugins that we depend upon */
  override def autoPlugins: Seq[AutoPlugin] = Seq(SonatypePlugin)

  def makeLicense(name: String, url: String): Elem = {
    <license>
      <name>{name}</name>
      <url>{url}</url>
      <distribution>repo</distribution>
    </license>
  }

  def publishAsMaven(project: Project): Project = {
    project
      .settings(
        publishMavenStyle := true,
        pomIncludeRepository := { _ => false },
        pomExtra := {
          val devs: Seq[scala.xml.Elem] = developers.value.map { (dev: Developer) =>
            <developer>
              <id>{dev.id}</id>
              <name>{dev.name}</name>
              <email>{dev.email}</email>
              <url>{dev.url.toString}</url>
            </developer>
          }
          val devList = <developers>{devs}</developers>
          val licList: scala.xml.Node = {
            val lics = licenses.value.map { case (nm, url) => makeLicense(nm, url.toExternalForm) }
            <licenses>{lics}</licenses>
          }
          NodeSeq.fromSeq(Seq(devList, licList))
        }
      )
  }

  val sonatypeOssSnapshots = "https://oss.sonatype.org/service/local/snapshots"
  val sonatypeOssStaging = "https://oss.sonatype.org/service/local/staging/deploy/maven2"

  def publishToSonaType(project: Project): Project = {
    project
      .enablePlugins(SonatypePlugin)
      .settings(sonatypeSettings)
      .settings(
        SonatypeKeys.sonatypeProfileName := organization.value,
        publishSnapshotsTo := MavenRepository("Sonatype OSS Snapshots", sonatypeOssSnapshots),
        publishReleasesTo := MavenRepository("Sonatype Maven Staging", sonatypeOssStaging),
        homepage := Some(
          url(s"https://github.com/${gitHubOrganization.value}/${gitHubRepository.value}")
        )
      )
      .configure(publishAsMaven)
  }

  def configure(project: Project): Project = {
    project.settings(
      publishSnapshotsTo := Resolver.defaultLocal,
      publishReleasesTo := Resolver.defaultLocal,
      Test / publishArtifact := false,
      publishTo := {
        if (isSnapshot.value) {
          Some(publishSnapshotsTo.value)
        } else {
          Some(publishReleasesTo.value)
        }
      },
      scmInfo := {
        val gitUrl =
          s"//github.com/${gitHubOrganization.value}/${normalizedName.value}"
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
