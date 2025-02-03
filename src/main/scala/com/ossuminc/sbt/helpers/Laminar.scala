package com.ossuminc.sbt.helpers

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport.*
import sbt.*
import sbt.Keys.libraryDependencies

object Laminar extends AutoPluginHelper {

  override def configure(project: Project): Project = apply()(project)

  def apply(
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
        val v_laminext = laminextVersion.getOrElse("0.17.1")
        val org = v_laminext match {
          case s: String if s.startsWith("0.") =>
            val strs = s.split(".")
            val minor = strs(1).toInt
            if (minor < 17) "io.laminext"
            else if (minor >= 18) "dev.laminext"
            else {
              val patch = strs(2).toInt
              if (patch >= 1) "dev.laminext"
              else "io.laminext"
            }
          case _: String => "dev.laminext"
        }
        val laminext: Seq[ModuleID] = {
          laminextModules.map {
            case "core"       => org %%% "core" % v_laminext
            case "fetch"      => org %%% "fetch" % v_laminext
            case "websocket"  => org %%% "websocket" % v_laminext
            case "ui"         => org %%% "ui" % v_laminext
            case "validation" => org %%% "validation" % v_laminext
            case "util"       => org %%% "util" % v_laminext
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
