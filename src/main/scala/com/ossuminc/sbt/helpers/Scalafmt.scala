package com.ossuminc.sbt.helpers

import org.scalafmt.sbt.ScalafmtPlugin
import sbt.{Project, _}
import sbt.Keys._

import scala.concurrent.duration._

object Scalafmt extends AutoPluginHelper {

  override def autoPlugins: Seq[sbt.AutoPlugin] = {
    Seq(ScalafmtPlugin)
  }

  val scalafmt_path: String = "/ossuminc/sbt-ossuminc/public/main/.scalafmt.conf"
  val scalafmt_conf: String = ".scalafmt.conf"

  def apply(project: Project): Project = {
    project
      .enablePlugins(ScalafmtPlugin)
      .settings(
        update := {
          val log = streams.value.log
          val localFile: File = baseDirectory.value / scalafmt_conf
          updateFromPublicRepository(localFile, scalafmt_path, 1.day, log)
          update.value
        }
      )
  }
}
