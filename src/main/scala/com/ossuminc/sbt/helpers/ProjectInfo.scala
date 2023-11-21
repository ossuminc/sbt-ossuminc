/*
 * Copyright 2015-2023 Reactific Software LLC & Ossum Inc.
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
import sbtbuildinfo.BuildInfoKeys.*
import sbtbuildinfo.BuildInfoOption.{BuildTime, ToJson, ToMap}
import sbtbuildinfo.*

import java.util.Calendar
import java.net.URI

object ProjectInfo extends AutoPluginHelper {

  override def autoPlugins: Seq[AutoPlugin] = Seq(sbtbuildinfo.BuildInfoPlugin)

  object Keys {
    val projectHomePage : SettingKey[URL] = settingKey[URL](
      "The url of the project's home page"
    )

    val projectStartYear: SettingKey[Int] = settingKey[Int](
      "The year in which the project was initiated with code"
    )

    val gitHubOrganization: SettingKey[String] = settingKey[String](
      "The github organization corresponding to the entity owning copyright to the code"
    )

    val gitHubRepository: SettingKey[String] = settingKey[String](
      "The name of teh gitHub repository this project is stored in"
    )

    val copyrightHolder: SettingKey[String] = settingKey[String](
      "The name of the business entity or person that holds the copyright"
    )

    val codePackage: SettingKey[String] = settingKey[String](
      "The main, top level Scala package name that contains the project's code"
    )

    val buildInfoPackage: SettingKey[String] = settingKey[String](
      "The name of the scala package in which the build info object should be created"
    )

    val buildInfoObjectName: SettingKey[String] = settingKey[String](
      "The name of the build info scala object that should be created"
    )

  }

  def configure(project: Project): Project = {
    project
      .enablePlugins(BuildInfoPlugin)
      .settings(
        ThisBuild / organization := "com.ossuminc",
        ThisBuild / organizationHomepage := Some(URI.create("https://com.ossuminc/").toURL),
        ThisBuild / organizationName := "Ossum Inc.",
        ThisBuild / versionScheme := Option("early-semver"),
        ThisBuild / licenses += ("Apache-2.0" -> URI.create("https://www.apache.org/licenses/LICENSE-2.0.txt").toURL),
        ThisBuild / homepage := Some(URI.create("https://github.com/ossuminc/" + normalizedName.value).toURL),
        ThisBuild / developers := List(Developer(
          "reid-spencer", "Reid Spencer", "", url("https://github.com/reid-spencer"))
        ),
        ThisBuild / Keys.copyrightHolder := "Ossum Inc.",
        ThisBuild / Keys.gitHubOrganization := "ossuminc",
        Keys.codePackage := "com.ossuminc",
        Keys.buildInfoObjectName := "BuildInfo",
        Keys.buildInfoPackage := "com.ossuminc",
        Keys.gitHubRepository := name.value,
        Keys.projectHomePage := url(s"https://github.com/${Keys.gitHubOrganization.value}/${Keys.gitHubRepository.value}"),
        baseDirectory := thisProject.value.base,
        target := baseDirectory.value / "target",
        logLevel := Level.Info,
        Test / fork := false,
        Test / logBuffered := false,
        buildInfoObject := Keys.buildInfoObjectName.value,
        buildInfoPackage := Keys.buildInfoPackage.value,
        buildInfoOptions := Seq(ToMap, ToJson, BuildTime),
        buildInfoUsePackageAsPath := true,
        buildInfoKeys ++= Seq[BuildInfoKey](
          name,
          normalizedName,
          description,
          version,
          organization,
          organizationName,
          BuildInfoKey.map(organizationHomepage) { case (k, v) =>
            k -> v.get.toString
          },
          BuildInfoKey.map(homepage) { case (_, v) =>
            "projectHomepage" -> v.map(_.toString).getOrElse(Keys.projectHomePage.value)
          },
          BuildInfoKey.map(licenses) { case (k, v) =>
            k -> v.map(_._1).mkString(", ")
          },
          apiURL,
          isSnapshot,
          buildInfoPackage,
          BuildInfoKey.map(startYear) { case (k, v) =>
            k -> v.map(_.toString).getOrElse(Keys.projectStartYear.toString)
          },
          BuildInfoKey.map(startYear) { case (_, v) =>
            "copyright" -> s"© ${v.map(_.toString).getOrElse(Keys.projectStartYear.toString)}-${
              Calendar
                .getInstance()
                .get(Calendar.YEAR)
            }} ${organizationName.value}"
          },
          scalaVersion,
          sbtVersion,
          BuildInfoKey.map(scalaVersion) { case (_, v) =>
            val version = if (v.head == '2') {
              v.substring(0, v.lastIndexOf('.'))
            } else v
            "scalaCompatVersion" -> version
          }
        )
      )
  }
}
