package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.libraryDependencies
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport.*

object Laminar extends AutoPluginHelper {

  override def configure(project: Project): Project = configure()(project)

  def configure(
    version: String = "17.1.0",
    dom_version: String = "2.8.0"
  )(project: Project): Project =
    project.settings(
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % dom_version,
        "com.raquo" %%% "laminar" % version
      )
    )
}
