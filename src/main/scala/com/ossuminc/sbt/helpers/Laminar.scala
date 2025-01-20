package com.ossuminc.sbt.helpers

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport.*
import sbt.*
import sbt.Keys.libraryDependencies

object Laminar extends AutoPluginHelper {

  override def configure(project: Project): Project = With()(project)

  def With(
    laminar_version: String = "17.2.0",
    dom_version: String = "2.8.0",
    waypoint_version: Option[String] = Some("9.0.0"),
    laminextVersion: Option[String] = None,
    laminextModules: Seq[String] = Seq.empty
  )(project: Project): Project = {
    project.settings(
      libraryDependencies ++= {
        val waypoint: Seq[ModuleID] =
          waypoint_version.map(v => "com.raquo" %%% "waypoint" % v).toSeq
        val v_laminex = laminextVersion.getOrElse("0.17.0")
        val org = v_laminex match {
          case s: String if s.startsWith("0.") =>
            val strs = s.split(".")
            val min = strs(1).toInt
            if (min < 17) "io.laminext"
            else if (min > 18) "dev.laminext"
            else if (strs(2).toInt >= 1) "dev.laminext"
            else "io.laminext"
          case _: String => "dev.laminext"
        }
        val laminext: Seq[ModuleID] = {
          laminextModules.map {
            case "core"       => org %%% "core" % v_laminex
            case "fetch"      => org %%% "fetch" % v_laminex
            case "websocket"  => org %%% "websocket" % v_laminex
            case "ui"         => org %%% "ui" % v_laminex
            case "validation" => org %%% "validation" % v_laminex
            case "util"       => org %%% "util" % v_laminex
          }
        }
        Seq(
          "org.scala-js" %%% "scalajs-dom" % dom_version,
          "com.raquo" %%% "laminar" % laminar_version
        ) ++ waypoint ++ laminext
      }
    )
  }
}
