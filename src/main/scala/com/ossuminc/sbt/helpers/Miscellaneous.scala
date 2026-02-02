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
import sbt.Keys.*
import sbt.{IO, *}
import xsbti.HashedVirtualFileRef

import java.io.File

/** General settings for the project */
object Miscellaneous {

  def useClassPathJar(project: Project): Project = {
    project.settings(
      Seq[Def.SettingsDefinition](
        scriptClasspath := {
          import com.typesafe.sbt.SbtNativePackager.*
          import com.typesafe.sbt.packager.Keys.*
          Miscellaneous.makeClasspathJar(
            scriptClasspathOrdering
              .map(Miscellaneous.makeRelativeClasspathNames)
              .value,
            (Universal / target).value
          )
        }
        // Note: mappings type changed in sbt 2.x from (File, String) to (HashedVirtualFileRef, String)
        // This functionality needs updating for sbt 2.x
      )*
    )
  }

  /** Use unmanaged JAR libraries from a libs/ directory.
    *
    * Note: In sbt 2.x, Classpath uses HashedVirtualFileRef instead of File.
    * This helper needs the FileConverter to convert Files to HashedVirtualFileRef.
    */
  def useUnmanagedJarLibs(project: Project): Project = {
    project
      .settings(
        Compile / unmanagedJars := Def.uncached {
          val fc = fileConverter.value
          val base = baseDirectory.value
          val jars = (base / "libs" ** "*.jar").get()
          jars.map(f => Attributed.blank(fc.toVirtualFile(f.toPath)))
        },
        Runtime / unmanagedJars := Def.uncached {
          val fc = fileConverter.value
          val base = baseDirectory.value
          val jars = (base / "libs" ** "*.jar").get()
          jars.map(f => Attributed.blank(fc.toVirtualFile(f.toPath)))
        },
        Test / unmanagedJars := Def.uncached {
          val fc = fileConverter.value
          val base = baseDirectory.value
          val jars = (base / "libs" ** "*.jar").get()
          jars.map(f => Attributed.blank(fc.toVirtualFile(f.toPath)))
        },
        Compile / packageBin / mappings ~= filter,
        Compile / packageSrc / mappings ~= filter,
        Compile / packageDoc / mappings ~= filter
      )
  }

  def buildShellPrompt: Def.Initialize[State => String] = {
    Def.setting { (state: State) =>
      val id = Project.extract(state).currentProject.id
      s"${name.value}($id) : $currBranch : ${version.value}>"
    }
  }

  private def currBranch: String = {
    import com.github.sbt.git.JGit
    val jgit = JGit(new File("."))
    jgit.branch
  }

  private def filter(ms: Seq[(HashedVirtualFileRef, String)]): Seq[(HashedVirtualFileRef, String)] = {
    ms.filter { case (_, path) =>
      path != "logback.xml" &&
      !path.startsWith("toignore") &&
      !path.startsWith("samples")
    }
  }

  private val classpath_jar = "classpath.jar"

  private def makeRelativeClasspathNames(
    mappings: Seq[(HashedVirtualFileRef, String)]
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

}
