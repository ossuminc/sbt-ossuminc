package com.ossuminc.sbt.helpers
import sbt.*
import sbt.Keys.*
import scalafix.sbt.*
import scalafix.sbt.ScalafixPlugin.autoImport.*

import java.nio.file.{Files, Path}

object Scalafix extends AutoPluginHelper {

  /** The configuration function to call for this plugin helper
    *
    * @param project
    *   The project to which the configuration should be applied
    *
    * @return
    *   The same project passed as an argument, post configuration
    */
  def configure(project: Project): Project = {
    project
      .enablePlugins(ScalafixPlugin)
      .settings(
        scalafixOnCompile := Files.exists(Path.of(".scalafix.conf")),
        ThisBuild / scalafixDependencies ++= Seq[ModuleID](
          // "com.github.xuwei-k" %% "scalafix-rules" % "0.3.3"
          // ModuleID: "GROUP" %% "ARTIFACT" % "VERSION"
        ),
        ThisBuild / semanticdbEnabled := true,
        ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
      )

  }
}
