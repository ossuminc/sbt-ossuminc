package com.ossuminc.sbt.helpers
import sbt.*
import sbt.Keys.*

import scala.scalanative.sbtplugin.ScalaNativePlugin
import scala.scalanative.sbtplugin.ScalaNativePlugin.autoImport.*
import scala.scalanative.build._

object Native extends AutoPluginHelper {

  /** The configuration function to call for this plugin helper to add support for Scala Native
    *
    * @param project
    *   The project to which the configuration should be applied
    *
    * @return
    *   The same project passed as an argument, post configuration
    */
  def configure(project: Project): Project = {
    configure()(project)
  }

  def configure(
    targetTriple: String = "arm64-apple-macosx11.0.0",
    gc: String = "commix",
    debug: Boolean = true,
    noLTO: Boolean = false,
    debugLog: Boolean = false,
    verbose: Boolean = false,
    ld64Path: String = "/opt/homebrew/opt/llvm/bin/ld64.lld"
  )(project: Project): Project = {
    project
      .enablePlugins(ScalaNativePlugin)
      .settings(
        // set to Debug for compilation details (Info is default)
        logLevel := {
          if (debugLog) { Level.Debug }
          else { Level.Info }
        },
        scalaVersion := "3.3.1",
        // defaults set with common options shown
        nativeConfig ~= { c =>
          {
            val mode = if (debug) Mode.debug else Mode.releaseFast
            val lto = if (noLTO) LTO.none else LTO.thin
            val compileOptions = if (verbose) { Seq("-v") } else {Seq.empty}
            val linkOptions = Seq(s"-fuse-ld=$ld64Path")
            c.withLTO(lto)
              .withMode(mode)
              .withGC(GC(gc))
              .withTargetTriple(targetTriple)
              .withCompileOptions(c.compileOptions ++ compileOptions)
              .withLinkingOptions(c.linkingOptions ++ linkOptions)
          }
        }
      )
  }
}
