package com.ossuminc.sbt.helpers

import com.github.sbt.git.GitPlugin
import sbt.*

object Git extends AutoPluginHelper {

  def configure(project: Project): Project = {
    project.enablePlugins(GitPlugin)
  }
}
