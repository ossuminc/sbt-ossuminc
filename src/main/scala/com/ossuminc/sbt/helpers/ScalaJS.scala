package com.ossuminc.sbt.helpers

import sbt.*

/** Scala.js compilation support.
  *
  * @deprecated This helper is awaiting sbt 2.0 support from sbt-scalajs
  *             and sbt-platform-deps plugins.
  */
@deprecated("Awaiting sbt 2.0 support from sbt-scalajs plugin", "2.0.0")
object ScalaJS extends AutoPluginHelper {

  /** Default version for scala-java-time dependency */
  val defaultScalaJavaTimeVersion = "2.6.0"

  /** Default version for scalatest dependencies */
  val defaultScalatestVersion = "3.2.19"

  override def apply(project: Project): Project = apply()(project)

  /** Configure Scala.js compilation - NOT AVAILABLE in sbt 2.x
    *
    * @param header JS header comment for output files
    * @param hasMain Whether the project has a main method
    * @param forProd Whether to optimize for production
    * @param withCommonJSModule Whether to use CommonJS modules (vs ES modules)
    * @param scalaJavaTimeVersion Version of scala-java-time to include
    * @param scalatestVersion Version of scalatest to include for testing
    */
  def apply(
    header: String = "no header",
    hasMain: Boolean = false,
    forProd: Boolean = true,
    withCommonJSModule: Boolean = false,
    scalaJavaTimeVersion: String = defaultScalaJavaTimeVersion,
    scalatestVersion: String = defaultScalatestVersion
  )(
    project: Project
  ): Project = {
    throw new UnsupportedOperationException(
      "ScalaJS is not available in sbt 2.x. " +
        "The sbt-scalajs plugin does not yet support sbt 2.0. " +
        "Please wait for plugin updates."
    )
  }
}
