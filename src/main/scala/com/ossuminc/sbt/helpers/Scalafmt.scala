package com.ossuminc.sbt.helpers

import org.scalafmt.sbt.ScalafmtPlugin
import sbt.{Project, _}
import sbt.Keys._

object Scalafmt extends AutoPluginHelper {

  object Keys {
    val putScalafmtConfETagsIn: SettingKey[File] =
      settingKey[File]("File path in local repo to store .scalafmt.conf.etag value")

  }
  override def autoPlugins: Seq[sbt.AutoPlugin] = {
    Seq(ScalafmtPlugin)
  }

  private val scalafmt_path: String = "/ossuminc/sbt-ossuminc/main/.scalafmt.conf"
  private val scalafmt_conf: File = file(System.getProperty("user.dir")) / ".scalafmt.conf"
  private val scalafmt_config_etag_path: File = file(System.getProperty("user.dir")) / ".scalafmt.conf.etag"
  def configure(project: Project): Project = {
    project
      .enablePlugins(ScalafmtPlugin)
      .settings(
        Keys.putScalafmtConfETagsIn := scalafmt_config_etag_path,
        update := {
          val log = streams.value.log
          updateFromPublicRepository(scalafmt_conf, Keys.putScalafmtConfETagsIn.value, scalafmt_path, log)
          update.value
        },
        cleanFiles += scalafmt_config_etag_path
      )
  }
}
