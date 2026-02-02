package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.libraryDependencies

/** RIDDL library and testkit support.
  *
  * @note In sbt 2.x, cross-platform (nonJVMDependency=true) is not available until
  *       sbt-platform-deps supports sbt 2.0. Use nonJVMDependency=false for JVM-only.
  */
object Riddl extends AutoPluginHelper {

  val latest_version = "1.2.0"

  override def apply(project: Project): Project = testKit()(project)

  def library(
    version: String = latest_version,
    nonJVMDependency: Boolean = false
  )(project: Project): Project = {
    if (nonJVMDependency) {
      throw new UnsupportedOperationException(
        "Riddl.library with nonJVMDependency=true is not available in sbt 2.x. " +
          "The sbt-platform-deps plugin (required for %%%) does not yet support sbt 2.0. " +
          "Use nonJVMDependency=false for JVM-only builds."
      )
    }
    project.settings(
      libraryDependencies += "com.ossuminc" %% "riddl-lib" % version
    )
  }

  def testKit(
    version: String = latest_version,
    nonJVMDependency: Boolean = false
  )(project: Project): Project = {
    if (nonJVMDependency) {
      throw new UnsupportedOperationException(
        "Riddl.testKit with nonJVMDependency=true is not available in sbt 2.x. " +
          "The sbt-platform-deps plugin (required for %%%) does not yet support sbt 2.0. " +
          "Use nonJVMDependency=false for JVM-only builds."
      )
    }
    Scalatest(project).settings(
      libraryDependencies += "com.ossuminc" %% "riddl-testkit" % version % Test
    )
  }
}
