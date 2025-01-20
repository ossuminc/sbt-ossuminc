package com.ossuminc.sbt.helpers
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport.*
import sbt.*
import sbt.Keys.*

import scala.scalanative.build.*
import scala.scalanative.sbtplugin.ScalaNativePlugin
import scala.scalanative.sbtplugin.ScalaNativePlugin.autoImport.*

object Native extends AutoPluginHelper {

  /** The configuration function to call for this plugin helper to add support for Scala Native
    *
    * @param project
    *   The project to which the configuration should be applied
    *
    * @return
    *   The same project passed as an argument, post configuration
    */
  def configure(project: Project): Project = apply()(project)

  def apply(
    mode: String = "fast",
    buildTarget: String = "static",
    gc: String = "none",
    lto: String = "none",
    debugLog: Boolean = false,
    verbose: Boolean = false,
    targetTriple: Option[String] = None,
    linkOptions: Seq[String] = Seq("-I/usr/include")
  )(project: Project): Project = {
    project
      .enablePlugins(ScalaNativePlugin)
      .settings(
        // set to Debug for compilation details (Info is default)
        logLevel := {
          if (debugLog) { Level.Debug }
          else { Level.Info }
        },
        // defaults set with common options shown
        Compile / nativeConfig ~= { c =>
          {
            val snMode: Mode =
              mode match {
                case s: String if s == "debug"   => Mode.debug
                case s: String if s == "fast"    => Mode.releaseFast
                case s: String if s == "full"    => Mode.releaseFull
                case s: String if s == "size"    => Mode.releaseSize
                case s: String if s == "release" => Mode.release
                case _: String                   => Mode.default
              }
            val snLTO =
              lto match {
                case s: String if s == "full" => LTO.full
                case s: String if s == "thin" => LTO.thin
                case s: String if s == "none" => LTO.none
                case _: String                => LTO.default
              }
            val compileOptions = if (verbose) { Seq("-v") }
            else { Seq.empty }
            val bTarget = buildTarget match {
              case "application" => BuildTarget.application
              case "dynamic"     => BuildTarget.libraryDynamic
              case "static"      => BuildTarget.libraryStatic
              case _             => BuildTarget.libraryStatic
            }
            val d = c
              .withLTO(snLTO)
              .withMode(snMode)
              .withGC(GC(gc))
              .withBuildTarget(bTarget)
              .withTargetTriple(targetTriple)
              .withCompileOptions(c.compileOptions ++ compileOptions)
              .withLinkingOptions(c.linkingOptions ++ linkOptions)
              .withEmbedResources(true)
            if (snMode == Mode.debug)
              d.withSourceLevelDebuggingConfig(
                _.enableAll
              ) // enable generation of debug information
                .withOptimize(false) // disable Scala Native optimizer
            else
              d.withOptimize(true) // enable Scala Native optimizer
          }
        },
        Test / nativeConfig ~= { c => c.withBuildTarget(BuildTarget.application) },
        concurrentRestrictions += Tags.limit(NativeTags.Link, 1),
        libraryDependencies ++= Seq(
          "org.scalactic" %%% "scalactic" % "3.2.19" % Test,
          "org.scalatest" %%% "scalatest" % "3.2.19" % Test
        )
      )
  }
}
