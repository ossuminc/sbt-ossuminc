package com.ossuminc.sbt

import com.ossuminc.sbt.OssumIncPlugin.autoImport.With
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import sbt.*
import sbt.Keys.*
import sbt.ScriptedPlugin.autoImport.{scriptedBufferLog, scriptedLaunchOpts}
import sbt.plugins.SbtPlugin

/** The object for configuration a sbt autoplugin */
object Plugin {

  /** Define a sub-project that produces an sbt plugin. It is necessary to also have used the
    * [[Root]] function because what that function sets up is necessary for publishing this module.
    * scoverage doesn't work with sbt plugins so it is disabled.
    * @param dirName
    *   The name of the directory in which the plugin code and tests exist
    * @param modName
    *   The name of the published artifact. If blank, the `dirName` will be used
    * @return
    *   The configured sbt project that is ready to build an sbt plugin
    */
  def apply(dirName: String, modName: String = ""): Project = {
    Project
      .apply(dirName, file(dirName))
      .enablePlugins(OssumIncPlugin, SbtPlugin, JavaAppPackaging)
      // Note: ScoverageSbtPlugin not available in sbt 2.x
      .configure(With.basic, With.Scala3, helpers.Scalafmt)
      .settings(
        name := dirName,
        moduleName := { if (modName.isEmpty) dirName else modName },
        scalaVersion := "3.7.1",
        sbtPlugin := true,
        scriptedLaunchOpts := {
          scriptedLaunchOpts.value ++ Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
        },
        scriptedBufferLog := false
      )
  }
}
