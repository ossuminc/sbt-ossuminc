package com.ossuminc.sbt.helpers

import sbt.*

/** Scala Native compilation support.
  *
  * @deprecated This helper is awaiting sbt 2.0 support from sbt-scala-native
  *             and sbt-platform-deps plugins.
  */
@deprecated("Awaiting sbt 2.0 support from sbt-scala-native plugin", "2.0.0")
object Native extends AutoPluginHelper {

  /** Default version for scalatest dependencies */
  val defaultScalatestVersion = "3.2.19"

  def apply(project: Project): Project = apply()(project)

  /** Configure Scala Native compilation - NOT AVAILABLE in sbt 2.x
    *
    * @param mode Build mode: "debug", "fast", "full", "size", "release"
    * @param buildTarget Build target: "application", "dynamic", "static"
    * @param gc Garbage collector: "none", "immix", "commix", "boehm"
    * @param lto Link-time optimization: "none", "thin", "full"
    * @param debugLog Enable debug logging
    * @param verbose Enable verbose compilation output
    * @param targetTriple Optional target triple for cross-compilation
    * @param linkOptions Additional linker options
    * @param scalatestVersion Version of scalatest to include for testing
    */
  def apply(
    mode: String = "fast",
    buildTarget: String = "static",
    gc: String = "none",
    lto: String = "none",
    debugLog: Boolean = false,
    verbose: Boolean = false,
    targetTriple: Option[String] = None,
    linkOptions: Seq[String] = Seq("-I/usr/include"),
    scalatestVersion: String = defaultScalatestVersion
  )(project: Project): Project = {
    throw new UnsupportedOperationException(
      "Native is not available in sbt 2.x. " +
        "The sbt-scala-native plugin does not yet support sbt 2.0. " +
        "Please wait for plugin updates."
    )
  }
}
