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

import sbt.{SettingKey, Project, settingKey, URL, url, Attributed, Compile}
import sbt.Keys._
import java.io.File
import sbtunidoc.ScalaUnidocPlugin

/** Plugin Settings For UniDoc, since it is not an AutoPlugin */
object Unidoc extends AutoPluginHelper {

  object Keys {
    val titleForDocs: SettingKey[String] = settingKey[String](
      "The name of the project as it should appear in documentation."
    )
  }

  def akkaMappings: Map[(String, String), URL] = Map(
    ("com.typesafe.akka", "akka-actor") -> url(s"http://doc.akka.io/api/akka/"),
    ("com.typesafe", "config") -> url("http://typesafehub.github.io/config/latest/api/")
  )

  def configure(project: Project): Project = {
    project
      .enablePlugins(ScalaUnidocPlugin)
      .settings(
        apiURL := Some(
          url(
            s"https://github.com/${RootProjectInfo.Keys.gitHubOrganization.value}/${normalizedName.value}/api/"
          )
        ),
        autoAPIMappings := true,
        apiMappings ++= {
          val cp: Seq[Attributed[File]] = (Compile / fullClasspath).value
          def findManagedDependency(
            organization: String,
            name: String
          ): Option[File] = {
            (for {
              entry <- cp
              module <- entry.get(moduleID.key)
              if module.organization == organization
              if module.name.startsWith(name)
              jarFile = entry.data
            } yield jarFile).headOption
          }

          val knownApiMappings: Map[(String, String), URL] = Map(
            ("org.scala-lang", "scala-library") -> url(
              s"http://www.scala-lang.org/api/${scalaVersion.value}/"
            )
          )

          for {
            ((org, lib), url) <- knownApiMappings
            dep = findManagedDependency(org, lib) if dep.isDefined
          } yield {
            dep.get -> url
          }
        }
      )
  }
}
