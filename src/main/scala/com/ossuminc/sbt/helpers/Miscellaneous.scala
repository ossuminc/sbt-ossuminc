/*
 * Copyright 2015-2026 Ossum Inc.
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

import java.io.File

/** General settings for the project */
object Miscellaneous {

  /** STUB on sbt 2.x: the classpath-jar helper relied on sbt-native-packager's
    * `scriptClasspathOrdering` plus `IO.jar`, which need migration to the sbt 2
    * virtual-file APIs. Deferred with the rest of the packaging work (see
    * NOTEBOOK.md "sbt 2.0 Migration Plan", Group D). Returns the project
    * unchanged after warning, rather than silently dropping the setting.
    */
  def useClassPathJar(project: Project): Project = {
    println(
      "[warn] With.ClassPathJar is not yet available on sbt 2.x (packaging " +
        "migration pending); proceeding without a classpath jar."
    )
    project
  }

  def useUnmanagedJarLibs(project: Project): Project = {
    project
      .settings(
        Compile / unmanagedJars := Def.uncached {
          given xsbti.FileConverter = fileConverter.value
          (baseDirectory.value / "libs" ** "*.jar").classpath
        },
        Runtime / unmanagedJars := Def.uncached {
          given xsbti.FileConverter = fileConverter.value
          (baseDirectory.value / "libs" ** "*.jar").classpath
        },
        Test / unmanagedJars := Def.uncached {
          given xsbti.FileConverter = fileConverter.value
          (baseDirectory.value / "libs" ** "*.jar").classpath
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

  /** Generic over the file-reference type so it works with sbt 2 mappings,
    * whose first element is now a `HashedVirtualFileRef` rather than a `File`.
    */
  private def filter[A](ms: Seq[(A, String)]): Seq[(A, String)] = {
    ms.filter { case (_, path) =>
      path != "logback.xml" &&
      !path.startsWith("toignore") &&
      !path.startsWith("samples")
    }
  }

}
