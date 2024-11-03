package com.ossuminc.sbt.helpers

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport.*
import sbt.*
import sbt.Keys.libraryDependencies

object Riddl extends AutoPluginHelper {

  override def configure(project: Project): Project =
    configure()(project)

  def configure(version: String = "0.54.0", nonJVMDependency: Boolean = true)(project: Project): Project =
    project.settings(
      libraryDependencies ++= {
        if (nonJVMDependency) {
          Seq(
            "com.ossuminc" %%% "riddl-lib" % version
          )
        } else {
          Seq(
            "com.ossuminc" %% "riddl-lib" % version
          )
        }
      }
    )

  def testKit(version: String = "0.54.0", nonJVMDependency: Boolean = true)(project: Project): Project = {
    ScalaTest.configure(project)
    project.settings(
      libraryDependencies ++= {
        if (nonJVMDependency) {
          Seq(
            "com.ossuminc" %%% "riddl-testkit" % version % Test
          )
        } else {
          Seq(
            "com.ossuminc" %% "riddl-testkit" % version % Test
          )
        }
      }
    )
  }
}
