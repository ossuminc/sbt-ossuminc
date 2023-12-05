package com.ossuminc.sbt.helpers

import sbt._
import scoverage.ScoverageKeys._
import scoverage.ScoverageSbtPlugin

object ScalaCoverage extends AutoPluginHelper {

  object Keys {
    val coveragePercent: SettingKey[Double] = settingKey[Double]("The default percentage coverage to require for a project")
  }

  /** The configuration function to call for this plugin helper
    *
    * @param project
    *   The project to which the configuration should be applied
    *
    * @return
    *   The same project passed as an argument, post configuration
    */
  def configure(project: Project): Project = {
    project
      .enablePlugins(ScoverageSbtPlugin)
      .settings(
        Keys.coveragePercent := 50,
        coverageFailOnMinimum := true,
        coverageMinimumStmtTotal := Keys.coveragePercent.value,
        coverageMinimumBranchTotal := Keys.coveragePercent.value,
        coverageMinimumStmtPerPackage := Keys.coveragePercent.value,
        coverageMinimumBranchPerPackage := Keys.coveragePercent.value,
        coverageMinimumStmtPerFile := Keys.coveragePercent.value,
        coverageMinimumBranchPerFile := Keys.coveragePercent.value,
        coverageExcludedPackages := "<empty>"
      )

  }
}
