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

import sbt.*
import sbt.Keys.*

import scala.sys.process.{Process, ProcessLogger}

/** HandyAliases Added To The Build */
object HandyAliases extends AutoPluginHelper {

  object Keys {
    private[sbt] val printClasspath = TaskKey[Unit](
      "print-class-path",
      "Print the project's compilation class path."
    )

    private[sbt] val printTestClasspath = TaskKey[Unit](
      "print-test-class-path",
      "Print the project's testing class path."
    )

    private[sbt] val printRuntimeClasspath = TaskKey[Unit](
      "print-runtime-class-path",
      "Print the project's runtime class path."
    )

    private[sbt] val compileOnly =
      TaskKey[File]("compile-only", "Compile just the specified files")
  }

  def configure(project: Project): Project = {
    project
      .settings(
        Global / commands ++= Seq(shell_command),
        Global / Keys.printClasspath := { printClassPath.value },
        Global / Keys.printTestClasspath := { printTestClassPath.value },
        Global / Keys.printRuntimeClasspath := { printRuntimeClassPath.value }
      )
      .settings {
        Seq(
          addCommandAlias("!", "sh"),
          addCommandAlias("tq", "testQuick"),
          addCommandAlias("to", "testOnly"),
          addCommandAlias("copmile", "compile"),
          addCommandAlias("tset", "test"),
          addCommandAlias("TEST", "; clean ; test"),
          addCommandAlias("tc", "Test/compile"),
          addCommandAlias("ctc", "; clean ; Test/compile"),
          addCommandAlias("pcp", "printClassPath"),
          addCommandAlias("ptcp", "printTestClassPath"),
          addCommandAlias("prcp", "printRuntimeClassPath"),
          addCommandAlias(
            "cov",
            "; clean ; coverage ; test ; coverageAggregate ; reload"
          )
        ).flatten
      }
  }

  private def printAClasspath(name: String, out: File, cp: Classpath): Unit = {
    println(s"----- $name: " + out.getCanonicalPath + ": FILES:")
    println(cp.files.map(_.getCanonicalPath).mkString("\n"))
    println(s"----- $name: END")
  }

  private def printClassPath: Def.Initialize[Task[Unit]] = Def.task[Unit] {
    val out = (Compile / target).value
    val cp = (Compile / fullClasspath).value
    printAClasspath(Compile.name, out, cp)
  }

  private def printTestClassPath: Def.Initialize[Task[Unit]] = Def.task[Unit] {
    val out = (Test / target).value
    val cp = (Test / fullClasspath).value
    printAClasspath(Test.name, out, cp)
  }

  private def printRuntimeClassPath: Def.Initialize[Task[Unit]] = Def.task[Unit] {
    val out = (Runtime / target).value
    val cp = (Runtime / fullClasspath).value
    printAClasspath(Runtime.name, out, cp)
  }

  private val handyPL = new ProcessLogger {
    def out(s: => String): Unit = println(s)

    def err(s: => String): Unit = println(s)

    def buffer[T](f: => T): T = f
  }

  private def shell_command: Command = {
    Command.args("sh", "Invoke a system shell and pass arguments to it") { (state, args) =>
      val builder = Process(args)
      builder.run(handyPL)
      state
    }
  }

}
