package com.ossuminc.sbt.helpers

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport.*
import sbt.*
import sbt.Keys.libraryDependencies

object Laminar extends AutoPluginHelper {

  override def configure(project: Project): Project = configure()(project)

  def configure(
    version: String = "17.1.0",
    dom_version: String = "2.8.0",
    waypoint_version: Option[String] = None
  )(project: Project): Project =
    project.settings(
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % dom_version,
        "com.raquo" %%% "laminar" % version
      ) ++ waypoint_version.map(v => "com.raquo" %%% "waypoint" % v)
    )
}
