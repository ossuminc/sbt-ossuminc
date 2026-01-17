package com.ossuminc.sbt.helpers

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport.*
import sbt.*
import sbt.Keys.libraryDependencies

/** Helper to add scala-java-time dependency for cross-platform java.time API.
  *
  * This is particularly useful for Scala.js and Scala Native projects that need
  * java.time support, since the JDK's java.time package is not available on
  * these platforms natively.
  *
  * @see https://github.com/cquiroz/scala-java-time
  */
object ScalaJavaTime extends AutoPluginHelper {

  val defaultVersion = "2.6.0"

  override def apply(project: Project): Project = apply()(project)

  /** Add scala-java-time dependency
    *
    * @param version The version of scala-java-time to use
    * @param nonJVM If true, uses %%% for cross-platform. If false, uses %% for
    *               JVM-only.
    */
  def apply(
    version: String = defaultVersion,
    nonJVM: Boolean = true
  )(project: Project): Project = {
    project.settings(
      libraryDependencies += {
        if (nonJVM) "io.github.cquiroz" %%% "scala-java-time" % version
        else "io.github.cquiroz" %% "scala-java-time" % version
      }
    )
  }
}
