package com.ossuminc.sbt.helpers

import sbt.*

/** Laminar UI library support for Scala.js.
  *
  * @deprecated This helper is awaiting sbt 2.0 support from sbt-platform-deps plugin
  *             (required for %%% syntax).
  */
@deprecated("Awaiting sbt 2.0 support from sbt-platform-deps plugin", "2.0.0")
object Laminar extends AutoPluginHelper {

  override def apply(project: Project): Project = apply()(project)

  def apply(
    laminar_version: String = "17.2.0",
    dom_version: String = "2.8.0",
    waypoint_version: Option[String] = Some("9.0.0"),
    laminextVersion: Option[String] = None,
    laminextModules: Seq[String] = Seq.empty
  )(project: Project): Project = {
    throw new UnsupportedOperationException(
      "Laminar is not available in sbt 2.x. " +
        "The sbt-platform-deps plugin (required for %%%) does not yet support sbt 2.0. " +
        "Please wait for plugin updates."
    )
  }
}
