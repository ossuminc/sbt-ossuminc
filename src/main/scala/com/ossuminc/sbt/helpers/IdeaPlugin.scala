package com.ossuminc.sbt.helpers

import org.jetbrains.sbtidea.SbtIdeaPlugin
import sbt.*
import org.jetbrains.sbtidea.SbtIdeaPlugin.autoImport.*

object IdeaPlugin extends AutoPluginHelper {

  def configure(project: Project): Project = apply()(project)

  def apply(
    name: String = "foo",
    description: String = "My cool IDEA plugin",
    changes: String = "",
    build: String = "2024.3.2.2",
    platform: String = "Community",
    dependsOnPlugins: Seq[String] = Seq("modules.platform"),
    maxMem: Int = 2048
  )(project: Project): Project = {
    project
      .enablePlugins(SbtIdeaPlugin)
      .settings(
        ThisBuild / intellijPluginName := name,
        ThisBuild / intellijBuild := build,
        ThisBuild / intellijPlatform := {
          platform match {
            case s: String if s == "Community" || s == "community" => IntelliJPlatform.IdeaCommunity
            case s: String if s == "Ultimate" || s == "ultimate"   => IntelliJPlatform.IdeaUltimate
            case _ => throw new IllegalArgumentException(s"Unknown platform: $platform")
          }
        },
        // Add dependent plugins bundled with IDEA to the set required
        intellijPlugins ++= dependsOnPlugins.map(x => s"com.intellij.$x").map(_.toPlugin),
        ThisBuild / patchPluginXml := pluginXmlOptions { xml =>
          xml.version = sbt.Keys.version.value
          xml.pluginDescription = description
          xml.changeNotes = changes
          xml.sinceBuild = intellijBuild.value
          xml.untilBuild = "351.*"
        },
        ThisBuild / intellijVMOptions := intellijVMOptions.value.copy(xmx = maxMem, xms = 256),
        ThisBuild / pluginVerifierOptions := pluginVerifierOptions.value.copy(
          offline = true // forbid the verifier from reaching the internet
        )
      )
  }
}
