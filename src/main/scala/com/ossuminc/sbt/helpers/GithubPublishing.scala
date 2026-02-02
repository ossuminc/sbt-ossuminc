package com.ossuminc.sbt.helpers

import sbt.*

/** GitHub Packages publishing support.
  *
  * @deprecated This helper is awaiting sbt 2.0 support from sbt-github-packages.
  *             Use alternative publishing methods (e.g., SonatypePublishing) until
  *             the plugin is updated.
  */
@deprecated("Awaiting sbt 2.0 support from sbt-github-packages plugin", "2.0.0")
object GithubPublishing extends AutoPluginHelper {

  def apply(project: Project): Project = {
    throw new UnsupportedOperationException(
      "GithubPublishing is not available in sbt 2.x. " +
        "The sbt-github-packages plugin does not yet support sbt 2.0. " +
        "Please use an alternative publishing method or wait for plugin updates."
    )
  }
}
