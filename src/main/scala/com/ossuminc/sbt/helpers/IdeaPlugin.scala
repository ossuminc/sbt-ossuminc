package com.ossuminc.sbt.helpers

import sbt.*

/** IntelliJ IDEA plugin development support.
  *
  * @deprecated This helper is awaiting sbt 2.0 support from sbt-idea-plugin.
  */
@deprecated("Awaiting sbt 2.0 support from sbt-idea-plugin", "2.0.0")
object IdeaPlugin extends AutoPluginHelper {

  def apply(project: Project): Project = apply()(project)

  /** Configure IntelliJ IDEA plugin development - NOT AVAILABLE in sbt 2.x
    *
    * @param name Plugin name
    * @param description Plugin description
    * @param changes Change notes
    * @param build IntelliJ build number (e.g., "243.21565.193" for IntelliJ 2024.3)
    * @param platform Platform type ("Community" or "Ultimate")
    * @param dependsOnPlugins Plugin IDs that this plugin depends on
    * @param maxMem Maximum memory for IDEA instance in MB
    * @return Configured project
    */
  def apply(
    name: String = "foo",
    description: String = "My cool IDEA plugin",
    changes: String = "",
    build: String = "243.21565.193",
    platform: String = "Community",
    dependsOnPlugins: Seq[String] = Seq.empty,
    maxMem: Int = 2048
  )(project: Project): Project = {
    throw new UnsupportedOperationException(
      "IdeaPlugin is not available in sbt 2.x. " +
        "The sbt-idea-plugin does not yet support sbt 2.0. " +
        "Please wait for plugin updates."
    )
  }
}
