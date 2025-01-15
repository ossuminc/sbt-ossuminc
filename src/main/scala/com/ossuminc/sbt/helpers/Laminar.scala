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
    laminextModules: Seq[String] = Seq.empty,
    laminextVersion: Option[String] = None,
  )(project: Project): Project =
    project.settings(
      libraryDependencies ++= {
        val waypoint: Seq[ModuleID] =
          waypoint_version.map(v => "com.raquo" %%% "waypoint" % v).toSeq
        val v_laminex = laminextVersion.getOrElse("0.17.1")
        val laminext: Seq[ModuleID] = {
          laminextModules.map {
            case "core" => "dev.laminext" %%% "core" % v_laminex
            case "fetch" => "dev.laminext" %%% "fetch" % v_laminex
            case "websocket" => "dev.laminext" %%% "websocket" % v_laminex
            case "ui" => "dev.laminext" %%% "ui" % v_laminex
            case "validation" => "dev.laminext" %%% "validation" % v_laminex
            case "util" => "dev.laminext" %%% "util" % v_laminex
          }
        }
        Seq(
          "org.scala-js" %%% "scalajs-dom" % dom_version,
          "com.raquo" %%% "laminar" % v_laminex
        ) ++ waypoint ++ laminext
      }
    )
}
