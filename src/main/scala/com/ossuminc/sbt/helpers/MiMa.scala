package com.ossuminc.sbt.helpers

import sbt.*

/** Binary compatibility checking with MiMa (Migration Manager).
  *
  * @deprecated This helper is awaiting sbt 2.0 support from sbt-mima-plugin and sbt-tasty-mima.
  */
@deprecated("Awaiting sbt 2.0 support from sbt-mima-plugin", "2.0.0")
object MiMa extends AutoPluginHelper {

  override def apply(project: Project): Project = without(project)

  /** Mark a project as not processed by MiMa */
  def without(project: Project): Project = {
    // In sbt 2.x without the plugin, just return the project unchanged
    project
  }

  /** Configure MiMa to operate on this project - NOT AVAILABLE in sbt 2.x
    * @param previousVersion
    *   The mimaPreviousArtifacts setting that MiMa requires will be set to
    *   {{{organization %% moduleName % previousVersion}}}
    * @param excludedClasses
    *   A list of classes to be excluded from Binary Issue checks.
    * @param reportSignatureIssues
    *   When set to `true`, this turns on the `mimaReportSignatureProblems` setting.
    */
  def apply(
    previousVersion: String,
    excludedClasses: Seq[String] = Seq.empty,
    reportSignatureIssues: Boolean = false
  )(project: Project): Project = {
    throw new UnsupportedOperationException(
      "MiMa is not available in sbt 2.x. " +
        "The sbt-mima-plugin does not yet support sbt 2.0. " +
        "Please wait for plugin updates."
    )
  }
}
