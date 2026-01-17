package com.ossuminc.sbt.helpers

import org.jetbrains.sbtidea.SbtIdeaPlugin
import sbt.*
import sbt.Keys.*
import org.jetbrains.sbtidea.SbtIdeaPlugin.autoImport.*
import org.jetbrains.sbtidea.verifier.FailureLevel

object IdeaPlugin extends AutoPluginHelper {

  def apply(project: Project): Project = apply()(project)

  /** Configure IntelliJ IDEA plugin development
    *
    * @param name Plugin name
    * @param description Plugin description
    * @param changes Change notes
    * @param build IntelliJ build number (e.g., "243.21565.193" for IntelliJ 2024.3)
    * @param platform Platform type ("Community" or "Ultimate")
    * @param dependsOnPlugins Plugin IDs that this plugin depends on
    * @param maxMem Maximum memory for IDEA instance in MB
    * @return Configured project
    */
  def apply(
    name: String = "foo",
    description: String = "My cool IDEA plugin",
    changes: String = "",
    build: String = "243.21565.193",  // IntelliJ IDEA 2024.3 - stable public release
    platform: String = "Community",
    dependsOnPlugins: Seq[String] = Seq.empty,
    maxMem: Int = 2048
  )(project: Project): Project = {
    project
      .enablePlugins(SbtIdeaPlugin)
      .settings(
        Compile / javacOptions ++= Seq("--release", "17"),
        Compile / scalacOptions ++= Seq("--release", "17"),
        libraryDependencies ++= Seq(
          "com.eclipsesource.minimal-json" % "minimal-json" % "0.9.5"
        ),
        Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
        Test / unmanagedResourceDirectories += baseDirectory.value / "testResources",
        ThisBuild / intellijPluginName := name,
        ThisBuild / intellijBuild := build,
        ThisBuild / intellijPlatform := {
          platform match {
            case s: String if s == "Community" || s == "community" => IntelliJPlatform.IdeaCommunity
            case s: String if s == "Ultimate" || s == "ultimate"   => IntelliJPlatform.IdeaUltimate
            case _ => throw new IllegalArgumentException(s"Unknown platform: $platform")
          }
        },
        // Add dependent plugins
        intellijPlugins ++= dependsOnPlugins.map(_.toPlugin),
        // Set up a default patch for plugin.xml
        ThisBuild / patchPluginXml := pluginXmlOptions { xml =>
          xml.version = sbt.Keys.version.value
          xml.pluginDescription = description
          xml.changeNotes = changes
          xml.sinceBuild = (ThisBuild / intellijBuild).value
          xml.untilBuild = "351.*"
        },
        ThisBuild / intellijVMOptions := intellijVMOptions.value.copy(xmx = maxMem, xms = 256),
        ThisBuild / pluginVerifierOptions := pluginVerifierOptions.value.copy(
          offline = true, // forbid the verifier from reaching the internet
          failureLevels = Set(
            FailureLevel.COMPATIBILITY_PROBLEMS,
            FailureLevel.INVALID_PLUGIN,
            FailureLevel.MISSING_DEPENDENCIES,
            FailureLevel.PLUGIN_STRUCTURE_WARNINGS
          )
        )
      )
  }
}
