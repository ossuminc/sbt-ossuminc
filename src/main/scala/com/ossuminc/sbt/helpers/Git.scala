package com.ossuminc.sbt.helpers

import com.typesafe.sbt.GitPlugin
import sbt._

object Git extends AutoPluginHelper {

  override def autoPlugins: Seq[AutoPlugin] = Seq(GitPlugin)

  def configure(project: Project): Project = {
    project
  }
}
