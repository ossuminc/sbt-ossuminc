package com.ossuminc.sbt.helpers
import sbt.Keys._
import sbt.Project

/** Helper for sbt-crossproject plugin */
object Cross extends AutoPluginHelper {
  import sbtcrossproject.CrossPlugin.autoImport._
  import scalajscrossproject.ScalaJSCrossPlugin.autoImport._
  import scalanativecrossproject.ScalaNativeCrossPlugin.autoImport._

  /** The configuration function to call for this plugin helper
    *
    * @param project
    * The project to which the configuration should be applied
    *
    * @return
    * The same project passed as an argument, post configuration
    */
  def configure(project: Project): Project = configure()(project)

  def configure(withNative: Boolean = true, withJS: Boolean = true, withJVM: Boolean = true)(
    project: Project
  ): Project = {
    val platforms = {
      (if (withJVM) Seq(JVMPlatform) else Seq.empty) ++
        (if (withJS) Seq(JSPlatform) else Seq.empty) ++
        (if (withNative) Seq(NativePlatform) else Seq.empty)
    }
    crossProject(platforms:_*)
      .withoutSuffixFor(JVMPlatform)
      .crossType(CrossType.Full)
      .settings(
        scalaVersion := "3.3.1"
      )
  }
}
