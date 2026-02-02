package com.ossuminc.sbt

import sbt.*

/** A CrossModule is a module that can be built for JVM, ScalaJS or Native execution.
  *
  * @deprecated This feature is awaiting sbt 2.0 support from sbt-scalajs-crossproject,
  *             sbt-scala-native-crossproject, sbt-scalajs, and sbt-scala-native plugins.
  */
@deprecated("Awaiting sbt 2.0 support from sbt-scalajs-crossproject plugin", "2.0.0")
object CrossModule {
  sealed trait Target
  case object JVMTarget extends Target
  case object JSTarget extends Target
  case object NativeTarget extends Target

  // Type aliases for backward compatibility
  val JVM: Target = JVMTarget
  val JS: Target = JSTarget
  val Native: Target = NativeTarget

  /** Define a cross-platform subproject - NOT AVAILABLE in sbt 2.x
    *
    * @param dirName
    *   The name of the subdirectory in which the module is located.
    * @param modName
    *   The name of the artifact to be published. If blank, it will default to the dirName
    * @return
    *   The project that was created and configured.
    */
  def apply(dirName: String, modName: String = "")(targets: Target*): Nothing = {
    throw new UnsupportedOperationException(
      "CrossModule is not available in sbt 2.x. " +
        "The sbt-scalajs-crossproject and sbt-scala-native-crossproject plugins do not yet support sbt 2.0. " +
        "Please use Module for JVM-only builds or wait for plugin updates."
    )
  }
}
