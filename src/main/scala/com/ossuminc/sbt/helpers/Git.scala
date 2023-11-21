package com.ossuminc.sbt.helpers

import com.github.sbt.git.GitPlugin
import sbt.*

object Git extends AutoPluginHelper {

  override def autoPlugins: Seq[AutoPlugin] = Seq(GitPlugin)

  def configure(project: Project): Project = {
    project.enablePlugins(GitPlugin)
  }
}
