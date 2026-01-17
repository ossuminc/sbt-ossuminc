package com.ossuminc.sbt.helpers

import com.github.sbt.git.GitPlugin
import sbt._

object Git extends AutoPluginHelper {

  def apply(project: Project): Project = {
    project.enablePlugins(GitPlugin)
  }
}
