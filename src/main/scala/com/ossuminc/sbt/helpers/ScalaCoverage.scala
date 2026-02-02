package com.ossuminc.sbt.helpers

import sbt.*

/** Code coverage support using Scoverage.
  *
  * @deprecated This helper is awaiting sbt 2.0 support from sbt-scoverage plugin.
  */
@deprecated("Awaiting sbt 2.0 support from sbt-scoverage plugin", "2.0.0")
object ScalaCoverage extends AutoPluginHelper {

  object Keys {
    val coveragePercent: SettingKey[Double] =
      settingKey[Double]("The default percentage coverage to require for a project")
  }

  /** The configuration function to call for this plugin helper
    *
    * @param project
    *   The project to which the configuration should be applied
    *
    * @return
    *   The same project passed as an argument, post configuration
    */
  def apply(project: Project): Project = {
    throw new UnsupportedOperationException(
      "ScalaCoverage is not available in sbt 2.x. " +
        "The sbt-scoverage plugin does not yet support sbt 2.0. " +
        "Please wait for plugin updates."
    )
  }
}
