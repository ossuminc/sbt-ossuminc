package com.ossuminc.sbt

import sbt.*
import sbt.Keys.*
import sbtcrossproject.CrossPlugin.autoImport.{JVMPlatform, JVMCrossProjectOps}
import sbtcrossproject.{CrossProject, CrossType, Platform}
import scalajscrossproject.ScalaJSCrossPlugin.autoImport.{JSPlatform,JSCrossProjectOps}
import scalanativecrossproject.ScalaNativeCrossPlugin.autoImport.{NativePlatform,NativeCrossProjectOps}

/** A CrossModule is a module that can be built for JVM, Javascript or Native execution. Use it like:
  * {{{val my_project = CrossModule("my_project", Javascript + JVM + Native).configure(...).settings(...)}}}
  */
object CrossModule {
  sealed trait Target { def platform: Platform }
  case object JVMTarget extends Target { def platform: Platform = JVMPlatform }
  case object JSTarget extends Target { def platform: Platform = JSPlatform }
  case object NativeTarget extends Target { def platform: Platform = NativePlatform }

  /** Define a sub-project or module of the root project. Make sure to use the [[Root]] function before this Module is
    * defined. No configuration is applied but you can do that by using the various With.* functions in this plugin.
    * `With.typical` is typical for Scala3 development
    * @param dirName
    *   The name of the sub-directory in which the module is located.
    * @param modName
    *   The name of the artifact to be published. If blank, it will default to the dirName
    * @return
    *   The project that was created and configured.
    */
  def apply(dirName: String, modName: String = "")(targets: Target*)(
    transforms: Project => Project*
  )(settings: Def.SettingsDefinition*): CrossProject = {
    val mname = { if (modName.isEmpty) dirName else modName }
    val crossProj = CrossProject(dirName, file(dirName))(targets.map(_.platform): _*)
      .crossType(CrossType.Full)
      .withoutSuffixFor(JVMPlatform)
      .enablePlugins(OssumIncPlugin)
      .configure(helpers.Scala3.configure)
      .configure(transforms: _*)
      .settings(settings: _*)
      .settings(
        name := dirName,
        moduleName := mname,
      )
    targets.foldLeft(crossProj) { case (cp: CrossProject, target: Target) =>
      target match {
        case JVMTarget => cp.jvmSettings( moduleName := mname )
        case JSTarget  => cp.jsSettings( moduleName := mname ++ "-js" )
          .configure(helpers.Javascript.configure(hasMain=false,forProd=true))
        case NativeTarget => cp.nativeSettings(moduleName := mname ++ "-native")
      }
      }
  }
//  private def mapToProject(cp: CrossProject, target: Target): Project = {
//    target match {
//      case JVMTarget    => cp.jvm
//      case JSTarget     => cp.js
//      case NativeTarget => cp.native
//    }
//  }
}
