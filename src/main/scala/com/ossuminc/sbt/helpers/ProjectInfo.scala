/*
 * Copyright 2015-2023 Ossum Inc. & Ossum Inc.
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

import com.typesafe.sbt.packager.Keys.maintainer
import sbt.*
import sbt.Keys.*
import sbt.plugins.MiniDependencyTreePlugin

object ProjectInfo extends AutoPluginHelper {

  override def autoPlugins: Seq[AutoPlugin] = Seq(sbtbuildinfo.BuildInfoPlugin)

  object Keys {
    val projectHomePage: SettingKey[URL] = settingKey[URL](
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
  }

  def configure(project: Project): Project = {
    project
      .enablePlugins(MiniDependencyTreePlugin)
      .settings(
        ThisBuild / organization := "com.ossuminc",
        ThisBuild / organizationHomepage := Some(url("https://com.ossuminc/")),
        ThisBuild / organizationName := "Ossum Inc.",
        ThisBuild / versionScheme := Option("early-semver"),
        ThisBuild / licenses := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt")),
        ThisBuild / homepage := Some(url("https://github.com/ossuminc/" + normalizedName.value)),
        ThisBuild / developers := List(
          Developer(
            "reid-spencer",
            "Reid Spencer",
            "",
            url("https://github.com/reid-spencer")
          )
        ),
        ThisBuild / maintainer := "reid@ossuminc.com",
        ThisBuild / Keys.copyrightHolder := "Ossum Inc.",
        ThisBuild / Keys.gitHubOrganization := "ossuminc",
        Keys.gitHubRepository := name.value,
        Keys.projectHomePage := url(
          s"https://github.com/${Keys.gitHubOrganization.value}/${Keys.gitHubRepository.value}"
        ),
        ThisBuild / Keys.projectStartYear := 2023,
        ThisBuild / Keys.gitHubOrganization := "ossuminc",
        ThisBuild / Keys.gitHubRepository := "unspecified",
        baseDirectory := thisProject.value.base,
        target := baseDirectory.value / "target",
        logLevel := Level.Info,
        Test / fork := false,
        Test / logBuffered := false
      )
  }
}
