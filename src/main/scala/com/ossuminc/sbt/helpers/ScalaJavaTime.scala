package com.ossuminc.sbt.helpers

import sbt.*
import sbt.Keys.libraryDependencies

/** Helper to add scala-java-time dependency for cross-platform java.time API.
  *
  * This is particularly useful for Scala.js and Scala Native projects that need
  * java.time support, since the JDK's java.time package is not available on
  * these platforms natively.
  *
  * @see https://github.com/cquiroz/scala-java-time
  *
  * @note In sbt 2.x, cross-platform (nonJVM=true) is not available until
  *       sbt-platform-deps supports sbt 2.0. Use nonJVM=false for JVM-only.
  */
object ScalaJavaTime extends AutoPluginHelper {

  val defaultVersion = "2.6.0"

  override def apply(project: Project): Project = apply()(project)

  /** Add scala-java-time dependency
    *
    * @param version The version of scala-java-time to use
    * @param nonJVM If true, uses %%% for cross-platform (NOT AVAILABLE in sbt 2.x).
    *               If false, uses %% for JVM-only.
    */
  def apply(
    version: String = defaultVersion,
    nonJVM: Boolean = false
  )(project: Project): Project = {
    if (nonJVM) {
      throw new UnsupportedOperationException(
        "ScalaJavaTime with nonJVM=true is not available in sbt 2.x. " +
          "The sbt-platform-deps plugin (required for %%%) does not yet support sbt 2.0. " +
          "Use nonJVM=false for JVM-only builds."
      )
    }
    project.settings(
      libraryDependencies += "io.github.cquiroz" %% "scala-java-time" % version
    )
  }
}
