package com.ossuminc.sbt.helpers

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport.*
import sbt.*
import sbt.Keys.libraryDependencies

object Laminar extends AutoPluginHelper {

  override def configure(project: Project): Project = configure()(project)

  def configure(
    version: String = "17.2.0",
    dom_version: String = "2.8.0",
    waypoint_version: Option[String] = Some("9.0.0"),
    laminextVersion: Option[String] = None,
    laminextModules: Seq[String] = Seq.empty
  )(project: Project): Project =
    project.settings(
      libraryDependencies ++= {
        val waypoint: Seq[ModuleID] =
          waypoint_version.map(v => "com.raquo" %%% "waypoint" % v).toSeq
        val laminext: Seq[ModuleID] = {
          laminextModules.map { module =>
            module match {
              case "core"       => "dev.laminext" %%% "core" % version
              case "fetch"      => "dev.laminext" %%% "fetch" % version
              case "websocket"  => "dev.laminext" %%% "websocket" % version
              case "ui"         => "dev.laminext" %%% "ui" % version
              case "validation" => "dev.laminext" %%% "validation" % version
              case "util"       => "dev.laminext" %%% "util" % version
            }
          }
        }
        Seq(
          "org.scala-js" %%% "scalajs-dom" % dom_version,
          "com.raquo" %%% "laminar" % version
        ) ++ waypoint ++ laminext
      }
    )
}
