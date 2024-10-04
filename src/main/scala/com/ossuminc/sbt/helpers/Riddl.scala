package com.ossuminc.sbt.helpers

import sbt.Keys.libraryDependencies
import sbt.*
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport.*

object Riddl extends AutoPluginHelper {

  override def configure(project: Project): Project =
    configure()(project)

  def configure(version: String = "0.52.1", nonJVMDependency: Boolean = true)(project: Project): Project =
    project.settings(
      libraryDependencies += {
        if (nonJVMDependency) {
          "com.ossuminc" %%% "riddl-diagrams" % version
        } else {
          "com.ossuminc" %% "riddl-commands" % version
        }
      }
    )
}
