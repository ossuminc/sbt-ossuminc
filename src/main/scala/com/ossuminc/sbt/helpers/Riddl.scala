package com.ossuminc.sbt.helpers

import sbt.Keys.libraryDependencies
import sbt.Project
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport.*

object Riddl extends AutoPluginHelper {

  override def configure(project: Project): Project =
    configure()(project)

  def configure(forJS: Boolean = false, version: String = "0.48.1")(project: Project): Project =
    project.settings(
      libraryDependencies += {
        if (forJS) {
          "com.ossuminc" %%% "riddl-diagrams-js" % version
        } else {
          "com.ossuminc" %%% "riddl-commands" % version
        }
      }
    )
}
