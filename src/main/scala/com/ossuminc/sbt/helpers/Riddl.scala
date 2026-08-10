package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.libraryDependencies

object Riddl extends AutoPluginHelper {

  /** Default riddl version for [[library]] and [[testKit]].
    *
    * **Move this with each riddl release.** It has gone stale twice and both times the symptom was
    * a resolve failure with nothing pointing at the cause: `1.0.0-RC6` outlived its artifacts and
    * reddened CI for three weeks in July 2026, and `1.29.0` sat here while riddl reached 1.31.0.
    *
    * Verified published as `riddl-lib_3` and `riddl-testkit_3` on GitHub Packages (2026-08-10) --
    * check with:
    * {{{
    * gh api /orgs/ossuminc/packages/maven/com.ossuminc.riddl-lib_3/versions
    * }}}
    *
    * Deliberately NOT 2.0.0-rc.N: riddl 2.0 has no final release yet, and a default that tracks a
    * prerelease is stale again within the week. Update this to 2.0.0 when riddl 2.0 ships.
    */
  val latest_version = "1.31.0"

  override def apply(project: Project) = testKit()(project)

  // `%%` is correct on every platform here: under sbt 2 the platform suffix
  // comes from the projectMatrix row, not from the operator, so there is
  // nothing for a caller to select between. These methods used to take a
  // `nonJVMDependency: Boolean` (a leftover from the sbt 1.x `%%%` era) whose
  // two branches were byte-identical -- a knob that could not change the
  // outcome, but invited flipping it to "fix" a resolution problem. Removed in
  // 3.2.0; do not reintroduce it.

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
