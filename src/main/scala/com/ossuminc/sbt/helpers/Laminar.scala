package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.libraryDependencies
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport.*

object Laminar extends AutoPluginHelper {

  override def configure(project: Project): Project =
    project.settings(
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % "2.8.0",
        "com.raquo" %%% "laminar" % "17.0.0"
      )
    )
}
