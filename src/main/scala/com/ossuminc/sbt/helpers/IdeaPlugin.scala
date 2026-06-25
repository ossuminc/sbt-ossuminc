package com.ossuminc.sbt.helpers

import sbt.*

/** STUB: IntelliJ IDEA plugin support is unavailable on sbt 2.x.
  *
  * The JetBrains `sbt-idea-plugin` has not published an sbt 2.0 build yet
  * (track https://youtrack.jetbrains.com/issue/SCL-23480). Until it does,
  * `With.IdeaPlugin` cannot configure IDEA plugin builds. Use the sbt 1.x line
  * of sbt-ossuminc (v1.4.x) for IntelliJ plugin projects until the plugin is
  * made sbt 2.0 compatible.
  */
object IdeaPlugin extends AutoPluginHelper {

  private def unavailable: Nothing = sys.error(
    "With.IdeaPlugin is unavailable on sbt 2.x: the JetBrains sbt-idea-plugin " +
      "has no sbt 2.0 release yet (track SCL-23480). Use sbt-ossuminc v1.4.x on " +
      "sbt 1.x for IntelliJ IDEA plugin builds until the plugin is updated for " +
      "sbt 2.0."
  )

  /** Unavailable on sbt 2.x — see object documentation. */
  def apply(project: Project): Project = unavailable

  /** Unavailable on sbt 2.x — see object documentation.
    *
    * Signature preserved so consumer builds still type-check; invoking it fails
    * fast with an explanatory message rather than silently doing nothing.
    */
  def apply(
    name: String = "foo",
    description: String = "My cool IDEA plugin",
    changes: String = "",
    build: String = "243.21565.193",
    platform: String = "Community",
    dependsOnPlugins: Seq[String] = Seq.empty,
    maxMem: Int = 2048
  )(project: Project): Project = unavailable
}
