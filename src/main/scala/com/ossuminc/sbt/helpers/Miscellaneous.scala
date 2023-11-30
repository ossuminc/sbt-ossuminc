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

import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.Keys.scriptClasspath
import sbt.Keys._
import sbt.{IO, _}

import java.io.File

/** General settings for the project */
object Miscellaneous extends AutoPluginHelper {

  def configure(project: Project): Project = { project }

  private def currBranch: String = {
    import com.github.sbt.git.JGit
    val jgit = JGit(new File("."))
    jgit.branch
  }

  def buildShellPrompt: Def.Initialize[State => String] = {
    Def.setting { (state: State) =>
      val id = Project.extract(state).currentProject.id
      s"${name.value}($id) : $currBranch : ${version.value}>"
    }
  }

  private def filter(ms: Seq[(File, String)]): Seq[(File, String)] = {
    ms.filter { case (_, path) =>
      path != "logback.xml" &&
      !path.startsWith("toignore") &&
      !path.startsWith("samples")
    }
  }

  def useUnmanagedJarLibs(project: Project): Project = {
    project
      .settings(
        Compile / unmanagedJars := {
          baseDirectory.map { base =>
            (base / "libs" ** "*.jar").classpath
          }.value
        },
        Runtime / unmanagedJars := {
          baseDirectory.map { base =>
            (base / "libs" ** "*.jar").classpath
          }.value
        },
        Test / unmanagedJars := {
          baseDirectory.map { base =>
            (base / "libs" ** "*.jar").classpath
          }.value
        },
        Compile / packageBin / mappings ~= filter,
        Compile / packageSrc / mappings ~= filter,
        Compile / packageDoc / mappings ~= filter
      )
  }

  private val classpath_jar = "classpath.jar"

  private def makeRelativeClasspathNames(
    mappings: Seq[(File, String)]
  ): Seq[String] = {
    for {
      (_, name) <- mappings
    } yield {
      // Here we want the name relative to the lib/ folder...
      // For now we just cheat...
      if (name.startsWith("lib/")) name.drop(4) else "../" + name
    }
  }

  private def makeClasspathJar(
    classPath: Seq[String],
    target: File
  ): Seq[String] = {
    val manifest = new java.util.jar.Manifest()
    manifest.getMainAttributes
      .putValue("Class-Path", classPath.mkString(" "))
    val classpathJar = target / "lib" / classpath_jar
    IO.jar(Seq.empty, classpathJar, manifest, None)
    Seq(classpath_jar)
  }

  def useClassPathJar(project: Project): Project = {
    project.settings(
      Seq[Def.SettingsDefinition](
        scriptClasspath := {
          import com.typesafe.sbt.SbtNativePackager._
          import com.typesafe.sbt.packager.Keys._
          Miscellaneous.makeClasspathJar(
            scriptClasspathOrdering
              .map(Miscellaneous.makeRelativeClasspathNames)
              .value,
            (Universal / target).value
          )
        },
        Universal / mappings += {
          (Universal / target).value / "lib" / "classpath.jar" -> "lib/classpath.jar"
        }
      ): _*
    )
  }
}
