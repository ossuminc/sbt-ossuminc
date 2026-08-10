package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.libraryDependencies

object Riddl extends AutoPluginHelper {

  val latest_version = "1.29.0"

  override def apply(project: Project) = testKit()(project)

  // `%%` is correct on every platform here: under sbt 2 the platform suffix
  // comes from the projectMatrix row, not from the operator, so there is
  // nothing for a caller to select between. These methods used to take a
  // `nonJVMDependency: Boolean` (a leftover from the sbt 1.x `%%%` era) whose
  // two branches were byte-identical -- a knob that could not change the
  // outcome, but invited flipping it to "fix" a resolution problem. Removed in
  // 3.1.0; do not reintroduce it.

  def library(version: String = latest_version)(project: Project): Project =
    project.settings(
      libraryDependencies ++= Seq(
        "com.ossuminc" %% "riddl-lib" % version
      )
    )

  def testKit(version: String = latest_version)(project: Project): Project = {
    Scalatest(project).settings(
      libraryDependencies ++= Seq(
        "com.ossuminc" %% "riddl-testkit" % version % Test
      )
    )
  }
}
