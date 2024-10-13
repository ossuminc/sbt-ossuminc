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

import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import sbt.*
import sbt.Keys.*

/** Compiler Settings Needed */
object Java extends AutoPluginHelper {

  val java_compile_options: Seq[String] = Seq[String](
    "-g",
    "-deprecation",
    "-encoding", "UTF-8",
    "-Xlint",
    "-Xmaxerrs", "50",
    "-Xmaxwarns", "50",
    "-Xprefer:source"
  )

  def configure(project: Project): Project = {
    project
      .enablePlugins(JavaAppPackaging)
      .settings(
        test / javaOptions  ++= Seq("-Xmx512m"),
        javacOptions ++= java_compile_options
      )
  }
}
