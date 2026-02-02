package com.ossuminc.sbt.helpers

import sbt.*

/** Scalafix automatic code rewriting support.
  *
  * @deprecated This helper is awaiting sbt 2.0 support from sbt-scalafix plugin.
  */
@deprecated("Awaiting sbt 2.0 support from sbt-scalafix plugin", "2.0.0")
object Scalafix extends AutoPluginHelper {

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
      "Scalafix is not available in sbt 2.x. " +
        "The sbt-scalafix plugin does not yet support sbt 2.0. " +
        "Please wait for plugin updates."
    )
  }
}
