package com.ossuminc.sbt.helpers

import org.jetbrains.sbtidea.SbtIdeaPlugin
import sbt.*
import org.jetbrains.sbtidea.SbtIdeaPlugin.autoImport.*


object IdeaPlugin extends AutoPluginHelper {

  def configure(project: Project): Project = apply()(project)

  /** Configure IntelliJ IDEA plugin development
    *
    * @param name Plugin name
    * @param description Plugin description
    * @param changes Change notes
    * @param build IntelliJ build number (e.g., "243.22562", "251.23774" for specific builds, or "243.*" for any 2024.3 build)
    * @param platform Platform type ("Community" or "Ultimate")
    * @param maxMem Maximum memory for IDEA instance in MB
    * @return Configured project
    */
  def apply(
    name: String = "foo",
    description: String = "My cool IDEA plugin",
    changes: String = "",
    build: String = "243.21565.193",  // IntelliJ IDEA 2024.3 - stable public release
    platform: String = "Community",
    maxMem: Int = 2048
  )(project: Project): Project = {
    project
      .enablePlugins(SbtIdeaPlugin)
      .settings(
        intellijPluginName := name,
        ThisBuild / intellijBuild := build,
        intellijPlatform := {
          platform match {
            case s: String if s == "Community" || s == "community" => IntelliJPlatform.IdeaCommunity
            case s: String if s == "Ultimate" || s == "ultimate" => IntelliJPlatform.IdeaUltimate
            case _ => throw new IllegalArgumentException(s"Unknown platform: $platform")
          }
        },
        // Set up a default patch for plugin.xml
        patchPluginXml := pluginXmlOptions { xml =>
          xml.version           = sbt.Keys.version.value
          xml.pluginDescription = description
          xml.changeNotes       = changes
          xml.sinceBuild        = (ThisBuild / intellijBuild).value
          xml.untilBuild        = "351.*"
        },
        ThisBuild / intellijVMOptions := intellijVMOptions.value.copy(xmx = maxMem, xms = 256),
        ThisBuild / pluginVerifierOptions := pluginVerifierOptions.value.copy(
          offline = true,           // forbid the verifier from reaching the internet
        ),
    )
  }
}
