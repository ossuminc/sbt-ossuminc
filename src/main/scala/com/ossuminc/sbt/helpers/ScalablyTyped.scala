package com.ossuminc.sbt.helpers

import sbt.*

/** ScalablyTyped converter support for TypeScript bindings.
  *
  * @deprecated This helper is awaiting sbt 2.0 support from sbt-converter plugin.
  */
@deprecated("Awaiting sbt 2.0 support from sbt-converter (ScalablyTyped) plugin", "2.0.0")
object ScalablyTyped extends AutoPluginHelper {

  override def apply(project: Project): Project = withoutScalajsBundler()(project)

  def withoutScalajsBundler(
    packageJsonDir: File = file("."),
    useScalaJsDom: Boolean = false,
    allTransitives: Boolean = true,
    exceptions: List[String] = List.empty[String],
    ignore: List[String] = List.empty[String],
    outputPackage: String = "org.ossum.sauce",
    withDebugOutput: Boolean = false
  )(project: Project): Project = {
    throw new UnsupportedOperationException(
      "ScalablyTyped is not available in sbt 2.x. " +
        "The sbt-converter plugin does not yet support sbt 2.0. " +
        "Please wait for plugin updates."
    )
  }

  def withScalajsBundler(
    dependencies: Map[String, String],
    useNPM: Boolean = true,
    useScalaJsDom: Boolean = false,
    allTransitives: Boolean = true,
    exceptions: List[String] = List.empty[String],
    ignore: List[String] = List.empty[String],
    outputPackage: String = "org.ossum.sauce",
    withDebugOutput: Boolean = false
  )(project: Project): Project = {
    throw new UnsupportedOperationException(
      "ScalablyTyped is not available in sbt 2.x. " +
        "The sbt-converter plugin does not yet support sbt 2.0. " +
        "Please wait for plugin updates."
    )
  }
}
