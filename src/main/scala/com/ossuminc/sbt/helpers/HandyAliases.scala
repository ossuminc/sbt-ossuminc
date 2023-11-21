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

import sbt._
import sbt.Keys._

import scala.sys.process.Process

/** HandyAliases Added To The Build */
object HandyAliases extends AutoPluginHelper {

  object Keys {
    private[sbt] val printClasspath = TaskKey[File](
      "print-class-path",
      "Print the project's compilation class path."
    )

    private[sbt] val printTestClasspath = TaskKey[File](
      "print-test-class-path",
      "Print the project's testing class path."
    )

    private[sbt] val printRuntimeClasspath = TaskKey[File](
      "print-runtime-class-path",
      "Print the project's runtime class path."
    )

    private[sbt] val compileOnly =
      TaskKey[File]("compile-only", "Compile just the specified files")
  }

  def configure(project: Project): Project = {
    project
      .settings(
        commands ++= Seq(shell_command, bang_command),
        Keys.printClasspath := { print_class_path.value },
        Keys.printTestClasspath := { print_test_class_path.value },
        Keys.printRuntimeClasspath := { print_runtime_class_path.value },

      )
      .settings {
        Seq(
          addCommandAlias("tq", "test-quick"),
          addCommandAlias("to", "test-only"),
          addCommandAlias("cq", "compile-quick"),
          addCommandAlias("copmile", "compile"),
          addCommandAlias("tset", "test"),
          addCommandAlias("TEST", "; clean ; test"),
          addCommandAlias("tc", "test:compile"),
          addCommandAlias("ctc", "; clean ; test:compile"),
          addCommandAlias(
            "cov",
            "; clean ; coverage ; test ; coverageAggregate ; reload"
          ),
          addCommandAlias("!", "sh")
        ).flatten
    }
  }

  private def print_class_path: Def.Initialize[Task[File]] = Def.task {
    val out = target.value
    val cp = (Compile / fullClasspath).value
    println("----- Compile: " + out.getCanonicalPath + ": FILES:")
    println(cp.files.map(_.getCanonicalPath).mkString("\n"))
    println("----- END")
    out
  }

  private def print_test_class_path: Def.Initialize[Task[File]] = Def.task {
    val out = target.value
    val cp = (Test/ fullClasspath).value
    println("----- Test: " + out.getCanonicalPath + ": FILES:")
    println(cp.files.map(_.getCanonicalPath).mkString("\n"))
    println("----- END")
    out
  }

  private def print_runtime_class_path: Def.Initialize[Task[File]] = Def.task {
    val out = target.value
    val cp = (Runtime / fullClasspath).value
    println("----- Runtime: " + out.getCanonicalPath + ": FILES:")
    println(cp.files.map(_.getCanonicalPath).mkString("\n"))
    println("----- END")
    out
  }

  private def shell_command: Command = {
    Command.args("sh", "Invoke a system shell and pass arguments to it") {
      (state, args) =>
        Process(args).!; state
    }
  }

  private def bang_command: Command = {
    Command.args("!", "Invoke a system shell and pass arguments to it") {
      (state, args) =>
        Process(args).!; state
    }
  }
}
