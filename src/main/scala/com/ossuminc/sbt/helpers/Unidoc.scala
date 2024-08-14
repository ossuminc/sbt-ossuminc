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

import com.ossuminc.sbt.OssumIncPlugin
import sbt.*
import sbt.Keys.*
import com.ossuminc.sbt.helpers.RootProjectInfo.Keys.{gitHubOrganization, gitHubRepository, projectStartYear}
import sbtunidoc.BaseUnidocPlugin.autoImport.*
import sbtunidoc.ScalaUnidocPlugin.autoImport.*
import sbtunidoc.ScalaUnidocPlugin
import scoverage.ScoverageSbtPlugin


/** Plugin Settings For UniDoc, since it is not an AutoPlugin */
object Unidoc extends AutoPluginHelper {

  object Keys {
    val titleForDocs: SettingKey[String] = settingKey[String](
      "The name of the project as it should appear in documentation."
    )
  }

//  def akkaMappings: Map[(String, String), URL] = Map(
//    ("com.typesafe.akka", "akka-actor") -> url(s"http://doc.akka.io/api/akka/"),
//    ("com.typesafe", "config") -> url("http://typesafehub.github.io/config/latest/api/")
//  )

  def configure(project: Project): Project = {
    project.configure(this.configure())
  }

  def configure(
   apiOutput: File = file("target/unidoc"),
   baseURL: Option[String] = None,
   inclusions: Seq[ProjectReference] = Seq.empty,
   exclusions: Seq[ProjectReference] = Seq.empty,
   logoURL: Option[String] = None,
   externalMappings: Seq[Seq[String]] = Seq.empty
  )(project: Project): Project = {
    project
      .enablePlugins(OssumIncPlugin,ScalaUnidocPlugin)
      .disablePlugins(ScoverageSbtPlugin)
      .settings(
        Compile / doc / target := apiOutput,
        apiURL := baseURL.map(url),
        ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(inclusions: _*) -- inProjects(exclusions: _*),
        ScalaUnidoc / scalaVersion := (Compile / scalaVersion).value,
        ScalaUnidoc / unidoc / target := apiOutput,
        Compile / doc / scalacOptions := {
          val logo = logoURL.getOrElse(s"https://www.scala-lang.org/api/${(Compile / scalaVersion).value}/project-logo/logo_dark.svg")
          Seq(
            "-project:RIDDL API Documentation",
            s"-project-version:${version.value}",
            s"-project-logo:$logo",
            s"-project-footer:Copyright ${projectStartYear.value} ${organizationName.value}. All Rights Reserved.",
            s"-revision:main",
            "-comment-syntax:wiki",
            s"-source-links:github://${gitHubOrganization.value}/${gitHubRepository.value}/main",
            s"-siteroot:${apiOutput.toString}",
            {
              val mappings: Seq[Seq[String]] = Seq(
                Seq(".*scala", "scaladoc3", s"https://scala-lang.org/api/${scalaVersion.value}/"),
                Seq(".*java", "javadoc", "https://docs.oracle.com/javase/21/docs/api/")
              ) ++ externalMappings
              s"-external-mappings:${mappings.map(_.mkString("::")).mkString(",")}"
            }
          )
        },
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
            ("org.scala-lang", "scala-library") -> url(s"http://www.scala-lang.org/api/${scalaVersion.value}/"),
            ("org.scalatest", "scalatest") -> url(s"http://https://www.scalatest.org/scaladoc/3.2.19/")
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
