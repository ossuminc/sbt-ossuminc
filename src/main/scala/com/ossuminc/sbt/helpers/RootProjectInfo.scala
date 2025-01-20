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

import com.ossuminc.sbt.helpers.Miscellaneous.buildShellPrompt
import sbt.*
import sbt.Keys.*
import sbt.plugins.MiniDependencyTreePlugin
import java.util.Calendar

object RootProjectInfo {

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
    
    val spdxLicense: SettingKey[String] = settingKey[String](
      "The spdx license to apply to the whole project"
    )
  }

  private val defaultDevs = List(
    Developer(
      "reid-spencer",
      "Reid Spencer",
      "",
      url("https://github.com/reid-spencer")
    )
  )

  def initialize(
    ghRepoName: String,
    ghOrgName: String = "ossuminc",
    startYr: Int = Calendar.getInstance().get(Calendar.YEAR),
    orgPackage: String = "com.ossuminc",
    orgName: String = "Ossum, Inc.",
    orgPage: URL = url("https://ossuminc.com/"),
    devs: List[Developer] = defaultDevs,
    spdxLicenseName: String = "Apache-2.0"
  )(project: Project): Project = {
    project
      .enablePlugins(MiniDependencyTreePlugin)
      .settings(
        ThisBuild / Keys.copyrightHolder := orgName,
        ThisBuild / Keys.gitHubOrganization := ghOrgName,
        ThisBuild / Keys.gitHubRepository := ghRepoName,
        ThisBuild / Keys.projectHomePage := url(
          s"https://github.com/${Keys.gitHubOrganization.value}/${Keys.gitHubRepository.value}"
        ),
        ThisBuild / Keys.projectStartYear := startYr,
        ThisBuild / versionScheme := Option("early-semver"),
        ThisBuild / organization := orgPackage,
        ThisBuild / organizationName := orgName,
        ThisBuild / organizationHomepage := Some(orgPage),
        ThisBuild / RootProjectInfo.Keys.spdxLicense := spdxLicenseName,
        ThisBuild / licenses := {
          spdxLicenseName match {
            case l @ "Apache-2.0"    => Seq(l -> url("https://opensource.org/license/apache-2-0"))
            case l @ "CDDL-1.0"      => Seq(l -> url("https://opensource.org/license/cddl-1-0"))
            case l @ "EPL-2.0"       => Seq(l -> url("https://opensource.org/license/epl-2-0"))
            case l @ "GPL-2.0"       => Seq(l -> url("https://opensource.org/license/gpl-2-0"))
            case l @ "GPL-3.0"       => Seq(l -> url("https://opensource.org/license/gpl-3-0"))
            case l @ "GPL-3.0-only"  => Seq(l -> url("https://opensource.org/license/gpl-3-0"))
            case l @ "AGPL-3.0-only" => Seq(l -> url("https://opensource.org/license/agpl-v3"))
            case l @ "LGPL-2.1"      => Seq(l -> url("https://opensource.org/license/lgpl-2-1"))
            case l @ "LGPL-3.0-only" => Seq(l -> url("https://opensource.org/license/lgpl-3-0"))
            case l @ "LGPL-2.0-only" => Seq(l -> url("https://opensource.org/license/lgpl-2-0"))
            case l @ "MPL-2.0"       => Seq(l -> url("https://opensource.org/license/mpl-2-0"))
            case l @ "BSD-2-Clause"  => Seq(l -> url("https://opensource.org/license/bsd-2-clause"))
            case l @ "BSD-3-Clause"  => Seq(l -> url("https://opensource.org/license/bsd-3-clause"))
            case l @ "MIT"           => Seq(l -> url("https://opensource.org/license/mit"))
            case s: String           => Seq(s -> url("https://opensource.org/license/unlicense"))
          }
        },
        ThisBuild / homepage := Some(Keys.projectHomePage.value),
        ThisBuild / developers := { if (devs.isEmpty) defaultDevs else devs },
        ThisBuild / logLevel := Level.Info,
        ThisBuild / Test / fork := false,
        ThisBuild / Test / logBuffered := false,
        Global / shellPrompt := buildShellPrompt.value,
        name := ghRepoName
      )
  }
}
