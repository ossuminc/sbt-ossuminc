package com.ossuminc.sbt.helpers

import sbt.Keys.libraryDependencies
import sbt.Project
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport.*

object Riddl extends AutoPluginHelper {

  override def configure(project: Project): Project =
    configure()(project)

  def configure(forJS: Boolean = false, version: String = "0.47.0-35-281f2c10")(project: Project): Project =
    project.settings(
      libraryDependencies += {
        if (forJS) {
          "com.ossuminc" %%% "riddl-diagrams-js" % version
        } else {
          "com.ossuminc" %%% "riddlc" % version
        }
      }
    )
}
