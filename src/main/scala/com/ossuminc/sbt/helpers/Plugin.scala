package com.ossuminc.sbt.helpers

import com.ossuminc.sbt.OssumIncPlugin.autoImport.With
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import sbt.*
import sbt.Keys.*
import sbt.ScriptedPlugin.autoImport.{scriptedBufferLog, scriptedLaunchOpts}
import sbt.plugins.SbtPlugin
import scoverage.ScoverageSbtPlugin
import com.ossuminc.sbt.helpers

object Plugin {

  def configure(project: Project): Project = {
    project
      .enablePlugins(SbtPlugin, JavaAppPackaging)
      .disablePlugins(ScoverageSbtPlugin)
      .configure(With.basic, With.scala2, helpers.Scalafmt.configure, With.SonatypePublishing)
      .settings(
        scalaVersion := "2.12.19",
        sbtPlugin := true,
        scriptedLaunchOpts := {
          scriptedLaunchOpts.value ++ Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
        },
        scriptedBufferLog := false
      )
  }
}
