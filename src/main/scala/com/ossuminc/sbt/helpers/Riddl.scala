package com.ossuminc.sbt.helpers

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport.*
import sbt.*
import sbt.Keys.libraryDependencies

object Riddl extends AutoPluginHelper {

  val latest_version = "1.0.0-RC6"

  override def apply(project: Project) = testKit()(project)

  def library(
    version: String = latest_version,
    nonJVMDependency: Boolean = true
  )(project: Project): Project =
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

  def testKit(
    version: String = latest_version,
    nonJVMDependency: Boolean = true
  )(project: Project): Project = {
    Scalatest(project).settings(
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
