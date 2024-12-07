package com.ossuminc.sbt

import sbt.*
import sbt.Keys.*
import sbtcrossproject.CrossPlugin.autoImport.{JVMCrossProjectOps, JVMPlatform}
import sbtcrossproject.{CrossProject, CrossType, Platform}
import scalajscrossproject.ScalaJSCrossPlugin.autoImport.*
import scalanativecrossproject.ScalaNativeCrossPlugin.autoImport.*

import scala.scalanative.sbtplugin.ScalaNativePlugin
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport.*

/** A CrossModule is a module that can be built for JVM, Javascript or Native execution. As with all modules the first
  * set of paUse it like:
  * {{{val my_project = CrossModule("dir_name", "module_name")(Javascript + JVM + Native).configure(...).settings(...)}}}
  */
object CrossModule {
  sealed trait Target { def platform: Platform }
  case object JVMTarget extends Target { def platform: Platform = JVMPlatform }
  case object JSTarget extends Target { def platform: Platform = JSPlatform }
  case object NodeTarget extends Target { def platform: Platform = NodePlatform }
  case object NativeTarget extends Target { def platform: Platform = NativePlatform }

  /** Define a subproject or module of the root project. Make sure to use the [[Root]] function before this Module is
    * defined. No configuration is applied, but you can do that by using the various With.* functions in this plugin.
    * `With.typical` is typical for Scala3 development
    * @param dirName
    *   The name of the subdirectory in which the module is located.
    * @param modName
    *   The name of the artifact to be published. If blank, it will default to the dirName
    * @return
    *   The project that was created and configured.
    */
  def apply(dirName: String, modName: String = "")(targets: Target*): CrossProject = {
    import org.scalajs.sbtplugin.ScalaJSPlugin
    val mname = { if (modName.isEmpty) dirName else modName }
    val cp2 = CrossProject(dirName, file(dirName))(targets.map(_.platform): _*)
      .crossType(CrossType.Full)
      .withoutSuffixFor(JVMPlatform)
      .enablePlugins(OssumIncPlugin)
      .settings(
        name := dirName,
        moduleName := mname
      )

    val cp3 =
      if (targets.contains(CrossModule.JVMTarget)) {
        if (targets.contains(CrossModule.JSTarget)) {
          cp2.jvmSettings(libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided")
        } else
          cp2
      } else
        cp2
    val cp4 =
      if (targets.contains(CrossModule.JSTarget)) {
        cp3
          .jsEnablePlugins(ScalaJSPlugin)
          .jsSettings(
            libraryDependencies ++= Seq(
              "io.github.cquiroz" %%% "scala-java-time" % "2.6.0",
              "org.scalactic" %%% "scalactic" % "3.2.19" % "test",
              "org.scalatest" %%% "scalatest" % "3.2.19" % "test",
              "org.scalatest" %%% "scalatest-funspec" % "3.2.19" % "test"
            )
          )
      } else cp3
    if (targets.contains(CrossModule.NativeTarget)) {
      val cp = cp4.nativeEnablePlugins(ScalaNativePlugin)
      if (targets.contains(CrossModule.JSTarget))
        cp.nativeSettings(libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided")
      else cp
    } else cp4
  }
}

case object NodePlatform extends Platform {
  def identifier: String = "node"
  def sbtSuffix: String = "NODE"
  def enable(project: Project): Project = project
}
